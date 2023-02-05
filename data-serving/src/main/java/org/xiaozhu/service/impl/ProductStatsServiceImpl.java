package org.xiaozhu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaozhu.mapper.ProductStatsMapper;
import org.xiaozhu.service.ProductStatsService;

import java.math.BigDecimal;

/**
 * 商品统计service接口实现类
 */
@Service
public class ProductStatsServiceImpl implements ProductStatsService {

    @Autowired
    private ProductStatsMapper productStatsMapper;

    @Override
    public BigDecimal getGMV(Integer date) {
        return productStatsMapper.selectGMV(date);
    }
}


