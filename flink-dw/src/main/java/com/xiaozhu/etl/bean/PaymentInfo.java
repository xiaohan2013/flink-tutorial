package com.xiaozhu.etl.bean;

import java.math.BigDecimal;

/**
 * Desc: 支付信息实体类
 */
public class PaymentInfo {
    Long id;
    Long order_id;
    Long user_id;
    BigDecimal total_amount;
    String subject;
    String payment_type;
    String create_time;
    String callback_time;   // 支付成功的时间需要看回调时间而不是创建时间

    public PaymentInfo() {
    }

    public Long getId() {
        return this.id;
    }

    public Long getOrder_id() {
        return this.order_id;
    }

    public Long getUser_id() {
        return this.user_id;
    }

    public BigDecimal getTotal_amount() {
        return this.total_amount;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getPayment_type() {
        return this.payment_type;
    }

    public String getCreate_time() {
        return this.create_time;
    }

    public String getCallback_time() {
        return this.callback_time;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setOrder_id(Long order_id) {
        this.order_id = order_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public void setTotal_amount(BigDecimal total_amount) {
        this.total_amount = total_amount;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setPayment_type(String payment_type) {
        this.payment_type = payment_type;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public void setCallback_time(String callback_time) {
        this.callback_time = callback_time;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof PaymentInfo)) return false;
        final PaymentInfo other = (PaymentInfo) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$order_id = this.getOrder_id();
        final Object other$order_id = other.getOrder_id();
        if (this$order_id == null ? other$order_id != null : !this$order_id.equals(other$order_id)) return false;
        final Object this$user_id = this.getUser_id();
        final Object other$user_id = other.getUser_id();
        if (this$user_id == null ? other$user_id != null : !this$user_id.equals(other$user_id)) return false;
        final Object this$total_amount = this.getTotal_amount();
        final Object other$total_amount = other.getTotal_amount();
        if (this$total_amount == null ? other$total_amount != null : !this$total_amount.equals(other$total_amount))
            return false;
        final Object this$subject = this.getSubject();
        final Object other$subject = other.getSubject();
        if (this$subject == null ? other$subject != null : !this$subject.equals(other$subject)) return false;
        final Object this$payment_type = this.getPayment_type();
        final Object other$payment_type = other.getPayment_type();
        if (this$payment_type == null ? other$payment_type != null : !this$payment_type.equals(other$payment_type))
            return false;
        final Object this$create_time = this.getCreate_time();
        final Object other$create_time = other.getCreate_time();
        if (this$create_time == null ? other$create_time != null : !this$create_time.equals(other$create_time))
            return false;
        final Object this$callback_time = this.getCallback_time();
        final Object other$callback_time = other.getCallback_time();
        if (this$callback_time == null ? other$callback_time != null : !this$callback_time.equals(other$callback_time))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof PaymentInfo;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $order_id = this.getOrder_id();
        result = result * PRIME + ($order_id == null ? 43 : $order_id.hashCode());
        final Object $user_id = this.getUser_id();
        result = result * PRIME + ($user_id == null ? 43 : $user_id.hashCode());
        final Object $total_amount = this.getTotal_amount();
        result = result * PRIME + ($total_amount == null ? 43 : $total_amount.hashCode());
        final Object $subject = this.getSubject();
        result = result * PRIME + ($subject == null ? 43 : $subject.hashCode());
        final Object $payment_type = this.getPayment_type();
        result = result * PRIME + ($payment_type == null ? 43 : $payment_type.hashCode());
        final Object $create_time = this.getCreate_time();
        result = result * PRIME + ($create_time == null ? 43 : $create_time.hashCode());
        final Object $callback_time = this.getCallback_time();
        result = result * PRIME + ($callback_time == null ? 43 : $callback_time.hashCode());
        return result;
    }

    public String toString() {
        return "PaymentInfo(id=" + this.getId() + ", order_id=" + this.getOrder_id() + ", user_id=" + this.getUser_id() + ", total_amount=" + this.getTotal_amount() + ", subject=" + this.getSubject() + ", payment_type=" + this.getPayment_type() + ", create_time=" + this.getCreate_time() + ", callback_time=" + this.getCallback_time() + ")";
    }
}
