package com.leyou.item.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.entity.Item;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/28
 * @描述
 */
@RestController
public class ItemController {
    @PostMapping("item")
    public ResponseEntity<Item> save(Item item){
        item.setId(1L);
        if (item.getPrice()==null){
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }
}