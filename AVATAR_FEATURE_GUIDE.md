# 用户头像功能指南

## 功能说明
- 上传头像文件（JPG、PNG、GIF、WEBP，最大10MB）
- 设置头像URL（外部图片链接）
- 删除头像
- 默认头像（用户名首字母）

---

## 数据库初始化

执行SQL文件：`deploy/mysql-deploy/dev/init/user_avatar.sql`

---

## API接口

### 获取当前用户头像
```
GET /api/v1/user/avatar
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "userId": "2205310510",
    "avatarPath": "/users/2205310510/avatar.jpg",
    "avatarUrl": "/api/v1/attachments/users/2205310510/avatar.jpg"
  }
}
```

### 获取指定用户头像
```
GET /api/v1/user/avatar/{userId}
```

### 上传头像文件
```
POST /api/v1/user/avatar/upload
Content-Type: multipart/form-data

file: [图片文件]
```

**响应**：
```json
{
  "code": 200,
  "data": {
    "userId": "2205310510",
    "avatarPath": "/users/2205310510/avatar.jpg",
    "avatarUrl": "/api/v1/attachments/users/2205310510/avatar.jpg"
  }
}
```

### 设置头像URL
```
POST /api/v1/user/avatar/url
Content-Type: application/json

{
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

### 删除头像
```
DELETE /api/v1/user/avatar
```

## 错误码（10008-10013）

- 10008: 文件为空
- 10009: 文件大小超过10MB
- 10010: 文件格式不支持
- 10011: 头像URL为空
- 10012: 头像URL格式错误
- 10013: 文件上传失败

---

## 快速开始

1. **执行SQL**：`deploy/mysql-deploy/dev/init/user_avatar.sql`
2. **后端配置**：`config/dev.env` 中 `ATTACHMENT_BASE_PATH=E:/JavaProgram/teacher-system/attachments`
3. **Gateway路由**：Nacos中添加 `/api/v1/attachments/**` 路由
4. **前端配置**：`.env.development` 中 `VITE_ATTACHMENT_BASE_URL=http://localhost:10001/api/v1/attachments/`
5. **重启服务**
6. **测试**：登录后点击左下角头像上传

