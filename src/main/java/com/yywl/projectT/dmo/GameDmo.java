package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Table(name = "game")
@Entity
@JsonIgnoreProperties(value = "parentId")
public class GameDmo implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	@Column(name = "parent_id")
	@JsonIgnore
	private Integer parentId = 0;

	@Transient
	private List<GameDmo> children;
	
	/**
	 * 是否需要计分
	 */
	private boolean isScoring;
	
	public boolean isScoring() {
		return isScoring;
	}

	public void setScoring(boolean isScoring) {
		this.isScoring = isScoring;
	}

	/**
	 * 是否显示在排行榜
	 */
	private boolean isShow;
	
	public boolean isShow() {
		return isShow;
	}

	public void setShow(boolean isShow) {
		this.isShow = isShow;
	}

	public GameDmo() {
		super();
	}

	public GameDmo(Integer id) {
		super();
		this.id = id;
	}

	public GameDmo(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public List<GameDmo> getChildren() {
		return children;
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setChildren(List<GameDmo> children) {
		this.children = children;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

}
