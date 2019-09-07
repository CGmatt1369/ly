package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.pojo.DTO.BrandDTO;
import com.leyou.item.service.TbBrandService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.item.service.TbCategoryBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author HM
 * @since 2019-08-27
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Override
    public PageResult<BrandDTO> searchPage(Integer page, Integer rows, String key, String sortBy, Boolean desc) {
        //分页插件mybatis-plus
        Page<TbBrand> p = new Page<>(page, rows);
        //条件查
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().like(TbBrand::getName, key).or().like(TbBrand::getLetter, key);
        }
        if (!StringUtils.isEmpty(sortBy)) {
            if (desc) p.setDesc(sortBy);
            else p.setAsc(sortBy);
        }
        IPage<TbBrand> page1 = this.page(p, queryWrapper);
        if (page1 == null || CollectionUtils.isEmpty(page1.getRecords())) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //转DTO
        List<BrandDTO> brandDTOS = BeanHelper.copyWithCollection(page1.getRecords(), BrandDTO.class);
        return new PageResult(brandDTOS, page1.getTotal(), Integer.parseInt(String.valueOf(page1.getPages())));
    }
@Autowired
private TbCategoryBrandService categoryBrandService;

    @Override
    @Transactional
    public void saveBrandAndCategory(TbBrand brand, List<Long> cids) {
        // 保存品牌表
        boolean brandB = this.save(brand);
        if (!brandB) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //得到品牌id
        Long bid = brand.getId();
        //判断cid商品Id
        if (!CollectionUtils.isEmpty(cids)) {
            ArrayList<TbCategoryBrand> tbCategoryBrands = new ArrayList<>();
            for (Long cid : cids) {
                TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
                tbCategoryBrand.setBrandId(bid);
                tbCategoryBrand.setCategoryId(cid);
                tbCategoryBrands.add(tbCategoryBrand);
            }
            // 保存中间表
            categoryBrandService.saveBatch(tbCategoryBrands);
        }
    }

    @Override
    @Transactional
    public void updateBrandAndCategory(TbBrand tbBrand, List<Long> cids) {
        //保存品牌信息
        boolean brandB = this.updateById(tbBrand);
        if (!brandB){
            throw  new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //得到品牌id
        Long brandId = tbBrand.getId();
        if (!CollectionUtils.isEmpty(cids)){
            //先删除之前的商品分类(第三表里面)  通过品牌id
            QueryWrapper<TbCategoryBrand> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TbCategoryBrand::getBrandId,brandId);
            boolean remove = categoryBrandService.remove(queryWrapper);
            if (!remove){
                throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
            }
            //之后添加新的cid
            List<TbCategoryBrand> categoryBrands =cids.stream().map(cid->{
                TbCategoryBrand tbCategoryBrand = new TbCategoryBrand();
                tbCategoryBrand.setCategoryId(cid);
                tbCategoryBrand.setBrandId(brandId);
                return tbCategoryBrand;
            }).collect(Collectors.toList());
            categoryBrandService.saveBatch(categoryBrands);
        }
    }
    @Autowired
    private TbBrandService brandService;
    @Override
    public List<BrandDTO> findBrandByCategoryId(Long categoryId) {
        //要通过第三表来查，自定义sql
        List<TbBrand> brands=this.baseMapper.selectBrandJoinCategory(categoryId);
        if (CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brands,BrandDTO.class);
    }
}
