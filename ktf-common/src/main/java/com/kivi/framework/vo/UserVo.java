package com.kivi.framework.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.alibaba.fastjson.JSON;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @description：UserVo
 */
@ApiModel(value = "UserVo对象", description = "用户")
@Data
@EqualsAndHashCode(callSuper = false)
public class UserVo implements Serializable {
	private static final long	serialVersionUID	= 1L;

	@ApiModelProperty(value = "主键id")
	private Long				id;

	@ApiModelProperty(value = "客户ID")
	private Long				cifId;

	@ApiModelProperty(value = "登录类型，00：手机号，01：邮箱，02：用户名，03：微信，04：支付宝，05：微博")
	private String				loginMode;

	@ApiModelProperty(value = "登陆名")
	private String				loginName;

	@ApiModelProperty(value = "密码")
	private String				password;

	@ApiModelProperty(value = "用户名")
	private String				name;

	@ApiModelProperty(value = " 性别")
	private Integer				sex;

	@ApiModelProperty(value = "用户类别（0：超级管理员，1：系统管理员、2：业务管理员、3：密钥操作员、4：密钥审核员、5：审计管理员、6：审计员）")
	private Integer				userType;

	@ApiModelProperty(value = "应用ID，默认值0")
	private Long				applicationId;

	@ApiModelProperty(value = "手机号")
	private String				phone;

	@ApiModelProperty(value = "邮箱")
	private String				email;

	@ApiModelProperty(value = "用户状态(0：正常，1：不正常)")
	private Integer				status;

	@ApiModelProperty(value = "过期字段（0-不过期，1-过期）")
	private Integer				expired;

	@ApiModelProperty(value = "所属企业")
	private Long				enterpriseId;

	@ApiModelProperty(value = "所属部门")
	private Long				departmentId;

	@ApiModelProperty(value = "用户职务")
	private Long				jobId;

	@ApiModelProperty(value = "是否领导（0-是，1-否）")
	private Integer				isLeader;

	@ApiModelProperty(value = "记录创建用户ID")
	private Long				createUserId;

	private String				enterpriseName;

	private String				departmentName;

	private String				jobName;

	/**
	 * 密码加密盐
	 */
	private String				salt;

	/**
	 * 上次登录IP
	 */
	private String				lastIp;
	/**
	 * 上次登录时间
	 */
	private LocalDateTime		lastTime;
	
	private LocalDateTime gmtCreate;

	/**
	 * 角色Id列表
	 */
	private List<Long>			roleIdList;

	/**
	 * 用户拥有的企业Id列表
	 */
	private List<Long>			enterpriseIdList;

	private List<RoleVo>		roles;

	/**
	 * 密码盐
	 */
	public String getCredentialsSalt() {
		return getLoginName() + getSalt();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}