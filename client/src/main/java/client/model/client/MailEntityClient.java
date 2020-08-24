package client.model.client;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import type.MailType;

import java.util.Objects;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
public class MailEntityClient {

    /**
     * 邮件id
     */
    private Integer id;

    /**
     * 发件人
     */
    private String srcUserName;

    /**
     * 邮件标题
     */
    private String title;

    /**
     *  此邮件是否已读
     */
    private MailType mailType = MailType.UNREAD;

    public MailEntityClient(Integer id, String srcUserName, String title) {
        this.id = id;
        this.srcUserName = srcUserName;
        this.title = title;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MailEntityClient that = (MailEntityClient) o;

        if (!Objects.equals(id, that.id)) {
            return false;
        }
        if (!Objects.equals(srcUserName, that.srcUserName)) {
            return false;
        }
        return Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (srcUserName != null ? srcUserName.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }
}
