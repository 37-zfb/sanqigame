package type;

/**
 * @author 张丰博
 */
public enum ChatType {
    /**
     * 私聊
     */
    PRIVATE_CHAT("私聊","server.cmdhandler.chat.PrivateChat"),
    /**
     * 群聊
     */
    PUBLIC_CHAT("群聊","server.cmdhandler.chat.PublicChat");
    private String chatType;

    private String handler;

    ChatType(String chatType, String handler) {
        this.chatType = chatType;
        this.handler = handler;
    }


    public String getChatType() {
        return chatType;
    }

    public String getHandler() {
        return handler;
    }
}
