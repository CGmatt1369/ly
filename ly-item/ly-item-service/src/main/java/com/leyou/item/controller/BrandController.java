package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.pojo.DTO.BrandDTO;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.awt.peer.PanelPeer;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/29
 * @描述 我的品牌
 */
@RestController
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    private TbBrandService brandService;

    /**
     * 品牌的分页查询
     */
    @GetMapping("page")
    public ResponseEntity<PageResult<BrandDTO>> queryBrandByPage(
            @RequestParam(value = "page",defaultValue = "1")Integer page,
            @RequestParam(value = "rows", defaultValue = "5")Integer rows,
            @RequestParam(value = "key", required = false)String key,
            @RequestParam(value = "sortBy", required = false)String sortBy,
            @RequestParam(value = "desc", defaultValue = "false")Boolean desc
            ) {
        return ResponseEntity.ok(brandService.searchPage(page,rows, key, sortBy, desc));
    }
/**
 * 品牌的新增
 */
    @PostMapping
    public ResponseEntity<Void> save(TbBrand brand, @RequestParam(name = "cids") List<Long> cids){
        brandService.saveBrandAndCategory(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
/**
 * 品牌的修改提交
 */
    @PutMapping
    public ResponseEntity<Void> update(TbBrand tbBrand,@RequestParam(name = "cids")List<Long> cids){
        brandService.updateBrandAndCategory(tbBrand,cids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    /**
     * 根据分类查询品牌信息
     */
    @GetMapping("/of/category")
    public ResponseEntity<List<BrandDTO>> findByCategoryId(@RequestParam(name = "id")  Long categoryId){
        return ResponseEntity.ok(brandService.findBrandByCategoryId(categoryId));
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public ResponseEntity<BrandDTO> findById(@PathVariable("id") Long id){
        TbBrand tbBrand = brandService.getById(id);
        BrandDTO brandDTO = BeanHelper.copyProperties(tbBrand, BrandDTO.class);
        return ResponseEntity.ok(brandDTO);
    }

    /**
     * ids查询品牌集合
     * @param ids
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<BrandDTO>> findBrandsByIds(@RequestParam(name = "ids")List<Long> ids){

        Collection<TbBrand> tbBrandCollection = brandService.listByIds(ids);
        if (CollectionUtils.isEmpty(tbBrandCollection)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<TbBrand> tbBrandList = (List<TbBrand>)tbBrandCollection;
        return ResponseEntity.ok(BeanHelper.copyWithCollection(tbBrandList,BrandDTO.class));
    }


}
