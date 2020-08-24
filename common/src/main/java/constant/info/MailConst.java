package constant.info;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 张丰博
 */
@Getter
@ToString
public final class MailConst {
    /**
     *  单个邮件，道具最大数
     */
    public  int MAX_PROPS_NUMBER;
    /**
     * 一次最多展示邮件数量
     */
    public  int MAX_SHOW_NUMBER;

    private static MailConst mailConst = null;

    private MailConst() {
    }

    public static MailConst getMailConst(){
        return mailConst;
    }

    private void init(MailConst mailConst){
        MailConst.mailConst = mailConst;
    }
}
