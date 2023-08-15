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
        long userId = 0;
        String userName = "未知用户名";
        String firstName = "未知昵称";
        if (sender.getConstructor() == TdApi.MessageSenderUser.CONSTRUCTOR) {
            userId = ((TdApi.MessageSenderUser)sender).userId;
            if (System.currentTimeMillis() - clientMainUser.userLastMsgTime.getOrDefault(userId, 0L) < TimeUnit.MINUTES.toMillis(clientMainUser.checkInterval)) {
                return;
            }
            clientMainUser.userLastMsgTime.put(userId, System.currentTimeMillis());
            TdApi.User user = clientMainUser.users.get(userId);
            firstName = Optional.ofNullable(user).map(u -> u.firstName).orElse("未知昵称");
            userName = Optional.ofNullable(user).map(u -> u.usernames).map(usernames -> usernames.activeUsernames).filter(names -> names.length > 0).map(names -> names[0]).orElse("未知用户名");
            if (clientMainUser.noListenUserId.contains(userId) || clientMainUser.noListenUserName.contains(userName)) {
                return;
            }
        } else if (sender.getConstructor() == TdApi.MessageSenderChat.CONSTRUCTOR) {
            userId = ((TdApi.MessageSenderChat)sender).chatId;
            TdApi.Chat chat = clientMainUser.chats.get(userId);
            if (chat != null) {
                firstName = chat.themeName;
                userName = chat.title;
            }
        }
        TdApi.Chat room = clientMainUser.chats.get(updateChat.lastMessage.chatId);
        TdApi.MessageContent content = updateChat.lastMessage.content;
        StringBuilder msg;
        if (content.getConstructor() == TdApi.MessageText.CONSTRUCTOR) {
            msg = new StringBuilder("文字消息:\n" + ((TdApi.MessageText) content).text.text);
        } else if(content.getConstructor() == TdApi.MessageChatAddMembers.CONSTRUCTOR) {
            msg = new StringBuilder("进群消息:\n");
            for (long memberUserId : ((TdApi.MessageChatAddMembers) content).memberUserIds) {
                TdApi.User member = clientMainUser.users.get(memberUserId);
                String memberName = Optional.ofNullable(member).map(u -> u.firstName).orElse("未知昵称");
                String memberUserName = Optional.ofNullable(member).map(u -> u.usernames).map(usernames -> usernames.activeUsernames).filter(names -> names.length > 0).map(names -> names[0]).orElse("未知用户名");
                msg.append("\"").append(memberUserId).append("-").append(memberName).append("-").append(memberUserName).append("\"加入了群\n");
            }
        } else if(content.getConstructor() == TdApi.MessageChatJoinByLink.CONSTRUCTOR || content.getConstructor() == TdApi.MessageChatJoinByRequest.CONSTRUCTOR) {
            msg = new StringBuilder("进群消息:\n");
            msg.append("\"").append("未知id").append("-").append("未知昵称").append("-").append("未知用户名").append("\"加入了群\n");
        } else {
            msg = new StringBuilder("非文字消息:\n" + content.getClass().getSimpleName());
        }
        msg.insert(0, "\"" + userId + "-" + firstName + "-" + userName + "\", 在\"" + room.title + "\"中说话\n");
        TdApi.FormattedText text = new TdApi.FormattedText(msg.toString(), null);
        TdApi.InputMessageContent sendContent = new TdApi.InputMessageText(text, false, true);
        clientMainUser.client.send(new TdApi.SendMessage(clientMainUser.receiveGroupId, 0, 0, null, null, sendContent), obj -> {});
    }

}
