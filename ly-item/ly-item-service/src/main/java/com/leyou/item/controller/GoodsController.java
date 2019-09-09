package com.leyou.item.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.DTO.SkuDTO;
import com.leyou.item.pojo.DTO.SpuDTO;
import com.leyou.item.pojo.DTO.SpuDetailDTO;
import com.leyou.item.service.impl.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.peer.PanelPeer;
import java.util.List;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/1
 * @描述
 */

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * 分页查询spu
     * @param page
     * @param rows
     * @param Key
     * @param saleable
     * @return
     */
    @GetMapping("spu/page")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(@RequestParam(name = "page",defaultValue = "1")Integer page,
                                          @RequestParam(name = "row",defaultValue = "5")Integer rows,
                                          @RequestParam(name = "key",required = false)String Key,
                                          @RequestParam(name = "saleable",required = false)Boolean saleable){
        PageResult<SpuDTO> pageResult=goodsService.findSpuByPage(page,rows,Key,saleable);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 保存商品，新增
     */
    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO){
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * 商品的上下架
     * /spu/saleable  ：PUT
     * id: 2
     * saleable: true
     */
    @PutMapping("/spu/saleable")
    public ResponseEntity<Void> updateSpuSaleable(@RequestParam("id")Long id,
                                                  @RequestParam("saleable")Boolean saleable){
        goodsService.updateSpuSaleable(id,saleable);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    /**
     * 编辑回显
     * spu/detail?id=2
     */
    @GetMapping("/spu/detail")
    public ResponseEntity<SpuDetailDTO> findSpuDetailById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(goodsService.findSpuDetailById(id));
    }
    /**
     * 根据spuID查询sku
     */
    @GetMapping("/sku/of/spu")
    public ResponseEntity<List<SkuDTO>> findSkuListBySpuId(@RequestParam(name="id") Long spuId){
        List<SkuDTO> skuDTOList = goodsService.findSkuListBySpuId(spuId);
        return ResponseEntity.ok(skuDTOList);
    }
    /**
     * 保存修改
     */
    @PutMapping("/goods")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO){
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("spu/{id}")
    public ResponseEntity<SpuDTO> findSpuById(@PathVariable("id")Long id){
        return ResponseEntity.ok(goodsService.findSpuById(id));
    }


}
