package com.leyou.item.service;

import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.item.pojo.DTO.BrandDTO;

import java.util.List;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
public interface TbBrandService extends IService<TbBrand> {

    PageResult<BrandDTO> searchPage(Integer page, Integer rows, String key, String sortBy, Boolean desc);

    void saveBrandAndCategory(TbBrand brand, List<Long> cids);

    void updateBrandAndCategory(TbBrand tbBrand, List<Long> cids);

    List<BrandDTO> findBrandByCategoryId(Long categoryId);
}
