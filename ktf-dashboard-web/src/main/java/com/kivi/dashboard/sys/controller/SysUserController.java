package com.kivi.dashboard.sys.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kivi.cif.entity.CifCustomerAuths;
import com.kivi.dashboard.base.DashboardController;
import com.kivi.dashboard.enums.KmsUserType;
import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.ShiroUser;
import com.kivi.dashboard.shiro.form.PasswordForm;
//import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.sys.dto.SysUserDTO;
import com.kivi.dashboard.sys.entity.SysUser;
import com.kivi.framework.constant.KtfConstant;
import com.kivi.framework.constant.enums.KtfStatus;
import com.kivi.framework.converter.BeanConverter;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.model.SelectNode;
import com.kivi.framework.service.KtfTokenService;
import com.kivi.framework.util.kit.StrKit;
import com.kivi.framework.vo.UserVo;
import com.kivi.framework.vo.page.PageInfoVO;
import com.kivi.framework.web.properties.KtfWebProperties;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 用户管理前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */

@Api(value = "用户管理", tags = { "用户管理" })
@RestController
@RequestMapping("/sys/user")
@Slf4j
public class SysUserController extends DashboardController {

	@Autowired
	private KtfWebProperties	ktfWebProperties;

	@Autowired
	private KtfTokenService		ktfTokenService;

	/**
	 * 所有用户列表
	 */
	@GetMapping("/list")
	@RequiresPermissions("sys/user/list")
	public ResultMap list(@RequestParam Map<String, Object> params) {
		// 只有超级管理员，才能查看所有管理员列表
		ShiroUser shiroUser = ShiroKit.getUser();
		if (shiroUser.getId() != KtfConstant.SUPER_ADMIN) {
			params.put("userId", ShiroKit.getUser().getId());
		}

		KmsUserType userType = KmsUserType.valueOf(shiroUser.getUserType());
		params.put("userType", userType.children());

		PageInfoVO<Map<String, Object>> page = sysUserService().selectDataGrid(params);

		return ResultMap.ok().put("page", page);
	}

	/**
	 * 获取登录的用户信息
	 */
	@GetMapping("/info")
	public ResultMap info() {
		return ResultMap.ok().put("user", ShiroKit.getUser());
	}

	@ApiOperation(value = "用户信息", notes = "用户信息")
	@GetMapping("/info/{id}")
	@RequiresPermissions("sys/user/info")
	public ResultMap info(@PathVariable("id") Long id) {
		UserVo		user		= sysUserService().selectByUserId(id);
		List<Long>	roleIdList	= sysUserRoleService().selectRoleIdListByUserId(user.getId());
		user.setRoleIdList(roleIdList);
		List<Long> enterpriseIdList = sysUserEnterpriseService().selectEnterpriseIdByUserId(user.getId());
		user.setEnterpriseIdList(enterpriseIdList);
		return ResultMap.ok().put("user", user);
	}

	/**
	 * 修改登录用户密码
	 */
	@PostMapping("/password")
	@RequiresPermissions("sys/user/password")
	public ResultMap password(@Valid @RequestBody PasswordForm form) {

		CifCustomerAuths	userAuth	= customerAuthsService().getById(ShiroKit.getUser().getId());
		String				password	= ShiroKit.md5(form.getPassword(),
				userAuth.getIdentifier() + userAuth.getCredentialSalt());
		if (!userAuth.getCredential().equals(password)) {
			return ResultMap.error("原密码不正确");
		}

		String				newSalt		= ShiroKit.getRandomSalt(16);
		String				newPassword	= ShiroKit.md5(form.getNewPassword(), userAuth.getIdentifier() + newSalt);

		CifCustomerAuths	updateAuth	= new CifCustomerAuths();
		updateAuth.setId(userAuth.getId());
		updateAuth.setCredentialSalt(newSalt);
		updateAuth.setCredential(newPassword);
		boolean ret = customerAuthsService().updateById(updateAuth);
		if (!ret) {
			return ResultMap.error("修改密码失败");
		}

		// 修改用户状态：初始-->正常
		if (KtfStatus.INIT.code == ShiroKit.getUser().getStatus()) {
			SysUser entity = new SysUser();
			entity.setId(ShiroKit.getUser().getId());
			entity.setStatus(KtfStatus.ENABLED.code);
			sysUserService().updateById(entity);
		}

		// 注销token
		ktfTokenService.evictJwt(userAuth.getId().toString());

		return ResultMap.ok("密码修改成功");
	}

	@ApiOperation(value = "密码重置", notes = "密码重置")
	@GetMapping("/passwordReset/{id}")
	public ResultMap passwordReset(@PathVariable("id") Long id) {
		SysUser entity = new SysUser();
		entity.setId(id);
		entity.setStatus(KtfStatus.INIT.code);
		sysUserService().updateById(entity);

		SysUser				user	= sysUserService().getById(id);

		String				salt	= ShiroKit.getRandomSalt(16);
		String				pwd		= ShiroKit.md5(DigestUtils.md5Hex(ktfWebProperties.getDefaultPassword()),
				user.getLoginName() + salt);
		CifCustomerAuths	cifAuth	= new CifCustomerAuths();
		cifAuth.setId(id);
		cifAuth.setCredential(pwd);
		cifAuth.setCredentialSalt(salt);
		customerAuthsService().updateById(cifAuth);

		return ResultMap.ok("重置成功");
	}

	/**
	 * 新增
	 */
	@ApiOperation(value = "新增用户", notes = "新增用户")
	@RequiresPermissions("sys/user/save")
	@PostMapping("/save")
	public ResultMap save(@Valid @RequestBody SysUserDTO sysUserDTO) {
		try {
			UserVo u = sysUserService().selectByLoginName(sysUserDTO.getLoginName());
			if (u != null) {
				return ResultMap.error("登录名已存在");
			}

			UserVo	user	= BeanConverter.convert(UserVo.class, sysUserDTO);
			String	salt	= ShiroKit.getRandomSalt(16);
			user.setSalt(salt);
			if (StrKit.isBlank(user.getPassword())) {
				user.setPassword(DigestUtils.md5Hex(ktfWebProperties.getDefaultPassword()));
			}
			String pwd = ShiroKit.md5(user.getPassword(), user.getLoginName() + salt);
			log.trace("默认密码md5：{}\n分散因子：{}\ncredential：{}", user.getPassword(), user.getLoginName() + salt, pwd);
			user.setPassword(pwd);
			user.setCreateUserId(ShiroKit.getUser().getId());
			sysUserService().saveByVo(user);
			return ResultMap.ok("添加成功");
		} catch (Exception e) {
			log.error("新增用户异常", e);
			return ResultMap.error("运行异常，请联系管理员");
		}
	}

	/**
	 * 修改
	 */
	@ApiOperation(value = "修改用户", notes = "修改用户")
	@RequiresPermissions("sys/user/update")
	@PostMapping("/update")
	public ResultMap updateById(@Valid @RequestBody SysUserDTO sysUserDTO) {
		try {
			UserVo user = BeanConverter.convert(UserVo.class, sysUserDTO);

			if (StringUtils.isNotBlank(user.getPassword())) {
				// 禁止通过本方法修改密码
				user.setPassword(null);
			}

			user.setCreateUserId(ShiroKit.getUser().getId());
			boolean b = sysUserService().updateByVo(user);
			if (b) {
				return ResultMap.ok("编辑成功！");
			} else {
				return ResultMap.ok("编辑失败！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("编辑失败，请联系管理员");
		}
	}

	/**
	 * 批量删除
	 */
	@ApiOperation(value = "批量删除用户", notes = "删除用户")
	@PostMapping("/delete")
	@RequiresPermissions("sys/user/delete")
	public ResultMap deleteBatchIds(@RequestBody Long[] ids) {
		try {
			if (ArrayUtils.contains(ids, KtfConstant.SUPER_ADMIN)) {
				return ResultMap.error("系统管理员不能删除");
			}
			if (ArrayUtils.contains(ids, ShiroKit.getUser().getId())) {
				return ResultMap.error("当前用户不能删除");
			}

			Boolean b = sysUserService().deleteBatch(ids);
			if (b) {
				return ResultMap.ok("删除成功！");
			} else {
				return ResultMap.ok("删除失败！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("批量删除失败，请联系管理员");
		}
	}

	/**
	 * 用户选择树
	 * 
	 * @return
	 */
	@ApiOperation(value = "用户选择树", notes = "用户选择树")
	@GetMapping("/getUserTree")
	public ResultMap getUserTree() {
		try {
			List<Map<String, Object>>	list		= sysUserService().selectUserTree();
			List<SelectNode>			nodeList	= list.stream().map(baseUser -> {
														SelectNode node = new SelectNode();
														node.setLabel(baseUser.get("name").toString());
														node.setValue(baseUser.get("id").toString());
														return node;
													}).collect(Collectors.toList());

			return ResultMap.ok().put("list", nodeList);
		} catch (Exception e) {
			e.printStackTrace();
			return ResultMap.error("运行异常，请联系管理员");
		}
	}

}
