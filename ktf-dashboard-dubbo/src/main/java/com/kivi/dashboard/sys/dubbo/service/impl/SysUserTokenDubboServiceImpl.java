package com.kivi.dashboard.sys.dubbo.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kivi.dashboard.sys.dto.SysUserTokenDTO;
import com.kivi.dashboard.sys.entity.SysUserToken;
import com.kivi.dashboard.sys.mapper.SysUserTokenMapper;
import com.kivi.dashboard.sys.service.ISysUserTokenService;
import com.kivi.framework.annotation.KtfTrace;
import com.kivi.framework.properties.KtfDashboardProperties;
import com.kivi.framework.vo.page.PageInfoVO;

/**
 * <p>
 * 系统用户Token 服务实现类
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */

@Service(version = KtfDashboardProperties.DUBBO_VERSION)
public class SysUserTokenDubboServiceImpl extends ServiceImpl<SysUserTokenMapper, SysUserToken>
		implements ISysUserTokenService {

	@Autowired
	private ISysUserTokenService iSysUserTokenService;

	/**
	 * 根据ID查询系统用户Token
	 */
	@KtfTrace("根据ID查询系统用户Token")
	@Override
	public SysUserTokenDTO getDTOById(Long id) {
		return iSysUserTokenService.getDTOById(id);
	}

	/**
	 * 新增系统用户Token
	 */
	@KtfTrace("新增系统用户Token")
	@Override
	public Boolean save(SysUserTokenDTO sysUserTokenDTO) {
		return iSysUserTokenService.save(sysUserTokenDTO);
	}

	/**
	 * 修改
	 */
	@KtfTrace("修改系统用户Token")
	@Override
	public Boolean updateById(SysUserTokenDTO sysUserTokenDTO) {
		return iSysUserTokenService.updateById(sysUserTokenDTO);
	}

	/**
	 * 查询列表
	 */
	@KtfTrace("查询列表系统用户Token")
	@Override
	public List<SysUserTokenDTO> list(SysUserTokenDTO sysUserTokenDTO) {
		return iSysUserTokenService.list(sysUserTokenDTO);
	}

	/**
	 * 指定列查询列表
	 */
	@KtfTrace("指定列查询列表系统用户Token")
	@Override
	public List<SysUserTokenDTO> list(Map<String, Object> params, String... columns) {
		return iSysUserTokenService.list(params, columns);
	}

	/**
	 * 模糊查询
	 */
	@KtfTrace("模糊查询系统用户Token")
	@Override
	public List<SysUserTokenDTO> listLike(SysUserTokenDTO sysUserTokenDTO) {
		return iSysUserTokenService.listLike(sysUserTokenDTO);
	}

	/**
	 * 指定列模糊查询
	 */
	@Override
	public List<SysUserTokenDTO> listLike(Map<String, Object> params, String... columns) {
		return iSysUserTokenService.listLike(params, columns);
	}

	/**
	 * 分页查询
	 */
	@Override
	@KtfTrace("分页查询系统用户Token")
	public PageInfoVO<SysUserTokenDTO> page(Map<String, Object> params) {
		return iSysUserTokenService.page(params);

	}

	@KtfTrace("根据token的值查找用户Token")
	@Override
	public SysUserToken selectByToken(String token) {
		return iSysUserTokenService.selectByToken(token);
	}

}
