package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    public ApartmentItemVo selectOneByRoomId(Long id) {
        RoomInfo roomInfo = roomInfoMapper.selectById(id);
        Long apartmentId = roomInfo.getApartmentId();
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);

        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(apartmentId);

        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,apartmentId);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,ItemType.APARTMENT);
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

        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(apartmentId);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //apartmentInfo
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        //labelInfo
        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(id);
        //graphVo
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,ItemType.APARTMENT);
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
        //facilityInfo
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);
        //minRent
        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(id);

        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setMinRent(minRent);
        return apartmentDetailVo;
    }

    public ApartmentItemVo selectOneByApartmentId(Long apartmentId) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);

        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(apartmentId);

        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId, apartmentId);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        List<GraphInfo> graphInfoList = graphInfoMapper.selectList(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(graphInfoList)) {
            for (GraphInfo graphInfo : graphInfoList) {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoList.add(graphVo);
            }
        }
        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(apartmentId);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }
}




