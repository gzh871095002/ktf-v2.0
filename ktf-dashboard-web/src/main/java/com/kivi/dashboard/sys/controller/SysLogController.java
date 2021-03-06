package com.kivi.dashboard.sys.controller;

import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kivi.dashboard.base.DashboardController;
//import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.sys.dto.SysLogDTO;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.vo.page.PageInfoVO;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * <p>
 * 系统日志 前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */

@Api(tags = { "系统日志" })
@RestController
@RequestMapping("/sys/log")
public class SysLogController extends DashboardController {

	/**
	 * 分页查询
	 */
	@ApiOperation(value = "分页查询系统日志", notes = "分页查询系统日志")
	@RequiresPermissions("sys/log/page")
	@GetMapping("/page")
	public ResultMap page(@RequestParam(required = false) Map<String, Object> params) {
		PageInfoVO<SysLogDTO> page = sysLogService().page(params);

		return ResultMap.ok().put("page", page);
	}

}
