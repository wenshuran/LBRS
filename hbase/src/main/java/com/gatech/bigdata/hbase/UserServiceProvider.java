package com.gatech.bigdata.hbase;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gatech.bigdata.entity.User;
import com.gatech.bigdata.mybatisplus.entity.UserVector;
import com.gatech.bigdata.mybatisplus.service.UserVectorService;
import com.gatech.bigdata.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DubboService(timeout = 50000)
@Component
public class UserServiceProvider implements UserService {

    @Autowired
    private HBaseService hBaseService;

    @Autowired
    private UserVectorService userVectorService;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void updateUser(User user){
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        if (user.getUserName() != null){
            keys.add("userName");
            values.add(user.getUserName());
        }
        if (user.getGender() != null){
            keys.add("gender");
            values.add(user.getGender());
        }
        String userVector = hBaseService.searchByRowKeyColumn("big_data", user.getUserId(), "userVector");
        if ("".equals(userVector)){
            UserVector vector = userVectorService.getOne(new QueryWrapper<UserVector>().eq("used", 0).last("limit 1"));
            keys.add("userVector");
            values.add(vector.getTag());
            vector.setUsed((short) 1);
            userVectorService.updateById(vector);
        }
        hBaseService.putData("big_data", user.getUserId(), "a", keys.toArray(new String[0]), values.toArray(new String[0]));
    }



    public Map<String, Map<String, String>>  test(){
        return hBaseService.getResultScanner("test_base");
    }

    @Override
    public List<String> getUserRecommend(String userId, float latitude, float longitude, int cityId) {
        String userTag = hBaseService.searchByRowKeyColumn("big_data", userId, "userVector");
        String gender = hBaseService.searchByRowKeyColumn("big_data", userId, "gender");
        String[] vectorArray = new String[7];
        vectorArray[0] = gender;
        vectorArray[1] = "0";
        vectorArray[2] = "0";
        vectorArray[3] = String.valueOf(latitude);
        vectorArray[4] = String.valueOf(longitude);
        vectorArray[5] = String.valueOf(cityId);
        vectorArray[6] = userTag;
        String userVector = String.join(",", vectorArray);
        userVector = userVector.replaceAll(" ", "");
//        String result = "[\"1\",\"2\", \"3\"]";
        Map<String, String> uriMap = new HashMap<>(2);
        uriMap.put("userVector", userVector);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(
            generateRequestParameters("http", "127.0.0.1:8005", uriMap),
            String.class
        );
        return JSON.parseArray(responseEntity.getBody(), String.class);
    }

    private String generateRequestParameters(String protocol, String uri, Map<String, String> params) {
        StringBuilder sb = new StringBuilder(protocol).append("://").append(uri);
        sb.append("?");
        for (Map.Entry<String, String> map : params.entrySet()) {
            sb.append(map.getKey())
                    .append("=")
                    .append(map.getValue())
                    .append("&");
        }
        uri = sb.substring(0, sb.length() - 1);
        return uri;
    }

}
