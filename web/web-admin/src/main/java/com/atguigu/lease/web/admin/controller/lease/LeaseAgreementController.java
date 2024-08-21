package com.atguigu.lease.web.admin.controller.lease;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Tag(name = "租约管理")
@RestController
@RequestMapping("/admin/agreement")
public class LeaseAgreementController {

    @Autowired
    private LeaseAgreementService leaseAgreementService;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private PaymentTypeService paymentTypeService;

    @Autowired
    private LeaseTermService leaseTermService;

    @Operation(summary = "保存或修改租约信息")
    @PostMapping("saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseAgreement leaseAgreement) {
        leaseAgreementService.saveOrUpdate(leaseAgreement);
        return Result.ok();
    }

    @Operation(summary = "根据条件分页查询租约列表")
    @GetMapping("page")
    public Result<IPage<AgreementVo>> page(@RequestParam long current, @RequestParam long size, AgreementQueryVo queryVo) {
        //经典分页查询
        IPage<AgreementVo> page = new Page<>(current, size);
        IPage<AgreementVo> list = leaseAgreementService.getIpageAgreementVo(page, queryVo);
        return Result.ok(list);
    }

    @Operation(summary = "根据id查询租约信息")
    @GetMapping(name = "getById")
    public Result<AgreementVo> getById(@RequestParam Long id) {
        AgreementVo agreementVo = new AgreementVo();
        LeaseAgreement leaseAgreement = leaseAgreementService.getById(id);
        ApartmentInfo apartmentInfo = apartmentInfoService.getById(leaseAgreement.getApartmentId());
        RoomInfo roomInfo = roomInfoService.getById(leaseAgreement.getRoomId());
        PaymentType paymentType = paymentTypeService.getById(leaseAgreement.getPaymentTypeId());
        LeaseTerm leaseTerm = leaseTermService.getById(leaseAgreement.getLeaseTermId());
        BeanUtils.copyProperties(leaseAgreement,agreementVo);
        agreementVo.setLeaseTerm(leaseTerm);
        agreementVo.setApartmentInfo(apartmentInfo);
        agreementVo.setPaymentType(paymentType);
        agreementVo.setRoomInfo(roomInfo);
        return Result.ok(agreementVo);
    }

    @Operation(summary = "根据id删除租约信息")
    @DeleteMapping("removeById")
    public Result removeById(@RequestParam Long id) {
        leaseAgreementService.removeById(id);
        return Result.ok();
    }

    @Operation(summary = "根据id更新租约状态")
    @PostMapping("updateStatusById")
    public Result updateStatusById(@RequestParam Long id, @RequestParam LeaseStatus status) {
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId,id);
        updateWrapper.set(LeaseAgreement::getStatus,status);
        leaseAgreementService.update(updateWrapper);
        return Result.ok();
    }

}

