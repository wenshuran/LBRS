package com.gatech.bigdata.controller;

import com.gatech.bigdata.entity.User;
import com.gatech.bigdata.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @DubboReference(url = "dubbo://127.0.0.1:12346", timeout = 50000)
    private UserService userService;

    @GetMapping("/test")
    @ResponseBody
    public Map<String, Map<String, String>> test(){
        return userService.test();
    }

    @PostMapping("/updateUser")
    public boolean updateUser(@RequestParam String userId, @RequestParam String userName,
                              @RequestParam String gender){
        try {
            User user = new User();
            user.setUserId(userId);
            user.setUserName(userName);
            user.setGender(gender);
            userService.updateUser(user);
            return true;
        }catch (Exception e){
            logger.error(e.getMessage());
            return false;
        }
    }
}
