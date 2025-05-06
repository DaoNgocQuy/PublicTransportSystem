package com.pts.services.impl;

import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Users> userOpt = userRepository.findByUsername(username);
    
        if(!userOpt.isPresent()) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng: " + username);
        }
        
        Users user = userOpt.get();
        
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Thêm tiền tố ROLE_ cho role từ database
        if(user.getRole() != null) {
            String role = user.getRole();
            if(!role.startsWith("ROLE_")) {
                role = "ROLE_" + role;
            }
            authorities.add(new SimpleGrantedAuthority(role));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        // Tạo UserDetails với avatar URL
        return new CustomUserDetails(
            user.getUsername(),
            user.getPassword(),
            user.getIsActive() != null ? user.getIsActive() : true,
            true, // account non-expired
            true, // credentials non-expired
            true, // account non-locked
            authorities,
            user.getAvatarUrl() // Truyền avatar URL
        );
    }
    
    // Tạo lớp CustomUserDetails để lưu thêm thông tin avatarUrl
    public static class CustomUserDetails extends org.springframework.security.core.userdetails.User implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        private final String avatarUrl;
        
        public CustomUserDetails(String username, String password, boolean enabled, 
                             boolean accountNonExpired, boolean credentialsNonExpired, 
                             boolean accountNonLocked, List<GrantedAuthority> authorities,
                             String avatarUrl) {
            super(username, password, enabled, accountNonExpired, 
                  credentialsNonExpired, accountNonLocked, authorities);
            this.avatarUrl = avatarUrl;
        }
        
        public String getAvatarUrl() {
            return avatarUrl;
        }
    }
}