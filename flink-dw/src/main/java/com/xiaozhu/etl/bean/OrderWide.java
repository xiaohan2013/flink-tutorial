package com.xiaozhu.etl.bean;

import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;

/**
 * Desc: 订单和订单明细关联宽表对应实体类
 */
public class OrderWide {
    Long detail_id;
    Long order_id ;
    Long sku_id;
    BigDecimal order_price ;
    Long sku_num ;
    String sku_name;
    Long province_id;
    String order_status;
    Long user_id;

    BigDecimal total_amount;
    BigDecimal activity_reduce_amount;
    BigDecimal coupon_reduce_amount;
    BigDecimal original_total_amount;
    BigDecimal feight_fee;
    BigDecimal split_feight_fee;
    BigDecimal split_activity_amount;
    BigDecimal split_coupon_amount;
    BigDecimal split_total_amount;

    String expire_time;
    String create_time;
    String operate_time;
    String create_date; // 把其他字段处理得到
    String create_hour;

    String province_name;//查询维表得到
    String province_area_code;
    String province_iso_code;
    String province_3166_2_code;

    Integer user_age ;
    String user_gender;

    Long spu_id;     //作为维度数据 要关联进来
    Long tm_id;
    Long category3_id;
    String spu_name;
    String tm_name;
    String category3_name;

    public OrderWide(OrderInfo orderInfo, OrderDetail orderDetail){
        mergeOrderInfo(orderInfo);
        mergeOrderDetail(orderDetail);

    }

    public OrderWide(Long detail_id, Long order_id, Long sku_id, BigDecimal order_price, Long sku_num, String sku_name, Long province_id, String order_status, Long user_id, BigDecimal total_amount, BigDecimal activity_reduce_amount, BigDecimal coupon_reduce_amount, BigDecimal original_total_amount, BigDecimal feight_fee, BigDecimal split_feight_fee, BigDecimal split_activity_amount, BigDecimal split_coupon_amount, BigDecimal split_total_amount, String expire_time, String create_time, String operate_time, String create_date, String create_hour, String province_name, String province_area_code, String province_iso_code, String province_3166_2_code, Integer user_age, String user_gender, Long spu_id, Long tm_id, Long category3_id, String spu_name, String tm_name, String category3_name) {
        this.detail_id = detail_id;
        this.order_id = order_id;
        this.sku_id = sku_id;
        this.order_price = order_price;
        this.sku_num = sku_num;
        this.sku_name = sku_name;
        this.province_id = province_id;
        this.order_status = order_status;
        this.user_id = user_id;
        this.total_amount = total_amount;
        this.activity_reduce_amount = activity_reduce_amount;
        this.coupon_reduce_amount = coupon_reduce_amount;
        this.original_total_amount = original_total_amount;
        this.feight_fee = feight_fee;
        this.split_feight_fee = split_feight_fee;
        this.split_activity_amount = split_activity_amount;
        this.split_coupon_amount = split_coupon_amount;
        this.split_total_amount = split_total_amount;
        this.expire_time = expire_time;
        this.create_time = create_time;
        this.operate_time = operate_time;
        this.create_date = create_date;
        this.create_hour = create_hour;
        this.province_name = province_name;
        this.province_area_code = province_area_code;
        this.province_iso_code = province_iso_code;
        this.province_3166_2_code = province_3166_2_code;
        this.user_age = user_age;
        this.user_gender = user_gender;
        this.spu_id = spu_id;
        this.tm_id = tm_id;
        this.category3_id = category3_id;
        this.spu_name = spu_name;
        this.tm_name = tm_name;
        this.category3_name = category3_name;
    }

    // 将订单的信息赋值给订单宽表
    public void  mergeOrderInfo(OrderInfo orderInfo  )  {
        if (orderInfo != null) {
            this.order_id = orderInfo.id;
            this.order_status = orderInfo.order_status;
            this.create_time = orderInfo.create_time;
            this.create_date = orderInfo.create_date;
            this.activity_reduce_amount = orderInfo.activity_reduce_amount;
            this.coupon_reduce_amount = orderInfo.coupon_reduce_amount;
            this.original_total_amount = orderInfo.original_total_amount;
            this.feight_fee = orderInfo.feight_fee;
            this.total_amount =  orderInfo.total_amount;
            this.province_id = orderInfo.province_id;
            this.user_id = orderInfo.user_id;
        }
    }

    // 将订单明细的信息赋值给订单宽表
    public void mergeOrderDetail(OrderDetail orderDetail  )  {
        if (orderDetail != null) {
            this.detail_id = orderDetail.id;
            this.sku_id = orderDetail.sku_id;
            this.sku_name = orderDetail.sku_name;
            this.order_price = orderDetail.order_price;
            this.sku_num = orderDetail.sku_num;
            this.split_activity_amount=orderDetail.split_activity_amount;
            this.split_coupon_amount=orderDetail.split_coupon_amount;
            this.split_total_amount=orderDetail.split_total_amount;
        }
    }

    // firstNonNull获取参数中第一个不为空的值
    public void mergeOtherOrderWide(OrderWide otherOrderWide){
        this.order_status = ObjectUtils.firstNonNull( this.order_status ,otherOrderWide.order_status);
        this.create_time =  ObjectUtils.firstNonNull(this.create_time,otherOrderWide.create_time);
        this.create_date =  ObjectUtils.firstNonNull(this.create_date,otherOrderWide.create_date);
        this.coupon_reduce_amount =  ObjectUtils.firstNonNull(this.coupon_reduce_amount,otherOrderWide.coupon_reduce_amount);
        this.activity_reduce_amount =  ObjectUtils.firstNonNull(this.activity_reduce_amount,otherOrderWide.activity_reduce_amount);
        this.original_total_amount =  ObjectUtils.firstNonNull(this.original_total_amount,otherOrderWide.original_total_amount);
        this.feight_fee = ObjectUtils.firstNonNull( this.feight_fee,otherOrderWide.feight_fee);
        this.total_amount =  ObjectUtils.firstNonNull( this.total_amount,otherOrderWide.total_amount);
        this.user_id =  ObjectUtils.<Long>firstNonNull(this.user_id,otherOrderWide.user_id);
        this.sku_id = ObjectUtils.firstNonNull( this.sku_id,otherOrderWide.sku_id);
        this.sku_name =  ObjectUtils.firstNonNull(this.sku_name,otherOrderWide.sku_name);
        this.order_price =  ObjectUtils.firstNonNull(this.order_price,otherOrderWide.order_price);
        this.sku_num = ObjectUtils.firstNonNull( this.sku_num,otherOrderWide.sku_num);
        this.split_activity_amount=ObjectUtils.firstNonNull(this.split_activity_amount);
        this.split_coupon_amount=ObjectUtils.firstNonNull(this.split_coupon_amount);
        this.split_total_amount=ObjectUtils.firstNonNull(this.split_total_amount);
    }

    public Long getDetail_id() {
        return this.detail_id;
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

    public BigDecimal getSplit_feight_fee() {
        return this.split_feight_fee;
    }

    public BigDecimal getSplit_activity_amount() {
        return this.split_activity_amount;
    }

    public BigDecimal getSplit_coupon_amount() {
        return this.split_coupon_amount;
    }

    public BigDecimal getSplit_total_amount() {
        return this.split_total_amount;
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

    public String getProvince_name() {
        return this.province_name;
    }

    public String getProvince_area_code() {
        return this.province_area_code;
    }

    public String getProvince_iso_code() {
        return this.province_iso_code;
    }

    public String getProvince_3166_2_code() {
        return this.province_3166_2_code;
    }

    public Integer getUser_age() {
        return this.user_age;
    }

    public String getUser_gender() {
        return this.user_gender;
    }

    public Long getSpu_id() {
        return this.spu_id;
    }

    public Long getTm_id() {
        return this.tm_id;
    }

    public Long getCategory3_id() {
        return this.category3_id;
    }

    public String getSpu_name() {
        return this.spu_name;
    }

    public String getTm_name() {
        return this.tm_name;
    }

    public String getCategory3_name() {
        return this.category3_name;
    }

    public void setDetail_id(Long detail_id) {
        this.detail_id = detail_id;
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

    public void setSplit_feight_fee(BigDecimal split_feight_fee) {
        this.split_feight_fee = split_feight_fee;
    }

    public void setSplit_activity_amount(BigDecimal split_activity_amount) {
        this.split_activity_amount = split_activity_amount;
    }

    public void setSplit_coupon_amount(BigDecimal split_coupon_amount) {
        this.split_coupon_amount = split_coupon_amount;
    }

    public void setSplit_total_amount(BigDecimal split_total_amount) {
        this.split_total_amount = split_total_amount;
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

    public void setProvince_name(String province_name) {
        this.province_name = province_name;
    }

    public void setProvince_area_code(String province_area_code) {
        this.province_area_code = province_area_code;
    }

    public void setProvince_iso_code(String province_iso_code) {
        this.province_iso_code = province_iso_code;
    }

    public void setProvince_3166_2_code(String province_3166_2_code) {
        this.province_3166_2_code = province_3166_2_code;
    }

    public void setUser_age(Integer user_age) {
        this.user_age = user_age;
    }

    public void setUser_gender(String user_gender) {
        this.user_gender = user_gender;
    }

    public void setSpu_id(Long spu_id) {
        this.spu_id = spu_id;
    }

    public void setTm_id(Long tm_id) {
        this.tm_id = tm_id;
    }

    public void setCategory3_id(Long category3_id) {
        this.category3_id = category3_id;
    }

    public void setSpu_name(String spu_name) {
        this.spu_name = spu_name;
    }

    public void setTm_name(String tm_name) {
        this.tm_name = tm_name;
    }

    public void setCategory3_name(String category3_name) {
        this.category3_name = category3_name;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof OrderWide)) return false;
        final OrderWide other = (OrderWide) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$detail_id = this.getDetail_id();
        final Object other$detail_id = other.getDetail_id();
        if (this$detail_id == null ? other$detail_id != null : !this$detail_id.equals(other$detail_id)) return false;
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
        final Object this$split_feight_fee = this.getSplit_feight_fee();
        final Object other$split_feight_fee = other.getSplit_feight_fee();
        if (this$split_feight_fee == null ? other$split_feight_fee != null : !this$split_feight_fee.equals(other$split_feight_fee))
            return false;
        final Object this$split_activity_amount = this.getSplit_activity_amount();
        final Object other$split_activity_amount = other.getSplit_activity_amount();
        if (this$split_activity_amount == null ? other$split_activity_amount != null : !this$split_activity_amount.equals(other$split_activity_amount))
            return false;
        final Object this$split_coupon_amount = this.getSplit_coupon_amount();
        final Object other$split_coupon_amount = other.getSplit_coupon_amount();
        if (this$split_coupon_amount == null ? other$split_coupon_amount != null : !this$split_coupon_amount.equals(other$split_coupon_amount))
            return false;
        final Object this$split_total_amount = this.getSplit_total_amount();
        final Object other$split_total_amount = other.getSplit_total_amount();
        if (this$split_total_amount == null ? other$split_total_amount != null : !this$split_total_amount.equals(other$split_total_amount))
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
        final Object this$province_name = this.getProvince_name();
        final Object other$province_name = other.getProvince_name();
        if (this$province_name == null ? other$province_name != null : !this$province_name.equals(other$province_name))
            return false;
        final Object this$province_area_code = this.getProvince_area_code();
        final Object other$province_area_code = other.getProvince_area_code();
        if (this$province_area_code == null ? other$province_area_code != null : !this$province_area_code.equals(other$province_area_code))
            return false;
        final Object this$province_iso_code = this.getProvince_iso_code();
        final Object other$province_iso_code = other.getProvince_iso_code();
        if (this$province_iso_code == null ? other$province_iso_code != null : !this$province_iso_code.equals(other$province_iso_code))
            return false;
        final Object this$province_3166_2_code = this.getProvince_3166_2_code();
        final Object other$province_3166_2_code = other.getProvince_3166_2_code();
        if (this$province_3166_2_code == null ? other$province_3166_2_code != null : !this$province_3166_2_code.equals(other$province_3166_2_code))
            return false;
        final Object this$user_age = this.getUser_age();
        final Object other$user_age = other.getUser_age();
        if (this$user_age == null ? other$user_age != null : !this$user_age.equals(other$user_age)) return false;
        final Object this$user_gender = this.getUser_gender();
        final Object other$user_gender = other.getUser_gender();
        if (this$user_gender == null ? other$user_gender != null : !this$user_gender.equals(other$user_gender))
            return false;
        final Object this$spu_id = this.getSpu_id();
        final Object other$spu_id = other.getSpu_id();
        if (this$spu_id == null ? other$spu_id != null : !this$spu_id.equals(other$spu_id)) return false;
        final Object this$tm_id = this.getTm_id();
        final Object other$tm_id = other.getTm_id();
        if (this$tm_id == null ? other$tm_id != null : !this$tm_id.equals(other$tm_id)) return false;
        final Object this$category3_id = this.getCategory3_id();
        final Object other$category3_id = other.getCategory3_id();
        if (this$category3_id == null ? other$category3_id != null : !this$category3_id.equals(other$category3_id))
            return false;
        final Object this$spu_name = this.getSpu_name();
        final Object other$spu_name = other.getSpu_name();
        if (this$spu_name == null ? other$spu_name != null : !this$spu_name.equals(other$spu_name)) return false;
        final Object this$tm_name = this.getTm_name();
        final Object other$tm_name = other.getTm_name();
        if (this$tm_name == null ? other$tm_name != null : !this$tm_name.equals(other$tm_name)) return false;
        final Object this$category3_name = this.getCategory3_name();
        final Object other$category3_name = other.getCategory3_name();
        if (this$category3_name == null ? other$category3_name != null : !this$category3_name.equals(other$category3_name))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof OrderWide;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $detail_id = this.getDetail_id();
        result = result * PRIME + ($detail_id == null ? 43 : $detail_id.hashCode());
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
        final Object $split_feight_fee = this.getSplit_feight_fee();
        result = result * PRIME + ($split_feight_fee == null ? 43 : $split_feight_fee.hashCode());
        final Object $split_activity_amount = this.getSplit_activity_amount();
        result = result * PRIME + ($split_activity_amount == null ? 43 : $split_activity_amount.hashCode());
        final Object $split_coupon_amount = this.getSplit_coupon_amount();
        result = result * PRIME + ($split_coupon_amount == null ? 43 : $split_coupon_amount.hashCode());
        final Object $split_total_amount = this.getSplit_total_amount();
        result = result * PRIME + ($split_total_amount == null ? 43 : $split_total_amount.hashCode());
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
        final Object $province_name = this.getProvince_name();
        result = result * PRIME + ($province_name == null ? 43 : $province_name.hashCode());
        final Object $province_area_code = this.getProvince_area_code();
        result = result * PRIME + ($province_area_code == null ? 43 : $province_area_code.hashCode());
        final Object $province_iso_code = this.getProvince_iso_code();
        result = result * PRIME + ($province_iso_code == null ? 43 : $province_iso_code.hashCode());
        final Object $province_3166_2_code = this.getProvince_3166_2_code();
        result = result * PRIME + ($province_3166_2_code == null ? 43 : $province_3166_2_code.hashCode());
        final Object $user_age = this.getUser_age();
        result = result * PRIME + ($user_age == null ? 43 : $user_age.hashCode());
        final Object $user_gender = this.getUser_gender();
        result = result * PRIME + ($user_gender == null ? 43 : $user_gender.hashCode());
        final Object $spu_id = this.getSpu_id();
        result = result * PRIME + ($spu_id == null ? 43 : $spu_id.hashCode());
        final Object $tm_id = this.getTm_id();
        result = result * PRIME + ($tm_id == null ? 43 : $tm_id.hashCode());
        final Object $category3_id = this.getCategory3_id();
        result = result * PRIME + ($category3_id == null ? 43 : $category3_id.hashCode());
        final Object $spu_name = this.getSpu_name();
        result = result * PRIME + ($spu_name == null ? 43 : $spu_name.hashCode());
        final Object $tm_name = this.getTm_name();
        result = result * PRIME + ($tm_name == null ? 43 : $tm_name.hashCode());
        final Object $category3_name = this.getCategory3_name();
        result = result * PRIME + ($category3_name == null ? 43 : $category3_name.hashCode());
        return result;
    }

    public String toString() {
        return "OrderWide(detail_id=" + this.getDetail_id() + ", order_id=" + this.getOrder_id() + ", sku_id=" + this.getSku_id() + ", order_price=" + this.getOrder_price() + ", sku_num=" + this.getSku_num() + ", sku_name=" + this.getSku_name() + ", province_id=" + this.getProvince_id() + ", order_status=" + this.getOrder_status() + ", user_id=" + this.getUser_id() + ", total_amount=" + this.getTotal_amount() + ", activity_reduce_amount=" + this.getActivity_reduce_amount() + ", coupon_reduce_amount=" + this.getCoupon_reduce_amount() + ", original_total_amount=" + this.getOriginal_total_amount() + ", feight_fee=" + this.getFeight_fee() + ", split_feight_fee=" + this.getSplit_feight_fee() + ", split_activity_amount=" + this.getSplit_activity_amount() + ", split_coupon_amount=" + this.getSplit_coupon_amount() + ", split_total_amount=" + this.getSplit_total_amount() + ", expire_time=" + this.getExpire_time() + ", create_time=" + this.getCreate_time() + ", operate_time=" + this.getOperate_time() + ", create_date=" + this.getCreate_date() + ", create_hour=" + this.getCreate_hour() + ", province_name=" + this.getProvince_name() + ", province_area_code=" + this.getProvince_area_code() + ", province_iso_code=" + this.getProvince_iso_code() + ", province_3166_2_code=" + this.getProvince_3166_2_code() + ", user_age=" + this.getUser_age() + ", user_gender=" + this.getUser_gender() + ", spu_id=" + this.getSpu_id() + ", tm_id=" + this.getTm_id() + ", category3_id=" + this.getCategory3_id() + ", spu_name=" + this.getSpu_name() + ", tm_name=" + this.getTm_name() + ", category3_name=" + this.getCategory3_name() + ")";
    }
}