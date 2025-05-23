/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pts.filters;

import com.pts.utils.JwtUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class JwtFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String uri = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        // Kiểm tra các địa chỉ cần JWT (API secure hoặc các endpoint liên quan đến thông báo và yêu thích)
        boolean requiresJwt = uri.startsWith(contextPath + "/api/secure/") || 
                     uri.startsWith(contextPath + "/api/notifications") || 
                     uri.startsWith(contextPath + "/api/favorites") ||
                     (uri.startsWith(contextPath + "/api/landmarks") && 
                      (httpRequest.getMethod().equals("POST") || 
                       httpRequest.getMethod().equals("PUT") || 
                       httpRequest.getMethod().equals("DELETE")));
        
        if (requiresJwt) {
            String header = httpRequest.getHeader("Authorization");
            
            if (header == null || !header.startsWith("Bearer ")) {
                httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            
            try {
                String token = header.substring(7); // Bỏ "Bearer " ở đầu
                String username = JwtUtils.validateTokenAndGetUsername(token);
                
                if (username != null) {
                    // Tạo authentication object và thiết lập vào SecurityContextHolder
                    // Đây là bước quan trọng để Spring Security biết user đã được xác thực
                    UsernamePasswordAuthenticationToken authToken = 
                        new UsernamePasswordAuthenticationToken(username, null, null);
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
