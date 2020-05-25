package com.kivi.dashboard.service.impl;

import org.springframework.stereotype.Service;

import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.ShiroUser;
import com.kivi.framework.dto.JwtUserDTO;
import com.kivi.framework.service.IJwtUserServie;

@Service
public class JwtUserServie implements IJwtUserServie {

	@Override
	public String getCredential(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCredential(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logouted(JwtUserDTO jwtUser) {
		// TODO Auto-generated method stub
	}

	@Override
	public JwtUserDTO getLoginUser() {
		ShiroUser user = ShiroKit.getUser();
		return JwtUserDTO.builder().id(user.getId()).identifier(user.getLoginName()).name(user.getName())
				.userType(user.getUserType()).build();
	}

}
