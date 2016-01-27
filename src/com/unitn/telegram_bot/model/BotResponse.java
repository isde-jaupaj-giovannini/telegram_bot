package com.unitn.telegram_bot.model;

import co.vandenham.telegram.botapi.requests.OptionalArgs;
import lombok.Data;

/**
 * Created by demiurgo on 1/27/16.
 */
@Data
public class BotResponse {
    final int chatID;
    final String text;
    final OptionalArgs args;


    public boolean hasArgs(){
        return args!=null;
    }
}
