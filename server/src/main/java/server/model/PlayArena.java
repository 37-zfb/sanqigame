package server.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
public class PlayArena {

    /**
     *  pk 对方id
     */
    private Integer targetUserId;

}
