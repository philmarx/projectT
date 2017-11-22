package com.yywl.projectT.dmo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "image")
public class ImageDmo implements Serializable {

	private static final long serialVersionUID = -710977005207698777L;
	@JsonIgnore
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	private long userId;

	@Column(name="image1_signature")
	private String image1Signature="";
	@Column(name="image2_signature")
	private String image2Signature="";
	@Column(name="image3_signature")
	private String image3Signature="";
	@Column(name="image4_signature")
	private String image4Signature="";
	@Column(name="image5_signature")
	private String image5Signature="";

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public String getImage1Signature() {
		return image1Signature;
	}

	public void setImage1Signature(String image1Signature) {
		this.image1Signature = image1Signature;
	}

	public String getImage2Signature() {
		return image2Signature;
	}

	public void setImage2Signature(String image2Signature) {
		this.image2Signature = image2Signature;
	}

	public String getImage3Signature() {
		return image3Signature;
	}

	public void setImage3Signature(String image3Signature) {
		this.image3Signature = image3Signature;
	}

	public String getImage4Signature() {
		return image4Signature;
	}

	public void setImage4Signature(String image4Signature) {
		this.image4Signature = image4Signature;
	}

	public String getImage5Signature() {
		return image5Signature;
	}

	public void setImage5Signature(String image5Signature) {
		this.image5Signature = image5Signature;
	}

}
