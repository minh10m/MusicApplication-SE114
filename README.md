# MusicApplication-SE114-FE

Đây là project **Front End** của ứng dụng nghe nhạc SE114, sử dụng **Kotlin** với **Jetpack Compose** cho Android.

## Yêu cầu

- **Android Studio** (khuyến nghị bản mới nhất)
- **JDK 17** trở lên
- Máy đã cài đặt **Git** để clone source code
- Đảm bảo backend đã chạy (tham khảo hướng dẫn backend trong repo BE)

## Hướng dẫn cài đặt & chạy

### 1. Clone source code

```bash
git clone https://github.com/minh10m/MusicApplication-SE114-BE.git
cd MusicApplication-SE114-BE
```

### 2. Mở project bằng Android Studio

- Mở Android Studio → **Open** → trỏ tới thư mục `MusicApplication-SE114-BE`
- Chờ Android Studio sync và build project (có thể mất vài phút lần đầu)

### 3. Cấu hình backend API & IP

- Mặc định app sẽ gọi tới backend qua port 8080
- Nếu backend chạy ở địa chỉ khác (ví dụ: IP máy thật, port khác...), hãy sửa lại BASE_URL trong file:
  ```
  app/src/main/java/com/example/musicapplicationse114/di/ApiModule.kt
  ```
  Ví dụ:
  ```kotlin
  private const val BASE_URL = "http://192.168.1.100:8080/"
  ```
  và sửa domain trong file :
  ```
  app/src/main/res/xml/network_security_config.xml
  ```
  Ví dụ:
  ```kotlin
        <domain includeSubdomains="true">192.168.1.100</domain>
  ```

### 4. Chạy ứng dụng

- Cắm thiết bị Android thật hoặc dùng **Android Emulator**
- Nhấn **Run** (Shift + F10 hoặc nút ▶️ trên Android Studio)
- Đăng nhập, đăng ký và trải nghiệm các tính năng

## Một số lưu ý

- Đảm bảo backend đã chạy trước khi đăng nhập hoặc sử dụng các tính năng liên quan đến API.
- Nếu gặp lỗi network, kiểm tra lại địa chỉ BASE_URL và kết nối mạng giữa thiết bị/emulator với backend.
- Nếu cần build lại project, chọn **Build > Rebuild Project** trong Android Studio.

## Tham khảo

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Android Studio User Guide](https://developer.android.com/studio/intro)

---

Nếu có vấn đề khi chạy project, hãy tạo issue hoặc liên hệ trực tiếp với nhóm phát triển.
