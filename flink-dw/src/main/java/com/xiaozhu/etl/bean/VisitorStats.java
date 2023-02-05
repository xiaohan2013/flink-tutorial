package com.xiaozhu.etl.bean;

/**
 * Desc: 访客统计实体类  包括各个维度和度量
 */
public class VisitorStats {
    //统计开始时间
    private String stt;
    //统计结束时间
    private String edt;
    //维度：版本
    private String vc;
    //维度：渠道
    private String ch;
    //维度：地区
    private String ar;
    //维度：新老用户标识
    private String is_new;
    //度量：独立访客数
    private Long uv_ct=0L;
    //度量：页面访问数
    private Long pv_ct=0L;
    //度量： 进入次数
    private Long sv_ct=0L;
    //度量： 跳出次数
    private Long uj_ct=0L;
    //度量： 持续访问时间
    private Long dur_sum=0L;
    //统计时间
    private Long ts;

    public VisitorStats(String stt, String edt, String vc, String ch, String ar, String is_new, Long uv_ct, Long pv_ct, Long sv_ct, Long uj_ct, Long dur_sum, Long ts) {
        this.stt = stt;
        this.edt = edt;
        this.vc = vc;
        this.ch = ch;
        this.ar = ar;
        this.is_new = is_new;
        this.uv_ct = uv_ct;
        this.pv_ct = pv_ct;
        this.sv_ct = sv_ct;
        this.uj_ct = uj_ct;
        this.dur_sum = dur_sum;
        this.ts = ts;
    }

    public String getStt() {
        return this.stt;
    }

    public String getEdt() {
        return this.edt;
    }

    public String getVc() {
        return this.vc;
    }

    public String getCh() {
        return this.ch;
    }

    public String getAr() {
        return this.ar;
    }

    public String getIs_new() {
        return this.is_new;
    }

    public Long getUv_ct() {
        return this.uv_ct;
    }

    public Long getPv_ct() {
        return this.pv_ct;
    }

    public Long getSv_ct() {
        return this.sv_ct;
    }

    public Long getUj_ct() {
        return this.uj_ct;
    }

    public Long getDur_sum() {
        return this.dur_sum;
    }

    public Long getTs() {
        return this.ts;
    }

    public void setStt(String stt) {
        this.stt = stt;
    }

    public void setEdt(String edt) {
        this.edt = edt;
    }

    public void setVc(String vc) {
        this.vc = vc;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    public void setAr(String ar) {
        this.ar = ar;
    }

    public void setIs_new(String is_new) {
        this.is_new = is_new;
    }

    public void setUv_ct(Long uv_ct) {
        this.uv_ct = uv_ct;
    }

    public void setPv_ct(Long pv_ct) {
        this.pv_ct = pv_ct;
    }

    public void setSv_ct(Long sv_ct) {
        this.sv_ct = sv_ct;
    }

    public void setUj_ct(Long uj_ct) {
        this.uj_ct = uj_ct;
    }

    public void setDur_sum(Long dur_sum) {
        this.dur_sum = dur_sum;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof VisitorStats)) return false;
        final VisitorStats other = (VisitorStats) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$stt = this.getStt();
        final Object other$stt = other.getStt();
        if (this$stt == null ? other$stt != null : !this$stt.equals(other$stt)) return false;
        final Object this$edt = this.getEdt();
        final Object other$edt = other.getEdt();
        if (this$edt == null ? other$edt != null : !this$edt.equals(other$edt)) return false;
        final Object this$vc = this.getVc();
        final Object other$vc = other.getVc();
        if (this$vc == null ? other$vc != null : !this$vc.equals(other$vc)) return false;
        final Object this$ch = this.getCh();
        final Object other$ch = other.getCh();
        if (this$ch == null ? other$ch != null : !this$ch.equals(other$ch)) return false;
        final Object this$ar = this.getAr();
        final Object other$ar = other.getAr();
        if (this$ar == null ? other$ar != null : !this$ar.equals(other$ar)) return false;
        final Object this$is_new = this.getIs_new();
        final Object other$is_new = other.getIs_new();
        if (this$is_new == null ? other$is_new != null : !this$is_new.equals(other$is_new)) return false;
        final Object this$uv_ct = this.getUv_ct();
        final Object other$uv_ct = other.getUv_ct();
        if (this$uv_ct == null ? other$uv_ct != null : !this$uv_ct.equals(other$uv_ct)) return false;
        final Object this$pv_ct = this.getPv_ct();
        final Object other$pv_ct = other.getPv_ct();
        if (this$pv_ct == null ? other$pv_ct != null : !this$pv_ct.equals(other$pv_ct)) return false;
        final Object this$sv_ct = this.getSv_ct();
        final Object other$sv_ct = other.getSv_ct();
        if (this$sv_ct == null ? other$sv_ct != null : !this$sv_ct.equals(other$sv_ct)) return false;
        final Object this$uj_ct = this.getUj_ct();
        final Object other$uj_ct = other.getUj_ct();
        if (this$uj_ct == null ? other$uj_ct != null : !this$uj_ct.equals(other$uj_ct)) return false;
        final Object this$dur_sum = this.getDur_sum();
        final Object other$dur_sum = other.getDur_sum();
        if (this$dur_sum == null ? other$dur_sum != null : !this$dur_sum.equals(other$dur_sum)) return false;
        final Object this$ts = this.getTs();
        final Object other$ts = other.getTs();
        if (this$ts == null ? other$ts != null : !this$ts.equals(other$ts)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof VisitorStats;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $stt = this.getStt();
        result = result * PRIME + ($stt == null ? 43 : $stt.hashCode());
        final Object $edt = this.getEdt();
        result = result * PRIME + ($edt == null ? 43 : $edt.hashCode());
        final Object $vc = this.getVc();
        result = result * PRIME + ($vc == null ? 43 : $vc.hashCode());
        final Object $ch = this.getCh();
        result = result * PRIME + ($ch == null ? 43 : $ch.hashCode());
        final Object $ar = this.getAr();
        result = result * PRIME + ($ar == null ? 43 : $ar.hashCode());
        final Object $is_new = this.getIs_new();
        result = result * PRIME + ($is_new == null ? 43 : $is_new.hashCode());
        final Object $uv_ct = this.getUv_ct();
        result = result * PRIME + ($uv_ct == null ? 43 : $uv_ct.hashCode());
        final Object $pv_ct = this.getPv_ct();
        result = result * PRIME + ($pv_ct == null ? 43 : $pv_ct.hashCode());
        final Object $sv_ct = this.getSv_ct();
        result = result * PRIME + ($sv_ct == null ? 43 : $sv_ct.hashCode());
        final Object $uj_ct = this.getUj_ct();
        result = result * PRIME + ($uj_ct == null ? 43 : $uj_ct.hashCode());
        final Object $dur_sum = this.getDur_sum();
        result = result * PRIME + ($dur_sum == null ? 43 : $dur_sum.hashCode());
        final Object $ts = this.getTs();
        result = result * PRIME + ($ts == null ? 43 : $ts.hashCode());
        return result;
    }

    public String toString() {
        return "VisitorStats(stt=" + this.getStt() + ", edt=" + this.getEdt() + ", vc=" + this.getVc() + ", ch=" + this.getCh() + ", ar=" + this.getAr() + ", is_new=" + this.getIs_new() + ", uv_ct=" + this.getUv_ct() + ", pv_ct=" + this.getPv_ct() + ", sv_ct=" + this.getSv_ct() + ", uj_ct=" + this.getUj_ct() + ", dur_sum=" + this.getDur_sum() + ", ts=" + this.getTs() + ")";
    }
}

