package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.dmo.CircleDmo;
import com.yywl.projectT.vo.CircleVo;

public interface CircleBo {

	void update(long userId, String token, long circleId, String notice,String avatarSignature,String bgSignature) throws Exception;

	void update(long userId, String token, long circleId, String notice) throws Exception;
	
	void remove(long userId, String token, long circleId) throws Exception;

	List<CircleDmo> findPage(int page, int size);

	void join(long userId, String token, long circleId) throws Exception;

	void quit(long userId, String token, long circleId) throws Exception;

	List<CircleDmo> findByManager_Id(Long userId);

	List<CircleDmo> findByNameLike(String name, int page, int size);

	/**
	 * 查看附近的圈子
	 * 
	 * @param circleId
	 * @param longitude
	 * @param latitude
	 * @return
	 */
	List<CircleDmo> findNearBy(double longitude, double latitude);

	/**
	 * 查看我加入的圈子，包括我创建的圈子
	 * 
	 * @param userId
	 * @param token
	 * @param page
	 * @param size
	 * @return
	 * @throws Exception
	 */
	List<CircleVo> findMyCircle(long userId, String token, int page, int size) throws Exception;

	/**
	 * 查看推荐的圈子
	 * 
	 * @return
	 */
	List<CircleDmo> findRecommand();

	/**
	 * 修改圈子头像，type=1修改主头像，type=2修改背景头像
	 * 
	 * @param userId
	 * @param token
	 * @param circleId
	 * @param signature
	 * @param type
	 * @throws Exception
	 */
	void updateImage(long userId, String token, long circleId, String signature, int type) throws Exception;

	/**
	 * 签到
	 * 
	 * @param userId
	 * @param token
	 * @param circleId
	 * @throws Exception
	 */
	void sign(long userId, String token, long circleId) throws Exception;

	/**
	 * 使用徽章增加经验值
	 * 
	 * @param userId
	 * @param token
	 * @param badge
	 * @throws Exception
	 */
	void useBadge(long userId, String token, long circleId, int badge) throws Exception;

	CircleDmo create(long userId, String token, String name, String city, String place, String notice, double longitude,
			double latitude) throws Exception;
}
