package server;

import server.model.PlayGuild;

import java.util.*;

/**
 * @author 张丰博
 * 公会管理
 */
public final class GuildManager {

    /**
     *  公会id  公会model
     */
    private static final Map<Integer, PlayGuild> GUILD_MAP = new HashMap<>();

    public static Collection<PlayGuild> listPlayGuild(){
        return GUILD_MAP.values();
    }

    /**
     *  获取公会id
     * @param playGuild
     * @return
     */
    public static synchronized Integer addGuild(PlayGuild playGuild) {
        if (playGuild == null) {
            return null;
        }
        Optional<Integer> max = GUILD_MAP.keySet().stream().max(Comparator.comparingInt(k -> k));
        Integer key  = max.orElse(0);
        int id = key + 1;
        playGuild.setId(id);
        GUILD_MAP.put(id, playGuild);
        return id;
    }

    /**
     * 通过id 获取公会对象
     * @param guildId
     * @return
     */
    public static synchronized PlayGuild getGuild(Integer guildId) {
        return GUILD_MAP.get(guildId);
    }

    /**
     * 判断此公会是否存在
     * @param guildName
     * @return
     */
    public static synchronized boolean isGuildNameDuplicate(String guildName){
        if (guildName == null){
            return true;
        }
        for (PlayGuild playGuild : GUILD_MAP.values()) {
            if (guildName.equals(playGuild.getGuildEntity().getGuildName())){
                return true;
            }

        }
        return false;
    }

    public static void removeGuild(PlayGuild playGuild){
        GUILD_MAP.remove(playGuild.getId());
    }

}
