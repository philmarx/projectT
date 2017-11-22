package com.yywl.projectT.bo;

import java.util.Date;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.yywl.projectT.bean.Keys;
import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.bean.enums.WithdrawalsEnum;
import com.yywl.projectT.dao.JdbcDao;
import com.yywl.projectT.dao.PayOrderDao;
import com.yywl.projectT.dao.RoomMemberDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dao.WithdrawalsDao;
import com.yywl.projectT.dmo.PayOrderDmo;
import com.yywl.projectT.dmo.TransactionDetailsDmo;
import com.yywl.projectT.dmo.UserDmo;
import com.yywl.projectT.dmo.WithdrawalsDmo;
import io.rong.messages.TxtMessage;

@Service
@Transactional(rollbackOn = Throwable.class)
public class WithdrawalsBo {

	private static final Log log = LogFactory.getLog(WithdrawalsBo.class);

	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Autowired
	WithdrawalsDao withdrawalsDao;

	@Autowired
	private MoneyTransactionBo moneyTransactionBo;

	@Autowired
	private RongCloudBean rongCloud;
	@Autowired
	RoomMemberDao roomMemberDao;
	@Autowired
	UserDao userDao;
	@Autowired
	JdbcDao jdbcDao;
	@Autowired
	PayOrderDao payOrderDao;

	/**
	 * 提交支付宝提现订单
	 * 
	 * @param user
	 * @param realName
	 * @param alipayAccount
	 * @param money
	 * @throws Exception
	 */
	@Transactional(rollbackOn = Throwable.class)
	public void saveV2(UserDmo user, String alipayAccount, int money,String imei) throws Exception {
		if (!user.isAuthorized()) {
			log.error(user.getId() + ":请先实名认证");
			throw new Exception("请先实名认证");
		}
		if (user.getAmount().intValue() < money) {
			log.error(user.getId() + ":余额不足");
			throw new Exception("余额不足");
		}
		if (money < 10) {
			log.error(user.getId() + ":最少提现0.1元");
			throw new Exception("最少提现0.1元");
		}
		int refundMoney = this.jdbcDao.calcRefundMoney(user.getId());
		if (refundMoney > 0) {
			log.error(user.getId() + ":请先退款");
			throw new Exception("您还有" + (String.format("%.2f", refundMoney / 100.0)) + "元保证金可以退还，此渠道是不用收取手续费的哦~");
		}
		boolean isMoneyError = this.moneyTransactionBo.isMoneyError(user);
		if (isMoneyError) {
			log.error(user.getId() + ":您的余额存在异常，请等待审核。");
			user.setAmount(user.getAmount() - money);
			user.setLockAmount(user.getLockAmount() + money);
			this.userDao.save(user);
			TransactionDetailsDmo tran = new TransactionDetailsDmo();
			tran.setCreateTime(new Date());
			tran.setCalc(true);
			tran.setDescription("支付宝提现处理中");
			tran.setMoney(0 - money);
			tran.setUser(user);
			this.transactionDetailsDao.save(tran);
			WithdrawalsDmo withdrawalsDmo = new WithdrawalsDmo(null, user, alipayAccount, money,
					WithdrawalsEnum.金额异常.ordinal(), new Date(), null, 3);
			withdrawalsDmo.setImei(imei);
			this.withdrawalsDao.save(withdrawalsDmo);
			log.error("提现ERROR " + user.getId() + ":您的余额存在异常");
			new Thread(() -> {
				try {
					this.rongCloud.sendSystemTextMsgToOne(user.getId().longValue(),
							new TxtMessage("提现审核中，请等待1-2个工作日。", null));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
			return;
		}
		if (money <= 2000) {
			user.setAmount(user.getAmount() - money);
			this.userDao.save(user);
			TransactionDetailsDmo tran = new TransactionDetailsDmo();
			tran.setCalc(true);
			tran.setCreateTime(new Date());
			tran.setDescription("支付宝提现成功");
			tran.setMoney(0 - money);
			tran.setUser(user);
			this.transactionDetailsDao.save(tran);
			PayOrderDmo order = new PayOrderDmo();
			order.setNotifyTime(new Date());
			order.setOutTradeNo(user.getId() + "e" + System.currentTimeMillis());
			order.setRefundAmount(money);
			order.setTotalAmount(0);
			order.setTradeNo(user.getId() + "e" + System.currentTimeMillis());
			order.setType(Keys.ALIPAY_TYPE);
			this.payOrderDao.save(order);
			WithdrawalsDmo withdrawalsDmo = new WithdrawalsDmo(null, user, alipayAccount, money,
					WithdrawalsEnum.提现成功.ordinal(), new Date(), new Date(), money);
			withdrawalsDmo.setImei(imei);
			withdrawalsDmo.setDealAdminId(888888);
			this.withdrawalsDao.save(withdrawalsDmo);
			Map<String, String> result = this.moneyTransactionBo.withdrawalsV2(user, alipayAccount, money);
			order.setTradeNo(result.get("orderId"));
			order.setOutTradeNo(result.get("outBizNo"));
			this.payOrderDao.save(order);
		} else {
			this.userDao.save(user);
			WithdrawalsDmo dmo = new WithdrawalsDmo(null, user, alipayAccount, money, WithdrawalsEnum.处理中.ordinal(),
					new Date(), null, 0);
			dmo.setImei(imei);
			this.withdrawalsDao.save(dmo);
			this.moneyTransactionBo.withdrawals(user, money);
			new Thread(() -> {
				try {
					rongCloud.sendSystemTextMsgToOne(user.getId(),
							new TxtMessage("您的提现请求已提交，处理需要1-2个工作日，请耐心等待", user.getId() + ""));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		}
	}

}
