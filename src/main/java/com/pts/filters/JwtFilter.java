package com.pts.filters;

import com.pts.utils.JwtUtils;
import com.pts.pojo.Users;
import com.pts.repositories.UserRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Bộ lọc JWT kiểm tra và xác thực token cho các request tới API bảo mật
 */
public class JwtFilter implements Filter {
    // Các hằng số cho token
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            chain.doFilter(request, response);
            return;
        }
    
        boolean requiresJwt = uri.startsWith(contextPath + "/api/secure/") || 
                     uri.startsWith(contextPath + "/api/notifications") || 
                     uri.startsWith(contextPath + "/api/favorites") ||
                     (uri.startsWith(contextPath + "/api/landmarks") && 
                      (httpRequest.getMethod().equals("POST") || 
                       httpRequest.getMethod().equals("PUT") || 
                       httpRequest.getMethod().equals("DELETE")));
          if (requiresJwt) {
            String header = httpRequest.getHeader(AUTHORIZATION_HEADER);
            
            if (header == null || !header.startsWith(TOKEN_PREFIX)) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            try {
                String token = header.substring(TOKEN_PREFIX.length()); // Bỏ "Bearer " ở đầu
                String username = JwtUtils.validateTokenAndGetUsername(token);
                  if (username != null) {
                    // Tìm thông tin người dùng và vai trò của họ
                    Users user = userRepository.getUserByUsername(username);
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    
                    if (user != null && user.getRole() != null) {
                        // Thêm tiền tố ROLE_ nếu chưa có
                        String role = user.getRole();
                        if (!role.startsWith("ROLE_")) {
                            role = "ROLE_" + role;
                        }
                        authorities.add(new SimpleGrantedAuthority(role));
                    } else {
                        // Mặc định là ROLE_USER nếu không tìm thấy thông tin
                        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    }
                    
                    // Tạo authentication object và thiết lập vào SecurityContextHolder
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } catch (Exception e) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}
