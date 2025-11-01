# RBAC 权限系统使用指南

## 📋 设计理念

**核心原则**：前端完全基于权限标识（perms）判断，不依赖角色判断。

### 为什么不返回角色？

1. **解耦角色和权限**：角色只是权限的容器，前端不应该关心用户是什么角色
2. **灵活性**：同一个角色在不同环境下可能有不同权限
3. **安全性**：减少前端暴露的信息
4. **标准化**：符合RBAC标准实践

---

## 🎯 权限标识命名规范

### 格式
```
模块:子模块:操作
```

### 示例
- `table:create` - 表格创建权限
- `table:data:score` - 表格数据打分权限
- `table:data:admin-field` - 编辑管理员字段权限
- `user:view` - 查看用户权限

---

## 📊 核心权限标识列表

### 表格管理权限
| 权限标识 | 说明 | 拥有该权限的角色 |
|---------|------|----------------|
| `table:create` | 创建表格 | 超级管理员 |
| `table:edit` | 编辑表格 | 超级管理员、管理员 |
| `table:delete` | 删除表格 | 超级管理员、管理员 |

### 表格数据权限
| 权限标识 | 说明 | 拥有该权限的角色 |
|---------|------|----------------|
| `table:data:view` | 查看数据 | 所有角色 |
| `table:data:add` | 添加数据 | 所有角色 |
| `table:data:edit` | 编辑数据 | 所有角色 |
| `table:data:delete` | 删除数据 | 所有角色 |
| `table:data:score` | **设置分数** | 超级管理员、管理员 |
| `table:data:admin-field` | **编辑管理员字段** | 超级管理员、管理员 |

### 用户管理权限
| 权限标识 | 说明 | 拥有该权限的角色 |
|---------|------|----------------|
| `user:view` | 查看用户 | 超级管理员、管理员 |
| `user:create` | 创建用户 | 超级管理员、管理员 |
| `user:edit` | 编辑用户 | 超级管理员、管理员 |
| `user:delete` | 删除用户 | 超级管理员、管理员 |

---

## 🔧 后端实现

### 1. 登录接口返回

**只返回权限标识列表，不返回角色**

```json
{
  "token": "...",
  "wordId": "2205310510",
  "username": "苏家秦",
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
  ]
}
```

### 2. 权限查询SQL

```sql
-- 查询用户的所有权限标识
SELECT DISTINCT p.perms 
FROM user_role ur
JOIN role_permission rp ON ur.role_id = rp.role_id
JOIN permission p ON rp.permission_id = p.id
WHERE ur.user_id = '2205310510'
AND p.perms IS NOT NULL;
```

---

## 🎨 前端使用

### 1. Store中的权限判断

```typescript
// src/store/user.ts
export const useUserStore = defineStore('user', () => {
  const permissions = computed(() => userInfo.value?.permissions || [])
  
  // 判断是否拥有指定权限
  const hasPermission = (permission: string): boolean => {
    return permissions.value.includes(permission)
  }
  
  return {
    permissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions
  }
})
```

### 2. 组件中使用

#### 方式一：使用computed属性

```vue
<template>
  <!-- 只有拥有设置分数权限的用户才能输入 -->
  <input 
    v-model="score"
    :disabled="!canSetScore"
    placeholder="请输入分数"
  />
  
  <!-- 只有拥有编辑管理员字段权限的用户才能编辑 -->
  <input 
    v-model="adminField"
    :disabled="!canEditAdminField"
    placeholder="管理员字段"
  />
</template>

<script lang="ts">
import { defineComponent, computed } from 'vue'
import { useUserStore } from '@/store/user'

export default defineComponent({
  setup() {
    const userStore = useUserStore()
    
    // 基于权限标识判断
    const canSetScore = computed(() => 
      userStore.hasPermission('table:data:score')
    )
    
    const canEditAdminField = computed(() => 
      userStore.hasPermission('table:data:admin-field')
    )
    
    return {
      canSetScore,
      canEditAdminField
    }
  }
})
</script>
```

#### 方式二：使用工具函数

```vue
<template>
  <button v-if="canCreate" @click="createTable">
    创建表格
  </button>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { hasPermission } from '@/utils/permission'

const canCreate = computed(() => hasPermission('table:create'))
</script>
```

### 3. 权限工具函数

```typescript
// src/utils/permission.ts
import { useUserStore } from '@/store/user'

/**
 * 判断是否拥有指定权限
 */
export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  return userStore.hasPermission(permission)
}

/**
 * 判断是否拥有任意一个指定权限
 */
export function hasAnyPermission(permissions: string[]): boolean {
  const userStore = useUserStore()
  return userStore.hasAnyPermission(permissions)
}

/**
 * 判断是否拥有所有指定权限
 */
export function hasAllPermissions(permissions: string[]): boolean {
  const userStore = useUserStore()
  return userStore.hasAllPermissions(permissions)
}
```

---

## 📝 实际应用示例

### 示例1：表格详情页的分数输入

**需求**：只有拥有 `table:data:score` 权限的用户才能设置分数

```vue
<template>
  <div class="form-group">
    <label class="form-label">
      分数
      <span v-if="!canSetScore" class="lock-icon" title="无权限设置分数">🔒</span>
    </label>
    <input 
      v-model.number="formData.score"
      type="number"
      :disabled="!canSetScore"
      class="form-input"
      :class="{ 'input-disabled': !canSetScore }"
      placeholder="请输入分数"
    />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  computed: {
    canSetScore(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:score');
    }
  }
})
</script>
```

### 示例2：管理员专属字段

**需求**：只有拥有 `table:data:admin-field` 权限的用户才能编辑管理员字段

```vue
<template>
  <div v-for="field in fields" :key="field.fieldName" class="form-group">
    <label class="form-label">
      {{ field.fieldName }}
      <span v-if="field.root && !canEditAdminField" class="lock-icon">🔒</span>
    </label>
    <input 
      v-model="formData[field.fieldName]"
      :disabled="field.root && !canEditAdminField"
      :placeholder="`请输入${field.fieldName}`"
    />
  </div>
</template>

<script lang="ts">
export default defineComponent({
  computed: {
    canEditAdminField(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:admin-field');
    }
  }
})
</script>
```

### 示例3：创建表格按钮

**需求**：只有拥有 `table:create` 权限的用户才能看到创建表格按钮

```vue
<template>
  <button 
    v-if="hasPermission('table:create')" 
    @click="createTable"
    class="btn-create"
  >
    + 创建表格
  </button>
</template>

<script setup lang="ts">
import { hasPermission } from '@/utils/permission'

const createTable = () => {
  // 创建表格逻辑
}
</script>
```

---

## 🔍 调试和验证

### 1. 查看用户权限

```javascript
// 在浏览器控制台执行
const userStore = useUserStore()
console.log('用户权限列表:', userStore.permissions)

// 输出示例：
// ['table:create', 'table:data:score', 'table:data:admin-field', ...]
```

### 2. 测试权限判断

```javascript
// 测试是否有设置分数权限
console.log('可以设置分数:', userStore.hasPermission('table:data:score'))

// 测试是否有创建表格权限
console.log('可以创建表格:', userStore.hasPermission('table:create'))
```

### 3. 后端日志

登录时查看后端日志，确认权限列表被正确查询：
```
获取用户权限列表: [table:create, table:data:score, ...]
```

---

## ⚠️ 注意事项

### 1. 前端权限判断的局限性

- **仅用于UI控制**：隐藏/禁用按钮和输入框
- **不能作为安全保障**：前端代码可被绕过
- **必须后端验证**：所有敏感操作必须在后端验证权限

### 2. 权限更新

- 修改用户角色或角色权限后，用户需要**重新登录**
- 权限信息存储在JWT token中，token过期前不会自动更新

### 3. 数据权限

- 普通成员虽然有 `table:data:edit` 和 `table:data:delete` 权限
- 但后端需要额外判断只能操作自己创建的数据（通过 `createdBy` 字段）

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

### 3. 清除前端缓存并重新登录

1. 打开浏览器开发者工具（F12）
2. Application → Clear storage → Clear site data
3. 重新登录
4. 查看控制台输出：`权限列表: ['table:create', ...]`

---

## ✅ 验证清单

- [ ] 数据库中权限表有数据
- [ ] 数据库中角色权限关系表有数据
- [ ] 用户角色表中有您的账号
- [ ] 后端服务已重启
- [ ] 前端缓存已清除
- [ ] 重新登录后能看到权限列表
- [ ] 控制台输出权限列表不为空
- [ ] 有 `table:data:score` 权限的用户可以输入分数
- [ ] 没有该权限的用户分数输入框被禁用

---

## 🎉 完成

现在您的系统拥有真正的RBAC权限控制：
- ✅ 前端基于权限标识判断，不依赖角色
- ✅ 权限判断更加精确和灵活
- ✅ 符合RBAC标准实践
- ✅ 易于扩展和维护

使用 `hasPermission('权限标识')` 即可判断任何权限！

