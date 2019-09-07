package com.leyou.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/7
 * @描述
 */
@Controller
public class PageController {

    /**
     * 商品的详情页
     */
    @GetMapping("item/{id}")
    public String toItemPage(Model model, @PathVariable("id")Long id){

        return null;
    }



}
