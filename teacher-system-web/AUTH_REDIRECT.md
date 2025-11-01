# 🔐 登录认证与重定向功能说明

## 📋 功能概述

实现了完整的登录认证和自动重定向功能：
1. **Token过期自动跳转**：当token过期或无效时，自动跳转到登录页
2. **保存原页面**：跳转登录时保存用户原本想访问的页面
3. **登录后返回**：登录成功后自动返回到原页面

---

## 🔄 完整流程

### 场景1：用户正常访问（有有效token）
```
用户访问 /home/table-management
  ↓
路由守卫检查: token存在 ✅
  ↓
正常进入页面
```

### 场景2：用户未登录访问受保护页面
```
用户访问 /home/table-management (无token)
  ↓
路由守卫检查: token不存在 ❌
  ↓
保存目标路径: redirect=/home/table-management
  ↓
跳转到: /login?redirect=/home/table-management
  ↓
用户登录成功
  ↓
自动跳转到: /home/table-management ✅
```

### 场景3：Token过期（操作时发现过期）
```
用户正在访问 /home/table-management
  ↓
用户点击"创建表格"
  ↓
发送请求，后端返回 401 Unauthorized
  ↓
Axios拦截器捕获401错误
  ↓
清除token和userInfo
  ↓
保存当前路径: redirect=/home/table-management
  ↓
跳转到: /login?redirect=/home/table-management
  ↓
显示提示: "登录已过期，请重新登录"
  ↓
用户重新登录
  ↓
自动跳转回: /home/table-management ✅
```

---

## 📂 涉及的文件

### 1. `src/router/index.ts` - 路由配置和守卫

#### 路由元数据
```typescript
meta: { requiresAuth: true } // 标记需要登录的路由
```

#### 路由守卫
```typescript
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)

    if (requiresAuth && !token) {
        // 需要登录但没有token
        ElMessage.warning('请先登录')
        next({
            name: 'Login',
            query: { redirect: to.fullPath } // 保存完整路径
        })
    } else if (to.name === 'Login' && token) {
        // 已登录用户访问登录页，重定向到首页
        next({ path: '/home' })
    } else {
        // 正常访问
        next()
    }
})
```

### 2. `src/utils/request.ts` - HTTP请求拦截器

#### 401错误处理
```typescript
case 401:
    errorMessage = data?.msg || '登录已过期，请重新登录'
    
    // 清除token和用户信息
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    
    // 保存当前路径并跳转登录
    const currentPath = window.location.pathname
    if (currentPath !== '/login' && currentPath !== '/register') {
        router.push({
            name: 'Login',
            query: { redirect: currentPath + window.location.search }
        })
    } else {
        router.push({ name: 'Login' })
    }
    break
```

### 3. `src/views/login/login.vue` - 登录页面

#### 登录成功后重定向
```typescript
// 获取重定向路径
const redirect = this.$route.query.redirect as string

// 跳转到保存的页面或首页
setTimeout(() => {
    if (redirect && redirect !== '/login' && redirect !== '/register') {
        this.$router.push(redirect)
    } else {
        this.$router.push('/home')
    }
}, 500)
```

---

## 🎯 关键点说明

### 1. 为什么要保存完整路径？
使用 `to.fullPath` 而不是 `to.path`，可以保存查询参数：
```
原路径: /home/table-management?id=123
保存为: redirect=/home/table-management?id=123
```

### 2. 为什么要排除登录和注册页？
防止循环重定向：
```typescript
if (currentPath !== '/login' && currentPath !== '/register')
```

### 3. 为什么要延迟跳转？
给用户时间看到成功提示：
```typescript
setTimeout(() => {
    this.$router.push(redirect)
}, 500)
```

### 4. 为什么401错误不显示错误提示后就跳转？
```typescript
// 显示错误提示
ElMessage.error(errorMessage)

// 然后执行跳转逻辑
router.push({ name: 'Login', query: { redirect: currentPath } })
```

---

## 🧪 测试场景

### 测试1：直接访问受保护页面（未登录）
**步骤：**
1. 清除localStorage中的token
2. 直接访问 `http://localhost:5173/home/table-management`

**预期结果：**
- ❌ 被拦截
- ✅ 跳转到 `/login?redirect=/home/table-management`
- ✅ 显示提示："请先登录"
- ✅ 登录成功后自动返回表格管理页面

### 测试2：Token过期（操作中过期）
**步骤：**
1. 正常登录
2. 等待token过期（30分钟）或手动修改token为无效值
3. 在表格管理页面点击"创建表格"

**预期结果：**
- ❌ 请求返回401
- ✅ 显示提示："登录已过期，请重新登录"
- ✅ 自动跳转到 `/login?redirect=/home/table-management`
- ✅ Token被清除
- ✅ 重新登录后返回表格管理页面

### 测试3：已登录访问登录页
**步骤：**
1. 已登录状态
2. 手动访问 `/login`

**预期结果：**
- ✅ 自动重定向到 `/home`

### 测试4：登录页URL带redirect参数
**步骤：**
1. 访问 `/login?redirect=/home/table-management`
2. 登录

**预期结果：**
- ✅ 登录成功后跳转到 `/home/table-management`
- ✅ 不是首页

### 测试5：正常登录（无redirect参数）
**步骤：**
1. 直接访问 `/login`
2. 登录

**预期结果：**
- ✅ 登录成功后跳转到 `/home`

---

## 🔒 安全说明

### 1. Token存储
```typescript
localStorage.setItem('token', userInfo.token)
```
- ✅ 持久化存储，刷新页面不丢失
- ⚠️ 注意XSS攻击风险（确保内容安全策略）

### 2. Token清除时机
- 401错误（token无效/过期）
- 用户主动登出
- 403错误时可选择是否清除

### 3. 重定向URL验证
```typescript
if (redirect && redirect !== '/login' && redirect !== '/register')
```
防止重定向到登录/注册页造成循环

---

## ⚙️ 配置选项

### 修改Token过期提示
在 `request.ts` 中：
```typescript
case 401:
    errorMessage = data?.msg || '您的自定义提示'
```

### 修改默认跳转页面
在 `login.vue` 中：
```typescript
this.$router.push('/your-default-page')
```

### 添加更多受保护路由
在 `router/index.ts` 中：
```typescript
{
    path: '/your-route',
    component: YourComponent,
    meta: { requiresAuth: true } // 添加这行
}
```

---

## 🐛 常见问题

### Q1: 登录后跳转到首页而不是原页面？
**A**: 检查以下几点：
1. URL中是否有 `redirect` 参数
2. `login.vue` 中是否正确读取了 `this.$route.query.redirect`
3. 控制台是否有错误

### Q2: Token过期后没有自动跳转？
**A**: 检查：
1. `request.ts` 中是否正确导入了 `router`
2. 401错误是否被正确捕获
3. 浏览器控制台是否有报错

### Q3: 出现循环重定向？
**A**: 确保：
1. 排除了 `/login` 和 `/register` 路径
2. 登录页面路由没有 `requiresAuth: true`
3. 路由守卫逻辑正确

### Q4: 刷新页面后又要登录？
**A**: 可能原因：
1. Token没有正确保存到localStorage
2. Token已过期
3. 路由守卫逻辑有问题

---

## 🎓 扩展功能建议

### 1. Token自动刷新
在token即将过期时自动刷新：
```typescript
// 在request拦截器中
if (tokenWillExpireSoon) {
    await refreshToken()
}
```

### 2. 记住登录状态
```typescript
// 登录时
if (rememberMe) {
    localStorage.setItem('rememberMe', 'true')
}
```

### 3. 多标签页同步登出
```typescript
// 监听storage变化
window.addEventListener('storage', (e) => {
    if (e.key === 'token' && !e.newValue) {
        // Token被删除，跳转登录
        router.push('/login')
    }
})
```

### 4. 页面数据保存
在跳转前保存表单数据：
```typescript
sessionStorage.setItem('formData', JSON.stringify(formData))
```

---

## 📊 流程图

```
                        用户访问页面
                             ↓
                    ┌────────┴────────┐
                    │  路由守卫检查   │
                    └────────┬────────┘
                             ↓
                  是否需要登录？ (requiresAuth)
                    ↙              ↘
                 否                  是
                 ↓                   ↓
              正常访问          是否有token？
                              ↙            ↘
                            有              无
                            ↓               ↓
                        正常访问      保存路径+跳转登录
                            ↓               ↓
                        发送请求        用户填写表单
                            ↓               ↓
                    后端返回401？      点击登录按钮
                    ↙         ↘            ↓
                 否           是       调用登录API
                 ↓            ↓            ↓
              正常显示    清除token    登录成功？
                          ↓            ↙       ↘
                    保存路径+跳转     是        否
                          ↓           ↓         ↓
                      显示提示   读取redirect  显示错误
                                      ↓
                                  跳转原页面
```

---

## ✅ 总结

你的系统现在已经实现了完整的登录认证和重定向功能：

1. ✅ **路由守卫**：未登录用户自动跳转登录页
2. ✅ **保存原路径**：使用query参数保存目标页面
3. ✅ **Token过期处理**：401错误自动清除token并跳转
4. ✅ **登录后返回**：自动跳转到原本想访问的页面
5. ✅ **已登录保护**：已登录用户访问登录页自动跳转首页
6. ✅ **用户体验**：友好的提示信息和平滑的跳转

现在你的用户体验已经非常完善了！🎉

