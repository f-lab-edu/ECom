# RBAC (Role-Based Access Control) 구현 가이드

## 🎯 개요

이 프로젝트에서 RBAC(Role-Based Access Control)가 완전히 활성화되었습니다. 사용자와 관리자는 역할에 따라 시스템의 다양한 기능에 접근할 수 있습니다.

## 🔐 역할 및 권한 체계

### 기본 역할 (Roles)

| 역할 | 설명 | 접근 권한 |
|------|------|----------|
| `ROLE_USER` | 일반 사용자 | 상품 조회, 장바구니 관리, 주문 관리, 배송지 관리 |
| `ROLE_ADMIN` | 관리자 | 상품 관리, 주문 관리 + 일반 사용자 권한 |
| `ROLE_SUPER_ADMIN` | 슈퍼 관리자 | 관리자 계정 생성 + 모든 권한 |

### 세부 권한 (Permissions)

| 권한 | 설명 |
|------|------|
| `READ_PRODUCT` | 상품 조회 |
| `CREATE_PRODUCT` | 상품 생성 |
| `UPDATE_PRODUCT` | 상품 수정 |
| `DELETE_PRODUCT` | 상품 삭제 |
| `MANAGE_ORDERS` | 주문 관리 |
| `MANAGE_USERS` | 사용자 관리 |
| `MANAGE_ADMINS` | 관리자 관리 |

## 🏗️ 구현 구조

### 1. 도메인 모델

```
User ←→ UserRole ←→ Role ←→ RolePermission ←→ Permission
Admin ←→ AdminRole ←→ Role ←→ RolePermission ←→ Permission
```

- **User/Admin**: 사용자 및 관리자 엔티티
- **UserRole/AdminRole**: 사용자-역할 매핑 테이블
- **Role**: 역할 엔티티
- **RolePermission**: 역할-권한 매핑 테이블
- **Permission**: 권한 엔티티

### 2. JWT 토큰 기반 인증

JWT 토큰에 사용자 ID와 역할 정보가 포함됩니다:

```json
{
  "userId": 123,
  "roles": ["ROLE_USER"],
  "type": "access"
}
```

### 3. Spring Security 설정

- **Method Level Security**: `@PreAuthorize` 어노테이션 활성화
- **URL Level Security**: 경로별 접근 제어
- **JWT Filter**: 토큰 기반 인증 처리

## 📍 API 엔드포인트 보안

### 공개 접근 (인증 불필요)

- `POST /api/v1/auth/signup` - 회원가입
- `POST /api/v1/auth/login` - 로그인
- `POST /api/v1/auth/refresh` - 토큰 갱신
- `GET /api/v1/products` - 상품 목록 조회
- `GET /api/v1/products/{id}` - 상품 상세 조회

### 사용자 권한 필요 (`ROLE_USER`)

- `GET /api/v1/cart` - 장바구니 조회
- `POST /api/v1/cart/products` - 장바구니 상품 추가
- `PUT /api/v1/cart/products/{productId}` - 장바구니 상품 수량 변경
- `DELETE /api/v1/cart/products/{productId}` - 장바구니 상품 삭제
- `POST /api/v1/order/product` - 상품 주문
- `GET /api/v1/order` - 주문 목록 조회
- `GET /api/v1/order/{orderId}` - 주문 상세 조회
- `GET /api/v1/shipping-address` - 배송지 목록 조회
- `POST /api/v1/shipping-address` - 배송지 생성
- `PUT /api/v1/shipping-address/{addressId}` - 배송지 수정

### 관리자 권한 필요 (`ROLE_ADMIN` 또는 `ROLE_SUPER_ADMIN`)

- `POST /api/v1/products/image` - 상품 이미지 업로드
- `POST /api/v1/products` - 상품 생성
- `PUT /api/v1/products/{productId}` - 상품 수정
- `DELETE /api/v1/products/{productId}` - 상품 삭제

### 슈퍼 관리자 권한 필요 (`ROLE_SUPER_ADMIN`)

- `POST /admin/v1/auth/admins` - 관리자 계정 생성

## 🛠️ 개발자 가이드

### 1. 권한 체크 어노테이션 사용

```java
@PreAuthorize("hasRole('USER')")
public void userOnlyMethod() { }

@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public void adminMethod() { }

@PreAuthorize("hasRole('SUPER_ADMIN')")
public void superAdminMethod() { }
```

### 2. SecurityUtil 유틸리티 클래스

```java
// 현재 사용자 ID 조회
Long userId = SecurityUtil.getCurrentUserId();

// 역할 체크
boolean isAdmin = SecurityUtil.isAdmin();
boolean isSuperAdmin = SecurityUtil.isSuperAdmin();

// 특정 역할 체크
boolean hasRole = SecurityUtil.hasRole("ROLE_USER");
boolean hasAnyRole = SecurityUtil.hasAnyRole("ROLE_ADMIN", "ROLE_SUPER_ADMIN");

// 리소스 소유자 체크
boolean isOwner = SecurityUtil.isResourceOwner(resourceOwnerId);
```

### 3. 사용자 역할 관리

새 사용자 가입 시 자동으로 `ROLE_USER` 역할이 부여됩니다:

```java
// AuthService.signup() 메소드에서 자동 처리
Role userRole = roleApiRepository.findByDescription("ROLE_USER")
    .orElseThrow(() -> new BadRequestException("Role not found"));
UserRole userRoleMapping = new UserRole(user, userRole);
userRoleApiRepository.save(userRoleMapping);
```

## 🗄️ 데이터베이스 초기화

`core/src/main/resources/data.sql` 파일을 통해 기본 역할과 권한이 자동으로 생성됩니다:

```sql
-- 기본 역할 생성
INSERT INTO role (description) VALUES 
('ROLE_USER'), ('ROLE_ADMIN'), ('ROLE_SUPER_ADMIN');

-- 기본 권한 생성
INSERT INTO permission (description) VALUES 
('READ_PRODUCT'), ('CREATE_PRODUCT'), ('UPDATE_PRODUCT'), 
('DELETE_PRODUCT'), ('MANAGE_ORDERS'), ('MANAGE_USERS'), ('MANAGE_ADMINS');

-- 역할-권한 매핑 설정
-- (자세한 내용은 data.sql 파일 참조)
```

## 🚀 배포 시 고려사항

### 1. CORS 설정

개발 환경에서는 모든 origin을 허용하지만, 운영 환경에서는 반드시 특정 도메인으로 제한해야 합니다:

```java
// SecurityConfig.java에서 수정 필요
config.setAllowedOriginPatterns(List.of("https://yourdomain.com"));
```

### 2. JWT 시크릿 키 관리

JWT 시크릿 키는 환경변수나 외부 설정 파일로 관리해야 합니다.

### 3. 슈퍼 관리자 계정 생성

시스템 초기 설정 시 슈퍼 관리자 계정을 수동으로 생성해야 합니다.

## 🔧 테스트

권한 체크가 올바르게 작동하는지 확인하려면:

1. 인증 없이 보호된 리소스 접근 시 401 Unauthorized 응답
2. 권한 없는 사용자가 관리자 기능 접근 시 403 Forbidden 응답
3. 올바른 권한을 가진 사용자는 정상 접근 가능

## 📝 주요 변경사항

1. ✅ SecurityConfig에서 실제 권한 체크 활성화
2. ✅ 메소드 레벨 보안 활성화 (`@EnableGlobalMethodSecurity`)
3. ✅ 모든 컨트롤러에 적절한 `@PreAuthorize` 어노테이션 적용
4. ✅ User-Role 매핑 시스템 구현
5. ✅ 사용자 가입 시 기본 역할 자동 부여
6. ✅ SecurityUtil 유틸리티 클래스 제공
7. ✅ 데이터베이스 초기 데이터 설정
8. ✅ CORS 설정 개선

이제 RBAC 시스템이 완전히 활성화되어 역할 기반의 세분화된 접근 제어가 가능합니다! 🎉