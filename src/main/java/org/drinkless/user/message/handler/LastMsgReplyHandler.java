package org.drinkless.user.message.handler;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.User;

import java.util.Properties;

/**
 * 最新消息回复
 */
public class LastMsgReplyHandler implements LastMsgHandler{

    @Override
    public void handle(User clientUser, TdApi.UpdateChatLastMessage updateChat) {
        if (clientUser.startTime > updateChat.lastMessage.date) {
            return;
        }
        TdApi.MessageSender sender = updateChat.lastMessage.senderId;
        long userId = ((TdApi.MessageSenderUser)sender).userId;
        TdApi.User user = clientUser.users.get(userId);
        if (user == null || clientUser.phoneNumber.equals(user.phoneNumber)) {
            return;
        }
        clientUser.userLastMsgTime.put(userId, System.currentTimeMillis());
        TdApi.MessageContent content = updateChat.lastMessage.content;
        if (content.getConstructor() != TdApi.MessageText.CONSTRUCTOR) {
            return;
        }
        String msg = ((TdApi.MessageText) content).text.text;
        //根据消息获取回复信息
        TdApi.ChatType chatType = clientUser.chats.get(updateChat.chatId).type;
        boolean isGroup = chatType.getConstructor() == TdApi.ChatTypeBasicGroup.CONSTRUCTOR || chatType.getConstructor() == TdApi.ChatTypeSupergroup.CONSTRUCTOR;
        Properties replyProp = isGroup ? User.replyGroupProperties : User.replyUserProperties;
        String reply = replyProp.getProperty(msg);
        if (reply == null || reply.trim().equals("")) {
            return;
        }
        TdApi.FormattedText text = new TdApi.FormattedText(reply, null);
        TdApi.InputMessageContent sendContent = new TdApi.InputMessageText(text, false, true);
        clientUser.client.send(new TdApi.SendMessage(updateChat.chatId, 0, 0, null, null, sendContent), clientUser.defaultHandler);
    }

}
