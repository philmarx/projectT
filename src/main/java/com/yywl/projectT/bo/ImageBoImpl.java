package com.yywl.projectT.bo;

import javax.transaction.Transactional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.ImageDao;
import com.yywl.projectT.dao.UserDao;
import com.yywl.projectT.dmo.ImageDmo;
import com.yywl.projectT.dmo.UserDmo;

@Service
@Transactional(rollbackOn = Throwable.class)
public class ImageBoImpl implements ImageBo {
	@Autowired
	ImageDao imageDao;
	@Autowired
	UserBo userBo;

	@Autowired
	UserDao userDao;

	private static final Log log=LogFactory.getLog(ImageBoImpl.class);
	
	@Override
	public void save(long userId, String token, String signature, String whitch) throws Exception {
		UserDmo user = userBo.loginByToken(userId, token);
		switch (whitch) {
		case "avatar":
			user.setAvatarSignature(signature);
			userDao.save(user);
			return;
		default:
			ImageDmo dmo = imageDao.findByUserId(user.getId());
			if (dmo == null) {
				dmo = new ImageDmo();
				dmo.setUserId(user.getId());
			}
			switch (whitch) {
			case "image1":
				dmo.setImage1Signature(signature);
				break;
			case "image2":
				dmo.setImage2Signature(signature);
				break;
			case "image3":
				dmo.setImage3Signature(signature);
				break;
			case "image4":
				dmo.setImage4Signature(signature);
				break;
			case "image5":
				dmo.setImage5Signature(signature);
				break;
			default:
				log.error("witch参数不正确");
				throw new Exception("witch参数不正确");
			}
			imageDao.save(dmo);
			break;
		}
	}
}
