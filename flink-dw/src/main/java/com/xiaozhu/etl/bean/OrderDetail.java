package com.xiaozhu.etl.bean;

import java.math.BigDecimal;

/**
 * Desc:订单明细实体类
 */
public class OrderDetail {
    Long id;
    Long order_id ; // 无外键约束
    Long sku_id;
    BigDecimal order_price ;
    Long sku_num ;
    String sku_name; // 冗余字段，可以减少连接查询的次数
    String create_time;
    BigDecimal split_total_amount;
    BigDecimal split_activity_amount;
    BigDecimal split_coupon_amount;
    Long create_ts; // 通过create_time转换

    public OrderDetail() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getOrder_id() {
        return this.order_id;
    }

    public Long getSku_id() {
        return this.sku_id;
    }

    public BigDecimal getOrder_price() {
        return this.order_price;
    }

    public Long getSku_num() {
        return this.sku_num;
    }

    public String getSku_name() {
        return this.sku_name;
    }

    public String getCreate_time() {
        return this.create_time;
    }

    public BigDecimal getSplit_total_amount() {
        return this.split_total_amount;
    }

    public BigDecimal getSplit_activity_amount() {
        return this.split_activity_amount;
    }

    public BigDecimal getSplit_coupon_amount() {
        return this.split_coupon_amount;
    }

    public Long getCreate_ts() {
        return this.create_ts;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }

    public void setSku_id(Long sku_id) {
        this.sku_id = sku_id;
    }

    public void setOrder_price(BigDecimal order_price) {
        this.order_price = order_price;
    }

    public void setSku_num(Long sku_num) {
        this.sku_num = sku_num;
    }

    public void setSku_name(String sku_name) {
        this.sku_name = sku_name;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setSplit_total_amount(BigDecimal split_total_amount) {
        this.split_total_amount = split_total_amount;
    }

    public void setSplit_activity_amount(BigDecimal split_activity_amount) {
        this.split_activity_amount = split_activity_amount;
    }

    public void setSplit_coupon_amount(BigDecimal split_coupon_amount) {
        this.split_coupon_amount = split_coupon_amount;
    }

    public void setCreate_ts(Long create_ts) {
        this.create_ts = create_ts;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OrderDetail)) return false;
        final OrderDetail other = (OrderDetail) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$order_id = this.getOrder_id();
        final Object other$order_id = other.getOrder_id();
        if (this$order_id == null ? other$order_id != null : !this$order_id.equals(other$order_id)) return false;
        final Object this$sku_id = this.getSku_id();
        final Object other$sku_id = other.getSku_id();
        if (this$sku_id == null ? other$sku_id != null : !this$sku_id.equals(other$sku_id)) return false;
        final Object this$order_price = this.getOrder_price();
        final Object other$order_price = other.getOrder_price();
        if (this$order_price == null ? other$order_price != null : !this$order_price.equals(other$order_price))
            return false;
        final Object this$sku_num = this.getSku_num();
        final Object other$sku_num = other.getSku_num();
        if (this$sku_num == null ? other$sku_num != null : !this$sku_num.equals(other$sku_num)) return false;
        final Object this$sku_name = this.getSku_name();
        final Object other$sku_name = other.getSku_name();
        if (this$sku_name == null ? other$sku_name != null : !this$sku_name.equals(other$sku_name)) return false;
        final Object this$create_time = this.getCreate_time();
        final Object other$create_time = other.getCreate_time();
        if (this$create_time == null ? other$create_time != null : !this$create_time.equals(other$create_time))
            return false;
        final Object this$split_total_amount = this.getSplit_total_amount();
        final Object other$split_total_amount = other.getSplit_total_amount();
        if (this$split_total_amount == null ? other$split_total_amount != null : !this$split_total_amount.equals(other$split_total_amount))
            return false;
        final Object this$split_activity_amount = this.getSplit_activity_amount();
        final Object other$split_activity_amount = other.getSplit_activity_amount();
        if (this$split_activity_amount == null ? other$split_activity_amount != null : !this$split_activity_amount.equals(other$split_activity_amount))
            return false;
        final Object this$split_coupon_amount = this.getSplit_coupon_amount();
        final Object other$split_coupon_amount = other.getSplit_coupon_amount();
        if (this$split_coupon_amount == null ? other$split_coupon_amount != null : !this$split_coupon_amount.equals(other$split_coupon_amount))
            return false;
        final Object this$create_ts = this.getCreate_ts();
        final Object other$create_ts = other.getCreate_ts();
        if (this$create_ts == null ? other$create_ts != null : !this$create_ts.equals(other$create_ts)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OrderDetail;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $order_id = this.getOrder_id();
        result = result * PRIME + ($order_id == null ? 43 : $order_id.hashCode());
        final Object $sku_id = this.getSku_id();
        result = result * PRIME + ($sku_id == null ? 43 : $sku_id.hashCode());
        final Object $order_price = this.getOrder_price();
        result = result * PRIME + ($order_price == null ? 43 : $order_price.hashCode());
        final Object $sku_num = this.getSku_num();
        result = result * PRIME + ($sku_num == null ? 43 : $sku_num.hashCode());
        final Object $sku_name = this.getSku_name();
        result = result * PRIME + ($sku_name == null ? 43 : $sku_name.hashCode());
        final Object $create_time = this.getCreate_time();
        result = result * PRIME + ($create_time == null ? 43 : $create_time.hashCode());
        final Object $split_total_amount = this.getSplit_total_amount();
        result = result * PRIME + ($split_total_amount == null ? 43 : $split_total_amount.hashCode());
        final Object $split_activity_amount = this.getSplit_activity_amount();
        result = result * PRIME + ($split_activity_amount == null ? 43 : $split_activity_amount.hashCode());
        final Object $split_coupon_amount = this.getSplit_coupon_amount();
        result = result * PRIME + ($split_coupon_amount == null ? 43 : $split_coupon_amount.hashCode());
        final Object $create_ts = this.getCreate_ts();
        result = result * PRIME + ($create_ts == null ? 43 : $create_ts.hashCode());
        return result;
    }

    public String toString() {
        return "OrderDetail(id=" + this.getId() + ", order_id=" + this.getOrder_id() + ", sku_id=" + this.getSku_id() + ", order_price=" + this.getOrder_price() + ", sku_num=" + this.getSku_num() + ", sku_name=" + this.getSku_name() + ", create_time=" + this.getCreate_time() + ", split_total_amount=" + this.getSplit_total_amount() + ", split_activity_amount=" + this.getSplit_activity_amount() + ", split_coupon_amount=" + this.getSplit_coupon_amount() + ", create_ts=" + this.getCreate_ts() + ")";
    }
}