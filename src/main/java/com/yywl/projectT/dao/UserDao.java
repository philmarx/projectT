package com.yywl.projectT.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yywl.projectT.dmo.UserDmo;

public interface UserDao extends JpaRepository<UserDmo, Long> {

	UserDmo findByPhone(String phone);

	UserDmo findByQqUid(String qqUid);

	UserDmo findByWxUid(String wxUid);

	UserDmo findByXlwbUid(String xlwbUid);

	long countByNickname(String nickname);

	boolean existsByPhone(String phone);

	boolean existsByNickname(String username);

	List<UserDmo> findByIdNot(Long id);

	boolean existsByQqUid(String uid);

	boolean existsByWxUid(String uid);

	boolean existsByXlwbUid(String uid);

	List<UserDmo> findByRecommenderId(long recommenderId);

	List<UserDmo> findByAvatarSignatureLike(String string);

	long countByIdNotAndNickname(long id, String nickname);

	boolean existsByAccount(String account);

	List<UserDmo> findByAccount(String account);

	long countByRecommenderIdAndIsInit(Long id, boolean b);

	List<UserDmo> findByQqUidIsNotNullOrWxUidIsNotNull();

	long countByIdCard(String idCard);
}
