# 快速配置指南

## 📦 环境变量配置

### 1. 创建 .env.development 文件

在 `teacher-system-web` 目录下创建 `.env.development` 文件：

```bash
# .env.development

# 附件服务器地址（用于存放用户头像等附件）
VITE_ATTACHMENT_BASE_URL=http://localhost:8080/attachments/
```

### 2. 如果需要生产环境配置

创建 `.env.production` 文件：

```bash
# .env.production

# 附件服务器地址
VITE_ATTACHMENT_BASE_URL=https://your-domain.com/attachments/
```

---

## 🚀 启动项目

### 1. 安装依赖（如果还没安装）

```bash
cd teacher-system-web
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

---

## ✅ 验证功能

### 1. 登录后查看左下角

应该能看到：
- 用户头像（或用户名首字母）
- 用户名
- 工号
- 退出登录按钮

### 2. 测试退出登录

- 点击"退出登录"按钮
- 确认退出
- 应该跳转到登录页

### 3. 测试头像显示

在浏览器控制台查看：

```javascript
const userStore = useUserStore()
console.log('头像URL:', userStore.userInfo?.avatar)
```

---

## 📝 注意事项

1. **环境变量文件不要提交到Git**
   - `.env.development` 和 `.env.production` 应该添加到 `.gitignore`
   - 提供 `env.development.example` 作为模板

2. **修改环境变量后需要重启**
   - 修改 `.env` 文件后需要重启开发服务器（Ctrl+C 然后重新 `npm run dev`）

3. **头像URL格式**
   - 完整URL：`https://example.com/avatar.jpg`
   - 相对路径：`/users/123/avatar.jpg` （会自动拼接 VITE_ATTACHMENT_BASE_URL）
   - 空值：显示用户名首字母

---

## 🎉 完成

现在您可以正常使用退出登录和用户头像功能了！

