package client.model.arena;

import client.model.PlayUserClient;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 张丰博
 */
@Data
@NoArgsConstructor
public class PlayArenaClient {

    /**
     *  是否在竞技场
     */
    private boolean isInArena;

    /**
     *  挑战者id
     */
    private Integer originateUserId;

    /**
     * pk者 对象
     */
    private volatile PlayUserClient challengeUser;

    /**
     *  客户端使用
     */
    private final Map<Integer, PlayUserClient> arenaUserMap = new ConcurrentHashMap<>();

}
