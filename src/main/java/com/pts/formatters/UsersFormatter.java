package com.pts.formatters;

import com.pts.pojo.Users;
import com.pts.services.UserService;
import java.text.ParseException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

@Component
public class UsersFormatter implements Formatter<Users> {

    @Autowired
    private UserService userService;

    @Override
    public String print(Users user, Locale locale) {
        return String.valueOf(user.getId());
    }

    @Override
    public Users parse(String userId, Locale locale) throws ParseException {
        return userService.getUserById(Integer.parseInt(userId));
    }
}
