# 前端API集成说明文档

## ✅ 已完成的工作

### 1. **封装了统一的 axios 请求类** (`src/utils/request.ts`)

#### 核心功能：
- ✅ **统一的请求基础配置**
  - 基础URL：`http://localhost:10001`（网关地址）
  - 超时时间：15秒
  - 默认Content-Type：application/json

- ✅ **请求拦截器**
  - 自动添加 Authorization token（从localStorage获取）
  - 请求前的统一处理

- ✅ **响应拦截器**
  - 自动解析后端统一响应格式：`{ code: 200, message: "success", data: ... }`
  - code=200时自动返回data部分
  - 统一错误处理和提示
  - 401自动跳转登录页并清除token

- ✅ **导出的请求方法**
  - `get<T>(url, config)` - GET请求
  - `post<T>(url, data, config)` - POST请求
  - `put<T>(url, data, config)` - PUT请求
  - `del<T>(url, config)` - DELETE请求
  - `patch<T>(url, data, config)` - PATCH请求

---

### 2. **封装了用户相关API** (`src/api/user.ts`)

#### API接口：

##### 🔐 **用户登录**
```typescript
login(params: LoginParams): Promise<UserVO>
```
**请求参数**：
- `id`: 工号
- `password`: 密码

**返回数据**：
- `token`: 访问令牌
- `expireAt`: 令牌过期时间
- `wordId`: 用户工号
- `username`: 用户名
- `avatar`: 用户头像
- `permissions`: 用户权限列表
- `route`: 动态路由树

**后端接口**：`POST /user/login`

---

##### 📝 **用户注册**
```typescript
register(params: RegisterParams): Promise<string>
```
**请求参数**：
- `id`: 工号
- `username`: 姓名
- `password`: 密码
- `email`: 邮箱
- `code`: 验证码

**返回数据**：成功提示信息

**后端接口**：`POST /user/register`

---

##### 📧 **发送验证码**
```typescript
sendCode(params: SendCodeParams): Promise<string>
```
**请求参数**：
- `username`: 姓名
- `email`: 邮箱

**返回数据**：成功提示信息

**后端接口**：`POST /user/send-code`

---

### 3. **集成登录功能** (`src/views/login/login.vue`)

#### 功能实现：
- ✅ 调用登录API
- ✅ 保存token到localStorage
- ✅ 保存用户信息到localStorage
- ✅ 登录成功后跳转到首页 (`/main/home`)
- ✅ 错误提示（通过request拦截器统一处理）
- ✅ Loading状态显示

---

### 4. **集成注册功能** (`src/views/login/register.vue`)

#### 功能实现：
- ✅ 发送验证码API调用
- ✅ 60秒倒计时功能
- ✅ 调用注册API
- ✅ 表单校验
- ✅ 注册成功后跳转到登录页
- ✅ 错误提示（通过request拦截器统一处理）
- ✅ Loading状态显示

---

## 🔧 配置说明

### 环境变量配置

由于`.env`文件被gitignore忽略，需要手动创建：

#### 开发环境 (`.env.development`)
```env
# API基础路径（网关地址）
VITE_API_BASE_URL=http://localhost:10001
```

#### 生产环境 (`.env.production`)
```env
# API基础路径（网关地址）
VITE_API_BASE_URL=https://your-production-domain.com
```

---

## 📋 使用示例

### 1. 在组件中使用API

```typescript
import { login, register, sendCode } from '@/api/user'

// 登录
const userInfo = await login({ id: '1234567890', password: '123456' })

// 注册
await register({
    id: '1234567890',
    username: '张三',
    password: '123456',
    email: 'zhangsan@example.com',
    code: '123456'
})

// 发送验证码
await sendCode({ username: '张三', email: 'zhangsan@example.com' })
```

### 2. 直接使用request工具

```typescript
import { post, get } from '@/utils/request'

// POST请求
const result = await post<string>('/user/login', { id: '123', password: '456' })

// GET请求
const data = await get<UserVO>('/user/info')
```

---

## 🎯 后端接口对照表

| 功能 | 前端API | 后端接口 | 方法 |
|------|---------|----------|------|
| 登录 | `login()` | `/user/login` | POST |
| 注册 | `register()` | `/user/register` | POST |
| 发送验证码 | `sendCode()` | `/user/send-code` | POST |

---

## 🔒 Token管理

### 存储位置
- Token存储在 `localStorage` 中，key为 `token`
- 用户信息存储在 `localStorage` 中，key为 `userInfo`

### 自动携带
- 所有请求会自动在请求头中添加 `Authorization: Bearer {token}`

### 自动清除
- 当收到401未授权响应时，会自动清除token和userInfo，并跳转到登录页

---

## 🚀 启动测试

### 1. 启动后端服务
确保以下服务已启动：
- MySQL数据库
- Nacos配置中心
- Gateway网关服务 (端口10001)
- User服务 (端口10002)

### 2. 启动前端项目
```bash
npm run dev
```

### 3. 测试流程
1. 访问 `http://localhost:5173/register`
2. 填写注册信息
3. 点击"发送验证码"（查看邮箱）
4. 完成注册
5. 跳转到登录页
6. 使用工号和密码登录
7. 登录成功后跳转到首页

---

## ⚠️ 注意事项

1. **CORS跨域问题**：
   - 开发环境中，前端运行在5173端口，后端在10001端口
   - 需要确保后端配置了CORS允许跨域
   - 或者在`vite.config.ts`中配置代理

2. **错误提示优化**：
   - 当前使用原生`alert`进行提示
   - 建议后续集成UI框架（如Element Plus）的消息组件

3. **TypeScript类型**：
   - 所有接口都有完整的TypeScript类型定义
   - 确保类型与后端接口保持一致

4. **密码安全**：
   - 建议密码在前端进行加密后再传输
   - 或使用HTTPS协议

---

## 📝 后续TODO

- [ ] 集成UI框架的消息提示组件（替换alert）
- [ ] 添加请求Loading全局状态管理
- [ ] 实现密码加密传输
- [ ] 添加请求重试机制
- [ ] 优化错误处理和日志记录
- [ ] 添加请求缓存机制
- [ ] 实现Token自动刷新

---

## 👨‍💻 技术栈

- **Vue 3** - 前端框架
- **TypeScript** - 类型系统
- **Axios** - HTTP客户端
- **Vue Router** - 路由管理
- **Vite** - 构建工具

---

创建时间：2025-11-01  
文档版本：v1.0

