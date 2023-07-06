package org.drinkless;

import org.drinkless.service.TelegramService;

public class Start {
    static {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        TelegramService.start();
    }

}
