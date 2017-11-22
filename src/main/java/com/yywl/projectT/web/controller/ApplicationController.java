package com.yywl.projectT.web.controller;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yywl.projectT.bean.IPBean;
import com.yywl.projectT.bean.ResultModel;
import com.yywl.projectT.dao.ApplicationDao;
import com.yywl.projectT.dao.DownloadChannelDao;
import com.yywl.projectT.dmo.ApplicationDmo;
import com.yywl.projectT.dmo.DownloadChannelDmo;

@RequestMapping("application")
@RestController
public class ApplicationController {

	/**
	 * 查看当前版本
	 * 
	 * @return
	 */
	@RequestMapping("versions/current")
	public Callable<String> currentVersion(String platform) {
		return () -> {
			List<ApplicationDmo> applicationDmos = this.applicationDao.findByPlatformAndIsCurrent(platform, true);
			if (applicationDmos != null && !applicationDmos.isEmpty()) {
				return applicationDmos.get(0).getVersion();
			} else {
				return "";
			}
		};
	}

	@RequestMapping("findOne")
	public Callable<ResultModel> findOne(String platform) {
		return () -> {
			List<ApplicationDmo> applicationDmos = this.applicationDao.findByPlatformAndIsCurrent(platform, true);
			if (applicationDmos != null && !applicationDmos.isEmpty()) {
				ApplicationDmo dmo = applicationDmos.get(0);
				Map<String, Object> map = new HashMap<>();
				map.put("downUrl", dmo.getDownUrl());
				map.put("version", dmo.getVersion());
				map.put("message", dmo.getMessage());
				map.put("force", dmo.isForce());
				map.put("remain", dmo.isRemind());
				return new ResultModel(true, "", map);
			} else {
				return new ResultModel(false, "当前没有对应的版本号", null);
			}
		};
	}

	@RequestMapping("getIp")
	public Callable<String> getIp(HttpServletRequest req) {
		return () -> {
			return IPBean.getIpAddress(req);
		};
	}

	@RequestMapping("getServerIp")
	public Callable<String> getServerIp() {
		return () -> {
			// 获取服务器的ip地址
			InetAddress address = InetAddress.getLocalHost();
			String hostAddress = address.getHostAddress();
			return hostAddress;
		};
	}

	@Autowired
	ApplicationDao applicationDao;

	@Autowired
	DownloadChannelDao downloadChannelDao;

	@RequestMapping("saveChannel")
	public Callable<ResultModel> saveChannel(String channel, String url, String description) {
		return () -> {
			if (StringUtils.isEmpty(url)) {
				return new ResultModel(false);
			}
			DownloadChannelDmo dmo = new DownloadChannelDmo(null, url, channel, description);
			this.downloadChannelDao.save(dmo);
			return new ResultModel();
		};
	}
}
