package com.yywl.projectT.bo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.ActivityDao;
import com.yywl.projectT.dao.AugActivityAllPrizeMoneyDao;
import com.yywl.projectT.dao.AugActivityHelperDao;
import com.yywl.projectT.dao.AugActivityLuckDrawDao;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.PropDao;
import com.yywl.projectT.dao.SeptActivityAllPrizeMoneyDao;
import com.yywl.projectT.dao.SeptActivityLuckDrawDao;
import com.yywl.projectT.dao.SpreadUserDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ActivityDmo;
import com.yywl.projectT.dmo.AugActivityAllPrizeMoneyDmo;
import com.yywl.projectT.dmo.AugActivityLuckDrawDmo;
import com.yywl.projectT.dmo.PropDmo;
import com.yywl.projectT.dmo.SeptActivityAllPrizeMoneyDmo;
import com.yywl.projectT.dmo.SeptActivityLuckDrawDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
@Transactional(rollbackOn = Throwable.class)
public class AugActivityLuckDrawBo {

	private final static Log log=LogFactory.getLog(AugActivityLuckDrawBo.class);

	@Autowired
	AugActivityLuckDrawDao luckDrawDao;

	@Autowired
	JdbcDao jdbcDao;

	@Autowired
	AugActivityAllPrizeMoneyDao allPrizeMoneyDao;
	
	@Autowired
	MoneyTransactionBo moneyTransactionBo;
	
	@Autowired
	AugActivityHelperDao augActivityHelperDao;
	
	@Transactional(rollbackOn = Throwable.class)
	public Map<String, Object> draw(UserDmo user) throws Exception {
		Map<String, Object> map = new HashMap<>();
		AugActivityAllPrizeMoneyDmo allPrize = this.allPrizeMoneyDao.findOne(0L);
		long helperCount = this.augActivityHelperDao.countByHelper_Id(user.getId());
		long drawCount = this.luckDrawDao.countByUser_Id(user.getId());
		Random random = new Random(System.currentTimeMillis());
		int randomInt=random.nextInt(10)+1;
		int money = 0;
		if (drawCount == 0) {
			if (helperCount < 10) {
				log.error(user.getId()+":助力10人可获取一次抽奖机会");
				throw new Exception("助力10人可获取一次抽奖机会");
			}
			int value = random.nextInt(10000);
			if (value < 9) {// 100元
				money = 10000;
			} else if (value < 99) {// 50元
				money = 5000;
			} else if (value < 999) {// 10元
				money = 1000;
			} else {// 5元
				money = 500;
			}
			if (allPrize.getAllMoney()<=money) {
				money=allPrize.getAllMoney();
				randomInt=1;
			}
			AugActivityLuckDrawDmo dmo = new AugActivityLuckDrawDmo(null, user, money, 1,new Date());
			this.luckDrawDao.save(dmo);
			allPrize.setAllMoney(allPrize.getAllMoney() - dmo.getMoney()*randomInt);
			allPrize.setJoinMember(allPrize.getJoinMember() + randomInt);
			this.allPrizeMoneyDao.save(allPrize);
			map.put("money", dmo.getMoney());
			map.put("frequency", (helperCount < 5 ? 1 : 2)-1);
			map.put("joinMember", allPrize.getJoinMember());
			map.put("allMoney", allPrize.getAllMoney());
			moneyTransactionBo.luckDraw(user,money);
			return map;
		}
		if (drawCount == 1 ) {
			if (helperCount < 25) {
				log.error(user.getId()+":助力25人可再获取一次抽奖机会");
				throw new Exception("助力25人可再获取一次抽奖机会");
			}
			int value = random.nextInt(10000);
			if (value < 9) {// 100元
				money = 10000;
			} else if (value < 99) {// 50元
				money = 5000;
			} else if (value < 999) {// 10元
				money = 1000;
			} else {// 5元
				money = 500;
			}
			if (allPrize.getAllMoney()<=money) {
				money=allPrize.getAllMoney();
				randomInt=1;
			}
			AugActivityLuckDrawDmo dmo = new AugActivityLuckDrawDmo(null, user, money, 2,new Date());
			this.luckDrawDao.save(dmo);
			allPrize.setAllMoney(allPrize.getAllMoney() - dmo.getMoney()*randomInt);
			allPrize.setJoinMember(allPrize.getJoinMember() + 10*randomInt);
			this.allPrizeMoneyDao.save(allPrize);
			map.put("money", dmo.getMoney());
			map.put("joinMember", allPrize.getJoinMember());
			map.put("allMoney", allPrize.getAllMoney());
			map.put("frequency", 0);
			moneyTransactionBo.luckDraw(user,money);
			return map;
		}
		log.error("您的抽奖次数已用完");
		throw new Exception("您的抽奖次数已用完");
	}

	@Autowired
	UserDao userDao;
	
	@Autowired
	SeptActivityLuckDrawDao septActivityLuckDrawDao;
	@Autowired
	ActivityDao activityDao;
	@Autowired
	SeptActivityAllPrizeMoneyDao septActivityAllPrizeMoneyDao;
	@Autowired
	SpreadUserDao spreadUserDao;
	@Transactional(rollbackOn = Throwable.class)
	public Map<String,Object> septDraw(UserDmo user) throws Exception {
		int count=this.spreadUserDao.countByUserId(user.getId());
		if (count!=0) {
			Map<String,Object> map=new HashMap<>();
			map.put("money", 0);
			return map;
		}
		Date now=new Date();
		ActivityDmo activityDmo=this.activityDao.findOne(3L);
		if (now.after(activityDmo.getEndTime())) {
			throw new Exception("活动已结束");
		}
		long userId=user.getId();
		//推荐人数
		Long recomends=this.userDao.countByRecommenderIdAndIsInit(userId,true);
		//抽过的次数
		Long drawFrequence=this.septActivityLuckDrawDao.countByUser_Id(userId);
		if ((recomends-drawFrequence*3)<3) {
			throw new Exception("您没有抽奖机会");
		}
		Random random=new Random(System.currentTimeMillis());
		int value = random.nextInt(10000);
		int money=0;
		if (value < 3000) {// 1元30% 
			money = 100;
		} else if (value < 6000) { //3元30%
			money = 300;
		} else if (value < 9000) { //5元30% 
			money = 500;
		} else if(value<(9500)){//10元 5%
			money = 1000;
		}else if(value<9900) {//观影券4%
			money=-1;
		}else if(value<9990) {//50元0.9%
			money=5000;
		}else { //100元 0.1%
			money=10000;
		}
		SeptActivityLuckDrawDmo dmo=new SeptActivityLuckDrawDmo(null, user, money, now);
		this.septActivityLuckDrawDao.save(dmo);
		if (money==-1) {
			PropDmo prop=this.propBo.findByUser_Id(user.getId());
			prop.setRemainMovieTicket(prop.getRemainMovieTicket()+1);
			propDao.save(prop);
		}else{
			this.moneyTransactionBo.luckDraw(user, money);
		}
		Map<String,Object> map=new HashMap<>();
		map.put("money", money);
		SeptActivityAllPrizeMoneyDmo record= septActivityAllPrizeMoneyDao.findOne(1L);
		record.setTimes(record.getTimes()+1);
		if (money==-1) {
			record.setMovieTicket(record.getMovieTicket()+1);
		}else {
			record.setMoney(record.getMoney()+money);
		}
		this.septActivityAllPrizeMoneyDao.save(record);
		return map;
	}
	@Autowired
	PropBo propBo;
	
	@Autowired
	PropDao propDao;
}
