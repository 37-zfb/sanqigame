package client.model.server.task;

import entity.MailProps;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class Task {

    /**
     * id
     */
    private Integer id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 描述
     */
    private String description;

    /**
     * 奖励经验值
     */
    private Integer experience;

    /**
     * 奖励道具
     */
//    private String rewardProps;
    private List<MailProps> rewardProps;

    /**
     * 奖励金币
     */
    private Integer rewardMoney;

    /**
     * 任务; 对话 or 击杀 or 通关类型
     */
    private String dialogue;
    private Integer killNumber;

    /**
     * 类型编码
     */
    private Integer typeCode;

    /**
     * 任务要求场景id；0表示任意场景
     */
    private Integer sceneId;

    /**
     * 副本id; 0表示此任务不在副本中
     */
    private Integer duplicateId;

    /**
     * npcId
     */
    private Integer npcId;
}
