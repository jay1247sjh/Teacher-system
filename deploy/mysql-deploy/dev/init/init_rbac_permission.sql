-- ====================================
-- RBAC 权限系统完整初始化脚本
-- 基于权限标识的权限控制，不依赖角色判断
-- ====================================

-- 1. 初始化角色表（role）
-- ====================================
INSERT INTO role (id, role_name, role_description) VALUES 
(1, '超级管理员', '拥有系统所有权限'),
(2, '管理员', '拥有管理权限，可以管理数据和设置分数'),
(3, '普通成员', '只能查看和编辑自己的数据')
ON DUPLICATE KEY UPDATE 
    role_name = VALUES(role_name),
    role_description = VALUES(role_description);

SELECT '=== 1. 角色表初始化完成 ===' AS '';

-- 2. 初始化权限表（permission）
-- ====================================

-- 2.1 目录级权限（DIR）
INSERT INTO permission (id, parent_id, permission_name, permission_description, type, path, redirect, component, perms) VALUES 
(1, 0, '管理员管理', '管理员相关功能目录', 'DIR', '/admin', NULL, NULL, NULL),
(2, 0, '表格列表', '表格列表功能目录', 'DIR', '/table', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE 
    permission_name = VALUES(permission_name),
    permission_description = VALUES(permission_description);

-- 2.2 菜单级权限（MENU）
INSERT INTO permission (id, parent_id, permission_name, permission_description, type, path, redirect, component, perms) VALUES 
(10, 1, '表格管理', '表格管理菜单', 'MENU', '/admin/table-management', NULL, 'table-management', NULL),
(11, 1, '账号管理', '账号管理菜单', 'MENU', '/admin/account-management', NULL, 'account-management', NULL),
(20, 2, '表格列表', '查看表格列表', 'MENU', '/table/list', NULL, 'table-list', NULL),
(21, 2, '表格详情', '查看表格详情', 'MENU', '/table/:id', NULL, 'table-detail', NULL)
ON DUPLICATE KEY UPDATE 
    permission_name = VALUES(permission_name),
    permission_description = VALUES(permission_description);

-- 2.3 按钮级权限（BUTTON）- 核心权限标识
INSERT INTO permission (id, parent_id, permission_name, permission_description, type, path, redirect, component, perms) VALUES 
-- 表格管理权限
(100, 10, '创建表格', '创建新表格的权限', 'BUTTON', NULL, NULL, NULL, 'table:create'),
(101, 10, '编辑表格', '编辑表格结构的权限', 'BUTTON', NULL, NULL, NULL, 'table:edit'),
(102, 10, '删除表格', '删除表格的权限', 'BUTTON', NULL, NULL, NULL, 'table:delete'),

-- 表格数据权限
(110, 21, '查看数据', '查看表格数据的权限', 'BUTTON', NULL, NULL, NULL, 'table:data:view'),
(111, 21, '添加数据', '添加表格数据的权限', 'BUTTON', NULL, NULL, NULL, 'table:data:add'),
(112, 21, '编辑数据', '编辑表格数据的权限', 'BUTTON', NULL, NULL, NULL, 'table:data:edit'),
(113, 21, '删除数据', '删除表格数据的权限', 'BUTTON', NULL, NULL, NULL, 'table:data:delete'),
(114, 21, '设置分数', '设置数据分数的权限（关键权限）', 'BUTTON', NULL, NULL, NULL, 'table:data:score'),
(115, 21, '编辑管理员字段', '编辑管理员专属字段的权限', 'BUTTON', NULL, NULL, NULL, 'table:data:admin-field'),

-- 用户管理权限
(120, 11, '查看用户', '查看用户列表的权限', 'BUTTON', NULL, NULL, NULL, 'user:view'),
(121, 11, '创建用户', '创建新用户的权限', 'BUTTON', NULL, NULL, NULL, 'user:create'),
(122, 11, '编辑用户', '编辑用户信息的权限', 'BUTTON', NULL, NULL, NULL, 'user:edit'),
(123, 11, '删除用户', '删除用户的权限', 'BUTTON', NULL, NULL, NULL, 'user:delete')
ON DUPLICATE KEY UPDATE 
    permission_name = VALUES(permission_name),
    permission_description = VALUES(permission_description);

-- 2.4 API级权限（API）
INSERT INTO permission (id, parent_id, permission_name, permission_description, type, path, redirect, component, perms) VALUES 
(200, 0, '表格API', '表格相关API权限', 'API', '/api/table/**', NULL, NULL, 'api:table'),
(201, 0, '用户API', '用户相关API权限', 'API', '/api/user/**', NULL, NULL, 'api:user')
ON DUPLICATE KEY UPDATE 
    permission_name = VALUES(permission_name),
    permission_description = VALUES(permission_description);

SELECT '=== 2. 权限表初始化完成 ===' AS '';

-- 3. 初始化角色权限关系（role_permission）
-- ====================================

-- 3.1 超级管理员（roleId=1）拥有所有权限
INSERT INTO role_permission (role_id, permission_id) VALUES
-- 目录权限
(1, 1), (1, 2),
-- 菜单权限
(1, 10), (1, 11), (1, 20), (1, 21),
-- 表格管理权限
(1, 100), (1, 101), (1, 102),
-- 表格数据权限
(1, 110), (1, 111), (1, 112), (1, 113), (1, 114), (1, 115),
-- 用户管理权限
(1, 120), (1, 121), (1, 122), (1, 123),
-- API权限
(1, 200), (1, 201)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 3.2 管理员（roleId=2）拥有除"创建表格"外的所有权限
INSERT INTO role_permission (role_id, permission_id) VALUES
-- 目录权限
(2, 1), (2, 2),
-- 菜单权限
(2, 10), (2, 11), (2, 20), (2, 21),
-- 表格管理权限（没有创建表格权限）
(2, 101), (2, 102),
-- 表格数据权限（包括设置分数和编辑管理员字段）
(2, 110), (2, 111), (2, 112), (2, 113), (2, 114), (2, 115),
-- 用户管理权限
(2, 120), (2, 121), (2, 122), (2, 123),
-- API权限
(2, 200), (2, 201)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 3.3 普通成员（roleId=3）只有基本查看和编辑自己数据的权限
INSERT INTO role_permission (role_id, permission_id) VALUES
-- 目录权限
(3, 2),
-- 菜单权限
(3, 20), (3, 21),
-- 表格数据权限（仅基本操作，没有设置分数和编辑管理员字段）
(3, 110), (3, 111), (3, 112), (3, 113),
-- API权限
(3, 200)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

SELECT '=== 3. 角色权限关系初始化完成 ===' AS '';

-- 4. 为您的账号分配超级管理员角色
-- ====================================
INSERT INTO user_role (user_id, role_id) VALUES ('2205310510', 1)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

SELECT '=== 4. 用户角色分配完成 ===' AS '';

-- ====================================
-- 验证和统计
-- ====================================

-- 角色权限统计
SELECT '=== 角色权限统计 ===' AS '';
SELECT 
    r.id AS '角色ID',
    r.role_name AS '角色名称',
    COUNT(rp.permission_id) AS '权限数量'
FROM role r
LEFT JOIN role_permission rp ON r.id = rp.role_id
GROUP BY r.id, r.role_name
ORDER BY r.id;

-- 关键权限标识列表
SELECT '=== 关键权限标识（BUTTON类型） ===' AS '';
SELECT 
    id AS '权限ID',
    permission_name AS '权限名称',
    perms AS '权限标识',
    permission_description AS '说明'
FROM permission 
WHERE type = 'BUTTON' AND perms IS NOT NULL
ORDER BY id;

-- 用户权限查询示例
SELECT '=== 用户2205310510的权限标识列表 ===' AS '';
SELECT DISTINCT p.perms AS '权限标识'
FROM user_role ur
JOIN role_permission rp ON ur.role_id = rp.role_id
JOIN permission p ON rp.permission_id = p.id
WHERE ur.user_id = '2205310510'
AND p.perms IS NOT NULL
ORDER BY p.perms;

-- ====================================
-- 完成提示
-- ====================================
SELECT '=== ✅ RBAC权限系统初始化完成 ===' AS '';
SELECT '前端权限判断示例：' AS '';
SELECT 'hasPermission("table:data:score") - 判断是否可以设置分数' AS '';
SELECT 'hasPermission("table:data:admin-field") - 判断是否可以编辑管理员字段' AS '';
SELECT 'hasPermission("table:create") - 判断是否可以创建表格' AS '';

