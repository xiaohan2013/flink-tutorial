package org.xiaozhu.service;

import java.math.BigDecimal;

/**
 * 商品统计service接口
 */
public interface ProductStatsService {
    // 获取某天的总交易额
    BigDecimal getGMV(Integer date);
}

