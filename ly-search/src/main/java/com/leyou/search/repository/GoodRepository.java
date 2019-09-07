package com.leyou.search.repository;

import com.leyou.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/3
 * @描述 调用sde接口，实现es基本的crud
 */
public interface GoodRepository extends ElasticsearchRepository<Goods,Long> {
}
