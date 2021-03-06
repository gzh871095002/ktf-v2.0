package com.kivi.dashboard.shiro;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kivi.dashboard.shiro.service.ShiroUserService;
import com.kivi.framework.component.SpringContextHolder;
import com.kivi.framework.constant.KtfError;
import com.kivi.framework.exception.KtfException;
import com.kivi.framework.service.KtfTokenService;
import com.kivi.framework.util.kit.DateTimeKit;
import com.kivi.framework.vo.ResourceVo;
import com.kivi.framework.vo.RoleVo;
import com.kivi.framework.vo.UserVo;
import com.kivi.framework.web.jwt.JwtKit;
import com.vip.vjtools.vjkit.collection.ListUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ShiroUserKit {

	@Autowired
	private KtfTokenService		ktfTokenService;

	@Autowired
	private ShiroUserService	shiroUserService;

	public static ShiroUserKit me() {
		return SpringContextHolder.getBean(ShiroUserKit.class);
	}

	/**
	 * 将UserVo赋值给shiroUser
	 *
	 * @param userVo
	 * @return
	 */
	public ShiroUser userVoToShiroUser(UserVo userVo) {
		if (userVo == null) {
			return null;
		} else {
			ShiroUser su = new ShiroUser();
			su.setId(userVo.getId());
			su.setCifId(userVo.getCifId());
			su.setName(userVo.getName());
			su.setLoginName(userVo.getLoginName());
			su.setUserType(userVo.getUserType());
			su.setStatus(userVo.getStatus());
			su.setIsLeader(userVo.getIsLeader());
			su.setLastIp(userVo.getLastIp());
			su.setLastTime(DateTimeKit.toDate(userVo.getLastTime()));
			List<RoleVo>	rvList	= userVo.getRoles();
			List<String>	urlSet	= new ArrayList<>();
			List<String>	roles	= new ArrayList<>();
			if (rvList != null && !rvList.isEmpty()) {
				for (RoleVo rv : rvList) {
					roles.add(rv.getName());
					List<ResourceVo> rList = shiroUserService.getRoleById(rv.getId()).getPermissions();
					if (rList != null && !rList.isEmpty()) {
						for (ResourceVo r : rList) {
							if (StringUtils.isNotBlank(r.getUrl())) {
								urlSet.add(r.getUrl());
							}
						}
					}
				}
			}
			su.setRoles(roles);
			su.setUrlSet(urlSet);
			List<Long>	enterpriseIdList	= new ArrayList<>();
			List<Long>	enterpriseIds		= shiroUserService.getEnterpriseIdByUserId(userVo.getId());
			if (enterpriseIds != null && enterpriseIds.size() > 0) {
				enterpriseIdList.addAll(enterpriseIds);
			}
			if (userVo.getEnterpriseId() != null) {
				enterpriseIdList.add(userVo.getEnterpriseId());
			}
			su.setEnterpriseIdList(removeDuplicate(enterpriseIdList));
			su.setEnterpriseId(userVo.getEnterpriseId());
			su.setDepartmentId(userVo.getDepartmentId());
			su.setJobId(userVo.getJobId());
			return su;
		}
	}

	/**
	 * list去重复
	 *
	 * @param list
	 * @return
	 */
	public static List<Long> removeDuplicate(List<Long> list) {
		ListUtil.uniqueNotNullList(list);
		return list;
	}

	/**
	 * 验证 accessToken
	 * 
	 * @param userId
	 * @param accessToken
	 * @return
	 */
	public Boolean verifyAccessToken(String userId, String accessToken) {

		String token = ktfTokenService.cache(userId);
		log.trace("从缓存中获取用户{}的token:{}", userId, token);
		if (token == null) {
			throw new KtfException(KtfError.E_UNAUTHORIZED, "登录状态已过期，请重新登录");
		}

		// 验证 token
		if (!JwtKit.verify(accessToken, token)) {
			log.error("验证JWT token失败");
			throw new KtfException(KtfError.E_UNAUTHORIZED, "用户尚未登录，请重新登录");
		}

		return true;
	}
}
