package org.drinkless.user.message.handler;

import org.drinkless.Start;
import org.drinkless.tdlib.TdApi;
import org.drinkless.user.MainUser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * 最新消息回复
 */
public class LastMsgReplyHandler implements LastMsgHandler{

    private static Set<Integer> groupAdminFlag = new HashSet<>(Arrays.asList(TdApi.ChatMemberStatusAdministrator.CONSTRUCTOR, TdApi.ChatMemberStatusCreator.CONSTRUCTOR));

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
        String msgKey = ((TdApi.MessageText) content).text.text;
        //根据消息获取回复信息
        TdApi.ChatType chatType = clientMainUser.chats.get(updateChat.chatId).type;
        int type = chatType.getConstructor();
        boolean isGroup = true;
        switch (type) {
            case TdApi.ChatTypeBasicGroup.CONSTRUCTOR:
                TdApi.BasicGroup basicGroup = clientMainUser.basicGroups.get(((TdApi.ChatTypeBasicGroup)chatType).basicGroupId);
                if (basicGroup == null || !groupAdminFlag.contains(basicGroup.status.getConstructor())) {
                    return;
                }
                break;
            case TdApi.ChatTypeSupergroup.CONSTRUCTOR:
                TdApi.Supergroup supergroup = clientMainUser.supergroups.get(((TdApi.ChatTypeSupergroup) chatType).supergroupId);
                if (supergroup == null || !groupAdminFlag.contains(supergroup.status.getConstructor())) {
                    return;
                }
                break;
            default:
                isGroup = false;
        }
        Properties replyProp = isGroup ? MainUser.replyGroupProperties : MainUser.replyUserProperties;
        String reply = replyProp.getProperty(msgKey);
        if (reply == null) {
            Set<String> likeMatch = isGroup ? Start.likeMatchGroupReply : Start.likeMatchUserReply;
            for (String match : likeMatch) {
                if (msgKey.contains(match)) {
                    msgKey = match;
                }
            }
        }
        reply = replyProp.getProperty(msgKey);
        boolean commonReply = false;
        if (reply == null) {
            reply = isGroup ? Start.appProperties.getProperty("群聊通用回复") : Start.appProperties.getProperty("私聊通用回复");
            commonReply = true;
        }
        if (reply == null || reply.trim().equals("")) {
            return;
        }
        Set<String> specReply = isGroup ? Start.specGroupReply : Start.specUserReply;
        long replyToMessageId =  commonReply || specReply.contains(msgKey) ? updateChat.lastMessage.id : 0;
        TdApi.FormattedText text = new TdApi.FormattedText(reply, null);
        TdApi.InputMessageContent sendContent = new TdApi.InputMessageText(text, false, true);
        clientMainUser.client.send(new TdApi.SendMessage(updateChat.chatId, 0, replyToMessageId, null, null, sendContent), object -> {});
    }

}
