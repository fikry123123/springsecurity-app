# Spring Security Application

REST API dengan Spring Boot 3.4.2, Java 17, Spring Security, JWT, PostgreSQL, dan manajemen user/manager.

## Fitur

- Registrasi dan login user
- Access token dan refresh token JWT
- Autentikasi stateless dengan role `ADMIN` dan `USER`
- CRUD user, pencarian, paginasi, dan foto profil
- CRUD manager serta penugasan manager ke user
- Upload dan penyajian file dari folder `uploads/`

## Teknologi

- Java 17
- Spring Boot 3.4.2
- Spring Security
- Spring Data JPA
- PostgreSQL
- JJWT
- Maven

## Persyaratan

- JDK 17+
- PostgreSQL
- Maven Wrapper (disertakan dalam proyek)

## Setup Database

Buat database PostgreSQL:

```sql
CREATE DATABASE spring_security;
```

Konfigurasi default ada di `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_security
spring.datasource.username=postgres
spring.datasource.password=12345678
spring.jpa.hibernate.ddl-auto=update
```

Sesuaikan username dan password PostgreSQL sesuai lingkungan lokal.

## Menjalankan Aplikasi

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Aplikasi tersedia di `http://localhost:8080`.

## Akun Admin Default

Akun admin dibuat otomatis saat pertama kali aplikasi dijalankan:

```text
Email    : admin@example.com
Password : admin
```

Ganti akun default tersebut saat deploy ke produksi.

## Autentikasi

Gunakan JWT pada header untuk endpoint yang dilindungi:

```http
Authorization: Bearer <access-token>
```

### Registrasi

```http
POST /api/v1/auth/signup
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login

```http
POST /api/v1/auth/signin
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Refresh Token

```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "token": "<refresh-token>"
}
```

## Endpoint Utama

| Area | Method | Endpoint | Akses |
|---|---|---|---|
| Auth | POST | `/api/v1/auth/signup` | Publik |
| Auth | POST | `/api/v1/auth/signin` | Publik |
| Auth | POST | `/api/v1/auth/refresh` | Publik |
| User | GET | `/api/v1/user` | `USER` |
| Admin | GET | `/api/v1/admin/users?page=1&size=10` | `ADMIN` |
| Admin | GET | `/api/v1/admin/users/{id}` | `ADMIN` |
| Admin | GET | `/api/v1/admin/users/search?query={kata}` | `ADMIN` |
| Admin | POST | `/api/v1/admin/users` | `ADMIN` |
| Admin | PUT | `/api/v1/admin/users/{id}` | `ADMIN` |
| Admin | DELETE | `/api/v1/admin/users/{id}` | `ADMIN` |
| Admin | PUT | `/api/v1/admin/users/{id}/update-image` | `ADMIN` |
| Admin | PUT | `/api/v1/admin/assign-user` | `ADMIN` |
| Admin | PUT | `/api/v1/admin/unassign` | `ADMIN` |
| Manager | GET/POST | `/api/v1/managers` | Publik |
| Manager | GET/PUT/DELETE | `/api/v1/managers/{id}` | Publik |
| File | GET | `/uploads/{filename}` | Publik |

Contoh body manager:

```json
{
  "department": "Engineering",
  "userId": 1
}
```

## Upload Foto

Gunakan `multipart/form-data` pada endpoint:

- `POST /api/v1/admin/users`
- `PUT /api/v1/admin/users/{id}/update-image`

Hanya format `PNG` dan `JPG` yang diterima. File disimpan di folder `uploads/`.

## Test

Windows:

```powershell
.\mvnw.cmd test
```

Frontend default yang diizinkan CORS adalah `http://localhost:3000`.
