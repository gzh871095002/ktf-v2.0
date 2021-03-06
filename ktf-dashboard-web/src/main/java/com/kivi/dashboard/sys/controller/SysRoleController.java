package com.kivi.dashboard.sys.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kivi.dashboard.base.DashboardController;
import com.kivi.dashboard.shiro.ShiroKit;
//import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.sys.dto.SysRoleDTO;
import com.kivi.dashboard.sys.dto.SysRoleResourceDTO;
import com.kivi.framework.constant.KtfConstant;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.model.TreeNode;
import com.kivi.framework.util.kit.StrKit;
import com.kivi.framework.vo.page.PageInfoVO;
import com.vip.vjtools.vjkit.number.NumberUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 角色 前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */

@Api(value = "角色管理接口", tags = { " 角色管理接口" })
@RestController
@RequestMapping("/sys/role")
@Slf4j
public class SysRoleController extends DashboardController {

	@ApiOperation(value = "角色信息", notes = "角色信息")
	@GetMapping("/info/{id}")
	@RequiresPermissions("sys/role/info")
	public ResultMap info(@PathVariable("id") String id) {
		SysRoleDTO	role			= sysRoleService().getDTOById(NumberUtil.toLongObject(id, -1L));

		// 查询角色对应的菜单
		List<Long>	resourceIdList	= sysRoleResourceService().selectResourceIdListByRoleId(role.getId());
		role.setResourceIdList(resourceIdList);
		List<SysRoleResourceDTO>	roleResourceList	= sysRoleResourceService()
				.selectResourceNodeListByRoleId(role.getId());
		List<TreeNode>				treeNodeList		= roleResourceList.stream().map(roleResource -> {
															TreeNode treeNode = new TreeNode();
															treeNode.setId(roleResource.getResourceId().toString());
															treeNode.setLabel(roleResource.getResource().getName());
															return treeNode;
														}).collect(Collectors.toList());

		role.setResourceNodeList(treeNodeList);
		return ResultMap.ok().put("role", role);
	}

	/**
	 * 新增
	 */
	@ApiOperation(value = "新增角色", notes = "新增角色")
	@RequiresPermissions("sys/role/save")
	@PostMapping("/save")
	public ResultMap save(@Valid @RequestBody SysRoleDTO sysRoleDTO) {
		try {
			// sysRoleDTO.setCreateUser(ShiroKit.getUser().getLoginName());
			sysRoleDTO.setCreateUserId(ShiroKit.getUser().getId());
			sysRoleService().saveByVo(sysRoleDTO);
			return ResultMap.ok("新增成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("添加失败，请联系管理员");
		}
	}

	/**
	 * 修改
	 */
	@ApiOperation(value = "修改角色", notes = "修改角色")
	@RequiresPermissions("sys/role/update")
	@PostMapping("/update")
	public ResultMap updateById(@RequestBody SysRoleDTO sysRoleDTO) {
		try {
			sysRoleDTO.setCreateUserId(ShiroKit.getUser().getId());
			sysRoleService().updateByVo(sysRoleDTO);
			return ResultMap.ok("编辑成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("编辑失败，请联系管理员");
		}
	}

	/**
	 * 删除
	 */
	@ApiOperation(value = "删除角色", notes = "删除角色")
	@PostMapping("/delete/{id}")
	@RequiresPermissions("sys/role/delete")
	public ResultMap delete(@PathVariable("id") Long id) {
		try {
			Boolean b = sysRoleService().removeById(id);
			if (b) {
				return ResultMap.ok("删除成功！");
			} else {
				return ResultMap.ok("删除失败！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("删除失败，请联系管理员");
		}
	}

	/**
	 * 批量删除
	 */
	@ApiOperation(value = "批量删除角色", notes = "批量删除角色")
	@PostMapping("/delete")
	@RequiresPermissions("sys/role/delete")
	public ResultMap deleteBatchIds(@RequestBody Long[] ids) {
		try {
			sysRoleService().deleteBatch(ids);
			return ResultMap.ok("删除成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("批量删除失败，请联系管理员");
		}
	}

	/**
	 * 查询列表
	 */
	@ApiOperation(value = "查询角色列表", notes = "查询角色列表")
	@RequiresPermissions("sys/role/list")
	@GetMapping("/list")
	public ResultMap list(@RequestParam Map<String, Object> params) {
		// 如果不是超级管理员，则只查询自己创建的角色列表
		if (ShiroKit.getUser().getId() != KtfConstant.SUPER_ADMIN) {
			params.put(SysRoleDTO.CREATE_USER_ID, ShiroKit.getUser().getId());
		}
		PageInfoVO<SysRoleDTO> page = sysRoleService().page(params);
		return ResultMap.ok().put("page", page);
	}

	/**
	 * 角色列表
	 */
	@GetMapping("/select")
	@RequiresPermissions("sys/role/select")
	public ResultMap select() {
		Map<String, Object> map = new HashMap<>();
		// 如果不是超级管理员，则只查询自己所拥有的角色列表
		if (ShiroKit.getUser().getId() != KtfConstant.SUPER_ADMIN) {
			map.put("createUserId", ShiroKit.getUser().getId());
		}
		List<SysRoleDTO> list = sysRoleService().listLike(map, StrKit.emptyArray());

		return ResultMap.ok().put("list", list);
	}

}
