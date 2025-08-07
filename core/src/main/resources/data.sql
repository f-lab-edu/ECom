-- 기본 역할 데이터 삽입
INSERT INTO role (description, created_at, updated_at) VALUES 
('ROLE_USER', NOW(), NOW()),
('ROLE_ADMIN', NOW(), NOW()),
('ROLE_SUPER_ADMIN', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 기본 권한 데이터 삽입
INSERT INTO permission (description, created_at, updated_at) VALUES 
('READ_PRODUCT', NOW(), NOW()),
('CREATE_PRODUCT', NOW(), NOW()),
('UPDATE_PRODUCT', NOW(), NOW()),
('DELETE_PRODUCT', NOW(), NOW()),
('MANAGE_ORDERS', NOW(), NOW()),
('MANAGE_USERS', NOW(), NOW()),
('MANAGE_ADMINS', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 역할-권한 매핑
-- USER 역할: 상품 조회만 가능
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM role r, permission p
WHERE r.description = 'ROLE_USER' AND p.description = 'READ_PRODUCT'
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- ADMIN 역할: 상품 관리, 주문 관리
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM role r, permission p
WHERE r.description = 'ROLE_ADMIN' AND p.description IN ('READ_PRODUCT', 'CREATE_PRODUCT', 'UPDATE_PRODUCT', 'DELETE_PRODUCT', 'MANAGE_ORDERS')
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- SUPER_ADMIN 역할: 모든 권한
INSERT INTO role_permission (role_id, permission_id, created_at, updated_at)
SELECT r.id, p.id, NOW(), NOW()
FROM role r, permission p
WHERE r.description = 'ROLE_SUPER_ADMIN'
ON DUPLICATE KEY UPDATE updated_at = NOW();