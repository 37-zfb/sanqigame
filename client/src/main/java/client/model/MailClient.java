package client.model;

import client.model.client.MailEntityClient;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 *  张丰博
 */
@Data
@NoArgsConstructor
public class MailClient {
    /**
     *  是否有邮件
     */
    private boolean isHave;

    /**
     *  所有的邮件
     */
    private final Map<Long,MailEntityClient> mailMap = new HashMap<>();

}
