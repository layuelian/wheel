package com.thomas.elasticsearch.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: lf
 * @Date: 2019/5/24 9:34
 */
public class SearchResult {
    private Long total;//记录总数
    private Float useTime;//搜索花费时间
    private String distance;//距离单位(米)
    //数据集合
    private List<Map<String, Object>> data = new ArrayList<>();

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Float getUseTime() {
        return useTime;
    }

    public void setUseTime(Float useTime) {
        this.useTime = useTime;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
