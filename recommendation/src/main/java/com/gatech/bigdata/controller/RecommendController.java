package com.gatech.bigdata.controller;

import com.gatech.bigdata.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {
    private Logger logger = LoggerFactory.getLogger(RecommendController.class);

    @DubboReference(url = "dubbo://127.0.0.1:12346", timeout = 50000)
    private UserService userService;

    @GetMapping("/getRecommend")
    public List<String> getRecommend(@RequestParam String userId, @RequestParam float latitude,
                                      @RequestParam float longitude, @RequestParam int cityId){
        try {
            return userService.getUserRecommend(userId, latitude, longitude, cityId);
        }catch (Exception e){
            logger.error(e.getMessage());
            return new ArrayList<>();
        }
    }
}
