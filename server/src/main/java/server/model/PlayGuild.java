package server.model;

import entity.db.GuildEntity;
import entity.db.GuildMemberEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author 张丰博
 */
@Setter
@Getter
@NoArgsConstructor
@ToString
public class PlayGuild {


    /**
     * 公会id
     */
    private Integer id;

    /**
     *  公会基础信息
     */
    private GuildEntity guildEntity;

    /**
     * 用户id  公会成员
     */
    private final Map<Integer,GuildMemberEntity> guildMemberMap = new ConcurrentHashMap<>();

}
