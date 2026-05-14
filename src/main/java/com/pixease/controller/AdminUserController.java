package com.pixease.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pixease.common.enums.ResultCode;
import com.pixease.common.enums.UserRole;
import com.pixease.common.exception.BusinessException;
import com.pixease.common.result.Result;
import com.pixease.dto.UserPageDTO;
import com.pixease.service.PeUserService;
import com.pixease.vo.UserVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理用户控制器，提供管理后台的用户查询接口
 * 仅管理员角色可访问
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    private final PeUserService peUserService;

    public AdminUserController(PeUserService peUserService) {
        this.peUserService = peUserService;
    }

    /**
     * 分页查询用户列表（管理后台使用）
     * 返回结果不包含密码字段
     * 仅管理员角色可操作
     */
    @GetMapping("/page")
    public Result<IPage<UserVO>> page(@RequestAttribute("role") Integer role,
                                      UserPageDTO dto) {
        if (!UserRole.isAdmin(role)) {
            throw new BusinessException(ResultCode.ROLE_FORBIDDEN);
        }
        IPage<UserVO> page = peUserService.page(dto);
        return Result.success(page);
    }
}
