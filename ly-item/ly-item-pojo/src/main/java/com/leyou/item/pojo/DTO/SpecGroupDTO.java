package com.leyou.item.pojo.DTO;

import lombok.Data;

import java.util.List;

@Data
public class SpecGroupDTO {
    private Long id;

    private Long cid;

    private String name;

    //组内的参数
    private List<SpecParamDTO> params;
}