package com.kivi.dashboard.enterprise.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
import org.springframework.web.multipart.MultipartFile;

//import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.enterprise.dto.EnterpriseDTO;
import com.kivi.dashboard.enterprise.entity.Enterprise;
import com.kivi.dashboard.shiro.ShiroKit;
import com.kivi.dashboard.shiro.ShiroUser;
import com.kivi.dashboard.sys.controller.UpLoadController;
import com.kivi.dashboard.sys.dto.SysFileDTO;
import com.kivi.dashboard.sys.entity.SysFile;
import com.kivi.framework.model.ResultMap;
import com.kivi.framework.model.SelectNode;
import com.kivi.framework.util.kit.StrKit;
import com.kivi.framework.vo.page.PageInfoVO;
import com.vip.vjtools.vjkit.collection.ListUtil;
import com.vip.vjtools.vjkit.number.NumberUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 企业信息 前端控制器
 * </p>
 *
 * @author Auto-generator
 * @since 2019-09-18
 */
//@ConditionalOnMissingBean(name = { "ktfDubboProperties" })
@Api(value = "企业管理接接口", tags = { "企业管理接口" })
@RestController
@RequestMapping("/enterprise")
@Slf4j
public class EnterpriseController extends UpLoadController {

	private Map<String, List<Map<String, String>>> uploadFileUrls = new ConcurrentHashMap<String, List<Map<String, String>>>();

	@ApiOperation(value = "企业信息信息", notes = "企业信息信息")
	@GetMapping("/info/{id}")
	@RequiresPermissions("enterprise/enterprise/info")
	public ResultMap info(@PathVariable("id") String id) {
		EnterpriseDTO dto = enterpriseService().getDTOById(NumberUtil.toLongObject(id, -1L));
		return ResultMap.ok().put("enterprise", dto);
	}

	/**
	 * 新增
	 */
	@ApiOperation(value = "新增企业信息", notes = "新增企业信息")
	@RequiresPermissions("enterprise/enterprise/save")
	@PostMapping("/save")
	public ResultMap save(@Valid @RequestBody EnterpriseDTO enterpriseDTO) {
		try {
			// enterpriseDTO.setCreateUser(ShiroKit.getUser().getLoginName());
			Enterprise enterprise = enterpriseService().save(enterpriseDTO);
			saveFile(enterprise.getId());
			return ResultMap.ok("新增成功！");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResultMap.error("添加失败，请联系管理员");
		}
	}

	/**
	 * 修改
	 */
	@ApiOperation(value = "修改企业信息", notes = "修改企业信息")
	@RequiresPermissions("enterprise/enterprise/update")
	@PostMapping("/update")
	public ResultMap updateById(@RequestBody EnterpriseDTO enterpriseDTO) {
		try {
			// enterpriseDTO.setUpdateUser(ShiroKit.getUser().getLoginName());
			Boolean b = enterpriseService().updateById(enterpriseDTO);
			saveFile(enterpriseDTO.getId());
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
	@ApiOperation(value = "批量删除企业信息", notes = "删除企业信息")
	@PostMapping("/delete/{id}")
	@RequiresPermissions("enterprise/enterprise/delete")
	public ResultMap delete(@PathVariable("id") Long id) {
		try {
			Boolean b = enterpriseService().removeById(id);
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
	 * 批量删除
	 */
	@ApiOperation(value = "批量删除企业信息", notes = "删除企业信息")
	@PostMapping("/delete")
	@RequiresPermissions("enterprise/enterprise/delete")
	public ResultMap deleteBatchIds(@RequestBody Long[] ids) {
		try {
			Boolean b = enterpriseService().removeByIds(Arrays.asList(ids));
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
	 * 分页查询
	 */
	@ApiOperation(value = "企业列表", notes = "企业列表")
	@RequiresPermissions("enterprise/enterprise/list")
	@GetMapping("/list")
	public ResultMap page(@RequestParam(required = false) Map<String, Object> params) {
		ShiroUser shiroUser = ShiroKit.getUser();
		// 不是管理员
		if (shiroUser.getUserType() != 0) {
			params.put(EnterpriseDTO.ID, ShiroKit.getUser().getId());
		}

		PageInfoVO<Map<String, Object>> page = enterpriseService().selectByPage(params);

		return ResultMap.ok().put("page", page);
	}

	/**
	 * 企业名称列表
	 *
	 * @return
	 */
	@ApiOperation(value = "企业名称列表", notes = "企业名称列表")
	@GetMapping("/names")
	@RequiresPermissions("enterprise/enterprise/names")
	public ResultMap names() {

		List<Map<String, Object>> list = enterpriseService().selectNames();
		return ResultMap.ok().put("list", list);
	}

	/**
	 * 企业选择
	 *
	 * @param areaCode
	 * @param industryCode
	 * @return
	 */
	@ApiOperation(value = "企业选择", notes = "企业选择")
	@ApiImplicitParams({
			@ApiImplicitParam(
					name = "areaId",
					value = "区域ID",
					required = false,
					dataType = "Integer",
					paramType = "query"),
			@ApiImplicitParam(
					name = "industryId",
					value = "行业ID",
					required = false,
					dataType = "Integer",
					paramType = "query") })
	@GetMapping("/getEnterpriseTree")
	public ResultMap getEnterpriseTree(
			@RequestParam(required = false, value = "areaId") Long areaId,
			@RequestParam(required = false, value = "industryId") Long industryId) {
		try {
			Map<String, Object>	params		= new HashMap<>();
			ShiroUser			shiroUser	= ShiroKit.getUser();
			// 不是管理员
			if (shiroUser.getUserType() != 0) {
				params.put("userId", ShiroKit.getUser().getId());
			}
			params.put("industryId", industryId);
			List<Map<String, Object>> list = enterpriseService().select(params);
			if (list == null)
				list = ListUtil.emptyList();

			List<SelectNode> nodeList = list.stream().map(map -> {
				SelectNode selectNode = new SelectNode();
				selectNode.setValue(map.get("id").toString());
				selectNode.setLabel(map.get("enterpriseName").toString());
				return selectNode;
			}).collect(Collectors.toList());
			return ResultMap.ok().put("list", nodeList);
		} catch (Exception e) {
			log.error("企业选择异常", e);
			return ResultMap.error("运行异常，请联系管理员");
		}
	}

	/**
	 * 上传附件
	 */
	@PostMapping("/uploadFile")
	public Object uploadFile(@RequestParam("file") MultipartFile[] files) {
		try {
			List<Map<String, String>>	uploadFileUrl	= uploads(files, "enterprise");
			// String fileName = "";
			String						filePath		= "";
			if (!uploadFileUrl.isEmpty() && uploadFileUrl.size() > 0) {
				for (Map<String, String> map : uploadFileUrl) {
					// fileName = map.get("fileName");
					filePath = map.get("filePath");
				}
				setUploadFile(uploadFileUrl);
				return ResultMap.ok().put("filePath", filePath);
			} else {
				return ResultMap.ok().put("filePath", "");
			}
		} catch (Exception e) {
			log.error("上传失败", e);
			return ResultMap.error("上传失败，请联系管理员");
		}
	}

	/**
	 * 列示附件
	 */
	@GetMapping("/selectFile/{id}")
	public Object listFile(@PathVariable("id") String id) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put(SysFile.DB_TABLE_ID, "t_enterprise");
		params.put(SysFile.DB_RECORD_ID, id);
		List<SysFileDTO> fileList = sysFileService().list(params, StrKit.emptyArray());
		return ResultMap.ok().put("fileList", fileList);
	}

	/**
	 * 删除附件
	 */
	@GetMapping("/deleteFileById/{id}")
	public Object deleteFileById(@PathVariable("id") String id) {
		try {
			SysFile sysFile = sysFileService().getById(id);
			if (sysFile != null) {
				sysFileService().removeById(sysFile.getId());
				deleteFileFromLocal(sysFile.getAttachmentPath());
			}
			return ResultMap.ok("删除成功");
		} catch (Exception e) {
			log.error("删除附件异常", e);
			return ResultMap.error("删除失败，请联系管理员");
		}
	}

	/**
	 * 删除附件(刚上传到后端的附件)
	 */
	@GetMapping("/deleteFileByName")
	public Object deleteFileByName(@RequestParam String fileName) {
		try {
			List<Map<String, String>> list = getUploadFile();
			if (StringUtils.isNotBlank(fileName) && ListUtil.isNotEmpty(list)) {
				for (Map<String, String> uploadFileUrl : list) {
					boolean canDel = false;
					if (uploadFileUrl.get("fileName").equalsIgnoreCase(fileName)) {
						deleteFileFromLocal(uploadFileUrl.get("filePath"));
						canDel = true;
						break;
					}
					if (canDel) {
						list.remove(uploadFileUrl);
						break;
					}
				}
			}
			return ResultMap.ok("删除成功");
		} catch (Exception e) {
			log.error("删除附件异常", e);
			return ResultMap.error("删除失败,请联系管理员");
		}
	}

	public Object saveFile(Long id) {
		try {
			if (getUploadFile() != null) {
				// 获取企业ID前缀，生成UUID
				for (Map<String, String> uploadFileUrl : getUploadFile()) {
					String	fileName	= uploadFileUrl.get("fileName");
					String	filePah		= uploadFileUrl.get("filePath");
					SysFile	sysFile		= new SysFile();
					sysFile.setRecordId(id);
					sysFile.setTableId("ktf_enterprise");
					sysFile.setAttachmentGroup("企业");
					sysFile.setAttachmentName(fileName);
					sysFile.setAttachmentPath(filePah);
					// 附件类型(0-word,1-excel,2-pdf,3-jpg,png,4-其他等)
					String fileType = fileName.substring(fileName.indexOf("."));
					if ("doc".equals(fileType.toLowerCase())) {
						sysFile.setAttachmentType(0);
					} else if ("xlsx".equals(fileType.toLowerCase())) {
						sysFile.setAttachmentType(1);
					} else if ("pdf".equals(fileType.toLowerCase())) {
						sysFile.setAttachmentType(2);
					} else if ("jpg".equals(fileType.toLowerCase()) || "png".equals(fileType.toLowerCase())) {
						sysFile.setAttachmentType(3);
					} else {
						sysFile.setAttachmentType(4);
					}
					sysFile.setSaveType(0);
					sysFile.setIsSync(0);
					sysFile.setCreateUser(ShiroKit.getUser().getLoginName());
					sysFileService().save(sysFile);
				}
				resetUploadFile();
			}
			return ResultMap.ok("保存成功");
		} catch (Exception e) {
			log.error("保存失败", e);
			return ResultMap.error("保存失败，请联系管理员");
		}
	}

	private void setUploadFile(List<Map<String, String>> uploadFileUrl) {
		ShiroUser	user	= ShiroKit.getUser();
		Object		o		= uploadFileUrls.get(user.getId().toString());
		if (o == null) {
			uploadFileUrls.put(user.getId().toString(), new ArrayList<>());
		}
		uploadFileUrls.get(user.getId().toString()).addAll(uploadFileUrl);
	}

	private List<Map<String, String>> getUploadFile() {
		ShiroUser user = ShiroKit.getUser();
		return uploadFileUrls.get(user.getId().toString());
	}

	private void resetUploadFile() {
		ShiroUser user = ShiroKit.getUser();
		uploadFileUrls.remove(user.getId().toString());
	}

}
