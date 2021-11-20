package com.gatech.bigdata.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class User implements Serializable {
    public String userId;
    public String userName;
    public String gender;
    public String userVector;
}
