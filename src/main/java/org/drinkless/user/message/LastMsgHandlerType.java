package org.drinkless.user.message;

import org.drinkless.user.message.handler.LastMsgHandler;
import org.drinkless.user.message.handler.LastMsgReplyHandler;
import org.drinkless.user.message.handler.LastMsgTipOtherGroupHandler;

public enum LastMsgHandlerType {

    TIP_OTHER_GROUP(new LastMsgTipOtherGroupHandler()),

    REPLY(new LastMsgReplyHandler()),

    ;

    private final LastMsgHandler handler;

    LastMsgHandlerType(LastMsgHandler handler) {
        this.handler = handler;
    }

    public LastMsgHandler getHandler() {
        return handler;
    }
}
