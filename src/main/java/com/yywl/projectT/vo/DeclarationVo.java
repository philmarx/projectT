package com.yywl.projectT.vo;

import java.util.Date;
import java.util.List;

public class DeclarationVo {
	private Long id;

	private String declareNickname;

	private String content;

	private List<DeclarationEvaluationVo> evaluations;

	private Long declareId;

	private String avatarSignature;

	private Date createTime;

	public DeclarationVo() {
		super();
	}

	public DeclarationVo(Long id, Long declarerId, String content, List<DeclarationEvaluationVo> evaluations,
			String nickname, String avatarSignature, Date createTime) {
		super();
		this.id = id;
		this.content = content;
		this.evaluations = evaluations;
		this.declareNickname = nickname;
		this.avatarSignature = avatarSignature;
		this.declareId = declarerId;
		this.createTime = createTime;
	}

	public DeclarationVo(Long id, String content, List<DeclarationEvaluationVo> evaluations, Date createTime) {
		super();
		this.id = id;
		this.content = content;
		this.evaluations = evaluations;
		this.createTime=createTime;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}

	public String getContent() {
		return content;
	}

	public Long getDeclareId() {
		return declareId;
	}

	public String getDeclareNickname() {
		return declareNickname;
	}

	public List<DeclarationEvaluationVo> getEvaluations() {
		return evaluations;
	}

	public Long getId() {
		return id;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public void setDeclareId(Long declareId) {
		this.declareId = declareId;
	}

	public void setDeclareNickname(String declareNickname) {
		this.declareNickname = declareNickname;
	}

	public void setEvaluations(List<DeclarationEvaluationVo> evaluations) {
		this.evaluations = evaluations;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
