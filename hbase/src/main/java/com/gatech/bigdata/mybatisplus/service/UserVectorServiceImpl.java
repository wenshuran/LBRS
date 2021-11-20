package com.gatech.bigdata.mybatisplus.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gatech.bigdata.mybatisplus.entity.UserVector;
import com.gatech.bigdata.mybatisplus.mapper.UserVectorMapper;
import org.springframework.stereotype.Service;

@Service
public class UserVectorServiceImpl extends ServiceImpl<UserVectorMapper, UserVector> implements UserVectorService{
}
