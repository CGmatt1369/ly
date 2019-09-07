package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.pojo.DTO.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    @Override
    public List<CategoryDTO> findCategoryListByParentId(Long pid) {
        //条件查询 父ID
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("parent_id",pid);//这样做要提前知道列名
        queryWrapper.lambda().eq(TbCategory::getParentId,pid);//面向对象方式条件查询，内部引用一个method类型，可以反射得到该名称并取得关联属性和字段名
        List<TbCategory> categoryList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(categoryList)){
            //空的抛出异常
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        //否则返回DTO的集合
        return BeanHelper.copyWithCollection(categoryList,CategoryDTO.class);
    }

    @Override//id获得分类信息
    public List<CategoryDTO> queryCategoryListByBrandId(Long brandId) {
        //查询 因为有中间表，所有方法需要自定义
        List<TbCategory> tbCategoryList=this.baseMapper.selectBrandJoinCategory(brandId);
        //判断
        if (CollectionUtils.isEmpty(tbCategoryList)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);
    }
}
