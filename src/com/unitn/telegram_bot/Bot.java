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
                    userExists.filter(pair -> !pair.getKey()).map(Pair::getValue).map(message -> UserState.newUser(message));

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
        UserStates nextState = state.getState();
        System.out.println(state.getState());
        switch (state.getState()) {
            case NOOB_INTRO:
                nextState = handleNoobIntro(msg, state);
                break;
            case NOOB_INFO:
                nextState = UserStates.NOOB_INTRO;
                break;
            case REGISTERING:
                break;
            case IDLE:
                break;
            case ASKING_STATS:
                break;
            case SAVING_DATA:
                break;
            case ABORTING_OPERATION:
                break;
        }
        return state.next(nextState);
    }


    private static BotResponse processResponse(UserState state) {

        String text = "";
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
                text = "Please write here your data in this format:\n<nickname> <weight> <height>!!";
                break;
            case IDLE:
                break;
            case ASKING_STATS:
                break;
            case SAVING_DATA:
                break;
            case ABORTING_OPERATION:
                break;
        }
        return new BotResponse(state.getTelegramChat(), text, args);
    }


    private static UserStates handleNoobIntro(Message message, UserState state) {
        switch (message.getText()) {
            case "REGISTER":
                return UserStates.REGISTERING;
            case "INFO":
                return UserStates.NOOB_INFO;
            default:
                return state.getState();
        }
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
