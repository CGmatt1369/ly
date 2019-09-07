package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.pojo.DTO.SpecGroupDTO;
import com.leyou.item.pojo.DTO.SpecParamDTO;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/31
 * @描述 规格
 */
@Service
public class SpecService {

    @Autowired
    private TbSpecGroupService specGroupService;
    @Autowired
    private TbSpecParamService specParamService;
    public List<SpecGroupDTO> findSpecGroupByCategoryId(Long id) {
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,id);
        List<TbSpecGroup> specGroupList = specGroupService.list(queryWrapper);
        if (CollectionUtils.isEmpty(specGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specGroupList,SpecGroupDTO.class);
    }


    public List<SpecParamDTO> findSpecParam(Long gid,Long cid,Boolean searching) {
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if (gid!=null && gid!=0){
            queryWrapper.lambda().eq(TbSpecParam::getGroupId,gid);
        }
        if(cid != null && cid != 0){
            queryWrapper.lambda().eq(TbSpecParam::getCid,cid);
        }
        if(searching != null){
            queryWrapper.lambda().eq(TbSpecParam::getSearching,searching);
        }
        List<TbSpecParam> specParams = specParamService.list(queryWrapper);
        if (CollectionUtils.isEmpty(specParams)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        List<SpecParamDTO> specParamDTOList = BeanHelper.copyWithCollection(specParams, SpecParamDTO.class);
        return specParamDTOList;
    }

     public void saveSpecGroup(SpecGroupDTO specGroupDTO) {
        TbSpecGroup tbSpecGroup = new TbSpecGroup();
        tbSpecGroup.setCid(specGroupDTO.getCid());
        tbSpecGroup.setName(specGroupDTO.getName());
        boolean bSave = specGroupService.save(tbSpecGroup);
        if (!bSave){
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }


    public void updateSpecGroup(TbSpecGroup specGroupDTO) {
        boolean bupdate = specGroupService.updateById(specGroupDTO);
        if (!bupdate){
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    @Transactional
    public void deleteSpecGroup(Long gid) {
        //删除tb_spec_group表
        //删除tb_spec_param表
        //删除商品
    }
}
