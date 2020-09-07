package server.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Getter
public class PlayFriend {

    /**
     * 朋友集合
     */
    private final Map<Integer, String> FRIEND_MAP = new HashMap<>();
}
