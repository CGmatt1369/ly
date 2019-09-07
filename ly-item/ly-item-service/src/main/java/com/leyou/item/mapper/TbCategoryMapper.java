package com.leyou.item.mapper;

import com.leyou.item.entity.TbCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 Mapper 接口
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbCategoryMapper extends BaseMapper<TbCategory> {
    //自定义查询，品牌查商品
    @Select("SELECT c.* FROM tb_category_brand a INNER JOIN tb_category c ON a.category_id=c.id WHERE a.brand_id=#{bid}")
    List<TbCategory> selectBrandJoinCategory(@Param("bid") Long brandId);

}
