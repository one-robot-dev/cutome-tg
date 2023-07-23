package org.drinkless.user.message.handler;

import org.drinkless.tdlib.TdApi;
import org.drinkless.user.MainUser;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 最新消息提醒到其他群
 */
public class LastMsgTipOtherGroupHandler implements LastMsgHandler{

    @Override
    public void handle(MainUser clientMainUser, TdApi.UpdateChatLastMessage updateChat) {
        if (clientMainUser.startTime > updateChat.lastMessage.date) {
            return;
        }
        TdApi.MessageSender sender = updateChat.lastMessage.senderId;
        if (clientMainUser.receiveGroupId == 0 || clientMainUser.receiveGroupId == updateChat.chatId || sender.getConstructor() != TdApi.MessageSenderUser.CONSTRUCTOR) {
            return;
        }
        long userId = ((TdApi.MessageSenderUser)sender).userId;
        TdApi.User user = clientMainUser.users.get(userId);
        if (user == null) {
            return;
        }
        if (System.currentTimeMillis() - clientMainUser.userLastMsgTime.getOrDefault(userId, 0L) < TimeUnit.MINUTES.toMillis(clientMainUser.checkInterval)) {
            return;
        }
        clientMainUser.userLastMsgTime.put(userId, System.currentTimeMillis());
        String userName = Optional.ofNullable(user.usernames).map(usernames -> usernames.activeUsernames).filter(names -> names.length > 0).map(names -> names[0]).orElse("");
        if ("".equals(userName) || clientMainUser.noListenUserId.contains(userId) || clientMainUser.noListenUserName.contains(userName)) {
            return;
        }
        TdApi.Chat room = clientMainUser.chats.get(updateChat.lastMessage.chatId);
        TdApi.MessageContent content = updateChat.lastMessage.content;
        String msg;
        if (content.getConstructor() == TdApi.MessageText.CONSTRUCTOR) {
            msg = "文字消息:\n" + ((TdApi.MessageText) content).text.text;
        } else {
            msg = "非文字消息:\n" + content.getClass().getSimpleName();
        }
        msg = "\"" + user.firstName + "-" + userName + "\", 在\"" + room.title + "\"中说话\n" + msg;
        TdApi.FormattedText text = new TdApi.FormattedText(msg, null);
        TdApi.InputMessageContent sendContent = new TdApi.InputMessageText(text, false, true);
        clientMainUser.client.send(new TdApi.SendMessage(clientMainUser.receiveGroupId, 0, 0, null, null, sendContent), clientMainUser.defaultHandler);
    }

}
