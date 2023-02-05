package org.xiaozhu.mapper;

import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * 商品统计Mapper接口
 */
public interface ProductStatsMapper {
    // 获取某天商品的总交易额
    @Select("select sum(order_amount) order_amount from product_stats_2022 where toYYYYMMDD(stt)=#{date}")
    BigDecimal selectGMV(Integer date);
}

