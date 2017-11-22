package com.yywl.projectT.bean;

import org.springframework.stereotype.Component;

@Component
public class Keys {

	/**
	 * 默认端口
	 */
	public static final int SERVER_PORT = 8080;

	/**
	 * 主服务器的ip地址
	 */
	public static final String MAIN_SERVER_IP = "192.168.8.88";
	
	public class JavaMail{
		public static final String USERNAME="ios@hzease.com";
		public static final String PASSWORD="easeAPPLE123"; 
	}
	/**
	 * 每页最多显示行数
	 */
	public static final int PAGE_MAX_SIZE = 50;

	/**
	 * 查看排行榜显示的数量
	 */
	public static final int ORDER_SIZE = 15;

	/**
	 * 房间万能密码
	 *
	 */
	public static final String ROOM_KEY = "AMIIBCgseMsjbg2ds3BZ";

	public static class Aliyun {
		public final static String STS_ACCESS_KEY_SECRET = "2FGcnVJFUAj0RqFdaR3WskshqkD2Uv";
		public final static String STS_ACCESS_KEY_ID = "LTAIG9PISoHt0Dnq";
		public final static String STS_ROLE_ARN = "acs:ram::1227591017927389:role/aliyunosstokengeneratorrole";
		public final static String STS_ROLE_SESSION_NAME = "external-username";
		public final static String REGION_CN_HANGZHOU = "cn-hangzhou";
		public final static String STS_API_VERSION = "2015-04-01";
		// endpoint以杭州为例，其它region请按实际情况填写
		public static final String STS_ENDPOINT = "http://oss-cn-hangzhou.aliyuncs.com";
		public static final String STS_BUCKET_NAME="tomeet-app-files";
	}

	public static class Weixin {
		// 支付的appid
		public final static String APP_PAY_ID = "wxc9ba3b479546a874";
		public final static String APP_PAY_KEY = "DXFvNseMvdfIFmCcU6pUOk8IgigrCEuX";
		public final static String MCH_ID = "1480291952";
		public static final String CALL = "http://tomeet-app.hzease.com/weixin/call";
		public final static String BUY_BADGE_CALL = "http://tomeet-app.hzease.com/badge/weixin/call";
		public final static String BUY_PROP_CALL = "http://tomeet-app.hzease.com/prop/weixin/call";
		// 网站应用
		public static final String WEB_ID = "wx1455b640ec310a00";
		public static final String WEB_SECRET = "f96a096ecd1474e7a82929818be1fd3a";
		/**
		 * 订阅号id
		 */
		public static final String SUBSCRIBE_ID = "wx59b691c92ccb58e1";
		public static final String SUBSCRIBE_SECRET = "5691031c5f8530a41ec7a61422f1fb26";

		//小程序
		public static final String LITTLE_APP_ID = "wxf3131e8280bc920e";
		public static final String LITTLE_APP_SECRET = "1f7e9dcdec8e4eae18c672387ac865af";
	}

	/**
	 * 支付宝订单方式
	 */
	public static final String ALIPAY_TYPE = "支付宝";
	/**
	 * 微信支付方式
	 */
	public static final String WEIXIN_TYPE = "微信";

	public static final class Alipay {
		public final static String APP_ID = "2017042506958849";
		/**
		 * 应用私钥
		 */
		public final static String APP_PRIVATE_KEY = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDBr1Qv+/VmbdV3vGoEFgxInc1UFzavTQ8J/eQVTIVb2IDRE70gX9tWaYoxKNj4RK6UuXwvDujdzc1JosJj4oOXSlB+gVXQAvjNNKVfPlvMIDK8GlcDTQznU/aWisSAHYnEsCIajj6QjHW+gwy6zM000FyRCbhzN9zgsHSAWQ2i/K9gMYqY6WO0vjuQ+at8ySXo2ZvW/uQVbkaRYxdvaxk/p6rXOoQf1za7qC0pidMMLVZ9eAd7EOI3LLyV/C5TP+h3ei5C6lYWU7z5Fq/wpD3qk5VtRBZYudyYVL4PrFZ2hpTIoCiwQczlfluLqFKxT0N67wx1hXleZuPPeY8fwZM1AgMBAAECggEBAIEpIsM9CtFbvVcz1p8S9O4YDa1eLaGJidXn8goiFWzCXGUQ/LZyNhrSFs3hp48ofiO/7giTsMb5T0UjvO5PR+HENRmntkoZMUHnMcDqHN1rO2olRpF0+H3riC2sLI4jJJR9wMBky2Qpxvtc+Ug4+z+VE6XFDbQINMvB4G8W8/kTz/dLFREQuSxCfduStLqGXO+maxfgaZ69g+4beWnoEqznqqxmc78saqRkkqENc6TDLyqIoHHyZ4SyKk9PzJaXVjrFJigOYbAPtFUL4a4OE+3MJtItKhQV2g1yMepixAiJzJOoDe+9WQHZ4Av++9oyGS9RdmyqMM2Egsv7+6IBYqECgYEA4qQ/0BLZtJ3yXtYAOBsAAhJj6GnFWoioY420PVStGKG41DV34iAkyhFkGdpylrv8A15nC/JaepJHhjmalpbG15XtcQ+Rwm/kkMxPhMQ8RNnIc3hUwIy5B0m09xfPgNSmaN8Dckw0+i5qfE+UVGTcxG6fW0MCUT5ahqDEPjkM2qcCgYEA2sYwWxdLpwvzNnrdnRquDsMWLsYlVOIog0259fR+wq9N99UQ76kw8A7JZlwqF8u5bphX0YgiLQbTCUWwM76zAR+e0mXh0k/f2MgqtU61qAVP6KfrX7laZRpxe4riU5MUlCVJGoiJC+PQDa+kaz4qr5Zk7ux/tDGLBtC2fWI/isMCgYEAlTVhNZFTdo+No1XsKwPLi5Gj6LzJywKxKWl/QelfVBoUXtEcrehPkCDG8CZazlXQYj9iRAE4A/4NzJZb6cw1+HSiL476fTHBHnsD1vqCWymtyCduVJ3QqrWhyBMK71wQenZoBHWbYyHvxQt3MeB9X20cyK5i9i3A12U8DWLtzX0CgYAbAzWdSbcRnSweofUTHOQ49mWayOR7IqGV9NbtQ48TQsoBu9EnhmCAg5LijKa14XW37h+Jw6FUpe8QygYDnZxEmz7lA6dqbGpbPfvn/xv27xD6zSEG5MAAk89HTdK8NwDneeMY35rsWOirh/BpPXfPzmYQCiuKeEOOR98AhE0MOQKBgQDKihqfg5Pn6DVOKRvyfuR2UPXNpkmyEc8xhC7EMiuuKiCSI4966Ie66h/Zxkaoc7iyx6Tezg3KnKvVYAelz9YjbO9mQyFrQ/1k++xFGI/b2/j9vuEyiugdqfYMElBNkrQcPcYmg4jLz6c1nBkQpylnSDqAHrFk067xOOenwF7qKQ==";
				
		/**
		 * 应用公钥
		 */
		public final static String APP_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwa9UL/v1Zm3Vd7xqBBYMSJ3NVBc2r00PCf3kFUyFW9iA0RO9IF/bVmmKMSjY+ESulLl8Lw7o3c3NSaLCY+KDl0pQfoFV0AL4zTSlXz5bzCAyvBpXA00M51P2lorEgB2JxLAiGo4+kIx1voMMuszNNNBckQm4czfc4LB0gFkNovyvYDGKmOljtL47kPmrfMkl6Nmb1v7kFW5GkWMXb2sZP6eq1zqEH9c2u6gtKYnTDC1WfXgHexDiNyy8lfwuUz/od3ouQupWFlO8+Rav8KQ96pOVbUQWWLncmFS+D6xWdoaUyKAosEHM5X5bi6hSsU9Deu8MdYV5Xmbjz3mPH8GTNQIDAQAB";
		/**
		 * 阿里公钥
		 */
		public final static String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvCvLwb8jl/kO60x/O2Lpr8kYHIDvS2hY5ozVMKGrgO+xEr7uw69o4NomhYOve1GhQmWehRzt0RpICiVSmTgka2WH/Z/5V/DFw39P6hOKkLg19b6DAFTOJwz6rrRzKnkvqZwCRlbs3MR8LHdQvNnb+e4rv1ZQBRXSh/dfZOnhD+7UNNMqv3s7iUdhgxUxiItHEWjY5Pg+6pI/V4VfmEPEChhkCmwQJjSC3LR6GXuiLMXpJK5pAcbgaWtn2j4I3UN6B2hHzyFniVyWyLXfE79ZKpKW/WlaAxoL/GoaKHdc4KC/L/s/Ru+NJpkNRabQrbu2v9RvPgnBU7uAfgHqsLX7IQIDAQAB";
		public final static String FORMAT = "json";
		public final static String CHARSET = "utf-8";

		public final static String CALL = "http://tomeet-app.hzease.com/alipay/call";

		public final static String APP_GATEWAY = "http://tomeet-app.hzease.com/alipay/gateway";

		public final static String BUY_BADGE_CALL = "http://tomeet-app.hzease.com/badge/alipay/call";

		public final static String BUY_PROP_CALL = "http://tomeet-app.hzease.com/prop/alipay/call";
	}

	public static class RongCloud {
		// 正式环境
		public final static String APP_KEY = "8w7jv4qb8dlvy";
		public final static String APP_SECRET = "WVXvhJkK8hBqL5";

		// 开发环境
		// public final static String APP_KEY = "mgb7ka1nmvikg";
		// public final static String APP_SECRET = "fmnAt5ZeMPdp";

		public final static String CMD_MSG_REFRESH_ROOM = "refreshRoom";
		public final static String CMD_MSG_ROOM_DISSOLVE = "roomDissolve";
		public final static String CMD_MSG_SENDLOCATION = "sendLocation";
		public final static String CMD_MSG_OUTMAN = "outMan";
		public final static String CMD_MSG_REFRESH_FRIENDS = "refreshFriends";
		public final static String CMD_MSG_RECEIVE_NOTES = "receiveScrip";

	}

	public static class JPhsh {
		public final static String TEMP_ID = "49723";
		public final static String APP_KEY = "2c1a3f3d166e936294efc85e";
		public final static String MARKET_SECRET = "fc087c690a65cd1960467818";

		public final static String AUTHORIZATION = "Basic MmMxYTNmM2QxNjZlOTM2Mjk0ZWZjODVlOmZjMDg3YzY5MGE2NWNkMTk2MDQ2NzgxOA==";
		public final static String ALIAS = "testAlias";
	}

	public static class LoginType {
		public final static String PHONE = "PHONE";
		public final static String WEIBO = "WEIBO";
		public final static String WECHAT = "WECHAT";
		public final static String QQ = "QQ";
	}

	public final static String SECRET = "tomeet";

	public final static String RONGCLOUD_SYSTEM_ID = "888888";

	public final static int AVERAGE_POINT = 5;

	public final static int NOTE_BADGE = 5;

	/**
	 * 喊话消耗徽章
	 */
	public static final int DECLARATION_BADGE = 5;

	public static class Circle {
		/**
		 * 创建和加入圈子时扣除的勋章
		 */
		public final static int badgeSpend = 1;
		public final static String PREFIX = "";
		/**
		 * 在圈内加入一次活动，经验值增加10
		 */
		public final static int JOIN_EXPERIENCE_ADD = 10;
		/**
		 * 在圈内创建一次活动，经验值增加20
		 */
		public final static int CREATE_EXPERIENCE_ADD = 20;

		/**
		 * 圈子签到一次，经验值加2分
		 */
		public final static int SIGN_EXPERIENCE_ADD = 2;
		/**
		 * 最多加入12个圈子
		 */
		public static final int JOIN_MAX = 12;

	}

	/**
	 * 默认徽章数
	 */
	public final static int INIT_BADGE = 10;

	/**
	 * 推荐一个人添加5片叶子
	 */
	public final static int RECOMMENDER__BADGE = 5;

	/**
	 * 会员经历一场活动，徽章增加1
	 */
	public final static int VIP_BADGE_ADD = 1;

	/**
	 * 系统用户ID，用于存剩余的保证金
	 */
	public final static long SYSTEM_ID = 888888L;

	public static class Room {
		public final static String PREFIX = "";
		/**
		 * 娱乐模式的分数
		 */
		public final static int ENTERTAINMENT_POINT = 25;
		/**
		 * 初始化的分数
		 */
		public final static int INIT_POINT = 1000;

		/**
		 * 每场活动的总人数限制
		 */
		public final static int MEMBER_COUNT = 50;

		/**
		 * 默认每天最多进行的活动数
		 */
		public final static int MAX_ROOM_COUNT = 3;

		/**
		 * 活动地点的有效范围
		 */
		public static final int DISTANCE = 300;
		/**
		 * 最多创建/加入10个房间
		 */
		public static final int JOIN_MAX = 10;
		/**
		 * 会员每天最多只能创建/加入3场活动
		 */
		public static final int VIP_JOIN_TODAY_MAX = 3;
		/**
		 * 每天最多只能创建/加入2场活动
		 */
		public static final int JOIN_TODAY_MAX = 2;
	}

	/**
	 * 使用一个徽章增加10个经验值
	 */
	public final static int USE_BADGE_ADD_EXPERIENCE = 10;

	/**
	 * 加入圈子时的经验值
	 */
	public static final int JOIN_CIRCLE_INIT_EXPERIENCE = 100;

	/**
	 * 创建圈子时增加的经验值
	 */
	public static final int CREATE_BADGE_ADD_EXPERIENCE = 200;

}
