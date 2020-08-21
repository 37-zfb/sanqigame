package server.cmdhandler.duplicate;

import lombok.extern.slf4j.Slf4j;
import model.duplicate.BossMonster;
import model.duplicate.BossSkill;
import model.profession.SummonMonster;
import server.model.User;
import server.timer.UserAutomaticSubHpTimer;
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


    public void bossSkillAttack(User user, BossMonster bossMonster, SummonMonster summonMonster) {

        if (bossMonster.getId().equals(BossMonsterType.Minotaur.getId())) {
            minotaur(user, bossMonster,summonMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.Goblin.getId())) {
            goblin(user, bossMonster,summonMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.CatDemon.getId())) {
            catDemon(user, bossMonster,summonMonster);
        } else if (bossMonster.getId().equals(BossMonsterType.MechanicalCow.getId())) {
            mechanicalCow(user, bossMonster,summonMonster);
        }

    }

    /**
     * 机械牛
     *
     * @param user
     * @param bossMonster
     */
    private void mechanicalCow(User user, BossMonster bossMonster,SummonMonster summonMonster) {
        if (user!=null){
            // 削弱对方防御10秒
            user.setWeakenDefense(100);
            BossSkill bossSkill = bossMonster.getBossSkillMap().get(4);
            int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())) + 100);
            user.setCurrHp(user.getCurrHp() - subHp);
            log.info("用户: {} ,受到boss技能加成: {} , 降低防御100持续10;", user.getUserName(), subHp);
        }else {
            summonMonster.setWeakenDefense(100);
            BossSkill bossSkill = bossMonster.getBossSkillMap().get(4);
            int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (summonMonster.getBaseDefense() - summonMonster.getWeakenDefense())) + 100);
            summonMonster.setHp(summonMonster.getHp() - subHp);
            log.info("召唤兽 ,受到boss技能加成: {} , 降低防御100持续10;", subHp);
        }

    }

    /**
     * 猫妖
     *
     * @param user
     * @param bossMonster
     */
    private void catDemon(User user, BossMonster bossMonster,SummonMonster summonMonster) {
        if (user!=null){
            //使对方出现出血状态
            BossSkill bossSkill = bossMonster.getBossSkillMap().get(3);
            int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())));
            user.setCurrHp(user.getCurrHp() - subHp);
            log.info("用户: {} ,受到boss技能加成: {} , 进入出血状态;", user.getUserName(), subHp);

            //记录
            // 使用定时器
            UserAutomaticSubHpTimer.getInstance().userSubHpAuto(user, 10);
        }else {
            //使对方出现出血状态
            BossSkill bossSkill = bossMonster.getBossSkillMap().get(3);
            int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (summonMonster.getBaseDefense() - summonMonster.getWeakenDefense())));
            summonMonster.setHp(summonMonster.getHp() - subHp);
            log.info("召唤兽,受到boss技能加成: {} , 进入出血状态;", subHp);

            //记录
            // 使用定时器
            UserAutomaticSubHpTimer.getInstance().summonMonsterSubHpAuto(user, summonMonster, subHp);
        }


    }

    /**
     * 哥布林
     *
     * @param user
     * @param bossMonster
     */
    private void goblin(User user, BossMonster bossMonster,SummonMonster summonMonster) {
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
    private void minotaur(User user, BossMonster bossMonster,SummonMonster summonMonster) {
        BossSkill bossSkill = bossMonster.getBossSkillMap().get(1);
        int subHp = (int) ((Math.random() * bossSkill.getDamage()) + 200) - (int) ((Math.random() * (user.getBaseDefense() - user.getWeakenDefense())));
        user.setCurrHp(user.getCurrHp() - subHp);
        log.info("用户: {} ,受到boss技能加成: {};", user.getUserName(), subHp);
    }

}
