package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private Users testUser;
    private AutoCloseable closeable;

    @BeforeEach
    public void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        // Khởi tạo user test
        testUser = new Users();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        testUser.setEmail("test@example.com");
        testUser.setRole("USER");
        testUser.setPhone("0123456789");
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setCreatedAt(new Date());
    }

    @AfterEach
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    @DisplayName("Kiểm tra đăng ký người dùng thành công")
    public void testRegisterUserSuccess() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.addUser(any(Users.class))).thenReturn(true);

        // Thực hiện test
        Users result = userService.registerUser(testUser);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByPhone("0123456789");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).addUser(testUser);
    }

    @Test
    @DisplayName("Kiểm tra đăng ký thất bại - tên đăng nhập đã tồn tại")
    public void testRegisterUserUsernameExists() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Thực hiện test và kiểm tra kết quả
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUser);
        });

        assertEquals("Tên đăng nhập đã tồn tại", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).addUser(any(Users.class));
    }

    @Test
    @DisplayName("Kiểm tra đăng nhập thành công")
    public void testLoginSuccess() {
        // Chuẩn bị dữ liệu test
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        // Thực hiện test
        Optional<Users> result = userService.login("testuser", "password123");

        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("Kiểm tra đăng nhập thất bại - sai mật khẩu")
    public void testLoginWrongPassword() {
        // Chuẩn bị dữ liệu test
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword())).thenReturn(false);

        // Thực hiện test
        Optional<Users> result = userService.login("testuser", "wrongpassword");

        // Kiểm tra kết quả
        assertFalse(result.isPresent());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongpassword", testUser.getPassword());
    }

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin người dùng")
    public void testUpdateProfile() {
        // Chuẩn bị dữ liệu test
        Users updatedUser = new Users();
        updatedUser.setId(1);
        updatedUser.setFullName("Updated Name");
        updatedUser.setEmail("updated@example.com");
        updatedUser.setPhone("0987654321");

        when(userRepository.getUserById(1)).thenReturn(testUser);
        when(userRepository.updateUser(any(Users.class))).thenReturn(true);

        // Thực hiện test
        Users result = userService.updateProfile(updatedUser);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("0987654321", result.getPhone());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(testUser.getUsername(), result.getUsername());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserById(1);
        verify(userRepository).updateUser(updatedUser);
    }

    @Test
    @DisplayName("Kiểm tra đổi mật khẩu thành công")
    public void testChangePasswordSuccess() {
        // Chuẩn bị dữ liệu test
        when(userRepository.getUserById(1)).thenReturn(testUser);
        // Đảm bảo password123 là giá trị hiện tại của password
        testUser.setPassword("password123");
        when(passwordEncoder.matches("oldPassword", "password123")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.updateUser(testUser)).thenReturn(true);

        // Thực hiện test
        boolean result = userService.changePassword(1, "oldPassword", "newPassword");

        // Kiểm tra kết quả
        assertTrue(result);
        assertEquals("encodedNewPassword", testUser.getPassword());
        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserById(1);
        verify(passwordEncoder).matches("oldPassword", "password123");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).updateUser(testUser);
    }

    @Test
    @DisplayName("Kiểm tra đổi mật khẩu thất bại - sai mật khẩu cũ")
    public void testChangePasswordWrongOldPassword() {
        // Chuẩn bị dữ liệu test
        when(userRepository.getUserById(1)).thenReturn(testUser);
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        // Thực hiện test
        boolean result = userService.changePassword(1, "wrongPassword", "newPassword");

        // Kiểm tra kết quả
        assertFalse(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserById(1);
        verify(passwordEncoder).matches("wrongPassword", testUser.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).updateUser(any(Users.class));
    }

    @Test
    @DisplayName("Kiểm tra lấy tất cả người dùng")
    public void testGetAllUsers() {
        // Chuẩn bị dữ liệu test
        List<Users> usersList = new ArrayList<>();
        usersList.add(testUser);

        Users secondUser = new Users();
        secondUser.setId(2);
        secondUser.setUsername("user2");
        secondUser.setEmail("user2@example.com");
        usersList.add(secondUser);

        when(userRepository.getAllUsers()).thenReturn(usersList);

        // Thực hiện test
        List<Users> result = userService.getAllUsers();

        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getAllUsers();
    }

    @Test
    @DisplayName("Kiểm tra xác thực token đặt lại mật khẩu hợp lệ")
    public void testValidateResetPasswordTokenValid() {
        // Chuẩn bị dữ liệu test
        String token = "valid-token";
        when(userRepository.getUserIdByResetToken(token)).thenReturn(Optional.of(1));
        when(userRepository.isResetTokenExpired(token)).thenReturn(false);

        // Thực hiện test
        Optional<Integer> result = userService.validateResetPasswordToken(token);

        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals(1, result.get());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserIdByResetToken(token);
        verify(userRepository).isResetTokenExpired(token);
    }

    @Test
    @DisplayName("Kiểm tra xác thực token đặt lại mật khẩu hết hạn")
    public void testValidateResetPasswordTokenExpired() {
        // Chuẩn bị dữ liệu test
        String token = "expired-token";
        when(userRepository.getUserIdByResetToken(token)).thenReturn(Optional.of(1));
        when(userRepository.isResetTokenExpired(token)).thenReturn(true);

        // Thực hiện test
        Optional<Integer> result = userService.validateResetPasswordToken(token);

        // Kiểm tra kết quả
        assertFalse(result.isPresent());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserIdByResetToken(token);
        verify(userRepository).isResetTokenExpired(token);
    }

    @Test
    @DisplayName("Kiểm tra đăng ký thất bại - email đã tồn tại")
    public void testRegisterUserEmailExists() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Thực hiện test và kiểm tra kết quả
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUser);
        });

        assertEquals("Email đã tồn tại", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).addUser(any(Users.class));
    }

    @Test
    @DisplayName("Kiểm tra đăng ký thất bại - số điện thoại đã tồn tại")
    public void testRegisterUserPhoneExists() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhone("0123456789")).thenReturn(true);

        // Thực hiện test và kiểm tra kết quả
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.registerUser(testUser);
        });

        assertEquals("Số điện thoại đã tồn tại", exception.getMessage());
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).existsByPhone("0123456789");
        verify(userRepository, never()).addUser(any(Users.class));
    }

    @Test
    @DisplayName("Kiểm tra cập nhật thông tin người dùng - người dùng không tồn tại")
    public void testUpdateProfileUserNotFound() {
        // Chuẩn bị dữ liệu test
        when(userRepository.getUserById(999)).thenReturn(null);

        // Thực hiện test
        Users updatedUser = new Users();
        updatedUser.setId(999);
        updatedUser.setFullName("Updated Name");

        // Kiểm tra kết quả
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.updateProfile(updatedUser);
        });

        assertEquals("Không tìm thấy người dùng", exception.getMessage());
        verify(userRepository).getUserById(999);
        verify(userRepository, never()).updateUser(any(Users.class));
    }

    @Test
    @DisplayName("Kiểm tra lấy người dùng theo ID thành công")
    public void testGetUserByIdSuccess() {
        // Chuẩn bị dữ liệu test
        when(userRepository.getUserById(1)).thenReturn(testUser);

        // Thực hiện test
        Optional<Users> result = userService.getUserById(1);

        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("testuser", result.get().getUsername());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserById(1);
    }

    @Test
    @DisplayName("Kiểm tra lấy người dùng theo ID không tồn tại")
    public void testGetUserByIdNotFound() {
        // Chuẩn bị dữ liệu test
        when(userRepository.getUserById(999)).thenReturn(null);

        // Thực hiện test
        Optional<Users> result = userService.getUserById(999);

        // Kiểm tra kết quả
        assertFalse(result.isPresent());

        // Xác minh các phương thức đã được gọi
        verify(userRepository).getUserById(999);
    }

    @Test
    @DisplayName("Kiểm tra lưu token đặt lại mật khẩu thành công")
    public void testSaveResetPasswordTokenSuccess() {
        // Chuẩn bị dữ liệu test
        String token = "reset-token";
        Date expiryTime = new Date();
        when(userRepository.saveResetPasswordToken(1, token, expiryTime)).thenReturn(true);

        // Thực hiện test
        boolean result = userService.saveResetPasswordToken(1, token, expiryTime);

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).saveResetPasswordToken(1, token, expiryTime);
    }

    @Test
    @DisplayName("Kiểm tra xóa token đặt lại mật khẩu thành công")
    public void testDeleteResetPasswordTokenSuccess() {
        // Chuẩn bị dữ liệu test
        String token = "reset-token";
        when(userRepository.deleteResetToken(token)).thenReturn(true);

        // Thực hiện test
        boolean result = userService.deleteResetPasswordToken(token);

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).deleteResetToken(token);
    }

    @Test
    @DisplayName("Kiểm tra cập nhật thời gian đăng nhập cuối")
    public void testUpdateLastLogin() {
        // Chuẩn bị dữ liệu test
        when(userRepository.updateLastLogin(1)).thenReturn(true);

        // Thực hiện test
        boolean result = userService.updateLastLogin(1);

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).updateLastLogin(1);
    }

    @Test
    @DisplayName("Kiểm tra phương thức updateUser")
    public void testUpdateUser() {
        // Chuẩn bị dữ liệu test
        when(userRepository.updateUser(any(Users.class))).thenReturn(true);

        // Thực hiện test
        boolean result = userService.updateUser(testUser);

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).updateUser(testUser);
    }

    @Test
    @DisplayName("Kiểm tra phương thức existsByUsername")
    public void testExistsByUsername() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Thực hiện test
        boolean result = userService.existsByUsername("testuser");

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    @DisplayName("Kiểm tra phương thức existsByEmail")
    public void testExistsByEmail() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Thực hiện test
        boolean result = userService.existsByEmail("test@example.com");

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    @DisplayName("Kiểm tra phương thức existsByPhone")
    public void testExistsByPhone() {
        // Chuẩn bị dữ liệu test
        when(userRepository.existsByPhone("0123456789")).thenReturn(true);

        // Thực hiện test
        boolean result = userService.existsByPhone("0123456789");

        // Kiểm tra kết quả
        assertTrue(result);

        // Xác minh các phương thức đã được gọi
        verify(userRepository).existsByPhone("0123456789");
    }

    @Test
    @DisplayName("Kiểm tra phương thức registerNewUser chưa được hỗ trợ")
    public void testRegisterNewUserNotSupported() {
        // Thực hiện test và kiểm tra kết quả
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            userService.registerNewUser("newuser", "password", "new@example.com");
        });

        assertEquals("Not supported yet.", exception.getMessage());
    }

    @Test
    @DisplayName("Kiểm tra phương thức registerNewUserWithAvatar chưa được hỗ trợ")
    public void testRegisterNewUserWithAvatarNotSupported() {
        // Thực hiện test và kiểm tra kết quả
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class, () -> {
            userService.registerNewUserWithAvatar("newuser", "password", "new@example.com", "avatar.jpg");
        });

        assertEquals("Not supported yet.", exception.getMessage());
    }
}
