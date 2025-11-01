# 权限系统重构说明文档

## 📋 概述

本次重构完全重新设计了前后端权限系统，解决了以下问题：
1. ✅ 后端JWT token中包含完整的角色信息（roleIds）
2. ✅ 前端使用Pinia store统一管理用户状态和权限，替代localStorage
3. ✅ 权限判断逻辑集中化、规范化
4. ✅ 支持持久化存储，页面刷新不丢失状态

---

## 🔧 后端修改

### 1. UserVO 增加角色字段

**文件**: `teacher-system-user-application/src/main/java/com/txq/application/entity/vo/UserVO.java`

```java
// 新增字段
private List<Integer> roleIds;  // 用户角色ID列表
```

### 2. 登录接口返回角色信息

**文件**: `teacher-system-user-application/src/main/java/com/txq/application/service/impl/UserServiceImpl.java`

登录时会：
- 从数据库查询用户的角色ID列表
- 将角色ID列表放入JWT token的payload中
- 在响应中返回角色ID列表给前端

### 3. TableDataServiceImpl 添加调试日志

**文件**: `teacher-system-table-application/src/main/java/com/txq/application/service/impl/TableDataServiceImpl.java`

添加了详细的日志输出，便于排查权限问题：
```java
log.info("保存表格数据 - 用户ID: {}, 角色列表: {}, 分数: {}", userId, roleIds, score);
log.info("是否是管理员: {}, 角色列表: {}", isAdmin, roleIds);
```

---

## 🎨 前端修改

### 1. 安装依赖

**文件**: `package.json`

新增依赖：
```json
"pinia": "^2.1.7",
"pinia-plugin-persistedstate": "^3.2.1"
```

**安装命令**：
```bash
cd teacher-system-web
npm install
```

### 2. 创建 Pinia Store

**文件**: `src/store/user.ts`

创建了用户状态管理store，包含：

#### 状态管理
- `userInfo`: 用户完整信息
- `isLoggedIn`: 是否已登录
- `roleIds`: 角色ID列表
- `userId`: 用户工号
- `username`: 用户名

#### 权限判断方法
- `isSuperAdmin`: 是否是超级管理员（roleId=1）
- `isAdmin`: 是否是管理员（roleId=1或2）
- `isMember`: 是否是普通成员（roleId=3）
- `hasRole(roleId)`: 是否拥有指定角色
- `hasAnyRole(roleIds)`: 是否拥有任意一个指定角色
- `hasAllRoles(roleIds)`: 是否拥有所有指定角色

#### 操作方法
- `setUserInfo(info)`: 设置用户信息
- `updateUserInfo(info)`: 更新用户信息
- `clearUserInfo()`: 清除用户信息（登出）
- `restoreUserInfo()`: 从localStorage恢复用户信息

#### 持久化配置
使用 `pinia-plugin-persistedstate` 插件自动持久化到localStorage

### 3. 注册 Pinia

**文件**: `src/main.ts`

```typescript
import pinia from "./store";
app.use(pinia);  // 必须在router之前注册
```

### 4. 更新API类型定义

**文件**: `src/api/user.ts`

```typescript
export interface UserVO {
    token: string
    expireAt: string
    wordId: string
    username: string
    avatar?: string
    roleIds: number[]       // 新增：角色ID列表
    permissions?: string[]
    route?: RouteVO
}
```

### 5. 修改登录逻辑

**文件**: `src/views/login/login.vue`

登录成功后：
```typescript
// 使用 Pinia store 保存用户信息
const { useUserStore } = await import('@/store/user')
const userStore = useUserStore()
userStore.setUserInfo(userInfo)
```

### 6. 更新路由守卫

**文件**: `src/router/index.ts`

使用store判断登录状态：
```typescript
const userStore = useUserStore()
if (requiresAuth && !userStore.isLoggedIn) {
    // 跳转登录页
}
```

### 7. 修改组件权限判断

**文件**: `src/views/main/table-detail.vue`

使用computed属性从store获取权限：
```typescript
computed: {
    isAdmin(): boolean {
        const userStore = useUserStore();
        return userStore.isAdmin;
    }
}
```

### 8. 创建权限工具函数

**文件**: `src/utils/permission.ts`

提供便捷的权限判断函数：
```typescript
import { isSuperAdmin, isAdmin, hasRole } from '@/utils/permission'
```

---

## 🗄️ 数据库配置

### 角色定义

| roleId | 角色名称 | 说明 |
|--------|---------|------|
| 1 | 超级管理员 | 拥有所有权限，可以设置分数 |
| 2 | 管理员 | 可以管理表格和数据，可以设置分数 |
| 3 | 普通成员 | 只能查看和编辑自己的数据，不能设置分数 |

### 初始化用户角色

**文件**: `deploy/mysql-deploy/dev/init/user_role_init.sql`

执行以下SQL为您的账号分配超级管理员角色：

```sql
-- 为工号 2205310510 分配超级管理员角色
INSERT IGNORE INTO user_role (user_id, role_id) VALUES ('2205310510', 1);

-- 验证
SELECT ur.*, u.username, r.role_name 
FROM user_role ur
LEFT JOIN user u ON ur.user_id = u.work_id
LEFT JOIN role r ON ur.role_id = r.role_id
WHERE ur.user_id = '2205310510';
```

---

## 🚀 部署步骤

### 1. 数据库初始化

```bash
# 连接到MySQL数据库
mysql -u root -p

# 选择数据库
use teacher_system;

# 执行角色初始化脚本
source E:/JavaProgram/teacher-system/deploy/mysql-deploy/dev/init/user_role_init.sql;
```

### 2. 后端部署

```bash
# 重新编译后端项目
cd E:/JavaProgram/teacher-system
mvn clean package -DskipTests

# 重启后端服务
# ... 根据您的部署方式重启
```

### 3. 前端部署

```bash
# 安装新依赖
cd teacher-system-web
npm install

# 启动开发服务器
npm run dev

# 或构建生产版本
npm run build
```

### 4. 清理旧数据

**重要**：首次部署后，建议清理浏览器的旧数据：
1. 打开浏览器开发者工具（F12）
2. 进入 Application/Storage 标签
3. 清除 localStorage 中的旧数据
4. 刷新页面重新登录

---

## 🔍 问题排查

### 问题1：超级管理员无法插入分数

**原因**：数据库中缺少用户角色数据

**解决方案**：
1. 查看后端日志，确认角色列表是否为空：
   ```
   保存表格数据 - 用户ID: xxx, 角色列表: [], 分数: xxx
   ```

2. 执行SQL检查：
   ```sql
   SELECT * FROM user_role WHERE user_id = '您的工号';
   ```

3. 如果为空，执行：
   ```sql
   INSERT INTO user_role (user_id, role_id) VALUES ('您的工号', 1);
   ```

4. **重新登录**让系统生成新的JWT token

### 问题2：前端获取不到角色信息

**排查步骤**：
1. 打开浏览器控制台，查看登录接口返回的数据
2. 检查 `userInfo.roleIds` 是否存在且为数组
3. 检查 localStorage 中 `teacher-system-user` 的数据
4. 查看路由守卫的日志输出

### 问题3：页面刷新后权限丢失

**原因**：Pinia持久化插件未正确配置

**解决方案**：
1. 确认 `pinia-plugin-persistedstate` 已安装
2. 确认 `src/store/index.ts` 中已注册插件
3. 清除浏览器缓存后重新登录

---

## 📊 权限控制矩阵

| 操作 | 超级管理员 | 管理员 | 普通成员 |
|-----|----------|--------|---------|
| 查看表格列表 | ✅ | ✅ | ✅ |
| 创建表格 | ✅ | ❌ | ❌ |
| 查看表格数据 | ✅ | ✅ | ✅ |
| 插入数据（普通字段） | ✅ | ✅ | ✅ |
| 插入数据（管理员字段） | ✅ | ✅ | ❌ |
| 设置分数 | ✅ | ✅ | ❌ |
| 编辑自己的数据 | ✅ | ✅ | ✅ |
| 编辑他人的数据 | ✅ | ✅ | ❌ |
| 删除自己的数据 | ✅ | ✅ | ✅ |
| 删除他人的数据 | ✅ | ✅ | ❌ |

---

## 🎯 使用示例

### 在组件中使用权限判断

```vue
<template>
  <!-- 只有管理员可见 -->
  <div v-if="isAdmin">
    <input v-model="score" placeholder="请输入分数" />
  </div>
  
  <!-- 只有超级管理员可见 -->
  <button v-if="isSuperAdmin" @click="deleteAll">删除所有</button>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useUserStore } from '@/store/user'

export default defineComponent({
  computed: {
    isAdmin() {
      return useUserStore().isAdmin
    },
    isSuperAdmin() {
      return useUserStore().isSuperAdmin
    }
  }
})
</script>
```

### 在JS/TS中使用权限判断

```typescript
import { isAdmin, hasRole, getRoleIds } from '@/utils/permission'

// 判断是否是管理员
if (isAdmin()) {
  // 执行管理员操作
}

// 判断是否拥有特定角色
if (hasRole(1)) {
  // 超级管理员操作
}

// 获取角色列表
const roles = getRoleIds()
console.log('当前用户角色:', roles)
```

---

## ✅ 测试清单

- [ ] 数据库中已为测试账号分配角色
- [ ] 后端服务已重启
- [ ] 前端依赖已安装（npm install）
- [ ] 前端服务已重启
- [ ] 浏览器localStorage已清理
- [ ] 重新登录后能看到角色信息
- [ ] 超级管理员可以设置分数
- [ ] 管理员可以设置分数
- [ ] 普通成员不能设置分数
- [ ] 权限判断在前端正常工作
- [ ] 页面刷新后权限不丢失

---

## 📝 注意事项

1. **必须重新登录**：修改数据库角色后，必须重新登录才能生成包含新角色的JWT token
2. **清理缓存**：首次部署后建议清理浏览器localStorage
3. **角色持久化**：用户信息通过Pinia自动持久化，无需手动管理
4. **安全性**：敏感操作仍需后端验证，前端权限判断仅用于UI展示

---

## 🔗 相关文件清单

### 后端
- `teacher-system-user-application/.../UserVO.java`
- `teacher-system-user-application/.../UserServiceImpl.java`
- `teacher-system-table-application/.../TableDataServiceImpl.java`
- `teacher-system-table-domain/.../ErrorCode.java`

### 前端
- `src/store/index.ts` - Pinia配置
- `src/store/user.ts` - 用户状态管理
- `src/utils/permission.ts` - 权限工具函数
- `src/api/user.ts` - API类型定义
- `src/main.ts` - Pinia注册
- `src/router/index.ts` - 路由守卫
- `src/views/login/login.vue` - 登录逻辑
- `src/views/main/table-detail.vue` - 权限使用示例
- `package.json` - 依赖配置

### 数据库
- `deploy/mysql-deploy/dev/init/user_role_init.sql` - 角色初始化脚本
- `deploy/mysql-deploy/dev/init/fix_admin_role.sql` - 快速修复脚本

---

## 🎉 完成

权限系统重构完成！现在您可以：
1. 执行数据库脚本分配角色
2. 重启后端服务
3. 安装前端依赖并重启
4. 重新登录测试权限功能

如有问题，请查看后端日志和浏览器控制台输出进行排查。

