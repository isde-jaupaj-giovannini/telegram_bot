package com.unitn.telegram_bot;

import co.vandenham.telegram.botapi.DefaultHandler;
import co.vandenham.telegram.botapi.TelegramBot;
import co.vandenham.telegram.botapi.requests.OptionalArgs;
import co.vandenham.telegram.botapi.types.Message;
import co.vandenham.telegram.botapi.types.ReplyKeyboardMarkup;
import com.unitn.process_centric_service.PCService;
import com.unitn.process_centric_service.ProcessCentricService;
import com.unitn.telegram_bot.model.BotResponse;
import com.unitn.telegram_bot.model.UserState;
import com.unitn.telegram_bot.model.UserState.UserStates;
import javafx.util.Pair;
import nz.sodium.*;

import java.util.HashMap;
import java.util.Map;

import static com.unitn.telegram_bot.model.UserState.validateHeight;
import static com.unitn.telegram_bot.model.UserState.validateName;
import static com.unitn.telegram_bot.model.UserState.validateWeight;

/**
 * Created by demiurgo on 1/17/16.
 */
public class Bot extends TelegramBot {

    ProcessCentricService pcService = new PCService().getPCSImplPort();

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

        Transaction.runVoid(() -> {
            CellLoop<Map<Integer, UserState>> usersL = new CellLoop<>();

            userExists =
                    incomingMessages.snapshot(usersL, (message, users) -> new Pair<>(users.containsKey(message.getFrom().getId()), message));// TODO insert PCS call

            fromUsers =
                    userExists.filter(Pair::getKey).map(Pair::getValue);

            fromNewUsers =
                    userExists.filter(pair -> !pair.getKey()).map(Pair::getValue).map(UserState::newUser);

            nextState =
                    fromUsers.snapshot(usersL, (message, usersMap) -> processNextState(message, usersMap.get(message.getFrom().getId())));

            usersL.loop(nextState.merge(fromNewUsers, (userState, noobStart) -> userState)
                    .accum(new HashMap<>(), (userState, userMap) -> {
                        userMap.put(userState.getTelegramId(), userState);
                        return userMap;
                    }));
            users = usersL;

            stateWriter = nextState.merge(fromNewUsers, (userState, noobStart) -> userState)
                    .map(Bot::processResponse);
        });


        // This listener outputs to the client

        stateWriter.listen(res -> {
            if (res.hasArgs()) {
                sendMessage(res.getChatID(), res.getText(), res.getArgs());
            } else {
                sendMessage(res.getChatID(), res.getText());
            }
        });
    }


    private static UserState processNextState(Message msg, UserState state) {
        final String cmd = msg.getText().toUpperCase().trim();
        final String text = msg.getText().trim();

        switch (state.getState()) {
            case NOOB_INTRO:
                switch (cmd){
                    case "REGISTER":
                        return state.next(UserStates.REGISTERING);
                    case "INFO":
                        return state.next(UserStates.NOOB_INFO);
                }
            case NOOB_INFO:
                return state.next(UserStates.NOOB_INTRO);
            case REGISTERING:
                switch (cmd){
                    case "YES":
                        return state.next(UserStates.REGISTERING_NAME);
                    case "NO":
                        return state.next(UserStates.NOOB_INTRO);
                }
            case REGISTERING_NAME:
                switch (cmd){
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        String name = validateName(text);
                        if(name!=null){
                            //TODO Store somewhere the actual value
                            return state.next(UserStates.REGISTERING_WEIGHT);
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTERING_WEIGHT:
                switch (cmd){
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        Integer weight = validateWeight(text);
                        if(weight!=null){
                            //TODO Store somewhere the actual value
                            return state.next(UserStates.REGISTERING_HEIGHT);
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTERING_HEIGHT:
                switch (cmd){
                    case "CANCEL":
                        return state.next(UserStates.NOOB_INTRO);
                    default:
                        Float height = validateHeight(text);
                        if(height!=null){
                            //TODO Store somewhere the actual value
                            //TODO Sync with other service at this point
                            return state.next(UserStates.REGISTRATION_COMPLETE);
                        }
                        //ELSE return current state  at the bottom of this function
                }
                break;
            case REGISTRATION_COMPLETE: // Intentional Fallthrough to handle command in these two states in the same way
            case IDLE:
                switch (cmd){
                    case "SAVE":
                        return state.next(UserStates.SAVING_DATA);
                    case "STATS":
                        return state.next(UserStates.ASKING_STATS);
                }
            case ASKING_STATS: // TODO expand
                return state.next(UserStates.IDLE);
            case SAVING_DATA: // TODO expand
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
            case REGISTRATION_COMPLETE:
                text = "Registration complete!\n";
            case IDLE:  // INTENTIONAL FALLTHROUGH
                text += "What do you want to do now?";
                args = oneTimeKeyboard("SAVE", "STATS");
                break;
            case ASKING_STATS:
                break;
            case SAVING_DATA:
                break;

        }
        return new BotResponse(state.getTelegramChat(), text, args);
    }


    // -------------------------------

    public static void main(String args[]) {

        final String botToken = System.getenv("TELEGRAM_TOKEN");

        if (botToken != null && !botToken.isEmpty()) {
            System.out.println("Using token:" + botToken);

            new Bot(System.getenv("TELEGRAM_TOKEN")).start();
        } else {
            System.out.println("Missing TELEGRAM_TOKEN enviroment variable");
        }
    }

    public static OptionalArgs oneTimeKeyboard(String ... buttons){
        return new OptionalArgs().replyMarkup(new ReplyKeyboardMarkup.Builder().setOneTimeKeyboard().row(buttons).build());
    }

    @DefaultHandler
    public void handleDefault(Message message) {
        incomingMessages.send(message);
    }
}
