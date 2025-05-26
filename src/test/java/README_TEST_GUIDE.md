# Hướng dẫn Unit Test

## Thiết lập

Dự án đã được cấu hình để sử dụng JUnit 5 và Mockito cho việc viết unit test. Các thư viện cần thiết đã được khai báo trong file `pom.xml`:

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>5.11.0</version>
    <scope>test</scope>
</dependency>

<!-- Spring Test -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>6.2.5</version>
    <scope>test</scope>
</dependency>

<!-- Hamcrest -->
<dependency>
    <groupId>org.hamcrest</groupId>
    <artifactId>hamcrest</artifactId>
    <version>2.2</version>
    <scope>test</scope>
</dependency>
```

## Cấu trúc các test

1. **Annotations**:
   - `@ExtendWith(MockitoExtension.class)`: Tích hợp Mockito với JUnit 5
   - `@Mock`: Tạo ra các đối tượng giả lập
   - `@InjectMocks`: Tự động tiêm các mock vào đối tượng cần test
   - `@BeforeEach`: Phương thức chạy trước mỗi test case
   - `@AfterEach`: Phương thức chạy sau mỗi test case
   - `@Test`: Đánh dấu một phương thức là test case
   - `@DisplayName`: Đặt tên hiển thị cho test case

2. **Cấu trúc của mỗi test case**:
   - Chuẩn bị dữ liệu test (Arrange)
   - Thực hiện hành động cần test (Act)
   - Kiểm tra kết quả (Assert)
   - Xác minh các tương tác (Verify)

## Chạy Unit Test

### Chạy từ IDE
Nếu bạn đang sử dụng IntelliJ IDEA hoặc Eclipse:
- Click chuột phải vào file test hoặc package chứa test
- Chọn "Run Tests" hoặc "Run As > JUnit Test"

### Chạy bằng Maven
```bash
mvn test
```

Để chạy một test class cụ thể:
```bash
mvn test -Dtest=UserServiceImplTest
```

## Các kỹ thuật test đã sử dụng

1. **Mocking**:
   - Sử dụng `@Mock` để tạo ra các đối tượng giả lập
   - `when(...).thenReturn(...)` để định nghĩa hành vi cho các mock
   - `verify(...)` để kiểm tra xem các phương thức đã được gọi hay chưa

2. **Assertion**:
   - `assertEquals()`: Kiểm tra giá trị bằng nhau
   - `assertTrue()`, `assertFalse()`: Kiểm tra giá trị boolean
   - `assertNotNull()`, `assertNull()`: Kiểm tra null
   - `assertThrows()`: Kiểm tra ngoại lệ

## Lưu ý

1. Luôn nhớ reset các mock sau mỗi test bằng cách sử dụng `MockitoAnnotations.openMocks(this)` và đóng nó trong `@AfterEach`.

2. Mỗi unit test nên tập trung vào một chức năng cụ thể và không phụ thuộc vào kết quả của các test khác.

3. Dùng `any()`, `anyString()`, `anyInt()`,... để linh hoạt hơn trong việc matching tham số khi làm việc với mock.

4. Để test các ngoại lệ, sử dụng `assertThrows()` thay vì try-catch.

5. Đặt tên test case có ý nghĩa để dễ hiểu khi test fail.
