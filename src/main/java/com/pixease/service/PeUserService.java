package com.pixease.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.pixease.dto.AdminLoginDTO;
import com.pixease.dto.ResetPasswordDTO;
import com.pixease.dto.UserLoginDTO;
import com.pixease.dto.UserPageDTO;
import com.pixease.dto.UserRegisterDTO;
import com.pixease.dto.UserUpdateDTO;
import com.pixease.entity.PeUser;
import com.pixease.vo.UserVO;

/**
 * 用户服务接口
 */
public interface PeUserService {

    /**
     * 用户注册（不含IP限制校验）
     *
     * @param dto 注册参数
     * @return 注册成功的用户信息（密码已脱敏）
     */
    PeUser register(UserRegisterDTO dto);

    /**
     * 用户注册（含IP限制校验）
     * 同一IP每天最多注册5次
     *
     * @param dto 注册参数
     * @param ip  客户端IP地址
     * @return 注册成功的用户信息（密码已脱敏）
     */
    PeUser register(UserRegisterDTO dto, String ip);

    /**
     * 普通用户登录
     * 连续5次密码错误将锁定账号30分钟
     *
     * @param dto 登录参数
     * @return JWT Token字符串
     */
    String login(UserLoginDTO dto);

    /**
     * 管理员登录
     * 校验账号密码后额外校验管理员角色
     * 连续5次密码错误将锁定账号30分钟
     *
     * @param dto 管理员登录参数
     * @return JWT Token字符串
     */
    String adminLogin(AdminLoginDTO dto);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param dto    更新参数
     * @return 更新后的用户信息（密码已脱敏）
     */
    PeUser update(Long userId, UserUpdateDTO dto);

    /**
     * 注销用户（逻辑删除）
     *
     * @param userId 用户ID
     */
    void delete(Long userId);

    /**
     * 找回密码
     * 通过邮箱验证码重置密码，同时清除该账号的登录锁定状态和Token
     *
     * @param dto 找回密码参数（邮箱+验证码+新密码）
     */
    void resetPassword(ResetPasswordDTO dto);

    /**
     * 根据ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（密码已脱敏）
     */
    PeUser getById(Long userId);

    /**
     * 分页查询用户列表（管理后台使用）
     * 返回结果不包含密码字段
     *
     * @param dto 分页查询参数
     * @return 分页结果（UserVO不含密码）
     */
    IPage<UserVO> page(UserPageDTO dto);
}
