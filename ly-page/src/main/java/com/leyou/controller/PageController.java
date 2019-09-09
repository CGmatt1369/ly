package com.leyou.controller;

import com.leyou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/7
 * @描述
 */
@Controller
public class PageController {

    @Autowired
    private PageService pageService;



    /**
     * 商品的详情页
     */
    @GetMapping("item/{id}")
    public String toItemPage(Model model, @PathVariable("id")Long id){
        //查询模型数据
        Map<String,Object> itemData=pageService.loadItemData(id);
        //存到数据模型
        model.addAllAttributes(itemData);
        return "item";
    }



}
