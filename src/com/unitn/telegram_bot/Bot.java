package com.unitn.telegram_bot;

import co.vandenham.telegram.botapi.DefaultHandler;
import co.vandenham.telegram.botapi.TelegramBot;
import co.vandenham.telegram.botapi.requests.OptionalArgs;
import co.vandenham.telegram.botapi.types.Message;
import co.vandenham.telegram.botapi.types.ReplyKeyboardMarkup;
import com.unitn.bl_service.BLService;
import com.unitn.bl_service.BLService_Service;
import com.unitn.bl_service.NewStepResponse;
import com.unitn.bl_service.StatsResponse;
import com.unitn.local_database.MeasureData;
import com.unitn.process_centric_service.PCService;
import com.unitn.process_centric_service.ProcessCentricService;
import com.unitn.storage_service.Goal;
import com.unitn.telegram_bot.model.BotResponse;
import com.unitn.telegram_bot.model.UserState;
import com.unitn.telegram_bot.model.UserState.*;
import nz.sodium.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import static com.unitn.telegram_bot.model.UserState.*;

/**
 * Created by demiurgo on 1/17/16.
 */
public class Bot extends TelegramBot {

    static ProcessCentricService pcService = new PCService().getPCSImplPort();
    static BLService blService = new BLService_Service().getBLServiceImplPort();

    // FRP
    StreamSink<Message> incomingMessages = new StreamSink<>();

    Cell<Map<Integer, UserState>> users;

    Stream<Pair<Boolean, Message>> userExists;


    Stream<Message> fromUsers; // EventStream of msg from user with an internal state
    Stream<UserState> fromNewUsers; // EventStream of msg from user not in our internal map
    Stream<UserState> nextState;
    Stream<BotResponse> stateWriter; // Stream that outputs what to write to the user at each interaction


    public Bot(final String botToken) {
        super(botToken);

        Transaction.runVoid(new Runnable() {
            @Override
            public void run() {
                CellLoop<Map<Integer, UserState>> usersL = new CellLoop<>();

                userExists =
                        incomingMessages.snapshot(usersL, new Lambda2<Message, Map<Integer, UserState>, Pair<Boolean, Message>>() {
                            @Override
                            public Pair<Boolean, Message> apply(Message message, Map<Integer, UserState> users) {
                                final int telId = message.getFrom().getId();
                                final boolean userExists = pcService.userExists(telId) || users.containsKey(telId);
                                return new Pair<>(userExists, message);
                            }
                        });

                fromUsers =
                        userExists.filter(new Lambda1<Pair<Boolean, Message>, Boolean>() {
                            @Override
                            public Boolean apply(Pair<Boolean, Message> booleanMessagePair) {
                                return booleanMessagePair.getKey();
                            }
                        }).map(new Lambda1<Pair<Boolean, Message>, Message>() {
                            @Override
                            public Message apply(Pair<Boolean, Message> booleanMessagePair) {
                                return booleanMessagePair.getValue();
                            }
                        });

                fromNewUsers =
                        userExists.filter(new Lambda1<Pair<Boolean, Message>, Boolean>() {
                            @Override
                            public Boolean apply(Pair<Boolean, Message> pair) {
                                return !pair.getKey();
                            }
                        }).map(new Lambda1<Pair<Boolean, Message>, Message>() {
                            @Override
                            public Message apply(Pair<Boolean, Message> booleanMessagePair) {
                                return booleanMessagePair.getValue();
                            }
                        }).map(new Lambda1<Message, UserState>() {
                            @Override
                            public UserState apply(Message message) {
                                return UserState.newUser(message);
                            }
                        });

                nextState =
                        fromUsers.snapshot(usersL, new Lambda2<Message, Map<Integer, UserState>, UserState>() {
                            @Override
                            public UserState apply(Message message, Map<Integer, UserState> usersMap) {
                                return processNextState(message, usersMap.get(message.getFrom().getId()));
                            }
                        });

                usersL.loop(nextState.merge(fromNewUsers, new Lambda2<UserState, UserState, UserState>() {
                    @Override
                    public UserState apply(UserState userState, UserState noobStart) {
                        return userState;
                    }
                })
                        .accum(new HashMap<Integer, UserState>(), new Lambda2<UserState, Map<Integer, UserState>, Map<Integer, UserState>>() {
                            @Override
                            public Map<Integer, UserState> apply(UserState userState, Map<Integer, UserState> userMap) {
                                userMap.put(userState.getTelegramId(), userState);
                                return userMap;
                            }
                        }));
                users = usersL;

                stateWriter = nextState.merge(fromNewUsers, new Lambda2<UserState, UserState, UserState>() {
                    @Override
                    public UserState apply(UserState userState, UserState noobStart) {
                        return userState;
                    }
                })
                        .map(new Lambda1<UserState, BotResponse>() {
                            @Override
                            public BotResponse apply(UserState state) {
                                return Bot.processResponse(state);
                            }
                        });
            }
        });


        // This listener outputs to the client

        stateWriter.listen(new Handler<BotResponse>() {
            @Override
            public void run(BotResponse res) {
                if (res.hasArgs()) {
                    Bot.this.sendMessage(res.getChatID(), res.getText(), res.getArgs());
                } else {
                    Bot.this.sendMessage(res.getChatID(), res.getText());
                }
            }
        });
    }


    private static UserState processNextState(Message msg, UserState state) {
        final String cmd = msg.getText().toUpperCase().trim();
        final String text = msg.getText().trim();

        if (state == null) { // This is triggered when a registered users comes back after a server restart, or a clean up of userStates map
            state = UserState.oldUser(msg);
        }
        switch (state.getState()) {
            case NOOB_INTRO:
                switch (cmd) {
                    case "REGISTER":
                        return state.next(UserStates.REGISTERING);
                    case "INFO":
                        return state.next(UserStates.NOOB_INFO);
                }
            case NOOB_INFO:
                return state.next(UserStates.NOOB_INTRO);
            case REGISTERING:
                switch (cmd) {
                    case "YES":
                        return state.next(UserStates.REGISTERING_NAME);
                    case "NO":
                        return state.next(UserStates.NOOB_INTRO);
                }
            case REGISTERING_NAME:
                switch (cmd) {
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        String name = validateName(text);
                        if (name != null) {
                            state.setReg_name(name);
                            return state.next(UserStates.REGISTERING_WEIGHT);
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTERING_WEIGHT:
                switch (cmd) {
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        Integer weight = validateWeight(text);
                        if (weight != null) {
                            state.setReg_weight(weight);
                            return state.next(UserStates.REGISTERING_HEIGHT);
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTERING_HEIGHT:
                switch (cmd) {
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        Float height = validateHeight(text);
                        if (height != null) {
                            state.setReg_height(height);
                            if (pcService.registerNewUser(state.getUserData())) {
                                return state.next(UserStates.REGISTRATION_COMPLETE);
                            } else {
                                return state.next(UserStates.REGISTRATION_FAILED);
                            }
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTRATION_FAILED:
                return state.next(UserStates.NOOB_INTRO);
            case REGISTRATION_COMPLETE: // Intentional Fallthrough to handle command in these two states in the same way
            case IDLE:
                switch (cmd) {
                    case "SAVE":
                        return state.next(UserStates.SAVING_DATA);
                    case "STATS":
                        state.setStatsResponse(blService.statistics(state.getTelegramId()));
                        return state.next(UserStates.ASKING_STATS);
                }
            case ASKING_STATS:
                return state.next(UserStates.IDLE);
            case SAVING_DATA:
                switch (cmd) {
                    case "CANCEL":
                        return state.next(UserStates.IDLE);
                    case "GOAL":
                        return state.next(UserStates.SAVE_GOAL);
                    case "STEPS":
                        return state.next(UserStates.SAVE_STEPS);
                }
            case SAVE_STEPS:
                MeasureData steps = validateSteps(text);
                if (steps != null) {
                    steps.setIdTelegram(state.getTelegramId());
                    NewStepResponse res = blService.saveNewSteps(steps);

                    state.setNewStepResponse(res);
                    return state.next(UserStates.SAVED_STEPS);
                } else {
                    return state.next(UserStates.SAVE_STEPS_FAILED);
                }
            case SAVED_STEPS: // Intentional fallthrough
            case SAVE_STEPS_FAILED:
                return state.next(UserStates.IDLE);
            case SAVE_GOAL:
                Goal g = validateGoal(cmd);
                if (g != null) {
                    pcService.saveGoal(state.getTelegramId(), g);
                    return state.next(UserStates.SAVED_GOAL);

                } else {
                    return state.next(UserStates.SAVE_GOAL_FAILED);
                }
            case SAVED_GOAL:// Intentional fallthrough
            case SAVE_GOAL_FAILED:
                return state.next(UserStates.IDLE);

        }
        return state;
    }


    private static BotResponse processResponse(UserState state) {

        String text = state.getState().name();//Initial value for debugging  ow "" is just as good // TODO set it as ""
        OptionalArgs args = null;
        switch (state.getState()) {
            case NOOB_INTRO:
                args = oneTimeKeyboard("REGISTER", "INFO");
                text = "Welcome to this Service!\nPress INFO to read its description or\nPress REGISTER to start using it!";
                break;
            case NOOB_INFO:
                text = "This is an awesome service!!!!!!";
                args = oneTimeKeyboard("BACK");
                break;
            case REGISTERING:
                text = "In order to register you will need to provide a NAME and your current WEIGTH and HEIGHT\nDo you wish to continue?";
                args = oneTimeKeyboard("YES", "NO");
                break;
            case REGISTERING_NAME:
                text = "Insert your name (nickname)";
                break;
            case REGISTERING_WEIGHT:
                text = "Insert your weight in kg (ex: 50)";
                break;
            case REGISTERING_HEIGHT:
                text = "Insert your height in meters (ex: 1.70)";
                break;
            case REGISTRATION_FAILED:
                text = "Registration failed.\nTry later";
                args = oneTimeKeyboard("Ok...");
                break;
            case REGISTRATION_COMPLETE:
                text = "Registration complete!\n";
            case IDLE:  // INTENTIONAL FALLTHROUGH
                text += "What do you want to do now?";
                args = oneTimeKeyboard("SAVE", "STATS");
                break;
            case ASKING_STATS:
                StatsResponse res = state.getStatsResponse();
                text = "Total number of Goals achieved "+res.getNrGoalsDone()+"/"+ (res.getNrGoalsDone()+res.getNrTodoGoals())
                        +"\nGraph of steps done in the last 7 days"
                        +state.getStatsResponse().getChart();
                args = oneTimeKeyboard("OK");
                break;
            case SAVING_DATA:
                text = "What do you want to save?";
                args = oneTimeKeyboard("STEPS", "GOAL", "CANCEL");
                break;
            case SAVE_STEPS:
                text = "Insert how many steps you did today:";
                break;
            case SAVED_STEPS:
                text = (state.getNewStepResponse().isStatus() ? "You reached your daily goal!\nRead a famous quote and have a funny comic!\n" : "You are still on your way to reach your goal.\nRead this movie quote!\n") +
                        state.getNewStepResponse().getMessage() + "\n"
                        + state.getNewStepResponse().getUrl();
                args = oneTimeKeyboard("OK");
                break;
            case SAVE_STEPS_FAILED:
                text = "Could not save steps";
                args = oneTimeKeyboard("OK");
                break;
            case SAVE_GOAL:
                text = "Which goal do you want to set?";
                args = oneTimeKeyboard("1000 in a day", "3000 in a day");
                break;
            case SAVED_GOAL:
                text = "Goal saved";
                args = oneTimeKeyboard("OK");
                break;
            case SAVE_GOAL_FAILED:
                text = "Could not save goal";
                args = oneTimeKeyboard("OK");
                break;
        }
        return new BotResponse(state.getTelegramChat(), text, args);
    }


    // -------------------------------

    public static void main(String args[]) {

        final String botToken = System.getenv("TELEGRAM_TOKEN");
        final String PORT = System.getenv("PORT");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket socket = new ServerSocket(Integer.parseInt(PORT));
                    while (PORT != null) { // Infinite loop to please our dark god HEROKU
                        socket.accept();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        if (botToken != null && !botToken.isEmpty()) {
            System.out.println("Using token:" + botToken);

            new Bot(System.getenv("TELEGRAM_TOKEN")).start();
        } else {
            System.out.println("Missing TELEGRAM_TOKEN enviroment variable");
        }
    }

    public static OptionalArgs oneTimeKeyboard(String... buttons) {
        return new OptionalArgs().replyMarkup(new ReplyKeyboardMarkup.Builder().setOneTimeKeyboard().row(buttons).build());
    }

    @DefaultHandler
    public void handleDefault(Message message) {
        incomingMessages.send(message);
    }
}
