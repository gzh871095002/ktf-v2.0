package com.kivi.dashboard.shiro.configure;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kivi.dashboard.shiro.service.ShiroUserService;
import com.kivi.dashboard.shiro.service.impl.ShiroUserServiceImpl;
import com.kivi.dashboard.sys.service.ISysRoleService;
import com.kivi.dashboard.sys.service.ISysUserEnterpriseService;
import com.kivi.dashboard.sys.service.ISysUserService;
import com.kivi.framework.properties.KtfDashboardProperties;

/**
 * @Description Apache Shiro配置类
 */

@Configuration
public class ShiroUserConfig {

	@Autowired(required = false)
	private ISysUserService				userService;
	@Autowired(required = false)
	private ISysRoleService				roleService;
	@Autowired(required = false)
	private ISysUserEnterpriseService	userEnterpriseService;

	@Reference(version = KtfDashboardProperties.DUBBO_VERSION, check = false)
	private ISysUserService				dubboUserService;
	@Reference(version = KtfDashboardProperties.DUBBO_VERSION, check = false)
	private ISysRoleService				dubboRoleService;
	@Reference(version = KtfDashboardProperties.DUBBO_VERSION, check = false)
	private ISysUserEnterpriseService	dubboUserEnterpriseService;

	/*
	 * @ConditionalOnProperty( prefix = KtfDashboardProperties.PREFIX, value =
	 * "enable-dubbo", havingValue = "false", matchIfMissing = true)
	 */
	@ConditionalOnMissingClass(value = { "com.kivi.dubbo.annotation.EnableKtfDubbo" })
	@Bean("shiroUserService")
	public ShiroUserService shiroUserService() {

		return new ShiroUserServiceImpl(userService, roleService, userEnterpriseService);
	}

	/*
	 * @ConditionalOnProperty( prefix = KtfDashboardProperties.PREFIX, value =
	 * "enable-dubbo", havingValue = "true", matchIfMissing = false)
	 */
	@ConditionalOnClass(name = { "com.kivi.dubbo.annotation.EnableKtfDubbo" })
	@Bean("shiroUserServiceDubbo")
	public ShiroUserService shiroUserServiceDubbo() {

		return new ShiroUserServiceImpl(dubboUserService, dubboRoleService, dubboUserEnterpriseService);
	}

}
