package com.yywl.projectT.bean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yywl.projectT.bean.component.RongCloudBean;
import com.yywl.projectT.dmo.FriendDmo;

import io.rong.messages.TxtMessage;

@Component
public class FriendPointUtil {
	/**
	 * 活动结束或者发送小纸条后增加好友的好感度
	 * 
	 * @param dmo
	 */
	public void addFriendPoint(FriendDmo dmo) {
		if (dmo.getEvaluatePoint() == 0 || dmo.getEvaluatedPoint() == 0) {
			return;
		}
		double point = dmo.getPoint();
		// 如果是红色好友
		if (point < 2) {
			dmo.setPoint(ValidatorBean.friendPoint(point + 1));
			// 如果是灰色好友
		} else if (point < 4) {
			dmo.setPoint(ValidatorBean.friendPoint(point + 0.4));
			// 如果是绿色好友
		} else if (point < 6) {
			dmo.setPoint(ValidatorBean.friendPoint(point + 0.2));
			// 如果是蓝色好友
		} else if (point < 8) {
			dmo.setPoint(ValidatorBean.friendPoint(point + 0.1));
		}else if(point<10) {
			dmo.setPoint(ValidatorBean.friendPoint(point + 0.1));
		}
		dmo.setPoint(ValidatorBean.friendPoint(dmo.getPoint()));
		if (dmo.getPoint() == 7) {
			new Thread(()->{
				try {
					rongCloud.sendMessageToFriend(dmo.getOwner().getId(), dmo.getFriend().getId(),
							new TxtMessage("经过我们的不懈努力，我们终于成为蓝色好友啦！", null));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		} else if (dmo.getPoint() == 9) {
			new Thread(()->{
				try {
					rongCloud.sendMessageToFriend(dmo.getOwner().getId(), dmo.getFriend().getId(),
							new TxtMessage("经过我们的不懈努力，我们终于成为金色好友啦！", null));
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			}).start();
		}
	}

	private final static Log log = LogFactory.getLog(FriendPointUtil.class);

	@Autowired
	RongCloudBean rongCloud;
}
