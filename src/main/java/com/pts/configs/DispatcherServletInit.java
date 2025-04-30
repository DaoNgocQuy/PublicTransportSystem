package com.pts.configs;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class DispatcherServletInit extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class[]{
            SpringSecurityConfigs.class,
            HibernateConfigs.class // nếu không có thì có thể bỏ đi
        };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{
            WebAppContextConfigs.class,
            ThymeleafConfig.class // nếu bạn có cấu hình Thymeleaf
        };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }
}