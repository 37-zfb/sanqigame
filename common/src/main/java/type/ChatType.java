package type;

/**
 * @author 张丰博
 */
public enum ChatType {
    /**
     *  私聊
     */
    PRIVATE_CHAT("私聊"),
    /**
     *  群聊
     */
    PUBLIC_CHAT("群聊")
    ;
    private String chatType;

    ChatType(String chatType){
        this.chatType = chatType;
    }


    public String getChatType() {
        return chatType;
    }
}
