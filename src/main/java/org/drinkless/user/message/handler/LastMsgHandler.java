package org.drinkless.user.message.handler;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.User;

public interface LastMsgHandler {

    void handle(User clientUser, TdApi.UpdateChatLastMessage updateChat);
}
