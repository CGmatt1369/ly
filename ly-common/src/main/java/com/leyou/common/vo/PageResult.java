package com.leyou.common.vo;

import lombok.Data;

import java.util.List;

/**
 * 封装一个分页
 * @param <T>
 */
@Data
public class PageResult<T> {
    private Long total;// 总条数
    private Integer totalPage;// 总页数
    private List<T> items;// 当前页数据

    public PageResult() {
    }

    public PageResult(Long total, List<T> items) {
        this.total = total;
        this.items = items;
    }

    public PageResult( List<T> items,Long total, Integer totalPage) {
        this.total = total;
        this.totalPage = totalPage;
        this.items = items;
    }
}