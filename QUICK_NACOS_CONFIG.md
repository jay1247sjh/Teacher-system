# 🚀 Nacos Gateway 快速配置

## 📋 需要在 Nacos 中添加的配置

### 1. 登录信息

- **URL**: `http://192.168.91.128:8848/nacos`
- **用户名**: `ROLE_DEV`
- **密码**: `123456`
- **命名空间**: `dcfacc8f-1b09-4ae1-ab96-4ba45c0ee54e`

### 2. 找到配置

- **Data ID**: `teacher-system-gateway.yaml`
- **Group**: `DEFAULT_GROUP`

### 3. 添加附件路由

在现有的 `routes` 列表中添加以下配置：

```yaml
        # 附件路由 - 转发到 User 服务处理静态资源
        - id: attachment-route
          uri: lb://teacher-system-login
          predicates:
            - Path=/api/v1/attachments/**
```

### 4. 完整示例

```yaml
spring:
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true

      default-filters:
        - RewritePath=/api/v1/(?<remaining>.*), /${remaining}

      routes:
        - id: login-service
          uri: lb://teacher-system-login
          predicates:
            - Path=/api/v1/user/**

        - id: table-service
          uri: lb://teacher-system-table
          predicates:
            - Path=/api/v1/table/**

        # 新增：附件路由
        - id: attachment-route
          uri: lb://teacher-system-login
          predicates:
            - Path=/api/v1/attachments/**
```

### 5. 发布配置

点击"发布"按钮保存配置。

### 6. 重启 Gateway

建议重启 Gateway 服务以确保配置生效：

```bash
# 如果使用 Docker
docker restart teacher-system-gateway

# 或者在 IDEA 中重启服务
```

## ✅ 验证配置

### 测试 URL

```
http://localhost:10001/api/v1/attachments/users/2205310510/avatar.jpg
```

如果能看到图片，说明配置成功！🎉

## 📝 相关文档

- `NACOS_GATEWAY_ATTACHMENT_ROUTE.md` - 详细配置说明
- `ATTACHMENT_CONFIG_SUMMARY.md` - 完整配置总结
- `attachments/TEST_AVATAR.md` - 测试步骤

---

**就这么简单！** 只需要在 Nacos 中添加 3 行配置即可。✨

