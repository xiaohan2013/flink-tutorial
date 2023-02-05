package com.xiaozhu.etl.bean;

import java.math.BigDecimal;

/**
 * Desc: 订单实体类
 */
public class OrderInfo {
    // 属性名需要与数据库中字段名相同
    Long id;
    Long province_id;
    String order_status;
    Long user_id;
    BigDecimal total_amount;    //实际付款金额
    BigDecimal activity_reduce_amount;
    BigDecimal coupon_reduce_amount;
    BigDecimal original_total_amount;
    BigDecimal feight_fee;
    String expire_time;
    String create_time;
    String operate_time;
    String create_date; // 把其他字段处理得到
    String create_hour;
    Long create_ts; // 通过create_time转换

    public OrderInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getProvince_id() {
        return this.province_id;
    }

    public String getOrder_status() {
        return this.order_status;
    }

    public Long getUser_id() {
        return this.user_id;
    }

    public BigDecimal getTotal_amount() {
        return this.total_amount;
    }

    public BigDecimal getActivity_reduce_amount() {
        return this.activity_reduce_amount;
    }

    public BigDecimal getCoupon_reduce_amount() {
        return this.coupon_reduce_amount;
    }

    public BigDecimal getOriginal_total_amount() {
        return this.original_total_amount;
    }

    public BigDecimal getFeight_fee() {
        return this.feight_fee;
    }

    public String getExpire_time() {
        return this.expire_time;
    }

    public String getCreate_time() {
        return this.create_time;
    }

    public String getOperate_time() {
        return this.operate_time;
    }

    public String getCreate_date() {
        return this.create_date;
    }

    public String getCreate_hour() {
        return this.create_hour;
    }

    public Long getCreate_ts() {
        return this.create_ts;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProvince_id(Long province_id) {
        this.province_id = province_id;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public void setTotal_amount(BigDecimal total_amount) {
        this.total_amount = total_amount;
    }

    public void setActivity_reduce_amount(BigDecimal activity_reduce_amount) {
        this.activity_reduce_amount = activity_reduce_amount;
    }

    public void setCoupon_reduce_amount(BigDecimal coupon_reduce_amount) {
        this.coupon_reduce_amount = coupon_reduce_amount;
    }

    public void setOriginal_total_amount(BigDecimal original_total_amount) {
        this.original_total_amount = original_total_amount;
    }

    public void setFeight_fee(BigDecimal feight_fee) {
        this.feight_fee = feight_fee;
    }

    public void setExpire_time(String expire_time) {
        this.expire_time = expire_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setOperate_time(String operate_time) {
        this.operate_time = operate_time;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public void setCreate_hour(String create_hour) {
        this.create_hour = create_hour;
    }

    public void setCreate_ts(Long create_ts) {
        this.create_ts = create_ts;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OrderInfo)) return false;
        final OrderInfo other = (OrderInfo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$province_id = this.getProvince_id();
        final Object other$province_id = other.getProvince_id();
        if (this$province_id == null ? other$province_id != null : !this$province_id.equals(other$province_id))
            return false;
        final Object this$order_status = this.getOrder_status();
        final Object other$order_status = other.getOrder_status();
        if (this$order_status == null ? other$order_status != null : !this$order_status.equals(other$order_status))
            return false;
        final Object this$user_id = this.getUser_id();
        final Object other$user_id = other.getUser_id();
        if (this$user_id == null ? other$user_id != null : !this$user_id.equals(other$user_id)) return false;
        final Object this$total_amount = this.getTotal_amount();
        final Object other$total_amount = other.getTotal_amount();
        if (this$total_amount == null ? other$total_amount != null : !this$total_amount.equals(other$total_amount))
            return false;
        final Object this$activity_reduce_amount = this.getActivity_reduce_amount();
        final Object other$activity_reduce_amount = other.getActivity_reduce_amount();
        if (this$activity_reduce_amount == null ? other$activity_reduce_amount != null : !this$activity_reduce_amount.equals(other$activity_reduce_amount))
            return false;
        final Object this$coupon_reduce_amount = this.getCoupon_reduce_amount();
        final Object other$coupon_reduce_amount = other.getCoupon_reduce_amount();
        if (this$coupon_reduce_amount == null ? other$coupon_reduce_amount != null : !this$coupon_reduce_amount.equals(other$coupon_reduce_amount))
            return false;
        final Object this$original_total_amount = this.getOriginal_total_amount();
        final Object other$original_total_amount = other.getOriginal_total_amount();
        if (this$original_total_amount == null ? other$original_total_amount != null : !this$original_total_amount.equals(other$original_total_amount))
            return false;
        final Object this$feight_fee = this.getFeight_fee();
        final Object other$feight_fee = other.getFeight_fee();
        if (this$feight_fee == null ? other$feight_fee != null : !this$feight_fee.equals(other$feight_fee))
            return false;
        final Object this$expire_time = this.getExpire_time();
        final Object other$expire_time = other.getExpire_time();
        if (this$expire_time == null ? other$expire_time != null : !this$expire_time.equals(other$expire_time))
            return false;
        final Object this$create_time = this.getCreate_time();
        final Object other$create_time = other.getCreate_time();
        if (this$create_time == null ? other$create_time != null : !this$create_time.equals(other$create_time))
            return false;
        final Object this$operate_time = this.getOperate_time();
        final Object other$operate_time = other.getOperate_time();
        if (this$operate_time == null ? other$operate_time != null : !this$operate_time.equals(other$operate_time))
            return false;
        final Object this$create_date = this.getCreate_date();
        final Object other$create_date = other.getCreate_date();
        if (this$create_date == null ? other$create_date != null : !this$create_date.equals(other$create_date))
            return false;
        final Object this$create_hour = this.getCreate_hour();
        final Object other$create_hour = other.getCreate_hour();
        if (this$create_hour == null ? other$create_hour != null : !this$create_hour.equals(other$create_hour))
            return false;
        final Object this$create_ts = this.getCreate_ts();
        final Object other$create_ts = other.getCreate_ts();
        if (this$create_ts == null ? other$create_ts != null : !this$create_ts.equals(other$create_ts)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OrderInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $province_id = this.getProvince_id();
        result = result * PRIME + ($province_id == null ? 43 : $province_id.hashCode());
        final Object $order_status = this.getOrder_status();
        result = result * PRIME + ($order_status == null ? 43 : $order_status.hashCode());
        final Object $user_id = this.getUser_id();
        result = result * PRIME + ($user_id == null ? 43 : $user_id.hashCode());
        final Object $total_amount = this.getTotal_amount();
        result = result * PRIME + ($total_amount == null ? 43 : $total_amount.hashCode());
        final Object $activity_reduce_amount = this.getActivity_reduce_amount();
        result = result * PRIME + ($activity_reduce_amount == null ? 43 : $activity_reduce_amount.hashCode());
        final Object $coupon_reduce_amount = this.getCoupon_reduce_amount();
        result = result * PRIME + ($coupon_reduce_amount == null ? 43 : $coupon_reduce_amount.hashCode());
        final Object $original_total_amount = this.getOriginal_total_amount();
        result = result * PRIME + ($original_total_amount == null ? 43 : $original_total_amount.hashCode());
        final Object $feight_fee = this.getFeight_fee();
        result = result * PRIME + ($feight_fee == null ? 43 : $feight_fee.hashCode());
        final Object $expire_time = this.getExpire_time();
        result = result * PRIME + ($expire_time == null ? 43 : $expire_time.hashCode());
        final Object $create_time = this.getCreate_time();
        result = result * PRIME + ($create_time == null ? 43 : $create_time.hashCode());
        final Object $operate_time = this.getOperate_time();
        result = result * PRIME + ($operate_time == null ? 43 : $operate_time.hashCode());
        final Object $create_date = this.getCreate_date();
        result = result * PRIME + ($create_date == null ? 43 : $create_date.hashCode());
        final Object $create_hour = this.getCreate_hour();
        result = result * PRIME + ($create_hour == null ? 43 : $create_hour.hashCode());
        final Object $create_ts = this.getCreate_ts();
        result = result * PRIME + ($create_ts == null ? 43 : $create_ts.hashCode());
        return result;
    }

    public String toString() {
        return "OrderInfo(id=" + this.getId() + ", province_id=" + this.getProvince_id() + ", order_status=" + this.getOrder_status() + ", user_id=" + this.getUser_id() + ", total_amount=" + this.getTotal_amount() + ", activity_reduce_amount=" + this.getActivity_reduce_amount() + ", coupon_reduce_amount=" + this.getCoupon_reduce_amount() + ", original_total_amount=" + this.getOriginal_total_amount() + ", feight_fee=" + this.getFeight_fee() + ", expire_time=" + this.getExpire_time() + ", create_time=" + this.getCreate_time() + ", operate_time=" + this.getOperate_time() + ", create_date=" + this.getCreate_date() + ", create_hour=" + this.getCreate_hour() + ", create_ts=" + this.getCreate_ts() + ")";
    }
}