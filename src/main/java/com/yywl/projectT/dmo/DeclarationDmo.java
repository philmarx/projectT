package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "declaration")
public class DeclarationDmo implements Serializable {

	private static final long serialVersionUID = -5761514746321175410L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "declarer_id")
	private UserDmo declarer;
	
	private String content;
	@OneToMany(mappedBy="declaration")
	@OrderBy("createTime ASC")
	private List<DeclarationEvaluationDmo> evaluations=new LinkedList<>();
	private String city;
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createTime;
	public Long getId() {
		return id;
	}
	
	public List<DeclarationEvaluationDmo> getEvaluations() {
		return evaluations;
	}

	public void setEvaluations(List<DeclarationEvaluationDmo> evaluations) {
		this.evaluations = evaluations;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public UserDmo getDeclarer() {
		return declarer;
	}
	public void setDeclarer(UserDmo declarer) {
		this.declarer = declarer;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public DeclarationDmo() {
		super();
	}
	
	

}
