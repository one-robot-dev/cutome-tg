package org.drinkless.user.message.handler;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.MainUser;

import java.util.Properties;

/**
 * 最新消息回复
 */
public class LastMsgReplyHandler implements LastMsgHandler{

    @Override
    public void handle(MainUser clientMainUser, TdApi.UpdateChatLastMessage updateChat) {
        if (clientMainUser.startTime > updateChat.lastMessage.date) {
            return;
        }
        TdApi.MessageSender sender = updateChat.lastMessage.senderId;
        long userId = ((TdApi.MessageSenderUser)sender).userId;
        TdApi.User user = clientMainUser.users.get(userId);
        if (user != null && clientMainUser.phoneNumber.equals(user.phoneNumber)) {
            return;
        }
        TdApi.MessageContent content = updateChat.lastMessage.content;
        if (content.getConstructor() != TdApi.MessageText.CONSTRUCTOR) {
            return;
        }
        String msg = ((TdApi.MessageText) content).text.text;
        //根据消息获取回复信息
        TdApi.ChatType chatType = clientMainUser.chats.get(updateChat.chatId).type;
        boolean isGroup = chatType.getConstructor() == TdApi.ChatTypeBasicGroup.CONSTRUCTOR || chatType.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR;
        Properties replyProp = isGroup ? MainUser.replyGroupProperties : MainUser.replyUserProperties;
        String reply = replyProp.getProperty(msg);
        if (reply == null || reply.trim().equals("")) {
            return;
        }
        TdApi.FormattedText text = new TdApi.FormattedText(reply, null);
        TdApi.InputMessageContent sendContent = new TdApi.InputMessageText(text, false, true);
        clientMainUser.client.send(new TdApi.SendMessage(updateChat.chatId, 0, 0, null, null, sendContent), object -> {});
    }

}
