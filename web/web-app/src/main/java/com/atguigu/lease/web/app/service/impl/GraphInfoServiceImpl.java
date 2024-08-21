package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.GraphInfo;
import com.atguigu.lease.web.app.service.GraphInfoService;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
* @author liubo
* @description 针对表【graph_info(图片信息表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
public class GraphInfoServiceImpl extends ServiceImpl<GraphInfoMapper, GraphInfo>
    implements GraphInfoService{

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Override
    public List<GraphVo> getApartmentGraphVoList(Long id) {
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        List<GraphInfo> graphInfoList = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(graphInfoList)) {
            for (GraphInfo graphInfo : graphInfoList) {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoList.add(graphVo);
            }
        }
        return graphVoList;
    }

    @Override
    public List<GraphVo> getRoomGraphVoList(Long id) {
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
        List<GraphInfo> graphInfoList = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(graphInfoList)) {
            for (GraphInfo graphInfo : graphInfoList) {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoList.add(graphVo);
            }
        }
        return graphVoList;
    }
}




