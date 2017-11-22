package com.yywl.projectT.dmo;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yywl.projectT.bean.GsonFactory;

@Entity
@Table(name = "user")
public class UserDmo implements Serializable {

	private static final long serialVersionUID = -7242158297419264901L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JsonIgnore
	private String token;

	/**
	 * 是否是超级用户(超级用户加入和创建房间不受限制)
	 */
	private boolean isSuperUser;

	/**
	 * 注册时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date registerTime;
	
	/**
	 * 初始化时间
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date initTime;
	
	
	/**
	 * 账号
	 */
	private String account;

	@JsonIgnore
	private String password;

	private String nickname = "";

	@OneToMany(mappedBy="user",fetch=FetchType.EAGER)
	@OrderBy("id desc")
	private List<ActivityTitleDmo> activityTitles=new LinkedList<>();

	private Integer amount = 0;

	private String realName;

	@Transient
	private boolean isVip;

	/**
	 * 徽章
	 */
	private int badge;

	/**
	 * 推荐者ID
	 */
	private long recommenderId=-1L;
	
	@Column(name = "lock_amount")
	private Integer lockAmount = 0;

	@Column(name = "id_card")
	@JsonIgnore
	private String idCard;

	private String phone;

	private boolean authorized;

	@Column(name = "qq_uid")
	@JsonIgnore
	private String qqUid;
	
	@Column(name = "wx_uid")
	@JsonIgnore
	private String wxUid;

	@Column(name = "xlwb_uid")
	@JsonIgnore
	private String xlwbUid;

	private Boolean gender;

	@Column(name = "is_init")
	private Boolean isInit = false;

	private String labels;
	
	private String avatarSignature = "";

	@Temporal(TemporalType.DATE)
	private Date birthday;

	public UserDmo() {
		super();
		this.recommenderId=-1L;
	}

	public UserDmo(Long id) {
		super();
		this.id = id;
		this.recommenderId=-1L;
	}

	public void addLabel(String label) {
		Set<String> labels = this.getLabels();
		if (labels==null) {
			labels=new HashSet<>();
		}
		labels.add(label);
		this.setLabels(labels);
	}

	public String getAccount() {
		return account;
	}

	public List<ActivityTitleDmo> getActivityTitles() {
		return activityTitles;
	}

	public Integer getAmount() {
		return amount;
	}

	public String getAvatarSignature() {
		return avatarSignature;
	}

	public int getBadge() {
		return badge;
	}

	public Date getBirthday() {
		return birthday;
	}

	public Boolean getGender() {
		return gender;
	}
	public Long getId() {
		return id;
	}

	public String getIdCard() {
		return idCard;
	}

	public Date getInitTime() {
		return initTime;
	}

	public Boolean getIsInit() {
		return isInit;
	}

	public boolean getIsVip() {
		return isVip;
	}

	public Set<String> getLabels() {
		@SuppressWarnings("unchecked")
		Set<String> list = GsonFactory.gson.fromJson(labels==null?"":labels, Set.class);
		return list == null ? new HashSet<String>() : list;
	}

	public Integer getLockAmount() {
		return lockAmount;
	}

	public String getNickname() {
		return this.nickname;
	}

	public String getPassword() {
		return password;
	}

	public String getPhone() {
		return phone;
	}

	public String getQqUid() {
		return qqUid;
	}

	public String getRealName() {
		return realName;
	}

	public long getRecommenderId() {
		return recommenderId;
	}

	public Date getRegisterTime() {
		return registerTime;
	}

	public String getToken() {
		return token;
	}

	public String getWxUid() {
		return wxUid;
	}

	public String getXlwbUid() {
		return xlwbUid;
	}

	public boolean isAuthorized() {
		return authorized;
	}

	public boolean isSuperUser() {
		return isSuperUser;
	}

	public void removeLabel(String label) {
		Set<String> labels = this.getLabels();
		labels.remove(label);
		this.setLabels(labels);
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public void setActivityTitles(List<ActivityTitleDmo> activityTitles) {
		this.activityTitles = activityTitles;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public void setAvatarSignature(String avatarSignature) {
		this.avatarSignature = avatarSignature;
	}

	public void setBadge(int badge) {
		this.badge = badge;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public void setGender(Boolean gender) {
		this.gender = gender;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public void setInitTime(Date initTime) {
		this.initTime = initTime;
	}

	public void setIsInit(Boolean isInit) {
		this.isInit = isInit;
	}

	public void setIsVip(boolean isVip) {
		this.isVip = isVip;
	}

	public void setLabels(Set<String> labels) {
		this.labels = GsonFactory.gson.toJson(labels);
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public void setLockAmount(Integer lockAmount) {
		this.lockAmount = lockAmount;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setQqUid(String qqUid) {
		this.qqUid = qqUid;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public void setRecommenderId(long presenterId) {
		this.recommenderId = presenterId;
	}

	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}

	public void setSuperUser(boolean isSuperUser) {
		this.isSuperUser = isSuperUser;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
	}

	public void setWxUid(String wxUid) {
		this.wxUid = wxUid;
	}

	public void setXlwbUid(String xlwbUid) {
		this.xlwbUid = xlwbUid;
	}
	
}
