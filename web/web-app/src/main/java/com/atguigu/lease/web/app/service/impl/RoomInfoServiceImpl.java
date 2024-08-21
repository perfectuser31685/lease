package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.AttrValueService;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.attr.AttrValueVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private BrowsingHistoryService browsingHistoryService;

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public IPage<RoomItemVo> getRoomPage(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        IPage<RoomItemVo> roomItemVoIPage = roomInfoMapper.getRoomIpage(page,queryVo);
        return roomItemVoIPage;
    }

    //存储到Redis
    @Override
    public RoomDetailVo getRoomDetailVo(Long id) {
        //先从缓存中查数据
        String key = RedisConstant.APP_ROOM_PREFIX+id;
        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);
        //缓存中没有，执行以下代码，从数据库查找
        if(roomDetailVo == null){
            //房间基本信息
            RoomInfo roomInfo = roomInfoMapper.selectById(id);
            //所属公寓信息,这里的信息是Vo，包含公寓图片列表等信息，需要在service层编写逻辑，因为是单个，不推荐在mapper中直接定义sql语句
            ApartmentItemVo apartmentItemVo = apartmentInfoService.selectOneByRoomId(id);
            //GraphVo
            LambdaQueryWrapper<GraphInfo> graphInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemId,id);
            graphInfoLambdaQueryWrapper.eq(GraphInfo::getItemType,ItemType.ROOM);
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
            //3.查询FeeValueVo
            List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);
            //4.查询attrValue
            List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

            //5.查询facilityInfo
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

            //6.查询labelInfo
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

            //7.查询paymentType
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

            //8.查询leaseTerm
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);

            //不重新new出来不行，不new就是null，而且不是一个对象，就是单纯的null，不能用于存储对象
            roomDetailVo = new RoomDetailVo();
            BeanUtils.copyProperties(roomInfo, roomDetailVo);

            roomDetailVo.setApartmentItemVo(apartmentItemVo);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setAttrValueVoList(attrvalueVoList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setFeeValueVoList(feeValueVoList);
            roomDetailVo.setLeaseTermList(leaseTermList);
            //将数据保存到缓存
            redisTemplate.opsForValue().set(key,roomDetailVo);
        }


        //保存浏览历史,不管从哪查都得执行
        Long userId = LoginUserHolder.getLoginUser().getUserId();
        browsingHistoryService.saveHistory(roomDetailVo,userId);

        return roomDetailVo;
    }

    @Override
    public IPage<RoomItemVo> getRoomByApartmentId(IPage<RoomItemVo> page, Long id) {
        IPage<RoomItemVo> roomItemVo = roomInfoMapper.selectByApartmentId(page,id);
        return roomItemVo;
    }
}




