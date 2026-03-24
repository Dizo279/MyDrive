# MyDrive 🗂️

Ứng dụng quản lý file đám mây cá nhân — clone Google Drive xây dựng bằng **Spring Boot 3** + **Angular 17** + **MySQL 8**.

---

## ✨ Tính năng

### 👤 Người dùng
- Đăng ký / Đăng nhập với JWT
- Remember me (lưu phiên đăng nhập)
- Chỉnh sửa thông tin cá nhân: username, email, mật khẩu (yêu cầu xác nhận mật khẩu hiện tại)

### 📁 Quản lý file
- Upload file (kéo thả hoặc click chọn) — tối đa 100MB
- Xem danh sách file dạng Dashboard: grid gần đây + bảng đầy đủ
- Phân loại tự động: Tài liệu, Hình ảnh, Âm nhạc, Video, Khác
- Tìm kiếm file realtime với highlight từ khớp
- Lọc theo loại, sắp xếp theo tên / ngày / dung lượng
- Xem chi tiết file, Tải xuống, Xóa file
- Hiển thị dung lượng đã dùng / tổng quota (donut chart)

### 🔗 Chia sẻ file
- Chia sẻ file qua **email** người nhận với quyền Chỉ xem hoặc Tải xuống
- Tạo **public link** với quyền và thời hạn tùy chỉnh
- Người nhận nhập link trực tiếp để truy cập không cần đăng nhập
- Thu hồi quyền chia sẻ bất kỳ lúc nào

### 🔔 Thông báo realtime
- Nhận thông báo ngay lập tức khi có file được chia sẻ (Server-Sent Events)
- Panel thông báo với badge số chưa đọc

### 🛡️ Admin Panel
- Xem danh sách toàn bộ user kèm thống kê dung lượng
- Chỉnh sửa quota theo plan (FREE / PRO / ADMIN) hoặc nhập GB tùy chỉnh
- Đổi role USER / ADMIN, Xóa user

---

## 🗂️ Cấu trúc project

```
D:\LTUDM\
├── mydrive-backend/
│   └── src/main/java/com/mydrive/
│       ├── config/           SecurityConfig, AppConfig
│       ├── controller/       REST Controllers
│       ├── dto/              Request & Response DTOs
│       ├── entity/           JPA Entities
│       ├── exception/        Global Exception Handler
│       ├── repository/       Spring Data JPA Repositories
│       ├── security/         JWT Filter, Provider
│       └── service/          Business Logic
│
├── mydrive-frontend/
│   └── src/app/
│       ├── core/             Guards, Interceptors
│       ├── features/
│       │   ├── auth/         Login, Register
│       │   ├── files/        Dashboard, Upload, Detail
│       │   ├── share/        Share List, Share View
│       │   ├── profile/      Profile Page
│       │   └── admin/        Admin Panel
│       └── services/         HTTP Services
│
└── README.md
```

---

## 🚀 Chạy ở môi trường DEV (local)

### Yêu cầu
| Tool | Phiên bản |
|------|-----------|
| Java | 21 (Temurin) |
| Node.js | 20+ |
| Angular CLI | 17 |
| MySQL | 8.0 |
| Maven | 3.8+ |

### Bước 1 — Chuẩn bị database
```sql
CREATE DATABASE mydrive_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'mydrive_user'@'localhost' IDENTIFIED BY 'mydrive123';
GRANT ALL PRIVILEGES ON mydrive_db.* TO 'mydrive_user'@'localhost';
FLUSH PRIVILEGES;
```

### Bước 2 — Chạy backend
```bash
cd D:\LTUDM\mydrive-backend
mvn spring-boot:run
```
> API chạy tại: `http://localhost:8080/api`

### Bước 3 — Chạy frontend
```bash
cd D:\LTUDM\mydrive-frontend
ng serve
```
> Web chạy tại: `http://localhost:4200`

---

## 📡 API Endpoints

### Auth
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/auth/register` | Đăng ký tài khoản |
| POST | `/api/auth/login` | Đăng nhập, trả về JWT |

### Files
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/files` | Danh sách file của user |
| POST | `/api/files/upload` | Upload file mới |
| GET | `/api/files/{id}` | Chi tiết file |
| GET | `/api/files/{id}/download` | Tải xuống file |
| GET | `/api/files/{id}/download-shared` | Tải file được share |
| DELETE | `/api/files/{id}` | Xóa file |

### Quota
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/quota/me` | Xem quota hiện tại |
| PUT | `/api/quota/upgrade?plan=PRO` | Nâng cấp plan |

### Share
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| POST | `/api/share/user` | Chia sẻ qua email |
| POST | `/api/share/public` | Tạo public link |
| GET | `/api/share/my-shares` | File mình đã share |
| GET | `/api/share/with-me` | File được share với mình |
| DELETE | `/api/share/{id}` | Thu hồi share |
| GET | `/api/share/public/{token}` | Xem file qua link |
| GET | `/api/share/public/{token}/download` | Tải file qua link |

### Profile
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/profile` | Xem thông tin cá nhân |
| PUT | `/api/profile` | Cập nhật thông tin |

### Notifications
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/notifications/subscribe` | Đăng ký nhận SSE |

### Admin
| Method | Endpoint | Mô tả |
|--------|----------|-------|
| GET | `/api/admin/users` | Danh sách tất cả user |
| GET | `/api/admin/users/{id}` | Chi tiết user |
| PUT | `/api/admin/users/{id}/quota` | Cập nhật quota |
| PUT | `/api/admin/users/{id}/role` | Đổi role |
| DELETE | `/api/admin/users/{id}` | Xóa user |

---

## 👥 Tài khoản mặc định

| Username | Password | Role | Plan |
|----------|----------|------|------|
| admin | password | ADMIN | ADMIN (100GB) |
| alice | password | USER | FREE (5GB) |
| bob | password | USER | FREE (5GB) |
| charlie | password | USER | FREE (5GB) |

---

## 🏗️ Công nghệ sử dụng

### Backend
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|---------|
| Spring Boot | 3.2.4 | Framework chính |
| Spring Security | 6 | Xác thực & phân quyền |
| Spring Data JPA | | ORM với MySQL |
| JWT (jjwt) | 0.11.5 | Token xác thực |
| Lombok | 1.18.34 | Giảm boilerplate code |
| MySQL | 8.0 | Cơ sở dữ liệu |

### Frontend
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|---------|
| Angular | 17 | Framework chính |
| Angular Material | | UI Components |
| RxJS | | Reactive programming |
| SCSS | | Styling |

---

## 📦 Quota Plans

| Plan | Dung lượng | Mô tả |
|------|-----------|-------|
| FREE | 5 GB | Mặc định khi đăng ký |
| PRO | 10 GB | Nâng cấp bởi Admin |
| ADMIN | 100 GB | Tài khoản quản trị |

---

## 🔒 Bảo mật

- JWT token hết hạn sau **24 giờ**
- Mật khẩu mã hóa bằng **BCrypt**
- Token lưu trong `sessionStorage` (mặc định) hoặc `localStorage` (Remember me)
- Tất cả API đều yêu cầu JWT (trừ auth và public link)
- Admin endpoints yêu cầu role `ADMIN`
- Xác nhận mật khẩu hiện tại khi thay đổi thông tin cá nhân

---

## 📝 Ghi chú phát triển

- Java 25 không tương thích Lombok — dùng **Java 21 Temurin**
- VS Code terminal dùng **CMD profile** để nhận đúng Java 21
- File lưu tại `./uploads/{userId}/{uuid}.ext` trên server
- SSE dùng query param `?token=` vì browser EventSource không hỗ trợ custom header
