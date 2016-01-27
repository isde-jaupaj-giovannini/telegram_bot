package com.unitn.telegram_bot.model;

import co.vandenham.telegram.botapi.types.Message;
import lombok.Data;

/**
 * Created by demiurgo on 1/27/16.
 */
@Data
public class UserState {

    final int telegramId;
    final int telegramChat;
    final UserStates state;


    public static UserState newUser(Message message){
        return new UserState(message.getFrom().getId(), message.getChat().getId(), UserStates.NOOB_INTRO);
    }

    public UserState next(UserStates newState){
        return new UserState(telegramId, telegramChat, newState );
    }


    public enum UserStates {
        NOOB_INTRO,
        NOOB_INFO,
        REGISTERING,

        // Registered users states
        IDLE,
        ASKING_STATS,
        SAVING_DATA,
        ABORTING_OPERATION,

    }
}
