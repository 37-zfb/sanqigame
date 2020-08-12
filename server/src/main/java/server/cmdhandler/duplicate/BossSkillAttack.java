package server.cmdhandler.duplicate;

import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.BossSkill;
import server.model.User;
import type.BossMonsterType;

/**
 * @author 张丰博
 */
@Slf4j
public class BossSkillAttack {

    private static final BossSkillAttack BOSS_SKILL_ATTACK = new BossSkillAttack();

    private BossSkillAttack() {
    }

    public static BossSkillAttack getInstance() {
        return BOSS_SKILL_ATTACK;
    }


    public void bossSkillAttack(User user, BossMonster bossMonster) {

        if (bossMonster.getId().equals(BossMonsterType.Minotaur.getId())) {
            minotaur(user, bossMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.Goblin.getId())) {
            goblin(user, bossMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.CatDemon.getId())) {
            catDemon(user, bossMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.MechanicalCow.getId())) {
            mechanicalCow(user, bossMonster);
        }

    }

    /**
     * 机械牛
     *
     * @param user
     * @param bossMonster
     */
    private void mechanicalCow(User user, BossMonster bossMonster) {
        // 削弱对方防御10秒
        user.setWeakenDefense(100);
        BossSkill bossSkill = bossMonster.getBossSkillMap().get(4);
        int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())) + 100);
        user.setCurrHp(user.getCurrHp() - subHp);
        log.info("用户: {} ,受到boss技能加成: {} , 降低防御100持续10;", user.getUserName(), subHp);
    }

    /**
     * 猫妖
     *
     * @param user
     * @param bossMonster
     */
    private void catDemon(User user, BossMonster bossMonster) {
        //使对方出现出血状态
        BossSkill bossSkill = bossMonster.getBossSkillMap().get(3);
        int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())));
        user.setCurrHp(user.getCurrHp() - subHp);
        log.info("用户: {} ,受到boss技能加成: {} , 降低防御100持续10;", user.getUserName(), subHp);

        //记录

    }

    /**
     * 哥布林
     *
     * @param user
     * @param bossMonster
     */
    private void goblin(User user, BossMonster bossMonster) {
        //攻击全部玩家
        BossSkill bossSkill = bossMonster.getBossSkillMap().get(2);
        int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())));
        user.setCurrHp(user.getCurrHp() - subHp);
        log.info("用户: {} ,受到boss技能加成: {} ;", user.getUserName(), subHp);
    }

    /**
     * 牛头怪
     * 释放技能增加攻击力
     *
     * @param user
     * @param bossMonster
     */
    private void minotaur(User user, BossMonster bossMonster) {
        BossSkill bossSkill = bossMonster.getBossSkillMap().get(1);
        int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())));
        user.setCurrHp(user.getCurrHp() - subHp);
        log.info("用户: {} ,受到boss技能加成: {} , 降低防御100持续10;", user.getUserName(), subHp);
    }

}
