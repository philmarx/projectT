package com.yywl.projectT.bo;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yywl.projectT.dao.SeptActivityMovieDao;
import com.yywl.projectT.dmo.SeptActivityMovieDmo;

@Service
public class SeptActivityMovieBo {
	@Autowired
	SeptActivityMovieDao septActivityMovieDao;

	@Transactional(rollbackOn=Throwable.class)
	public void vote(List<Integer> movieIds) {
		List<SeptActivityMovieDmo> list=this.septActivityMovieDao.findByIdIn(movieIds);
		for (SeptActivityMovieDmo dmo : list) {
			dmo.setVote(dmo.getVote()+1);
			this.septActivityMovieDao.save(dmo);
		}
	}
}
