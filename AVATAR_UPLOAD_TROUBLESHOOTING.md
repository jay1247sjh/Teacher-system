# 头像上传失败排查指南

## 🔍 快速诊断清单

### 1. 检查前端请求
打开浏览器开发者工具（F12）→ Network 标签页，查看上传请求：

**请求URL应该是**：
```
POST http://localhost:10001/api/v1/user/avatar/upload
```

**检查项**：
- [ ] 请求状态码（200表示成功）
- [ ] 请求头是否包含 `Authorization: Bearer xxx`
- [ ] Content-Type 是否为 `multipart/form-data`
- [ ] 文件是否正确附加在请求中

### 2. 检查后端日志
查看User服务的日志，搜索关键字：

```bash
# 查看上传日志
grep "上传头像" logs/application.log

# 查看错误日志
grep "ERROR" logs/application.log | grep "头像"
```

**常见错误日志**：
- `文件不能为空` → 错误码 10008
- `文件大小不能超过10MB` → 错误码 10009
- `不支持的文件格式` → 错误码 10010
- `文件上传失败` → 错误码 10013

### 3. 检查数据库表
确认 `user_avatar` 表已创建：

```sql
SHOW TABLES LIKE 'user_avatar';
DESC user_avatar;
```

### 4. 检查环境变量
确认 `config/dev.env` 中配置了：

```bash
ATTACHMENT_BASE_PATH=E:/JavaProgram/teacher-system/attachments
```

### 5. 检查目录权限
```bash
# Windows PowerShell
Get-Acl E:\JavaProgram\teacher-system\attachments | Format-List

# 确保应用有写权限
```

---

## 🐛 常见错误及解决方案

### 错误1：404 Not Found

**症状**：
```
POST http://localhost:10001/api/v1/user/avatar/upload
Status: 404 Not Found
```

**原因**：
1. Gateway路由未配置
2. User服务未启动
3. URL路径错误

**解决方案**：

**检查Gateway路由**（Nacos）：
```yaml
# teacher-system-gateway.yaml
spring:
  cloud:
    gateway:
      routes:
        - id: login-service
          uri: lb://teacher-system-login
          predicates:
            - Path=/api/v1/user/**
```

**检查服务注册**：
访问 Nacos 控制台：`http://192.168.91.128:8848/nacos`
查看 `teacher-system-login` 服务是否在线

---

### 错误2：401 Unauthorized

**症状**：
```
Status: 401 Unauthorized
```

**原因**：
- Token过期
- Token无效
- 未登录

**解决方案**：
1. 重新登录获取新token
2. 检查localStorage中的token是否存在
3. 检查请求头是否正确携带token

---

### 错误3：413 Request Entity Too Large

**症状**：
```
Status: 413 Request Entity Too Large
```

**原因**：
- 文件大小超过10MB
- Nginx/Gateway限制了请求体大小

**解决方案**：

**前端验证**（已实现）：
```typescript
if (file.size > 10 * 1024 * 1024) {
  ElMessage.error('文件大小不能超过 10MB！')
  return false
}
```

**如果需要调整限制**，修改Gateway配置：
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

---

### 错误4：500 Internal Server Error

**症状**：
```
Status: 500 Internal Server Error
{
  "code": 10013,
  "message": "文件上传失败"
}
```

**原因**：
- 目录不存在或无写权限
- 磁盘空间不足
- 文件系统IO异常

**解决方案**：

**1. 检查目录**：
```powershell
# 检查目录是否存在
Test-Path E:\JavaProgram\teacher-system\attachments

# 创建目录
New-Item -ItemType Directory -Path "E:\JavaProgram\teacher-system\attachments\users" -Force
```

**2. 检查磁盘空间**：
```powershell
Get-PSDrive E | Select-Object Used, Free
```

**3. 查看详细错误**：
查看后端日志中的完整堆栈信息

---

### 错误5：跨域问题（CORS）

**症状**：
```
Access to XMLHttpRequest at 'http://localhost:10001/api/v1/user/avatar/upload' 
from origin 'http://localhost:5173' has been blocked by CORS policy
```

**原因**：
- Gateway未配置CORS
- 前端代理配置错误

**解决方案**：

**检查前端代理**（`vite.config.ts`）：
```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:10001',
      changeOrigin: true
    }
  }
}
```

**或配置Gateway CORS**：
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods: "*"
            allowedHeaders: "*"
```

---

### 错误6：文件格式不支持

**症状**：
```
{
  "code": 10010,
  "message": "不支持的文件格式，仅支持: jpg, jpeg, png, gif, webp"
}
```

**原因**：
- 文件扩展名不在允许列表中

**解决方案**：
1. 转换图片格式
2. 使用支持的格式：JPG、PNG、GIF、WEBP

---

### 错误7：用户未登录

**症状**：
```
UserContext.getUserId() 返回 null
```

**原因**：
- JWT拦截器未正确设置UserContext
- Token解析失败

**解决方案**：
检查 `JwtAuthenticationInterceptor` 是否正确设置了UserContext

---

## 🔧 调试步骤

### 步骤1：测试文件上传（Postman/Apifox）

```
POST http://localhost:10002/user/avatar/upload
Authorization: Bearer YOUR_TOKEN
Content-Type: multipart/form-data

Body:
  file: [选择文件]
```

**直接访问User服务（绕过Gateway）测试**

### 步骤2：查看完整请求响应

```javascript
// 浏览器Console
fetch('http://localhost:10001/api/v1/user/avatar/upload', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + localStorage.getItem('teacher-system-user')
  },
  body: formData
}).then(res => res.json()).then(console.log)
```

### 步骤3：检查Service层日志

在 `UserAvatarServiceImpl.java` 中添加更多日志：

```java
@Override
public UserAvatarVO uploadAvatarFile(String userId, MultipartFile file) {
    log.info("开始上传头像: userId={}, filename={}, size={}", 
        userId, file.getOriginalFilename(), file.getSize());
    
    validateFile(file);
    log.info("文件验证通过");
    
    // ... 其他代码
}
```

---

## ✅ 验证清单

上传失败时，按顺序检查：

1. [ ] 前端请求URL正确（包含 `/api/v1` 前缀）
2. [ ] Token有效且正确携带
3. [ ] 文件大小 < 10MB
4. [ ] 文件格式为 JPG/PNG/GIF/WEBP
5. [ ] Gateway路由已配置
6. [ ] User服务正常运行
7. [ ] 数据库表 `user_avatar` 已创建
8. [ ] 环境变量 `ATTACHMENT_BASE_PATH` 已配置
9. [ ] 目录 `E:\JavaProgram\teacher-system\attachments` 存在且有写权限
10. [ ] 后端日志无ERROR

---

## 📝 收集诊断信息

如果问题仍未解决，请提供以下信息：

1. **前端错误信息**：
   - 浏览器Console的错误
   - Network面板的请求详情
   - 响应状态码和响应体

2. **后端日志**：
   ```bash
   # 最近的错误日志
   tail -100 logs/application.log | grep -i error
   ```

3. **配置信息**：
   - `config/dev.env` 内容
   - Gateway路由配置（Nacos）
   - 前端 `.env.development` 内容

4. **环境信息**：
   - 操作系统版本
   - Java版本
   - Node.js版本
   - 浏览器版本

---

## 🚀 快速修复命令

```powershell
# 1. 确保目录存在
New-Item -ItemType Directory -Path "E:\JavaProgram\teacher-system\attachments\users" -Force

# 2. 检查配置
Get-Content config\dev.env | Select-String "ATTACHMENT"

# 3. 重启User服务
# 在IDEA中重启 UserApplication

# 4. 清除浏览器缓存并重新登录
```

---

## 💡 提示

- 优先检查浏览器Network面板的请求详情
- 查看后端日志是最直接的排查方式
- 使用Postman直接测试可以排除前端问题
- 确保所有服务都已重启以加载新配置

