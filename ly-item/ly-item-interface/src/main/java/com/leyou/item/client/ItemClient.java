package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.DTO.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/3
 * @描述 编写item的feign接口
 */
@FeignClient("item-service")
public interface ItemClient {

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @GetMapping("/brand/{id}")
    BrandDTO findBrandById(@PathVariable("id") Long id);

    /**
     * 根据id的集合查询商品分类
     * @param idList 商品分类的id集合
     * @return 分类集合
     */
    @GetMapping("/category/list")
    List<CategoryDTO> findCategoryByIds(@RequestParam("ids") List<Long> idList);

    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param Key
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
     PageResult<SpuDTO> findBySpuPage(@RequestParam(name = "page",defaultValue = "1")Integer page,
                                                    @RequestParam(name = "row",defaultValue = "5")Integer rows,
                                                    @RequestParam(name = "key",required = false)String Key,
                                                    @RequestParam(name = "saleable",required = false)Boolean saleable);

    /**
     * 根据spuID查询spuDetail
     * @param id spuID
     * @return SpuDetail
     */
    @GetMapping("/spu/detail")
    SpuDetailDTO findSpuDetailById(@RequestParam("id") Long id);



    /**
     * 根据spuID查询sku
     */
    @GetMapping("/sku/of/spu")
     List<SkuDTO> findSkuBySpuId(@RequestParam(name="id") Long spuId);

    /**
     * 查询规格参数
     * @param gid 组id
     * @param cid 分类id
     * @param searching 是否用于搜索
     * @return 规格组集合
     */
    @GetMapping("/spec/params")
     List<SpecParamDTO> findSpecParams(@RequestParam(name = "gid",required = false) Long gid,
                                                            @RequestParam(name = "cid",required = false) Long cid,
                                                            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     * 根据品牌id批量查询品牌
     * @param idList
     * @return
     */
    @GetMapping("brand/list")
    List<BrandDTO> findBrandsByIds(@RequestParam("ids")List<Long> idList);




}
