package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.pojo.DTO.CategoryDTO;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/29
 * @描述 商品分类
 */
@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private TbCategoryService categoryService;

    @RequestMapping(value = "of/parent",method = RequestMethod.GET)
    public ResponseEntity<List<CategoryDTO>> findByParentId(@RequestParam(name = "pid")Long pid){
        List<CategoryDTO> categoryListByParentId = categoryService.findCategoryListByParentId(pid);
        return ResponseEntity.ok(categoryListByParentId);
    }


    /**
     * 品牌回显商品
     */
    @GetMapping("/of/brand")
    public ResponseEntity<List<CategoryDTO>> findByBrandId(@RequestParam(name = "id")Long brandId){
        List<CategoryDTO> categoryDTOS = categoryService.queryCategoryListByBrandId(brandId);
        return ResponseEntity.ok(categoryDTOS);
    }
    /**
     * 根据id的集合查询商品分类
     * @return 分类集合
     */
    @GetMapping("/list")
    public ResponseEntity<List<CategoryDTO>>  findById(@RequestParam(name = "ids") List<Long> ids){
        Collection<TbCategory> categoryCollection = categoryService.listByIds(ids);
        if(CollectionUtils.isEmpty(categoryCollection)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<CategoryDTO> categoryDTOList = categoryCollection.stream().map(categroy -> {
            return BeanHelper.copyProperties(categroy, CategoryDTO.class);
        }).collect(Collectors.toList());

        return ResponseEntity.ok(categoryDTOList);
    }

    /**
     * /category/levels  id: 76
     * id name parentId isParent sort  返回DTO
     */
    @GetMapping("levels")
    public ResponseEntity<List<CategoryDTO>> findLevels(@RequestParam(name = "id")Long id){
        List<TbCategory> list=new ArrayList<>();
        TbCategory category3 = categoryService.getById(id);//通过id查出分类
        if (category3==null){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        TbCategory category2 = categoryService.getById(category3.getParentId());
        TbCategory category1 = categoryService.getById(category2.getParentId());
        list.add(category1);
        list.add(category2);
        list.add(category3);
        List<CategoryDTO> categoryDTOS = BeanHelper.copyWithCollection(list, CategoryDTO.class);
        return ResponseEntity.ok(categoryDTOS);
    }


}
