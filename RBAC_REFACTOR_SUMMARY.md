# RBAC 权限系统重构总结

## 🎯 重构目标

**核心理念**：前端完全基于权限标识（perms）判断，不依赖角色判断。

### 为什么要重构？

❌ **之前的问题**：
- 前端通过 `isAdmin`、`isSuperAdmin` 等角色判断来控制权限
- 权限控制不够精确，无法细粒度控制
- 角色和权限耦合，不符合RBAC标准

✅ **重构后的优势**：
- 前端只关心权限标识，不关心角色
- 使用 `hasPermission('table:data:score')` 精确判断
- 完全符合RBAC标准实践
- 更灵活、更易维护

---

## 📦 修改内容

### 1. 后端修改

#### 1.1 UserVO - 只返回权限列表
**文件**: `teacher-system-user-application/.../UserVO.java`

```java
public class UserVO {
    private String token;
    private Instant expireAt;
    private String wordId;
    private String username;
    private String avatar;
    private List<String> permissions;  // ✅ 只返回权限标识列表
    // ❌ 移除了 roleIds 字段
    private RouteVO route;
}
```

#### 1.2 UserServiceImpl - 登录时只返回权限
**文件**: `teacher-system-user-application/.../UserServiceImpl.java`

```java
// 获取用户角色ID列表（仅用于查询权限）
List<Integer> roleIds = userRoleRepository.getRoleIdById(id.getId());

// 获取用户权限列表
List<String> permissions = permissionRepository.getPermsByRoleIds(roleIds);

// 返回给前端（只返回权限，不返回角色）
return new UserVO(
    token,
    expireAt,
    id.getId(),
    username,
    null,
    permissions,  // ✅ 只返回权限标识
    null
);
```

---

### 2. 前端修改

#### 2.1 API类型定义
**文件**: `src/api/user.ts`

```typescript
export interface UserVO {
    token: string
    expireAt: string
    wordId: string
    username: string
    avatar?: string
    permissions: string[]   // ✅ 只有权限列表
    // ❌ 移除了 roleIds 字段
    route?: RouteVO
}
```

#### 2.2 Store重构
**文件**: `src/store/user.ts`

```typescript
export interface UserInfo {
  token: string
  expireAt: string
  wordId: string
  username: string
  avatar?: string
  permissions: string[]  // ✅ 只存储权限标识
  // ❌ 移除了 roleIds 字段
}

// ❌ 移除了所有角色相关的判断方法：
// - isSuperAdmin
// - isAdmin
// - isMember
// - hasRole
// - hasAnyRole
// - hasAllRoles

// ✅ 只保留权限相关的判断方法：
// - hasPermission(permission: string)
// - hasAnyPermission(permissions: string[])
// - hasAllPermissions(permissions: string[])
```

#### 2.3 权限工具函数
**文件**: `src/utils/permission.ts`

```typescript
// ❌ 移除了所有角色相关函数：
// - isSuperAdmin()
// - isAdmin()
// - isMember()
// - hasRole(roleId)
// - hasAnyRole(roleIds)
// - hasAllRoles(roleIds)
// - getRoleIds()

// ✅ 只保留权限相关函数：
export function hasPermission(permission: string): boolean
export function hasAnyPermission(permissions: string[]): boolean
export function hasAllPermissions(permissions: string[]): boolean
export function getPermissions(): string[]
```

#### 2.4 组件修改
**文件**: `src/views/main/table-detail.vue`

```vue
<template>
  <!-- ❌ 之前：基于角色判断 -->
  <input :disabled="!isAdmin" />
  
  <!-- ✅ 现在：基于权限标识判断 -->
  <input :disabled="!canSetScore" />
</template>

<script lang="ts">
export default defineComponent({
  computed: {
    // ❌ 移除了角色判断
    // isAdmin(): boolean
    // isSuperAdmin(): boolean
    
    // ✅ 使用权限标识判断
    canSetScore(): boolean {
      return useUserStore().hasPermission('table:data:score');
    },
    canEditAdminField(): boolean {
      return useUserStore().hasPermission('table:data:admin-field');
    }
  }
})
</script>
```

---

### 3. 数据库脚本

#### 3.1 新增权限初始化脚本
**文件**: `deploy/mysql-deploy/dev/init/init_rbac_permission.sql`

- 初始化角色表（3个角色）
- 初始化权限表（20+个权限，包含完整的权限标识）
- 初始化角色权限关系（基于权限标识分配）
- 为用户分配角色

#### 3.2 核心权限标识

| 权限标识 | 说明 | 类型 |
|---------|------|------|
| `table:create` | 创建表格 | BUTTON |
| `table:edit` | 编辑表格 | BUTTON |
| `table:delete` | 删除表格 | BUTTON |
| `table:data:view` | 查看数据 | BUTTON |
| `table:data:add` | 添加数据 | BUTTON |
| `table:data:edit` | 编辑数据 | BUTTON |
| `table:data:delete` | 删除数据 | BUTTON |
| **`table:data:score`** | **设置分数** | BUTTON |
| **`table:data:admin-field`** | **编辑管理员字段** | BUTTON |
| `user:view` | 查看用户 | BUTTON |
| `user:create` | 创建用户 | BUTTON |
| `user:edit` | 编辑用户 | BUTTON |
| `user:delete` | 删除用户 | BUTTON |

---

## 🔄 使用对比

### 之前的方式（基于角色）

```vue
<template>
  <!-- ❌ 基于角色判断 -->
  <input :disabled="!isAdmin" placeholder="请输入分数" />
</template>

<script lang="ts">
export default defineComponent({
  computed: {
    isAdmin(): boolean {
      const userStore = useUserStore();
      return userStore.isAdmin;  // 判断是否是管理员角色
    }
  }
})
</script>
```

**问题**：
- 不够精确：管理员可能有很多权限，但我们只需要判断"设置分数"这一个权限
- 不够灵活：如果要给某个角色单独开放/关闭某个权限，需要修改代码
- 不符合RBAC：角色和权限耦合

### 现在的方式（基于权限标识）

```vue
<template>
  <!-- ✅ 基于权限标识判断 -->
  <input :disabled="!canSetScore" placeholder="请输入分数" />
</template>

<script lang="ts">
export default defineComponent({
  computed: {
    canSetScore(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:score');  // 精确判断权限
    }
  }
})
</script>
```

**优势**：
- ✅ 精确：只判断"设置分数"这一个权限
- ✅ 灵活：在数据库中修改角色权限即可，无需改代码
- ✅ 符合RBAC：完全基于权限标识

---

## 📊 登录返回数据对比

### 之前
```json
{
  "token": "...",
  "wordId": "2205310510",
  "username": "苏家秦",
  "roleIds": [1],  // ❌ 返回角色ID
  "permissions": ["table:create", "..."]
}
```

### 现在
```json
{
  "token": "...",
  "wordId": "2205310510",
  "username": "苏家秦",
  "permissions": [  // ✅ 只返回权限标识
    "table:create",
    "table:edit",
    "table:delete",
    "table:data:view",
    "table:data:add",
    "table:data:edit",
    "table:data:delete",
    "table:data:score",
    "table:data:admin-field",
    "user:view",
    "user:create",
    "user:edit",
    "user:delete",
    "api:table",
    "api:user"
  ]
}
```

---

## 🚀 部署步骤

### 1. 执行数据库脚本

```bash
mysql -u root -p teacher_system < E:/JavaProgram/teacher-system/deploy/mysql-deploy/dev/init/init_rbac_permission.sql
```

### 2. 重启后端服务

```bash
cd E:/JavaProgram/teacher-system
mvn clean package -DskipTests
# 重启服务
```

### 3. 清除前端缓存

1. 打开浏览器开发者工具（F12）
2. Application → Clear storage → Clear site data
3. 刷新页面

### 4. 重新登录

1. 使用工号 `2205310510` 登录
2. 查看控制台输出：
   ```
   权限列表: ['table:create', 'table:data:score', ...]
   ```

### 5. 验证功能

- ✅ 分数输入框不再被禁用
- ✅ 可以正常输入分数
- ✅ 管理员字段可以编辑

---

## ✅ 验证清单

- [ ] 数据库脚本执行成功
- [ ] 后端服务重启成功
- [ ] 前端缓存已清除
- [ ] 重新登录成功
- [ ] 控制台显示权限列表（不是角色列表）
- [ ] 权限列表包含 `table:data:score`
- [ ] 分数输入框可以编辑
- [ ] 管理员字段可以编辑
- [ ] 普通成员登录后分数输入框被禁用

---

## 📚 相关文档

1. **`RBAC_PERMISSION_GUIDE.md`** - 详细的使用指南
   - 权限标识列表
   - 前后端使用示例
   - 调试方法

2. **`init_rbac_permission.sql`** - 数据库初始化脚本
   - 角色初始化
   - 权限初始化
   - 角色权限关系初始化

3. **`PERMISSION_LIST_UPDATE.md`** - 权限列表功能说明
   - 后端实现细节
   - 前端使用方法

---

## 🎉 重构完成

现在您的系统拥有真正的RBAC权限控制：

- ✅ 前端完全基于权限标识判断
- ✅ 不依赖角色判断
- ✅ 权限控制更加精确和灵活
- ✅ 符合RBAC标准实践
- ✅ 易于扩展和维护

使用方式：
```typescript
// 判断是否可以设置分数
if (hasPermission('table:data:score')) {
  // 显示分数输入框
}

// 判断是否可以编辑管理员字段
if (hasPermission('table:data:admin-field')) {
  // 允许编辑
}
```

**简单、直观、强大！** 🚀

