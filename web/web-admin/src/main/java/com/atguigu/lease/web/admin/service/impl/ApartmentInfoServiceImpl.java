package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;

    @Autowired
    private ApartmentLabelService apartmentLabelService;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;


    //对于更改图片标签等操作，通过删除全部图片标签，然后重新加入剩下的图片标签来实现

    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        //需要先进行判断是否为更新操作，因为更新和保存内部的逻辑是不同的，保存只需要插入，更新需要先进行删除；
        //saveOrUpdate只能对ApartmentInfo中的数据进行操作
        boolean isUpdate = apartmentSubmitVo.getId()!=null;   //传入id不为空则为更新
        super.saveOrUpdate(apartmentSubmitVo);
        //更新，进行删除操作
        if(isUpdate){
            //删除图片、配套、标签、杂费
            //删除图片，注意点，删除的图片所属类型是公寓，id也要匹配
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,apartmentSubmitVo.getId());
            graphInfoService.remove(graphInfoLambdaQueryWrapper);
            //删除配套,不能直接删除配套，这样其它公寓也没了，需要操作公寓配套关联表
            LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,apartmentSubmitVo.getId());
            apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);
            //删除杂费
            LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(apartmentFeeValueLambdaQueryWrapper);
            //删除标签
            LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
            apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,apartmentSubmitVo.getId());
            apartmentLabelService.remove(apartmentLabelLambdaQueryWrapper);
        }

        //保存更新都要执行的插入操作
        //保存图片，要注意获取的是GraphVo
        List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
        if(!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoArrayList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfoArrayList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoArrayList);
        }
        //保存配套,配套获取的是id列表，最终插入的应该是ApartmentFacility列表
        List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<ApartmentFacility> facilityList = new ArrayList<>();
            for (Long facilityId : facilityInfoIdList) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityId);
                facilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(facilityList);
        }

        //插入标签列表
        List<Long> labelIds = apartmentSubmitVo.getLabelIds();
        if(!CollectionUtils.isEmpty(labelIds)){
            ArrayList<ApartmentLabel> labelArrayList = new ArrayList<>();
            for (Long labelId : labelIds) {
                ApartmentLabel apartmentLabel = new ApartmentLabel();
                apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                apartmentLabel.setLabelId(labelId);
                labelArrayList.add(apartmentLabel);
            }
            apartmentLabelService.saveBatch(labelArrayList);
        }



        //插入杂费列表
        List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
        if (!CollectionUtils.isEmpty(feeValueIds)) {
            ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
            for (Long feeValueId : feeValueIds) {
                ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                apartmentFeeValue.setFeeValueId(feeValueId);
                apartmentFeeValueList.add(apartmentFeeValue);
            }
            apartmentFeeValueService.saveBatch(apartmentFeeValueList);
        }


    }

    //根据省市区分页查询公寓信息，公寓信息包括房间总数和空闲房间数
    //所以返回的是一个ApartmentItemVo列表，空闲房间数可以根据签约列表去计算出每个公寓的签约数，总房间数减去就是空闲房间数
    @Override
    public IPage<ApartmentItemVo> pageItem(IPage<ApartmentItemVo> apartmentItemVoPage, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(apartmentItemVoPage,queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //包括一个公寓信息，图片Vo列表，配套列表，标签列表，杂费Vo列表
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        //公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if(apartmentInfo == null){
            return null;
        }
        //图片信息
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,ItemType.APARTMENT);
        List<GraphInfo> graphInfoList = graphInfoService.list(graphInfoLambdaQueryWrapper);
        List<GraphVo> graphVoList = new ArrayList<>();
        if(!CollectionUtils.isEmpty(graphInfoList)) {
            for (GraphInfo graphInfo : graphInfoList) {
                GraphVo graphVo = new GraphVo();
                graphVo.setName(graphInfo.getName());
                graphVo.setUrl(graphInfo.getUrl());
                graphVoList.add(graphVo);
            }
        }
        //配套信息,要注意配套信息没有apartment_id关键字，要通过两者关系表找到相应配套id，然后找出Vo
        List<FacilityInfo> facilityInfoList = facilityInfoMapper.getFacilityByApartmentId(id);

        //列表信息，与配套基本一致
        List<LabelInfo> labelInfoList = labelInfoMapper.getLabelByApartmentId(id);

        //杂费Vo,这里比前两项多了一张表，Vo，根据公寓id查找value，再根据valueId找对应的Key
        List<FeeValueVo> feeValueVoList = feeValueMapper.getFeeValueByApartmentId(id);


        //赋值
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);
        return apartmentDetailVo;
    }

    @Override
    public void removeByAppartmrntId(Long id) {
        //删除公寓还有关联的房间信息，应该在删除之前看看是否有房间，有的话要先提示删除房间
        //mapper中有统计个数的方法，这里选择用mapper
        LambdaQueryWrapper<RoomInfo> roomInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        roomInfoLambdaQueryWrapper.eq(RoomInfo::getApartmentId,id);
        Long count = roomInfoMapper.selectCount(roomInfoLambdaQueryWrapper);

        //根据房间个数分情况讨论，房间不为0终止删除,并返还给前端响应信息
        //这里返还响应可以通过全局异常的形式抛出
        if(count>0){
            throw new LeaseException(ResultCodeEnum.ADMIN_APARTMENT_DELETE_ERROR);
        }

        //调用本身的方法去删除公寓Info
        super.removeById(id);
        //除此之外还要删除Vo中的附加信息，在saveOrUpdate中有这四项的删除
        //删除图片、配套、标签、杂费
        //删除图片，注意点，删除的图片所属类型是公寓，id也要匹配
        LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
        graphInfoService.remove(graphInfoLambdaQueryWrapper);
        //删除配套,不能直接删除配套，这样其它公寓也没了，需要操作公寓配套关联表
        LambdaQueryWrapper<ApartmentFacility> apartmentFacilityLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFacilityLambdaQueryWrapper.eq(ApartmentFacility::getApartmentId,id);
        apartmentFacilityService.remove(apartmentFacilityLambdaQueryWrapper);
        //删除杂费
        LambdaQueryWrapper<ApartmentFeeValue> apartmentFeeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentFeeValueLambdaQueryWrapper.eq(ApartmentFeeValue::getApartmentId,id);
        apartmentFeeValueService.remove(apartmentFeeValueLambdaQueryWrapper);
        //删除标签
        LambdaQueryWrapper<ApartmentLabel> apartmentLabelLambdaQueryWrapper = new LambdaQueryWrapper<>();
        apartmentLabelLambdaQueryWrapper.eq(ApartmentLabel::getApartmentId,id);
        apartmentLabelService.remove(apartmentLabelLambdaQueryWrapper);
    }
}




