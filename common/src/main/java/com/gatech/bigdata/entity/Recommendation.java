package com.gatech.bigdata.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Recommendation implements Serializable {
    public String userId;
    public String timeStamp;
    public String recommends;
}
