package com.leyou.item.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/29
 * @描述 品牌DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrandDTO {

    /**
     * 品牌id
     */
    private Long id;

    /**
     * 品牌名称
     */
    private String name;

    /**
     * 品牌图片地址
     */
    private String image;

    /**
     * 品牌的首字母
     */
    private String letter;
}
