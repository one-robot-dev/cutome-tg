package org.drinkless.user.message.handler;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.MainUser;

public interface LastMsgHandler {

    void handle(MainUser clientMainUser, TdApi.UpdateChatLastMessage updateChat);
}
