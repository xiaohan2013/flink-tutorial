package com.xiaozhu.etl.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    private String id;
    private Integer userId;
    private Integer money;
    private Long createTime;
}
