package com.leyou.item.controller;

import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.pojo.DTO.SpecGroupDTO;
import com.leyou.item.pojo.DTO.SpecParamDTO;
import com.leyou.item.service.impl.SpecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/31
 * @描述
 */
@RestController
@RequestMapping("/spec")
public class SpecController {

    @Autowired
    private SpecService specService;

    /**
     * 根据分类id查询 规格组信息
     *
     * @param id
     * @return
     */
    @GetMapping("/groups/of/category")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam(name = "id") Long id) {
        List<SpecGroupDTO> groupDTOS = specService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(groupDTOS);
    }

    /**
     * 根据分组id查询规格参数
     */
    @GetMapping("params")
    public ResponseEntity<List<SpecParamDTO>> findSpecParam(@RequestParam(name = "gid",required = false) Long gid,
                                                            @RequestParam(name = "cid",required = false) Long cid,
                                                            @RequestParam(value = "searching", required = false) Boolean searching) {
        List<SpecParamDTO> specParamDTOList = specService.findSpecParam(gid,cid,searching);
        return ResponseEntity.ok(specParamDTOList);
    }

    /**
     * 新增分组spec/group POST cid name
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroup(@RequestBody SpecGroupDTO specGroupDTO) {
        specService.saveSpecGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 修改分组
     * spec/group PUT
     * {cid: 3, name: "ceshi1", id: 16}
     */
    @PutMapping("group")
    public ResponseEntity<Void> editSpecGroup(@RequestBody TbSpecGroup specGroupDTO){
        specService.updateSpecGroup(specGroupDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * 删除分组
     * spec/group/16
     *  DELETE
     */

    @DeleteMapping
    public ResponseEntity<Void> deleteSpecGroup(@RequestParam(name = "")Long gid){
        specService.deleteSpecGroup(gid);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }



}
