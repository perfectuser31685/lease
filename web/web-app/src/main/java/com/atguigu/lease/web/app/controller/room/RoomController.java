package com.atguigu.lease.web.app.controller.room;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.vo.room.RoomDetailVo;
import com.atguigu.lease.web.app.vo.room.RoomItemVo;
import com.atguigu.lease.web.app.vo.room.RoomQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "房间信息")
@RestController
@RequestMapping("/app/room")
public class RoomController {

    @Autowired
    private RoomInfoService roomInfoService;

    //分页查询，这里的分页查询涉及到多表映射，但是mybatis的多表映射和分页查询是有问题的，不能直接使用之前的嵌套结果映射，也就是collection
    //在这里应该使用嵌套查询，也就是写多个查询语句，其中的一些语句作为子查询放到collection中，每当调用的时候就执行这个子查询
    //常规的嵌套结果查询不分主次，在join后会丢失没有图片等信息的房间，在实际中这些房间应该能够显示
    @Operation(summary = "分页查询房间列表")
    @GetMapping("pageItem")
    public Result<IPage<RoomItemVo>> pageItem(@RequestParam long current, @RequestParam long size, RoomQueryVo queryVo) {
        IPage<RoomItemVo> page = new Page<>();
        IPage<RoomItemVo> result = roomInfoService.getRoomPage(page,queryVo);
        return Result.ok(result);
    }

    //要在浏览结束保存浏览历史
    //因为这个是最常用的功能，所以将查出来的的数据放入Redis缓存中进行缓存优化
    //Redis现成的有两种,StringRedisTemplate和RedisTemplate<Object,Object>,但是我们要存储的是RoomDetailVo对象，前者只能存储String类型
    //后者可以存储，但是在Redis客户端key会出现乱码，所以这里我们采用自定义的Redis方法
    @Operation(summary = "根据id获取房间的详细信息")
    @GetMapping("getDetailById")
    public Result<RoomDetailVo> getDetailById(@RequestParam Long id) {
        RoomDetailVo roomDetailVo = roomInfoService.getRoomDetailVo(id);
        return Result.ok(roomDetailVo);
    }

    @Operation(summary = "根据公寓id分页查询房间列表")
    @GetMapping("pageItemByApartmentId")
    public Result<IPage<RoomItemVo>> pageItemByApartmentId(@RequestParam long current, @RequestParam long size, @RequestParam Long id) {
        IPage<RoomItemVo> page = new Page<>(current,size);
        IPage<RoomItemVo> result = roomInfoService.getRoomByApartmentId(page,id);
        return Result.ok(result);
    }
}
