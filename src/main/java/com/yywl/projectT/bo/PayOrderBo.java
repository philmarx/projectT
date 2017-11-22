package com.yywl.projectT.bo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.transaction.Transactional;

import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.PayOrderDao;
import com.yywl.projectT.dao.TransactionDetailsDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.UserDmo;

@Service
public class PayOrderBo {
	@Autowired
	PayOrderDao payOrderDao;

	@Autowired
	UserDao userDao;

	@Autowired
	MoneyTransactionBo moneyTransactionBo;
	
	@Transactional(rollbackOn=Throwable.class)
	public void saveAlipayAmount(Map<String,String> params) throws Exception{
		String outTradeNo=params.get("out_trade_no");
		Long userId=Long.valueOf(outTradeNo.split("a")[0]);
		if (this.payOrderDao.existsByOutTradeNo(outTradeNo)) {
			return;
		}
		int totalAmount = (int) Math.floor(Double.valueOf(params.get("total_amount")) * 100);
		Date notifyTime=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(params.get("notify_time"));
		String tradeNo=params.get("trade_no");
		this.moneyTransactionBo.saveAlipayAmount(userId,tradeNo,outTradeNo,totalAmount,notifyTime);
	}
	
	@Autowired
	TransactionDetailsDao transactionDetailsDao;

	@Transactional(rollbackOn=Throwable.class)
	public void refundAlipay(long id,String outTradeNo, String tradeNo, int refundAmount, String outTradeNo2) throws Exception {
		this.moneyTransactionBo.refundAlipay(id, outTradeNo, tradeNo,refundAmount,outTradeNo2);
	}
	
	@Transactional(rollbackOn=Throwable.class)
	public void refundWeixin(long id,UserDmo user, Element root) throws Exception {
		String refund_fee=root.elementText("refund_fee");
		int refundFee=Integer.valueOf(refund_fee);
		String transactionId=root.elementText("transaction_id");
		String outTradeNo=root.elementText("out_trade_no").replaceAll("a", "b");
		this.moneyTransactionBo.refundWeixin(id,user,refundFee,transactionId,outTradeNo);
	}
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	@Transactional(rollbackOn=Throwable.class)
	public void saveWeiXinAmount(Element root) throws ParseException {
		//String bank_type = root.elementText("bank_type");
		//String cash_fee = root.elementText("cash_fee");
		//String is_subscribe = root.elementText("is_subscribe");
		//String nonce_str = root.elementText("nonce_str");
		//String openid = root.elementText("openid");
		String outTradeNo = root.elementText("out_trade_no");
		//String sign = root.elementText("sign");
		String time_end = root.elementText("time_end");
		String total_fee = root.elementText("total_fee");
		int totalFee=Integer.valueOf(total_fee);
		//String trade_type = root.elementText("trade_type");
		String transactionId = root.elementText("transaction_id");
		long userId = Long.valueOf(outTradeNo.split("a")[0]);
		if (this.payOrderDao.existsByOutTradeNo(outTradeNo)) {
			return;
		}
		Date createTime = dateFormat.parse(time_end);
		this.moneyTransactionBo.saveWeixinAmount(userId,outTradeNo,totalFee,transactionId,createTime);
	}

}
