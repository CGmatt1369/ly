package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.*;
import com.leyou.item.pojo.DTO.SkuDTO;
import com.leyou.item.pojo.DTO.SpuDTO;
import com.leyou.item.pojo.DTO.SpuDetailDTO;
import com.leyou.item.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/1
 * @描述
 */
@Service
public class GoodsService {

    @Autowired
    private TbSpuService spuService;

    public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        Page<TbSpu> spuPage = new Page<>(page,rows);
        //查询条件
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        //模糊查询 商品 名称
        if(!StringUtils.isEmpty(key)){
            queryWrapper.lambda().like(TbSpu::getName,key);
        }
        //是否上下架
        if(saleable != null){
            queryWrapper.lambda().eq(TbSpu::getSaleable,saleable);
        }
        //排序
        queryWrapper.lambda().orderByDesc(TbSpu::getUpdateTime);
        //分页
        IPage<TbSpu> spuIPage = spuService.page(spuPage, queryWrapper);
        if (spuIPage==null|| CollectionUtils.isEmpty(spuIPage.getRecords())){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SpuDTO> spuDTOS = BeanHelper.copyWithCollection(spuIPage.getRecords(), SpuDTO.class);

        //封装一个方法 处理品牌名称和分类名称
        this.handleBrandAndCategoryName(spuDTOS);
        //返回PageResult
        PageResult<SpuDTO> pageResult = new PageResult<>(spuDTOS,spuIPage.getTotal(),Integer.valueOf(String.valueOf(spuIPage.getPages())));
        return pageResult;
    }

    @Autowired
    private TbCategoryService categoryService;
    @Autowired
    private TbBrandService brandService;

    private void handleBrandAndCategoryName(List<SpuDTO> spuDTOS) {
        for (SpuDTO spuDTO : spuDTOS) {
            //商品分类名字 SpuDTO里有三级标题的list方法：getCategoryIds
            List<Long> categoryIds = spuDTO.getCategoryIds();
            //t通告ids查出Category
            Collection<TbCategory> tbCategories = categoryService.listByIds(categoryIds);
            //Categorys查出名称
            /*String categoryName = tbCategories.stream().map(category -> {
                return category.getName();
            }).collect(Collectors.joining("/"));*/
            String categoryName =
                    tbCategories.stream().map(TbCategory::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryName);
            //查询品牌的名称
            Long brandId = spuDTO.getBrandId();
            TbBrand brand = brandService.getById(brandId);
            spuDTO.setBrandName(brand.getName());
        }
    }
    @Autowired
    private TbSpuDetailService spuDetailService;
    @Autowired
    private TbSkuService skuService;
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //保存spu
        boolean bspuSave = spuService.save(BeanHelper.copyProperties(spuDTO, TbSpu.class));
        if (!bspuSave){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //取出spuid
        Long spuDTOId = spuDTO.getId();
        //保存spu_detail
        SpuDetailDTO spuDetail = spuDTO.getSpuDetail();
        spuDetail.setSpuId(spuDTOId);
        boolean bspuDetail = spuDetailService.save(BeanHelper.copyProperties(spuDetail, TbSpuDetail.class));
        if (!bspuDetail){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //保存sku
        List<SkuDTO> skus = spuDTO.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        List<TbSku> tbSkuList = skus.stream().map(skuDTO -> {
            skuDTO.setSpuId(spuDTOId);
            return BeanHelper.copyProperties(skuDTO, TbSku.class);
        }).collect(Collectors.toList());
        boolean bsku = skuService.saveBatch(tbSkuList);
        if(!bsku){
            throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Transactional
    public void updateSpuSaleable(Long id, Boolean saleable) {
        //更新spu
        TbSpu tbSpu = new TbSpu();
        tbSpu.setId(id);
        tbSpu.setSaleable(saleable);
        boolean bspu = spuService.updateById(tbSpu);
        if (!bspu){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //更新sku 通过spu_id
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().eq(TbSku::getSpuId,id);
        updateWrapper.lambda().set(TbSku::getEnable,saleable);
        boolean bupdate = skuService.update(updateWrapper);
        if (!bupdate){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    public SpuDetailDTO findSpuDetailById(Long spuId) {
        QueryWrapper<TbSpuDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpuDetail::getSpuId,spuId);
        TbSpuDetail spuDetail = spuDetailService.getOne(queryWrapper);
        if (spuDetail==null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail,SpuDetailDTO.class);
    }

    public List<SkuDTO> findSkuListBySpuId(Long spuId) {
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        List<TbSku> tbSkuList = skuService.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSkuList)){
            throw  new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSkuList,SkuDTO.class);
    }

@Transactional(rollbackFor = Exception.class)
    public void updateGoods(SpuDTO spuDTO) {
        //这次修改带来了修改的id
    //得到id
    Long spuDTOId = spuDTO.getId();
    if (spuDTOId==null){
        throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
    }
    //删除sku
    QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
    queryWrapper.lambda().eq(TbSku::getSpuId,spuDTOId);
    boolean bremove = skuService.remove(queryWrapper);
    if (!bremove){
        throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
    }
    //更新spu
    TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
    tbSpu.setUpdateTime(null);
    boolean bspu = spuService.updateById(tbSpu);
    if(!bspu){
        throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
    }
    //更新spu_detail
    SpuDetailDTO spuDetail = spuDTO.getSpuDetail();
    TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDetail, TbSpuDetail.class);
    tbSpuDetail.setUpdateTime(null);
    boolean bspudatil = spuDetailService.updateById(tbSpuDetail);
    if (!bspudatil){
        throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
    }
    //新增sku
    List<SkuDTO> skus = spuDTO.getSkus();
    if (CollectionUtils.isEmpty(skus)){
        throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
    }
    List<TbSku> tbSkuList = skus.stream().map(skuDTO -> {
        skuDTO.setSpuId(spuDTOId);
        skuDTO.setEnable(false);//添加false
        return BeanHelper.copyProperties(skuDTO, TbSku.class);
    }).collect(Collectors.toList());
    boolean bsku = skuService.saveBatch(tbSkuList);
    if(!bsku){
        throw  new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
    }
}
}
