package com.yywl.projectT.bo;

import java.util.Date;
import java.util.List;

import com.yywl.projectT.dmo.RoomDmo;
import com.yywl.projectT.dmo.UserDmo;

public interface RoomBo {
	/**
	 * 加入房间
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param password
	 * @throws Exception
	 */
	void join(UserDmo user, Long roomId, String password) throws Exception;

	/**
	 * 校验加入的所有未开始的房间个数
	 * 
	 * @param userId
	 * @throws Exception
	 */
	void validateJoinAllCounts(long userId) throws Exception;

	void validateJoinTodayCounts(long userId, Date beginTime) throws Exception;

	/**
	 * 查找房间
	 * 
	 * @throws Exception
	 * 
	 */
	RoomDmo findOne(Long id) throws Exception;

	/**
	 * 创建房间
	 * 
	 * @param userId
	 * @param token
	 * @param name
	 * @param password
	 * @param beginTime
	 * @param endTime
	 * @param place
	 * @param money
	 * @param phone
	 * @param memberCount
	 * @param manCount
	 * @param womanCount
	 * @param description
	 * @param gameId
	 * @throws Exception
	 */
	RoomDmo createRoom(long userId, String token, String name, String password, String beginTime, String endTime,
			String place, int money, int memberCount, int manCount, int womanCount, String description,
			double longitude, double latitude, int gameId, String city, long belongCircle, boolean open, int gameMode)
			throws Exception;

	/**
	 * 分页查找房间
	 * 
	 * @param gameId
	 * @param page
	 *            从0开始
	 * @param size
	 * @param sort
	 *            查找的优先级，可以是join_member，distance,create_time,如果为空则是综合排序
	 * @param longitude
	 *            经度
	 * @param latitude
	 *            纬度
	 * @return
	 * @throws Exception
	 */
	List<RoomDmo> findRoomsByGameOrder(int gameId, String games, Integer state, Double longitude, Double latitude,
			int page, int size, String city) throws Exception;

	/**
	 * 查找我加入的房间
	 * 
	 * @param userId
	 * @param token
	 * @param page
	 *            从0开始
	 * @param size
	 * @return
	 * @throws Exception
	 */
	List<RoomDmo> findMyJoinRooms(UserDmo user, int page, int size) throws Exception;

	/**
	 * 查找本人创建的房间
	 * 
	 * @param userId
	 * @param token
	 * @param page从0开始
	 * @param size
	 * @return
	 * @throws Exception
	 */
	List<RoomDmo> findMyCreateRooms(Long userId, String token, int page, int size) throws Exception;

	/**
	 * 退出房间
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @throws Exception
	 */
	void quit(long userId, String token, long roomId) throws Exception;

	/**
	 * 修改房间信息
	 * 
	 * @param roomId
	 * @param name
	 * @param description
	 * @param womanCount
	 * @param manCount
	 * @param memberCount
	 * @param endTime
	 * @throws Exception
	 */
	void updateRoom(long userId, String token, long roomId, String name, String description, String beginTime,
			String endTime, int memberCount, int manCount, int womanCount) throws Exception;

	/**
	 * 离开房间
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @throws Exception
	 */
	void leave(long userId, String token, long roomId) throws Exception;

	/**
	 * 踢人
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param memberId
	 * @throws Exception
	 */
	void tiRen(long userId, String token, long roomId, long memberId, String reason) throws Exception;

	/**
	 * 准备开始
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @throws Exception
	 */
	void ready(long userId, String token, long roomId) throws Exception;

	/**
	 * 是否已经加入过
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @return
	 * @throws Exception
	 */
	boolean isJoined(long userId, String token, long roomId) throws Exception;

	/**
	 * 活动结束后设置好友关系
	 * 
	 * @param userId
	 * @param roomId
	 * @throws Exception
	 */
	void addFriends(long userId, long roomId);

	/**
	 * 删除房间
	 * 
	 * @param room
	 * @throws Exception
	 */
	void delete(RoomDmo room);

	/**
	 * 设置房间是否公开
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param isOpen
	 * @throws Exception
	 */
	void setOpen(long userId, String token, long roomId, boolean isOpen) throws Exception;

	/**
	 * 由前段收到cmd命令sendLocation 时调用。
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param latitude
	 * @param longitude
	 */
	void sendLocation(long userId, String token, long roomId, double latitude, double longitude, String place,
			String ip) throws Exception;

	/**
	 * 房主取消准备
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 */
	void managerCancelRoom(long userId, String token, long roomId) throws Exception;

	/**
	 * 房间评价完后计算表现的最终得分，并自动评价好友好感度，修改状态
	 * 
	 * @param room
	 * @throws Exception
	 */
	void evalute(RoomDmo room);

	/**
	 * 房主点击开始
	 * 
	 * @param user
	 * @param room
	 * @throws Exception
	 */
	void begin(UserDmo user, RoomDmo room, long now) throws Exception;

	/**
	 * 出发
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @throws Exception
	 */
	void attend(long userId, String token, long roomId) throws Exception;

	/**
	 * 签到
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param latitude
	 * @param longitude
	 * @throws Exception
	 */
	void sign(long userId, String token, long roomId, double latitude, double longitude, String ip) throws Exception;

	/**
	 * 补签
	 * 
	 * @param userId
	 * @param token
	 * @param roomId
	 * @param latitude
	 * @param longitude
	 * @throws Exception
	 */
	void signAgain(long userId, String token, long roomId, double latitude, double longitude, String ip)
			throws Exception;

	boolean isJoined(UserDmo user, Long roomId);

	void setOnline(long userId, long roomId, boolean online) throws Exception;

	List<RoomDmo> findMyRunningRooms(long userId, Integer gameId);

	void notLate(long userId, String token, long roomId, String reason, String photo, long certifierId)
			throws Exception;

	void cancelReady(long userId, String token, long roomId) throws Exception;

	void sendLocationV2(long userId, String token, Long roomId, double latitude, double longitude, String place,
			String udid, String ipAddress) throws Exception;

	RoomDmo createChatRoom(UserDmo user, String name, boolean anonymous, String password) throws Exception;


	List<RoomDmo> findByGame_Id(int game, int page, int size);

	List<RoomDmo> findRoomsByGameOrderV2(int gameId, String games, Integer state, Double longitude, Double latitude,
			int page, int size, String city) throws Exception;

	List<RoomDmo> findMyJoinRoomsV2(UserDmo userDmo, int page, int size);

	List<RoomDmo> findMyRunningRoomsV2(long userId, Integer gameId);

}
