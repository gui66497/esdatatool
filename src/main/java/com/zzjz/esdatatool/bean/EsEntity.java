package com.zzjz.esdatatool.bean;

/**
 * @Description Todo
 * @Author 房桂堂
 * @Date 2019/9/18 9:03
 */
public class EsEntity {

    /**
     * es地址 (例如http://ip:9200)
     */
    String esCon;

    /**
     * 目标es地址 (例如http://ip:9200)
     */
    String targetEsCon;

    /**
     * 索引 (一般是-或_)
     */
    String index;

    /**
     * 分隔符
     */
    String separator;

    /**
     * 日期 (可以是2019.10.01也可以是2018.10.01-2018.10.12)
     */
    String dates;

    public String getEsCon() {
        return esCon;
    }

    public void setEsCon(String esCon) {
        this.esCon = esCon;
    }

    public String getTargetEsCon() {
        return targetEsCon;
    }

    public void setTargetEsCon(String targetEsCon) {
        this.targetEsCon = targetEsCon;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }
}
