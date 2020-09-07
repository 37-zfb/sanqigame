package client.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 张丰博
 */
@Getter
public class PlayFriendClient {
    /**
     * 朋友集合
     */
    private final Map<Integer,String> friendMap = new HashMap<>();
}
