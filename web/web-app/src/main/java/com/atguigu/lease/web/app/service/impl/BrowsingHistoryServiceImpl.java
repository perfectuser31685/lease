package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.BrowsingHistory;
import com.atguigu.lease.web.app.mapper.BrowsingHistoryMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;

    @Override
    public IPage<HistoryItemVo> getHistoryPage(IPage<HistoryItemVo> page, Long userId) {
        return browsingHistoryMapper.selectHistoryPage(page, userId);
    }

    @Override
    //方法异步执行
    @Async
    public void saveHistory(RoomDetailVo roomDetailVo,Long userId) {
        //要先判断是否是首次浏览,只有首次浏览才会插入
        //房间id
        Long roomId = roomDetailVo.getId();
        //根据这两者进行判断
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId, userId);
        queryWrapper.eq(BrowsingHistory::getRoomId, roomId);
        BrowsingHistory browsingHistory = browsingHistoryMapper.selectOne(queryWrapper);
        //不为空则更新
        if (!(browsingHistory == null)){
            browsingHistory.setBrowseTime(new Date());
            browsingHistoryMapper.updateById(browsingHistory);
        }else{
            BrowsingHistory browsingHistory1 = new BrowsingHistory();
            browsingHistory1.setUserId(userId);
            browsingHistory1.setRoomId(roomId);
            browsingHistory1.setBrowseTime(new Date());
            browsingHistoryMapper.insert(browsingHistory1);
        }

    }
}