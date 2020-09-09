package com.kivi.dashboard.shiro.service.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.kivi.dashboard.shiro.service.ShiroUserService;
import com.kivi.dashboard.sys.service.ISysRoleService;
import com.kivi.dashboard.sys.service.ISysUserEnterpriseService;
import com.kivi.dashboard.sys.service.ISysUserService;
import com.kivi.framework.vo.UserVo;

public class ShiroUserServiceImpl implements ShiroUserService {

	private final ISysUserService			userService;
	private final ISysRoleService			roleService;
	private final ISysUserEnterpriseService	userEnterpriseService;

	public ShiroUserServiceImpl(ISysUserService userService, ISysRoleService roleService,
			ISysUserEnterpriseService userEnterpriseService) {
		this.userService			= userService;
		this.roleService			= roleService;
		this.userEnterpriseService	= userEnterpriseService;
	}

	@Override
	public UserVo getUserByLoginName(String loginName) {
		return userService.selectByLoginName(loginName);
	}

//	@Override
//	public SysRole getRoleById(Long roleId) {
//		return roleService.getById(roleId);
//	}

	@Override
	public List<Long> getEnterpriseIdByUserId(Long userId) {
		return userEnterpriseService.selectEnterpriseIdByUserId(userId);
	}

	@Override
	public UserVo getUserById(Long userId) {
		return userService.selectByUserId(userId);
	}

	@Override
	public Set<String> getUserPermissions(Long userId) {
		Set<String> urls = userService.selectUserPermissions(userId);
		return urls.stream().filter(url -> StringUtils.isNoneBlank(url)).collect(Collectors.toSet());
	}

}
