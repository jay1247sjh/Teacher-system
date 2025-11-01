# 权限列表功能更新说明

## 📋 更新内容

### 问题
之前登录接口返回的 `permissions` 字段为 `null`，前端无法获取用户的具体权限列表。

### 解决方案
完善了后端权限查询逻辑，登录时从数据库查询用户的所有权限标识（perms）并返回给前端。

---

## 🔧 后端修改

### 1. 新增 PermissionMapper
**文件**: `teacher-system-user-infrastructure/.../PermissionMapper.java`

```java
@Select("...")
List<String> selectPermsByRoleIds(@Param("roleIds") List<Integer> roleIds);
```

通过 SQL 关联查询：`role_permission` → `permission`，获取用户所有角色的权限标识。

### 2. 新增 PermissionPO
**文件**: `teacher-system-user-infrastructure/.../PermissionPO.java`

匹配数据库 `permission` 表结构，包含所有字段。

### 3. 新增 PermissionRepositoryImpl
**文件**: `teacher-system-user-infrastructure/.../PermissionRepositoryImpl.java`

实现权限查询逻辑：
```java
@Override
public List<String> getPermsByRoleIds(List<Integer> roleIds) {
    return permissionMapper.selectPermsByRoleIds(roleIds);
}
```

### 4. 更新 PermissionRepository 接口
**文件**: `teacher-system-user-domain/.../PermissionRepository.java`

添加方法：
```java
List<String> getPermsByRoleIds(List<Integer> roleIds);
```

### 5. 更新 UserServiceImpl
**文件**: `teacher-system-user-application/.../UserServiceImpl.java`

登录时查询并返回权限列表：
```java
// 获取用户角色ID列表
List<Integer> roleIds = userRoleRepository.getRoleIdById(id.getId());

// 获取用户权限列表（根据角色查询）
List<String> permissions = permissionRepository.getPermsByRoleIds(roleIds);

// 返回给前端
return new UserVO(
    token,
    expireAt,
    wordId,
    username,
    avatar,
    roleIds,
    permissions,  // ✅ 现在返回完整的权限列表
    route
);
```

---

## 🎨 前端修改

### 1. 更新 UserInfo 接口
**文件**: `src/store/user.ts`

```typescript
export interface UserInfo {
  token: string
  expireAt: string
  wordId: string
  username: string
  avatar?: string
  roleIds: number[]
  permissions: string[]  // ✅ 权限标识列表
}
```

### 2. 添加权限判断方法到 Store

```typescript
// 判断是否拥有指定权限
const hasPermission = (permission: string): boolean => {
  return permissions.value.includes(permission)
}

// 判断是否拥有任意一个指定权限
const hasAnyPermission = (permissionList: string[]): boolean => {
  return permissionList.some(perm => permissions.value.includes(perm))
}

// 判断是否拥有所有指定权限
const hasAllPermissions = (permissionList: string[]): boolean => {
  return permissionList.every(perm => permissions.value.includes(perm))
}
```

### 3. 更新权限工具函数
**文件**: `src/utils/permission.ts`

添加便捷的权限判断函数：
```typescript
import { hasPermission, hasAnyPermission, hasAllPermissions } from '@/utils/permission'
```

---

## 🚀 使用示例

### 后端返回的数据格式

登录成功后，后端返回：
```json
{
  "token": "eyJhbGciOiJSUzI1NiJ9...",
  "expireAt": "2025-11-01T12:00:00",
  "wordId": "2205310510",
  "username": "苏家秦",
  "avatar": null,
  "roleIds": [1],
  "permissions": [
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
  ],
  "route": null
}
```

### 前端使用权限判断

#### 1. 在组件中使用

```vue
<template>
  <!-- 只有拥有 'table:create' 权限的用户才能看到 -->
  <button v-if="hasCreatePermission" @click="createTable">
    创建表格
  </button>
  
  <!-- 只有拥有 'table:data:score' 权限的用户才能设置分数 -->
  <input 
    v-if="hasScorePermission" 
    v-model="score" 
    placeholder="请输入分数" 
  />
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { useUserStore } from '@/store/user'

export default defineComponent({
  setup() {
    const userStore = useUserStore()
    
    const hasCreatePermission = computed(() => 
      userStore.hasPermission('table:create')
    )
    
    const hasScorePermission = computed(() => 
      userStore.hasPermission('table:data:score')
    )
    
    return {
      hasCreatePermission,
      hasScorePermission
    }
  }
})
</script>
```

#### 2. 在 JS/TS 中使用

```typescript
import { hasPermission, hasAnyPermission } from '@/utils/permission'

// 判断是否有创建表格权限
if (hasPermission('table:create')) {
  // 执行创建表格逻辑
}

// 判断是否有任意一个管理权限
if (hasAnyPermission(['table:edit', 'table:delete'])) {
  // 显示管理按钮
}
```

#### 3. 路由守卫中使用

```typescript
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  
  // 检查路由所需权限
  const requiredPermission = to.meta.permission as string
  
  if (requiredPermission && !userStore.hasPermission(requiredPermission)) {
    ElMessage.error('您没有访问该页面的权限')
    next('/403')
  } else {
    next()
  }
})
```

---

## 📊 权限标识说明

根据初始化脚本，系统定义了以下权限标识：

### 表格管理权限
- `table:create` - 创建表格
- `table:edit` - 编辑表格
- `table:delete` - 删除表格

### 表格数据权限
- `table:data:view` - 查看数据
- `table:data:add` - 添加数据
- `table:data:edit` - 编辑数据
- `table:data:delete` - 删除数据
- `table:data:score` - 设置分数（仅管理员）
- `table:data:admin-field` - 编辑管理员字段

### 用户管理权限
- `user:view` - 查看用户
- `user:create` - 创建用户
- `user:edit` - 编辑用户
- `user:delete` - 删除用户

### API权限
- `api:table` - 表格相关API
- `api:user` - 用户相关API

---

## ✅ 角色权限分配

### 超级管理员（roleId=1）
✅ 拥有所有权限（15+个）

### 管理员（roleId=2）
✅ 除 `table:create` 外的所有权限（14+个）

### 普通成员（roleId=3）
✅ 基本权限（7个）：
- `table:data:view`
- `table:data:add`
- `table:data:edit`
- `table:data:delete`
- `api:table`

❌ 没有的权限：
- `table:create`
- `table:data:score`
- `table:data:admin-field`
- 所有用户管理权限

---

## 🔍 验证步骤

### 1. 后端验证

查看登录接口返回的数据：
```bash
# 查看后端日志
# 应该能看到权限列表被查询和返回
```

### 2. 前端验证

打开浏览器控制台，登录后查看：
```javascript
// 查看用户信息
const userStore = useUserStore()
console.log('权限列表:', userStore.permissions)

// 应该输出类似：
// ['table:create', 'table:data:score', ...]
```

### 3. 数据库验证

```sql
-- 查询用户的所有权限
SELECT DISTINCT p.perms 
FROM user_role ur
JOIN role_permission rp ON ur.role_id = rp.role_id
JOIN permission p ON rp.permission_id = p.id
WHERE ur.user_id = '2205310510'
AND p.perms IS NOT NULL;
```

---

## 🎯 优势

### 1. 细粒度权限控制
不再仅依赖角色判断，可以精确到具体功能按钮。

### 2. 灵活的权限配置
可以在数据库中灵活调整角色权限，无需修改代码。

### 3. 前后端一致
前后端使用相同的权限标识，便于维护。

### 4. 易于扩展
添加新权限只需：
1. 在数据库 `permission` 表中添加记录
2. 在 `role_permission` 表中分配给相应角色
3. 前端使用 `hasPermission('new:permission')` 判断

---

## 📝 注意事项

1. **权限标识命名规范**：使用 `模块:操作` 或 `模块:子模块:操作` 格式
   - ✅ `table:create`
   - ✅ `table:data:score`
   - ❌ `createTable`

2. **权限判断位置**：
   - 前端：UI展示控制（隐藏/禁用按钮）
   - 后端：业务逻辑控制（必须验证，前端判断可被绕过）

3. **权限更新**：修改角色权限后，用户需要重新登录才能生效

4. **空权限处理**：如果用户没有任何角色，`permissions` 将返回空数组 `[]`

---

## 🎉 完成

现在您的系统拥有完整的 RBAC 权限控制：
- ✅ 角色管理
- ✅ 权限管理
- ✅ 角色权限关联
- ✅ 用户角色关联
- ✅ 前端权限判断
- ✅ 后端权限验证

可以灵活地为不同用户分配不同的角色和权限！

