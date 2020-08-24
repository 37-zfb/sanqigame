package client.model;

import lombok.*;

/**
 * @author 张丰博
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserResumeState {

    private Integer fromHp;

    private Integer toHp;

    private Long startTimeHp;

    private Long endTimeHp;




    private Integer fromMp;

    private Integer toMp;

    private Long startTimeMp;

    private Long endTimeMp;

}
