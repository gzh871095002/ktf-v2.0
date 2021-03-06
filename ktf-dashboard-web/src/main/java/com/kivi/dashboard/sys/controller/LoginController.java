package com.kivi.dashboard.sys.controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.code.kaptcha.Producer;
import com.kivi.cif.entity.CifCustomerAuths;
import com.kivi.cif.properties.CifProperties;
import com.kivi.cif.service.CifCustomerAuthsService;
import com.kivi.dashboard.auth.KtfAuthentication;
import com.kivi.dashboard.base.DashboardController;
import com.kivi.dashboard.enterprise.entity.Enterprise;
import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.ShiroUser;
import com.kivi.dashboard.shiro.form.LoginForm;
import com.kivi.framework.cache.redis.IRedisService;
import com.kivi.framework.constant.KtfError;
import com.kivi.framework.constant.enums.KtfStatus;
import com.kivi.framework.exception.KtfException;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.properties.KtfDashboardProperties;
import com.kivi.framework.service.KtfTokenService;
import com.kivi.framework.vo.UserVo;
import com.kivi.framework.web.constant.WebConst;
import com.kivi.framework.web.jwt.JwtKit;
import com.kivi.framework.web.jwt.JwtUserKit;
import com.kivi.framework.web.util.kit.HttpKit;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description 登录退出Controller
 */
@Api(value = "登录退出", tags = { " 登录退出" })
@RestController
@Slf4j
public class LoginController extends DashboardController {

	@Autowired
	private Producer				producer;

	@Autowired(required = false)
	private IRedisService			redisService;

	@Autowired
	private KtfAuthentication		ktfAuthentication;

	// 30分钟过期
	@Autowired
	private KtfDashboardProperties	ktfDashboardProperties;

	@Autowired
	private KtfTokenService			ktfTokenService;

	@Reference(check = false, version = CifProperties.DUBBO_VERSION)
	private CifCustomerAuthsService	customerAuthsService;

	@GetMapping("/captcha/status")
	public Boolean isKaptcha() {

		return ktfDashboardProperties.getEnableKaptcha();
	}

	@GetMapping("captcha.jpg")
	public void kaptcha(HttpServletResponse response, String uuid) {
		log.info("前台请求的UUID:" + uuid);
		if (StringUtils.isBlank(uuid)) {
			throw new RuntimeException("uuid不能为空");
		}
		// 生成文字验证码
		String code = producer.createText();
		redisService.set(uuid, code, expire());

		response.setHeader("Cache-Control", "no-store,no-cache");
		response.setContentType("image/png");

		BufferedImage image = producer.createImage(code);
		try (ServletOutputStream outputStream = response.getOutputStream()) {
			ImageIO.write(image, "png", outputStream);
		} catch (IOException e) {
			log.error("返回客户端图片异常", e);
			throw new KtfException("返回客户端图片异常", e);
		}
	}

	/**
	 * 登录
	 * 
	 * @throws Exception
	 */
	@ApiOperation(value = "登录", notes = "登录")
	@PostMapping("/sys/login")
	public Object login(@Valid @RequestBody LoginForm form) throws Exception {
		log.info("POST请求登录");

		if (ktfDashboardProperties.getEnableKaptcha()) {
			String validateCode = (String) redisService.get(form.getUuid());
			log.info("session中的图形码字符串:{}", validateCode);

			// 比对
			if (StringUtils.isBlank(validateCode) || StringUtils.isBlank(form.getCaptcha())
					|| !StringUtils.equalsIgnoreCase(validateCode, form.getCaptcha())) {
				return ResultMap.error("验证码错误");
			}
		}

		UserVo userVo = sysUserService().selectByLoginName(form.getUserName());
		if (null == userVo) {
			log.error("账号{}不存在", form.getUserName());
			return ResultMap.error(KtfError.E_UNAUTHORIZED, "账号或密码不正确");
		}

		if (!ktfAuthentication.auth(form, userVo)) {
			log.error("密码不正确");
			return ResultMap.error(KtfError.E_UNAUTHORIZED, "账号或密码不正确");
		}

		// 当企业不存在或者企业被禁用不允许登录
		if (userVo.getUserType() == 1) {
			Enterprise sysEnterprise = enterpriseService().getById(userVo.getEnterpriseId());
			if (null != sysEnterprise && sysEnterprise.getStatus() == 1) {
				return ResultMap.error("企业被禁用，该账户不允许登录");
			} else if (null == sysEnterprise) {
				return ResultMap.error("企业不存在，该账户不允许登录");
			}
		}

		if (ktfDashboardProperties.getEnableKaptcha())
			// 清除验证码
			redisService.del(form.getUuid());

		// 判断用户状态
		if (KtfStatus.LOCKED.code == userVo.getStatus()) {
			// 用户已被禁用
			return ResultMap.error(KtfError.E_LOCKED, "用户已被禁用");
		}

		// 生成token，并保存到数据库
		ResultMap result = createToken(userVo);

		// 保存登录时间和IP
		CompletableFuture.supplyAsync(() -> {
			CifCustomerAuths customerAuths = new CifCustomerAuths();
			customerAuths.setId(userVo.getId());
			customerAuths.setLastIp(HttpKit.getRemoteAddress());
			customerAuths.setLastTime(LocalDateTime.now());
			// customerAuths.updateById();
			return customerAuthsService.updateById(customerAuths);
		});

		return result;
	}

	/**
	 * 退出
	 */
	@ApiOperation(value = "退出", notes = "退出")
	@PostMapping("/sys/logout")
	public ResultMap logout() throws Exception {
		// 生成一个token
		ShiroUser shiroUser = ShiroKit.getUser();
		ktfTokenService.evictJwt(shiroUser.getId().toString());
		/*
		 * JwtUserKit jwtUser =
		 * JwtUserKit.builder().id(shiroUser.getId()).identifier(shiroUser.getLoginName(
		 * )) .name(shiroUser.getName()).build();
		 * 
		 * String token = JwtKit.create(jwtUser, shiroUser.getId().toString(),
		 * DateTime.now().plusSeconds(expire()).toDate()); // 修改token SysUserToken
		 * tokenEntity = new SysUserToken();
		 * tokenEntity.setUserId(ShiroKit.getUser().getId());
		 * tokenEntity.setToken(token); sysUserTokenService().updateById(tokenEntity);
		 */
		return ResultMap.ok();
	}

	public ResultMap createToken(UserVo userVo) throws Exception {

		// 从缓存中获取
		// 生成一个token
		String		token		= ktfTokenService.token(userVo.getId(), userVo.getCifId(), userVo.getUserType(),
				userVo.getLoginMode());

		// 过期时间
		DateTime	now			= DateTime.now();
		Date		expireTime	= now.plusSeconds(expire()).toDate();
		JwtUserKit	jwtUser		= JwtUserKit.builder().id(userVo.getId()).identifier(userVo.getLoginName())
				.name(userVo.getName()).userType(userVo.getUserType()).build();

		String		jwtToken	= JwtKit.create(jwtUser, token, expireTime);

		// 判断是否生成过token
		/*
		 * SysUserToken tokenEntity = userTokenService.getById(userVo.getId()); if
		 * (tokenEntity == null) { tokenEntity = new SysUserToken();
		 * tokenEntity.setUserId(userVo.getId()); tokenEntity.setToken(token);
		 * tokenEntity.setExpireTime(DateTimeKit.toLocalDateTime(expireTime)); //
		 * 保存token userTokenService.save(tokenEntity); } else {
		 * tokenEntity.setToken(token);
		 * tokenEntity.setExpireTime(DateTimeKit.toLocalDateTime(expireTime)); //
		 * 更新token userTokenService.updateById(tokenEntity); }
		 */

		// 缓存Jwt Toen
		ktfTokenService.cacheJwt(jwtUser.getId().toString(), token, jwtToken, expire());

		ResultMap r = KtfStatus.ENABLED.code == userVo.getStatus() ? ResultMap.ok()
				: ResultMap.error(KtfError.ACCEPTED, "首次访问");

		r.put("token", jwtToken).put(WebConst.AUTH_TOKEN_EXPIRE, expire());
		return r;
	}

	private int expire() {
		return ktfDashboardProperties.getSession().getExpireTime();
	}
}
