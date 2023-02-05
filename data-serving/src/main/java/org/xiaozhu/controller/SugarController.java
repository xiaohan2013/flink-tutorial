package org.xiaozhu.controller;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.xiaozhu.service.ProductStatsService;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 大屏展示控制层
 */
@RestController
@RequestMapping("/api/sugar")
public class SugarController {

    @Autowired
    private ProductStatsService productStatsService;

    @RequestMapping("/gmv")
    public String getGMV(@RequestParam(value = "date",defaultValue = "0") Integer date){
        if (date == 0){
            date = now();
        }

        // 调用service获取总交易额
        BigDecimal gmv = productStatsService.getGMV(date);

        String json = "{\"status\": 0,\"data\": "+gmv+"}";

        return json;
    }

    // 获取当前日期
    private Integer now() {
        String yyyyMMdd = DateFormatUtils.format(new Date(), "yyyyMMdd");
        return Integer.valueOf(yyyyMMdd);
    }
}

