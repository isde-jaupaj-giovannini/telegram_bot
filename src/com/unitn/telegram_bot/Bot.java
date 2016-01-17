package com.unitn.telegram_bot;

import co.vandenham.telegram.botapi.DefaultHandler;
import co.vandenham.telegram.botapi.TelegramBot;
import co.vandenham.telegram.botapi.requests.OptionalArgs;
import co.vandenham.telegram.botapi.types.Message;
import co.vandenham.telegram.botapi.types.ReplyKeyboardHide;

/**
 * Created by demiurgo on 1/17/16.
 */
public class Bot extends TelegramBot {


    public Bot(final String botToken) {
        super(botToken);
    }



    @DefaultHandler
    public void handleDefault(Message message) {
        sendMessage(message.getChat().getId(),
                "Ciao "+message.getFrom().getFirstName()+"!",
                new OptionalArgs().replyMarkup(ReplyKeyboardHide.getSelectiveInstance()));
    }


    public static void main(String args[]){

        final String botToken = System.getenv("TELEGRAM_TOKEN");

        if(botToken!=null && !botToken.isEmpty()) {
            System.out.println("Using token:" + botToken);

            new Bot(System.getenv("TELEGRAM_TOKEN")).start();
        }else{
            System.out.println("Missing TELEGRAM_TOKEN enviroment variable");
        }
    }
}
