package com.pts.repositories.impl;

import com.pts.pojo.Users;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryImplTest {
    
    @Mock
    private JdbcTemplate jdbcTemplate;
    
    @InjectMocks
    private UserRepositoryImpl userRepository;
    
    private Users testUser;
    
    @BeforeEach
    public void setUp() {
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
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo tên đăng nhập tồn tại")
    public void testFindByUsernameExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("testuser")))
                .thenReturn(Arrays.asList(testUser));
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByUsername("testuser");
        
        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(eq("SELECT * FROM users WHERE username = ?"), 
                any(RowMapper.class), eq("testuser"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo tên đăng nhập không tồn tại")
    public void testFindByUsernameNotExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("nonexistent")))
                .thenReturn(Arrays.asList());
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByUsername("nonexistent");
        
        // Kiểm tra kết quả
        assertFalse(result.isPresent());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(eq("SELECT * FROM users WHERE username = ?"), 
                any(RowMapper.class), eq("nonexistent"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo email tồn tại")
    public void testFindByEmailExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("test@example.com")))
                .thenReturn(Arrays.asList(testUser));
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByEmail("test@example.com");
        
        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(eq("SELECT * FROM users WHERE email = ?"), 
                any(RowMapper.class), eq("test@example.com"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo email không tồn tại")
    public void testFindByEmailNotExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("nonexistent@example.com")))
                .thenReturn(Arrays.asList());
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByEmail("nonexistent@example.com");
        
        // Kiểm tra kết quả
        assertFalse(result.isPresent());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(eq("SELECT * FROM users WHERE email = ?"), 
                any(RowMapper.class), eq("nonexistent@example.com"));
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại username")
    public void testExistsByUsername() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("testuser")))
                .thenReturn(1);
        
        // Thực hiện test
        boolean result = userRepository.existsByUsername("testuser");
        
        // Kiểm tra kết quả
        assertTrue(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM users WHERE username = ?"), 
                eq(Integer.class), eq("testuser"));
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại username xảy ra ngoại lệ")
    public void testExistsByUsernameException() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("testuser")))
                .thenThrow(new DataAccessException("Database error") {});
        
        // Thực hiện test
        boolean result = userRepository.existsByUsername("testuser");
        
        // Kiểm tra kết quả - khi có ngoại lệ thì phương thức nên trả về false
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM users WHERE username = ?"), 
                eq(Integer.class), eq("testuser"));
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại email")
    public void testExistsByEmail() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("test@example.com")))
                .thenReturn(1);
        
        // Thực hiện test
        boolean result = userRepository.existsByEmail("test@example.com");
        
        // Kiểm tra kết quả
        assertTrue(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM users WHERE email = ?"), 
                eq(Integer.class), eq("test@example.com"));
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại email xảy ra ngoại lệ")
    public void testExistsByEmailException() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("test@example.com")))
                .thenThrow(new DataAccessException("Database error") {});
        
        // Thực hiện test
        boolean result = userRepository.existsByEmail("test@example.com");
        
        // Kiểm tra kết quả - khi có ngoại lệ thì phương thức nên trả về false
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM users WHERE email = ?"), 
                eq(Integer.class), eq("test@example.com"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra lấy tất cả người dùng")
    public void testGetAllUsers() {
        // Chuẩn bị dữ liệu test
        Users user2 = new Users();
        user2.setId(2);
        user2.setUsername("user2");
        
        when(jdbcTemplate.query(anyString(), any(RowMapper.class)))
                .thenReturn(Arrays.asList(testUser, user2));
        
        // Thực hiện test
        List<Users> result = userRepository.getAllUsers();
        
        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(eq("SELECT * FROM users"), any(RowMapper.class));
    }
    
    @Test
    @DisplayName("Kiểm tra cập nhật thời gian đăng nhập cuối")
    public void testUpdateLastLogin() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.update(anyString(), any(Date.class), eq(1)))
                .thenReturn(1);
        
        // Thực hiện test
        boolean result = userRepository.updateLastLogin(1);
        
        // Kiểm tra kết quả
        assertTrue(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(eq("UPDATE users SET last_login = ? WHERE id = ?"), 
                any(Date.class), eq(1));
    }
    
    @Test
    @DisplayName("Kiểm tra cập nhật thời gian đăng nhập cuối thất bại")
    public void testUpdateLastLoginFailed() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.update(anyString(), any(Date.class), eq(999)))
                .thenReturn(0); // Không có bản ghi nào được cập nhật
        
        // Thực hiện test
        boolean result = userRepository.updateLastLogin(999);
        
        // Kiểm tra kết quả
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(eq("UPDATE users SET last_login = ? WHERE id = ?"), 
                any(Date.class), eq(999));
    }
    
    @Test
    @DisplayName("Kiểm tra xóa người dùng")
    public void testDeleteUser() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.update(anyString(), eq(1)))
                .thenReturn(1);
        
        // Thực hiện test
        boolean result = userRepository.deleteUser(1);
        
        // Kiểm tra kết quả
        assertTrue(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(eq("DELETE FROM users WHERE id = ?"), eq(1));
    }
    
    @Test
    @DisplayName("Kiểm tra xóa người dùng thất bại")
    public void testDeleteUserFailed() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.update(anyString(), eq(999)))
                .thenReturn(0); // Không có bản ghi nào được xóa
        
        // Thực hiện test
        boolean result = userRepository.deleteUser(999);
        
        // Kiểm tra kết quả
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(eq("DELETE FROM users WHERE id = ?"), eq(999));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra lấy người dùng theo ID tồn tại")
    public void testGetUserByIdExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(1)))
                .thenReturn(testUser);
        
        // Thực hiện test
        Users result = userRepository.getUserById(1);
        
        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals(1, result.getId());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE id = ?"), 
                any(RowMapper.class), eq(1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra lấy người dùng theo ID không tồn tại")
    public void testGetUserByIdNotExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), any(RowMapper.class), eq(999)))
                .thenThrow(new EmptyResultDataAccessException(1));
        
        // Thực hiện test
        Users result = userRepository.getUserById(999);
        
        // Kiểm tra kết quả
        assertNull(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT * FROM users WHERE id = ?"), 
                any(RowMapper.class), eq(999));
    }      @Test
    @DisplayName("Kiểm tra thêm người dùng mới")
    public void testAddUser() {
        // Sử dụng Answer để thay thế hành vi mặc định của update
        doAnswer(invocation -> {
            // Lấy đối số thứ 2 (KeyHolder)
            KeyHolder kh = invocation.getArgument(1);
            // Giả lập việc thiết lập key vào KeyHolder
            Field keyField = GeneratedKeyHolder.class.getDeclaredField("keyList");
            keyField.setAccessible(true);
            keyField.set(kh, Arrays.asList(Map.of("id", 1)));
            return 1;
        }).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        
        // Thực hiện test
        boolean result = userRepository.addUser(testUser);
        
        // Kiểm tra kết quả
        assertTrue(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }
    
    @Test
    @DisplayName("Kiểm tra thêm người dùng mới thất bại")
    public void testAddUserFailed() {
        // Chuẩn bị dữ liệu test
        doReturn(0).when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        
        // Thực hiện test
        boolean result = userRepository.addUser(testUser);
        
        // Kiểm tra kết quả
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }
    
    @Test
    @DisplayName("Kiểm tra thêm người dùng mới xảy ra ngoại lệ")
    public void testAddUserException() {
        // Chuẩn bị dữ liệu test
        doThrow(new DataAccessException("Database error") {})
            .when(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        
        // Thực hiện test
        boolean result = userRepository.addUser(testUser);
        
        // Kiểm tra kết quả
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra lấy người dùng theo tên đăng nhập")
    public void testGetUserByUsername() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(
            eq("SELECT * FROM users WHERE username = ?"), 
            any(RowMapper.class), 
            eq("testuser")
        )).thenReturn(Collections.singletonList(testUser));
        
        // Thực hiện test
        Users result = userRepository.getUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(
            eq("SELECT * FROM users WHERE username = ?"), 
            any(RowMapper.class), 
            eq("testuser")
        );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra lấy người dùng theo tên đăng nhập không tồn tại")
    public void testGetUserByUsernameNotExists() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.query(
            eq("SELECT * FROM users WHERE username = ?"), 
            any(RowMapper.class), 
            eq("nonexistent")
        )).thenReturn(Collections.emptyList());
        
        // Thực hiện test
        Users result = userRepository.getUserByUsername("nonexistent");
        
        // Kiểm tra kết quả
        assertNull(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).query(
            eq("SELECT * FROM users WHERE username = ?"), 
            any(RowMapper.class), 
            eq("nonexistent")
        );
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo số điện thoại tồn tại")
    public void testFindByPhoneExists() {
        // Chuẩn bị dữ liệu test
        doReturn(Collections.singletonList(testUser)).when(jdbcTemplate).query(
            eq("SELECT * FROM users WHERE phone = ?"), 
            any(RowMapper.class), 
            eq("0123456789")
        );
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByPhone("0123456789");
        
        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals("0123456789", result.get().getPhone());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Kiểm tra tìm người dùng theo số điện thoại không tồn tại")
    public void testFindByPhoneNotExists() {
        // Chuẩn bị dữ liệu test
        doReturn(Collections.emptyList()).when(jdbcTemplate).query(
            eq("SELECT * FROM users WHERE phone = ?"), 
            any(RowMapper.class), 
            eq("9999999999")
        );
        
        // Thực hiện test
        Optional<Users> result = userRepository.findByPhone("9999999999");
        
        // Kiểm tra kết quả
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại số điện thoại")
    public void testExistsByPhone() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).queryForObject(
            eq("SELECT COUNT(*) FROM users WHERE phone = ?"), 
            eq(Integer.class), 
            eq("0123456789")
        );
        
        // Thực hiện test
        boolean result = userRepository.existsByPhone("0123456789");
        
        // Kiểm tra kết quả
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra tồn tại số điện thoại xảy ra ngoại lệ")
    public void testExistsByPhoneException() {
        // Chuẩn bị dữ liệu test
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("0123456789")))
                .thenThrow(new DataAccessException("Database error") {});
        
        // Thực hiện test
        boolean result = userRepository.existsByPhone("0123456789");
        
        // Kiểm tra kết quả
        assertFalse(result);
        
        // Xác minh các phương thức đã được gọi
        verify(jdbcTemplate).queryForObject(eq("SELECT COUNT(*) FROM users WHERE phone = ?"), 
                eq(Integer.class), eq("0123456789"));
    }
    
    @Test
    @DisplayName("Kiểm tra cập nhật thông tin người dùng")
    public void testUpdateUser() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).update(anyString(), any(Object[].class));
        
        // Cập nhật thông tin test user
        testUser.setFullName("Updated Name");
        testUser.setEmail("updated@example.com");
        
        // Thực hiện test
        boolean result = userRepository.updateUser(testUser);
        
        // Kiểm tra kết quả
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra cập nhật thông tin người dùng thất bại")
    public void testUpdateUserFailed() {
        // Chuẩn bị dữ liệu test
        doReturn(0).when(jdbcTemplate).update(anyString(), any(Object[].class));
        
        // Cập nhật thông tin test user
        testUser.setFullName("Updated Name");
        testUser.setEmail("updated@example.com");
        
        // Thực hiện test
        boolean result = userRepository.updateUser(testUser);
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra cập nhật thông tin người dùng xảy ra ngoại lệ")
    public void testUpdateUserException() {
        // Chuẩn bị dữ liệu test
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).update(anyString(), any(Object[].class));
        
        // Cập nhật thông tin test user
        testUser.setFullName("Updated Name");
        testUser.setEmail("updated@example.com");
        
        // Thực hiện test
        boolean result = userRepository.updateUser(testUser);
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra lưu token đặt lại mật khẩu")
    public void testSaveResetPasswordToken() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).update(eq("DELETE FROM reset_password_token WHERE user_id = ?"), eq(1));
        doReturn(1).when(jdbcTemplate).update(
            eq("INSERT INTO reset_password_token (user_id, token, expiry_date) VALUES (?, ?, ?)"), 
            eq(1), 
            eq("reset-token-123"), 
            any(java.sql.Timestamp.class)
        );
        
        Date expiryDate = new Date();
        String token = "reset-token-123";
        
        // Thực hiện test
        boolean result = userRepository.saveResetPasswordToken(1, token, expiryDate);
        
        // Kiểm tra kết quả
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra lưu token đặt lại mật khẩu thất bại")
    public void testSaveResetPasswordTokenFailed() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).update(eq("DELETE FROM reset_password_token WHERE user_id = ?"), eq(1));
        doReturn(0).when(jdbcTemplate).update(
            eq("INSERT INTO reset_password_token (user_id, token, expiry_date) VALUES (?, ?, ?)"), 
            eq(1), 
            eq("reset-token-123"), 
            any(java.sql.Timestamp.class)
        );
        
        Date expiryDate = new Date();
        String token = "reset-token-123";
        
        // Thực hiện test
        boolean result = userRepository.saveResetPasswordToken(1, token, expiryDate);
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra lưu token đặt lại mật khẩu xảy ra ngoại lệ")
    public void testSaveResetPasswordTokenException() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).update(eq("DELETE FROM reset_password_token WHERE user_id = ?"), eq(1));
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).update(
            eq("INSERT INTO reset_password_token (user_id, token, expiry_date) VALUES (?, ?, ?)"), 
            eq(1), 
            eq("reset-token-123"), 
            any(java.sql.Timestamp.class)
        );
        
        Date expiryDate = new Date();
        String token = "reset-token-123";
        
        // Thực hiện test
        boolean result = userRepository.saveResetPasswordToken(1, token, expiryDate);
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra lấy user ID từ token đặt lại mật khẩu")
    public void testGetUserIdByResetToken() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).queryForObject(
            eq("SELECT user_id FROM reset_password_token WHERE token = ?"), 
            eq(Integer.class), 
            eq("valid-token")
        );
        
        // Thực hiện test
        Optional<Integer> result = userRepository.getUserIdByResetToken("valid-token");
        
        // Kiểm tra kết quả
        assertTrue(result.isPresent());
        assertEquals(1, result.get());
    }
    
    @Test
    @DisplayName("Kiểm tra lấy user ID từ token đặt lại mật khẩu không tồn tại")
    public void testGetUserIdByResetTokenNotExists() {
        // Chuẩn bị dữ liệu test
        doThrow(new EmptyResultDataAccessException(1)).when(jdbcTemplate).queryForObject(
            eq("SELECT user_id FROM reset_password_token WHERE token = ?"), 
            eq(Integer.class), 
            eq("invalid-token")
        );
        
        // Thực hiện test
        Optional<Integer> result = userRepository.getUserIdByResetToken("invalid-token");
        
        // Kiểm tra kết quả
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Kiểm tra lấy user ID từ token đặt lại mật khẩu xảy ra ngoại lệ")
    public void testGetUserIdByResetTokenException() {
        // Chuẩn bị dữ liệu test
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).queryForObject(
            eq("SELECT user_id FROM reset_password_token WHERE token = ?"), 
            eq(Integer.class), 
            eq("valid-token")
        );
        
        // Thực hiện test
        Optional<Integer> result = userRepository.getUserIdByResetToken("valid-token");
        
        // Kiểm tra kết quả
        assertFalse(result.isPresent());
    }
    
    @Test
    @DisplayName("Kiểm tra token đặt lại mật khẩu đã hết hạn")
    public void testIsResetTokenExpired() {
        // Chuẩn bị dữ liệu test
        doReturn(true).when(jdbcTemplate).queryForObject(
            eq("SELECT expiry_date < NOW() as expired FROM reset_password_token WHERE token = ?"), 
            eq(Boolean.class), 
            eq("expired-token")
        );
        
        // Thực hiện test
        boolean result = userRepository.isResetTokenExpired("expired-token");
        
        // Kiểm tra kết quả
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra token đặt lại mật khẩu chưa hết hạn")
    public void testIsResetTokenNotExpired() {
        // Chuẩn bị dữ liệu test
        doReturn(false).when(jdbcTemplate).queryForObject(
            eq("SELECT expiry_date < NOW() as expired FROM reset_password_token WHERE token = ?"), 
            eq(Boolean.class), 
            eq("valid-token")
        );
        
        // Thực hiện test
        boolean result = userRepository.isResetTokenExpired("valid-token");
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra token đặt lại mật khẩu không tồn tại")
    public void testIsResetTokenExpiredNotExists() {
        // Chuẩn bị dữ liệu test
        doThrow(new EmptyResultDataAccessException(1)).when(jdbcTemplate).queryForObject(
            eq("SELECT expiry_date < NOW() as expired FROM reset_password_token WHERE token = ?"), 
            eq(Boolean.class), 
            eq("nonexistent-token")
        );
        
        // Thực hiện test
        boolean result = userRepository.isResetTokenExpired("nonexistent-token");
        
        // Kiểm tra kết quả - nếu token không tồn tại, coi như đã hết hạn
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra token đặt lại mật khẩu xảy ra ngoại lệ")
    public void testIsResetTokenExpiredException() {
        // Chuẩn bị dữ liệu test
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).queryForObject(
            eq("SELECT expiry_date < NOW() as expired FROM reset_password_token WHERE token = ?"), 
            eq(Boolean.class), 
            eq("valid-token")
        );
        
        // Thực hiện test
        boolean result = userRepository.isResetTokenExpired("valid-token");
        
        // Kiểm tra kết quả - khi có ngoại lệ, coi như đã hết hạn
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra xóa token đặt lại mật khẩu")
    public void testDeleteResetToken() {
        // Chuẩn bị dữ liệu test
        doReturn(1).when(jdbcTemplate).update(
            eq("DELETE FROM reset_password_token WHERE token = ?"), 
            eq("token-to-delete")
        );
        
        // Thực hiện test
        boolean result = userRepository.deleteResetToken("token-to-delete");
        
        // Kiểm tra kết quả
        assertTrue(result);
    }
    
    @Test
    @DisplayName("Kiểm tra xóa token đặt lại mật khẩu thất bại")
    public void testDeleteResetTokenFailed() {
        // Chuẩn bị dữ liệu test
        doReturn(0).when(jdbcTemplate).update(
            eq("DELETE FROM reset_password_token WHERE token = ?"), 
            eq("nonexistent-token")
        );
        
        // Thực hiện test
        boolean result = userRepository.deleteResetToken("nonexistent-token");
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
    
    @Test
    @DisplayName("Kiểm tra xóa token đặt lại mật khẩu xảy ra ngoại lệ")
    public void testDeleteResetTokenException() {
        // Chuẩn bị dữ liệu test
        doThrow(new DataAccessException("Database error") {}).when(jdbcTemplate).update(
            eq("DELETE FROM reset_password_token WHERE token = ?"), 
            eq("token-to-delete")
        );
        
        // Thực hiện test
        boolean result = userRepository.deleteResetToken("token-to-delete");
        
        // Kiểm tra kết quả
        assertFalse(result);
    }
}
