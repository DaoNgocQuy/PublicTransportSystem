package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import com.pts.services.impl.UserDetailsServiceImpl.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDetailsServiceImplTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;
    
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
        testUser.setAvatarUrl("http://example.com/avatar.jpg");
        testUser.setIsActive(true);
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng theo tên đăng nhập thành công")
    public void testLoadUserByUsernameSuccess() {
        // Chuẩn bị dữ liệu test
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Thực hiện test
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        
        // Kiểm tra xem UserDetails có phải là CustomUserDetails không
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals("http://example.com/avatar.jpg", customUserDetails.getAvatarUrl());
        
        // Kiểm tra quyền (authorities)
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng theo tên đăng nhập không tồn tại")
    public void testLoadUserByUsernameNotFound() {
        // Chuẩn bị dữ liệu test
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        // Thực hiện test và kiểm tra kết quả
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername("nonexistent");
        });
        
        assertEquals("Không tìm thấy người dùng: nonexistent", exception.getMessage());
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("nonexistent");
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng với role null")
    public void testLoadUserByUsernameWithNullRole() {
        // Chuẩn bị dữ liệu test
        testUser.setRole(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Thực hiện test
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(userDetails);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng với role đã có tiền tố ROLE_")
    public void testLoadUserByUsernameWithRolePrefix() {
        // Chuẩn bị dữ liệu test
        testUser.setRole("ROLE_ADMIN");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Thực hiện test
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(userDetails);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng với isActive là null")
    public void testLoadUserByUsernameWithNullIsActive() {
        // Chuẩn bị dữ liệu test
        testUser.setIsActive(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Thực hiện test
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(userDetails);
        assertTrue(userDetails.isEnabled());
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Kiểm tra tải người dùng với avatarUrl là null")
    public void testLoadUserByUsernameWithNullAvatarUrl() {
        // Chuẩn bị dữ liệu test
        testUser.setAvatarUrl(null);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        // Thực hiện test
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");
        
        // Kiểm tra kết quả
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof CustomUserDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertNull(customUserDetails.getAvatarUrl());
        
        // Xác minh các phương thức đã được gọi
        verify(userRepository).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("Kiểm tra các phương thức của CustomUserDetails")
    public void testCustomUserDetails() {        // Khởi tạo dữ liệu test
        String username = "testuser";
        String password = "password";
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;
        List<GrantedAuthority> authorities = 
                new ArrayList<>(java.util.Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));
        String avatarUrl = "http://example.com/avatar.jpg";
        
        // Thực hiện test
        CustomUserDetails customUserDetails = new CustomUserDetails(
                username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities, avatarUrl);
          // Kiểm tra kết quả
        assertEquals(username, customUserDetails.getUsername());
        assertEquals(password, customUserDetails.getPassword());
        assertEquals(enabled, customUserDetails.isEnabled());
        assertEquals(accountNonExpired, customUserDetails.isAccountNonExpired());
        assertEquals(credentialsNonExpired, customUserDetails.isCredentialsNonExpired());
        assertEquals(accountNonLocked, customUserDetails.isAccountNonLocked());
        assertTrue(customUserDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertEquals(1, customUserDetails.getAuthorities().size());
        assertEquals(avatarUrl, customUserDetails.getAvatarUrl());
    }
}
