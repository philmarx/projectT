package com.yywl.projectT.web.controller;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.dao.ActivityDao;
import com.yywl.projectT.dao.AnnouncementDao;
import com.yywl.projectT.dao.ErrorMessageDao;
import com.yywl.projectT.dmo.ActivityDmo;
import com.yywl.projectT.dmo.AnnouncementDmo;
import com.yywl.projectT.dmo.ErrorMessageDmo;

@RestController
@RequestMapping("activity")
public class ActivityController {
	@Autowired
	ActivityDao activityDao;

	@RequestMapping("findAll")
	public Callable<ResultModel> findAll() {
		return () -> {
			Sort sort = new Sort(Direction.DESC, "id");
			List<ActivityDmo> list = this.activityDao.findByEnable(true, sort);
			return new ResultModel(true, "", list);
		};
	}

	@Autowired
	ErrorMessageDao errorMessageDao;

	@RequestMapping("sendError")
	public Callable<ResultModel> sendError(String platform, String version, String message, Long userId) {
		return () -> {
			this.errorMessageDao.save(new ErrorMessageDmo(null, userId, platform, version, new Date(), message));
			return new ResultModel(true);
		};
	}

	@Autowired
	AnnouncementDao announcementDao;

	@RequestMapping("findAnnouncements")
	public Callable<ResultModel> findAnnouncements() {
		return () -> {
			List<AnnouncementDmo> list = this.announcementDao.findByIsEffectiveAndExpiryDateGreaterThanEqual(true, new Date());
			return new ResultModel(true, "", list);
		};
	}
}
