# MusicApplication-SE114 (Back End)

Đây là project back end cho ứng dụng quản lý nhạc, cung cấp các API cho các chức năng: playlist, album, nghệ sĩ, bài hát, yêu thích, tải về, theo dõi nghệ sĩ, v.v.

---

## 🚀 Yêu cầu hệ thống
- Java 17+
- Maven 3.6+
- PostgreSQL (hoặc cloud PostgreSQL)
- Redis (dùng cho cache)
- Tài khoản Cloudinary (upload ảnh)
- Tài khoản Gmail (gửi mail xác thực)
- Internet để kết nối các dịch vụ cloud

---

## ⚙️ Cấu hình môi trường

**Lưu ý:**  
File cấu hình môi trường `env.properties` **KHÔNG được cung cấp trong source code** (bạn cần tự tạo file này).

**Vị trí:**  
`src/main/resources/env.properties`

**Ví dụ nội dung file:**
```properties
PORT=8080
CLOUDINARY_URL=cloudinary://<cloudinary_key>:<cloudinary_secret>@<cloudinary_cloud>
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
redis_host=your_redis_host
redis_port=your_redis_port
redis_password=your_redis_password
redis_ttl=10800000
db.url=jdbc:postgresql://<host>:<port>/<db>?sslmode=require
db.username=your_db_user
db.password=your_db_password
jwt.secret=your_jwt_secret
jwt.access-expiration=3600000
jwt.refresh-expiration=604800000
```
> **Bạn cần tự tạo và điền thông tin phù hợp vào file này trước khi chạy project.**

---

## 🛠️ Hướng dẫn chạy project

### 1. Clone source code
```bash
git clone https://github.com/minh10m/MusicApplication-SE114-BE.git
cd MusicApplication-SE114-BE/code/be
```

### 2. Tạo file cấu hình môi trường
- Tạo file `src/main/resources/env.properties`
- Điền đầy đủ thông tin cấu hình như hướng dẫn ở trên

### 3. Cài đặt các thư viện
```bash
mvn clean install
```

### 4. Chạy ứng dụng
```bash
mvn spring-boot:run
```
Hoặc:
```bash
java -jar target/be-*.jar
```

### 5. Truy cập API
- Mặc định API chạy ở port **8080** (có thể đổi trong `env.properties`)
- Swagger UI:  
  `http://localhost:8080/swagger-ui/index.html`

---

## 📝 Một số lưu ý
- **Database:** Tạo database PostgreSQL trước khi chạy, hoặc sử dụng cloud PostgreSQL.
- **Redis:** Dùng cho cache, có thể dùng Redis cloud hoặc local.
- **Cloudinary:** Dùng để upload ảnh nhạc, playlist, album, avatar nghệ sĩ...
- **Mail:** Dùng để gửi mail xác thực, quên mật khẩu...
- **JWT:** Đã cấu hình sẵn trong env.properties.
- **File env.properties không được commit lên git.**

---

## 🧑‍💻 Đóng góp
- Fork và tạo pull request nếu muốn đóng góp thêm tính năng hoặc sửa lỗi.
- Mọi ý kiến đóng góp vui lòng gửi về email hoặc tạo issue trên Github.

---

**Chúc bạn sử dụng project hiệu quả!**  
Nếu gặp vấn đề khi chạy, hãy kiểm tra lại file `env.properties` hoặc liên hệ với người phát triển.