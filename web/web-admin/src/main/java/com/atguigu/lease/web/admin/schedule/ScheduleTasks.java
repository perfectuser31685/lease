package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTasks {

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    //这个任务会被定时执行
    @Scheduled(cron = "0 0 0 * * *")
    public void checkLease(){
        //每天0时0分0秒检查是否过期
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        //过期时间小于当前时间，表明已过期
        updateWrapper.le(LeaseAgreement::getLeaseEndDate,new Date());
        updateWrapper.in(LeaseAgreement::getStatus,LeaseStatus.SIGNED,LeaseStatus.WITHDRAWING);
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED);
        leaseAgreementService.update(updateWrapper);
    }

}
