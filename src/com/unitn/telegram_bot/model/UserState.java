package com.unitn.telegram_bot.model;

import co.vandenham.telegram.botapi.types.Message;
import com.unitn.local_database.UserData;
import lombok.Data;

/**
 * Created by demiurgo on 1/27/16.
 */
@Data
public class UserState {

    final int telegramId;
    final int telegramChat;
    final UserStates state;

    String reg_name;
    Integer reg_weight;
    Float reg_height;

    public static UserState newUser(Message message){
        return new UserState(message.getFrom().getId(), message.getChat().getId(), UserStates.NOOB_INTRO);
    }

    public static UserState oldUser(Message message){
        return new UserState(message.getFrom().getId(), message.getChat().getId(), UserStates.IDLE);
    }


    public UserState next(UserStates newState){
        final UserState nextState = new UserState(telegramId, telegramChat, newState );
        nextState.reg_name = reg_name;
        nextState.reg_weight = reg_weight;
        nextState.reg_height = reg_height;
        return  nextState;
    }


    public UserData getUserData(){
        final UserData user = new UserData();
        user.setIdTelegram(telegramId);
        user.setName(reg_name);
        user.setWeight(reg_weight);
        user.setHeight(reg_height);
        return user;
    }


    public enum UserStates {
        NOOB_INTRO,
        NOOB_INFO,
        REGISTERING,
        REGISTERING_NAME,
        REGISTERING_WEIGHT,
        REGISTERING_HEIGHT,
        REGISTRATION_FAILED,
        REGISTRATION_COMPLETE,

        // Registered users states
        IDLE,
        ASKING_STATS,
        SAVING_DATA,

    }

    // VALIDATION FUNCTION USED IN REGISTRATION STEPS

    public static String validateName(String name){
        return name != null && !name.isEmpty() ? name.trim() : null;
    }

    public static Integer validateWeight(String weight){
        try{
            return Integer.parseInt(weight);
        }catch (NumberFormatException e){
            return null;
        }
    }

    public static Float validateHeight(String height){
        try{
            return Float.parseFloat(height);
        }catch (NumberFormatException e){
            return null;
        }
    }
}
