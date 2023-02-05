package com.xiaozhu.etl.bean;

public class TableProcess {
    //动态分流Sink常量，改为小写和脚本一致
    public static final String SINK_TYPE_HBASE = "hbase";
    public static final String SINK_TYPE_KAFKA = "kafka";
    public static final String SINK_TYPE_CK = "clickhouse";
    //来源表
    String sourceTable;
    //操作类型 insert,update,delete
    String operateType;
    //输出类型 hbase kafka
    String sinkType;
    //输出表(主题)
    String sinkTable;
    //输出字段
    String sinkColumns;
    //主键字段
    String sinkPk;
    //建表扩展
    String sinkExtend;

    public TableProcess() {
    }

    public String getSourceTable() {
        return this.sourceTable;
    }

    public String getOperateType() {
        return this.operateType;
    }

    public String getSinkType() {
        return this.sinkType;
    }

    public String getSinkTable() {
        return this.sinkTable;
    }

    public String getSinkColumns() {
        return this.sinkColumns;
    }

    public String getSinkPk() {
        return this.sinkPk;
    }

    public String getSinkExtend() {
        return this.sinkExtend;
    }

    public void setSourceTable(String sourceTable) {
        this.sourceTable = sourceTable;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public void setSinkType(String sinkType) {
        this.sinkType = sinkType;
    }

    public void setSinkTable(String sinkTable) {
        this.sinkTable = sinkTable;
    }

    public void setSinkColumns(String sinkColumns) {
        this.sinkColumns = sinkColumns;
    }

    public void setSinkPk(String sinkPk) {
        this.sinkPk = sinkPk;
    }

    public void setSinkExtend(String sinkExtend) {
        this.sinkExtend = sinkExtend;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TableProcess)) return false;
        final TableProcess other = (TableProcess) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$sourceTable = this.getSourceTable();
        final Object other$sourceTable = other.getSourceTable();
        if (this$sourceTable == null ? other$sourceTable != null : !this$sourceTable.equals(other$sourceTable))
            return false;
        final Object this$operateType = this.getOperateType();
        final Object other$operateType = other.getOperateType();
        if (this$operateType == null ? other$operateType != null : !this$operateType.equals(other$operateType))
            return false;
        final Object this$sinkType = this.getSinkType();
        final Object other$sinkType = other.getSinkType();
        if (this$sinkType == null ? other$sinkType != null : !this$sinkType.equals(other$sinkType)) return false;
        final Object this$sinkTable = this.getSinkTable();
        final Object other$sinkTable = other.getSinkTable();
        if (this$sinkTable == null ? other$sinkTable != null : !this$sinkTable.equals(other$sinkTable)) return false;
        final Object this$sinkColumns = this.getSinkColumns();
        final Object other$sinkColumns = other.getSinkColumns();
        if (this$sinkColumns == null ? other$sinkColumns != null : !this$sinkColumns.equals(other$sinkColumns))
            return false;
        final Object this$sinkPk = this.getSinkPk();
        final Object other$sinkPk = other.getSinkPk();
        if (this$sinkPk == null ? other$sinkPk != null : !this$sinkPk.equals(other$sinkPk)) return false;
        final Object this$sinkExtend = this.getSinkExtend();
        final Object other$sinkExtend = other.getSinkExtend();
        if (this$sinkExtend == null ? other$sinkExtend != null : !this$sinkExtend.equals(other$sinkExtend))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TableProcess;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $sourceTable = this.getSourceTable();
        result = result * PRIME + ($sourceTable == null ? 43 : $sourceTable.hashCode());
        final Object $operateType = this.getOperateType();
        result = result * PRIME + ($operateType == null ? 43 : $operateType.hashCode());
        final Object $sinkType = this.getSinkType();
        result = result * PRIME + ($sinkType == null ? 43 : $sinkType.hashCode());
        final Object $sinkTable = this.getSinkTable();
        result = result * PRIME + ($sinkTable == null ? 43 : $sinkTable.hashCode());
        final Object $sinkColumns = this.getSinkColumns();
        result = result * PRIME + ($sinkColumns == null ? 43 : $sinkColumns.hashCode());
        final Object $sinkPk = this.getSinkPk();
        result = result * PRIME + ($sinkPk == null ? 43 : $sinkPk.hashCode());
        final Object $sinkExtend = this.getSinkExtend();
        result = result * PRIME + ($sinkExtend == null ? 43 : $sinkExtend.hashCode());
        return result;
    }

    public String toString() {
        return "TableProcess(sourceTable=" + this.getSourceTable() + ", operateType=" + this.getOperateType() + ", sinkType=" + this.getSinkType() + ", sinkTable=" + this.getSinkTable() + ", sinkColumns=" + this.getSinkColumns() + ", sinkPk=" + this.getSinkPk() + ", sinkExtend=" + this.getSinkExtend() + ")";
    }
}
