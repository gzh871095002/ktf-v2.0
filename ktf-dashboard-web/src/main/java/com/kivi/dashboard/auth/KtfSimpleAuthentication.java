package com.kivi.dashboard.auth;

import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.form.LoginForm;
import com.kivi.framework.annotation.KtfTrace;
import com.kivi.framework.vo.UserVo;

public class KtfSimpleAuthentication implements KtfAuthentication {

	@KtfTrace("默认用户密码认证")
	@Override
	public boolean auth(LoginForm form, UserVo userVo) {
		return userVo.getPassword().equals(ShiroKit.md5(form.getPassword(), userVo.getLoginName() + userVo.getSalt()));
	}

	@Override
	public boolean verify(String userName, String plainData, String signData) {
		return true;
	}

}
