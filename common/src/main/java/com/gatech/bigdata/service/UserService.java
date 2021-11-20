package com.gatech.bigdata.service;

import com.gatech.bigdata.entity.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface UserService {
    void updateUser(User user);
    Map<String, Map<String, String>>  test();
    List<String> getUserRecommend(String userId, float latitude, float longitude, int cityId) throws ExecutionException, InterruptedException, TimeoutException;
}
