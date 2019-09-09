package com.leyou.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.pojo.DTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/9/9
 * @描述
 */
@Slf4j
@Service
public class PageService {

    @Autowired
    private ItemClient itemClient;
    //得到动态数据
    public Map<String, Object> loadItemData(Long id) {
        Map map=new HashMap();
        SpuDTO spuDTO = itemClient.findSpuById(id);
        String spuName = spuDTO.getName();
        String subTitle = spuDTO.getSubTitle();
        BrandDTO brandDTO = itemClient.findBrandById(spuDTO.getBrandId());
        List<CategoryDTO> categoryDTOList = itemClient.findCategoryByIds(spuDTO.getCategoryIds());
        SpuDetailDTO spuDetail = itemClient.findSpuDetailById(id);
        List<SkuDTO> skuDTOList = itemClient.findSkuBySpuId(id);
        List<SpecGroupDTO> specGroupAndParamByCid = itemClient.findSpecsByCid(spuDTO.getCid3());

        map.put("categories",categoryDTOList);
        map.put("brand",brandDTO);
        map.put("spuName",spuName);
        map.put("subTitle",subTitle);
        map.put("detail",spuDetail);
        map.put("skus",skuDTOList);
        map.put("specs",specGroupAndParamByCid);
        return map;
    }
    //实现thymeleaf
    @Autowired
    private SpringTemplateEngine templateEngine;
    @Value("${ly.static.itemDir}")
    private String itemDir;
    @Value("${ly.static.itemTemplate}")
    private String itemTemplate;
    public void createItemHtml(Long id) {
        //上下文
        Context context = new Context();
        //动态数据
        context.setVariables(loadItemData(id));
        //准备路径
        File dir = new File(itemDir);
        if (!dir.exists()){
            if (!dir.mkdirs()){
                // 创建失败，抛出异常
                log.error("【静态页服务】创建静态页目录失败，目录地址：{}", dir.getAbsolutePath());
                throw new LyException(ExceptionEnum.DIRECTORY_WRITER_ERROR);
            }
        }
        File filePath = new File(dir, id + ".html");//html路径
        //输出流
        try(PrintWriter writer =new PrintWriter(filePath,"UTF-8")){
            templateEngine.process(itemTemplate,context,writer);
        }catch (IOException e){
            log.error("【静态页服务】静态页生成失败，商品id：{}", id, e);
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }


    }
}
