package com.yywl.projectT.bo;

import java.util.List;

import com.yywl.projectT.dmo.GameDmo;

public interface GameBo {
	GameDmo findTree(Integer id);
	
	/**
	 * 获取根分类
	 * @return
	 */
	List<GameDmo> findRoots();
	/**
	 * 查找父目录
	 * @param parentId
	 * @return
	 */
	GameDmo findParent(Integer parentId);
}
