package com.txq.application.service;

import com.txq.application.entity.query.EmailQuery;
import com.txq.application.entity.query.LoginQuery;
import com.txq.application.entity.query.UserQuery;
import com.txq.application.entity.vo.SimpleUserVO;
import com.txq.application.entity.vo.UserManagementVO;
import com.txq.application.entity.vo.UserVO;

import java.util.List;

/**
 * 用户信息接口
 */
public interface IUserService {
    /**
     * 注册
     */
    void register(UserQuery registerDTO);

    /**
     * 发送验证码
     */
    void sendCode(EmailQuery emailQuery);

    /**
     * 登录
     */
    UserVO login(LoginQuery loginQuery);
    
    /**
     * 获取所有普通用户列表
     */
    List<SimpleUserVO> getNormalUsers();
    
    /**
     * 获取所有用户列表（含角色信息，不含密码）- 超级管理员功能
     */
    List<UserManagementVO> getAllUsersForManagement();
    
    /**
     * 创建用户 - 超级管理员功能
     */
    void createUser(String id, String username, String password, String email, String code, List<Integer> roleIds);
    
    /**
     * 更新用户 - 超级管理员功能
     */
    void updateUser(String id, String username, String email, List<Integer> roleIds, String newPassword);
    
    /**
     * 删除用户（逻辑删除）- 超级管理员功能
     */
    void deleteUser(String id);
}
