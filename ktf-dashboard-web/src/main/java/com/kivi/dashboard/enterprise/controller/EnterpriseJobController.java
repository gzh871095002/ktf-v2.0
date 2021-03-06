package com.kivi.dashboard.enterprise.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.kivi.dashboard.base.DashboardController;
import com.kivi.dashboard.enterprise.dto.EnterpriseDepartmentDTO;
//import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.enterprise.dto.EnterpriseJobDTO;
import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.ShiroUser;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.model.SelectNode;
import com.kivi.framework.vo.page.PageInfoVO;
import com.vip.vjtools.vjkit.number.NumberUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 企业职务配置 前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */
//@ConditionalOnMissingBean(name = { "ktfDubboProperties" })
@Api(tags = { "企业职务配置" })
@RestController
@RequestMapping("/enterprise/enterpriseJob")
@Slf4j
public class EnterpriseJobController extends DashboardController {

	@ApiOperation(value = "企业职务配置信息", notes = "企业职务配置信息")
	@GetMapping("/info/{id}")
	@RequiresPermissions("enterprise/enterpriseJob/info")
	public ResultMap info(@PathVariable("id") String id) {
		EnterpriseJobDTO		dto			= enterpriseJobService().getDTOById(NumberUtil.toLongObject(id, -1L));
		EnterpriseDepartmentDTO	department	= enterpriseDepartmentService().getDTOById(dto.getDepartmentId());
		dto.setEnterpriseDepartment(department);
		return ResultMap.ok().put("enterpriseJob", dto);
	}

	/**
	 * 新增
	 */
	@ApiOperation(value = "新增企业职务配置", notes = "新增企业职务配置")
	@RequiresPermissions("enterprise/enterpriseJob/save")
	@PostMapping("/save")
	public ResultMap save(@Valid @RequestBody EnterpriseJobDTO enterpriseJobDTO) {
		try {
			// enterpriseJobDTO.setCreateUser(ShiroKit.getUser().getLoginName());
			Boolean b = enterpriseJobService().save(enterpriseJobDTO);
			if (b) {
				return ResultMap.ok("新增成功！");
			} else {
				return ResultMap.ok("新增失败！");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("添加失败，请联系管理员");
		}
	}

	/**
	 * 修改
	 */
	@ApiOperation(value = "修改企业职务配置", notes = "修改企业职务配置")
	@RequiresPermissions("enterprise/enterpriseJob/update")
	@PostMapping("/update")
	public ResultMap updateById(@RequestBody EnterpriseJobDTO enterpriseJobDTO) {
		try {
			// enterpriseJobDTO.setUpdateUser(ShiroKit.getUser().getLoginName());
			Boolean b = enterpriseJobService().updateById(enterpriseJobDTO);
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
	 * 删除
	 */
	@ApiOperation(value = "删除企业职务配置", notes = "删除企业职务配置")
	@PostMapping("/delete/{id}")
	@RequiresPermissions("enterprise/enterpriseJob/delete")
	public ResultMap delete(@PathVariable("id") Long id) {
		try {
			Boolean b = enterpriseJobService().removeById(id);
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
	@ApiOperation(value = "批量删除企业职务配置", notes = "批量删除企业职务配置")
	@PostMapping("/delete")
	@RequiresPermissions("enterprise/enterpriseJob/delete")
	public ResultMap deleteBatchIds(@RequestBody Long[] ids) {
		try {
			Boolean b = enterpriseJobService().removeByIds(Arrays.asList(ids));
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
	 * 查询列表
	 */
	@ApiOperation(value = "查询列表", notes = "查询列表")
	@RequiresPermissions("enterprise/enterpriseJob/list")
	@GetMapping("/list")
	public ResultMap list(@RequestParam(required = false) Map<String, Object> params) {
		ShiroUser shiroUser = ShiroKit.getUser();
		// 不是管理员
		if (shiroUser.getUserType() != 0) {
			params.put("userId", ShiroKit.getUser().getId());
		}
		PageInfoVO<Map<String, Object>> page = enterpriseJobService().selectByPage(params);
		return ResultMap.ok().put("page", page);
	}

	/**
	 * 企业部门职位选择
	 *
	 * @param deptId
	 * @return
	 */
	@ApiOperation(value = "企业部门职位选择", notes = "企业部门职位选择")
	@ApiImplicitParam(name = "deptId", value = "部门ID", required = true, dataType = "String", paramType = "query")
	@GetMapping("/selectJobTree")
	public ResultMap selectJobTree(@RequestParam String deptId) {
		try {
			List<SelectNode>	nodeList	= Lists.newArrayList();
			Map<String, Object>	params		= Maps.newHashMap();
			if (StringUtils.isNotBlank(deptId)) {
				params.put("deptId", deptId);
			}
			List<EnterpriseJobDTO> jobList = enterpriseJobService().select(params);
			if (!jobList.isEmpty()) {
				jobList.forEach(job -> {
					SelectNode selectNode = new SelectNode();
					selectNode.setValue(job.getId().toString());
					selectNode.setLabel(job.getJobName());
					nodeList.add(selectNode);
				});
			}
			return ResultMap.ok().put("list", nodeList);
		} catch (Exception e) {
			log.error("企业部门职位选择失败", e);
			return ResultMap.error("运行异常，请联系管理员");
		}
	}

}
