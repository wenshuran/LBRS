package com.gatech.bigdata.hbase;

import com.gatech.bigdata.HBaseApplication;
import com.gatech.bigdata.entity.User;
import com.gatech.bigdata.mybatisplus.entity.UserVector;
import com.gatech.bigdata.mybatisplus.service.UserVectorService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = HBaseApplication.class)
public class HBaseApplicationTest {
    @Resource
    private HBaseService hbaseService;

    @Autowired
    private UserServiceProvider userServiceProvider;

    @Autowired
    private UserVectorService userVectorService;

    //测试创建表
    @Test
    public void testCreateTable() {
        hbaseService.creatTable("big_data", Arrays.asList("a", "back"));
    }
    //测试加入数据
    @Test
    public void testPutData() {
//        hbaseService.putData("test_base", "000001", "a", new String[]{
//                "project_id", "varName", "coefs", "pvalues", "tvalues",
//                "create_time"}, new String[]{"40866", "mob_3", "0.9416",
//                "0.0000", "12.2293", "null"});
//        hbaseService.putData("test_base", "000002", "a", new String[]{
//                "project_id", "varName", "coefs", "pvalues", "tvalues",
//                "create_time"}, new String[]{"40866", "idno_prov", "0.9317",
//                "0.0000", "9.8679", "null"});
//        hbaseService.putData("test_base", "000003", "a", new String[]{
//                "project_id", "varName", "coefs", "pvalues", "tvalues",
//                "create_time"}, new String[]{"40866", "education", "0.8984",
//                "0.0000", "25.5649", "null"});
        hbaseService.putData("big_data", "4704239730", "a", new String[]{
                "username"}, new String[]{"Shuran Wen"});
    }
    //测试遍历全表
    @Test
    public void testGetResultScanner() {
//        Map<String, Map<String, String>> result2 = hbaseService.getResultScanner("test_base");
        Map<String, Map<String, String>> result2 = userServiceProvider.test();
        System.out.println("-----遍历查询全表内容-----");
        result2.forEach((k, value) -> {
            System.out.println(k + "--->" + value);
        });
    }

    @Test
    public void getRowColumnData(){
        System.out.println(hbaseService.searchByRowKeyColumn("big_data", "4704239730", "userVector"));
    }

    @Test
    public void testMatchUserTag(){
        User user = new User();
        user.setUserId("4704239730");
        user.setUserName("Shuran Wen");
        userServiceProvider.updateUser(user);
    }

    @Test
    public void testGetRecommend() throws ExecutionException, InterruptedException, TimeoutException {
        userServiceProvider.getUserRecommend("4704239730", 1.1f, 2.2f, 1);
    }

    @Test
    public void fillUserVector(){
        try {
            ArrayList<UserVector> list = new ArrayList<>();
            Set<String> customerIds = new HashSet<>();
            BufferedReader reader = new BufferedReader(new FileReader("/Users/wenshuran/Downloads/customer_basic.csv"));
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> items = Arrays.asList(line.split(","));
                if (customerIds.contains(items.get(1))){
                    continue;
                }
                customerIds.add(items.get(1));
                UserVector vector = new UserVector();
                List<String> tmps = new ArrayList<>(items.subList(7, items.size() - 2));
                tmps.add(items.get(items.size() - 1));
                vector.setTag(String.join(", ", tmps));
                vector.setUsed((short) 0);
                list.add(vector);
            }
            userVectorService.saveBatch(list);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
