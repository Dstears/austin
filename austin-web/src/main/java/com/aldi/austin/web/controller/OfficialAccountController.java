package com.aldi.austin.web.controller;


import com.google.common.base.Throwables;
import com.aldi.austin.common.enums.RespStatusEnum;
import com.aldi.austin.support.utils.AccountUtils;
import com.aldi.austin.web.annotation.AustinAspect;
import com.aldi.austin.web.annotation.AustinResult;
import com.aldi.austin.web.exception.CommonException;
import com.aldi.austin.web.utils.Convert4Amis;
import com.aldi.austin.web.vo.amis.CommonAmisVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.template.WxMpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 微信服务号
 *
 * @author 3y
 */
@Slf4j
@AustinAspect
@RequestMapping("/officialAccount")
@RestController
@Api("微信服务号")
public class OfficialAccountController {

    @Autowired
    private AccountUtils accountUtils;


    /**
     * @param id 账号Id
     * @return
     */
    @GetMapping("/template/list")
    @ApiOperation("/根据账号Id获取模板列表")
    @AustinResult
    public List<CommonAmisVo> queryList(Integer id) {
        try {
            List<CommonAmisVo> result = new ArrayList<>();
            WxMpService wxMpService = accountUtils.getAccountById(id, WxMpService.class);

            List<WxMpTemplate> allPrivateTemplate = wxMpService.getTemplateMsgService().getAllPrivateTemplate();
            for (WxMpTemplate wxMpTemplate : allPrivateTemplate) {
                CommonAmisVo commonAmisVo = CommonAmisVo.builder().label(wxMpTemplate.getTitle()).value(wxMpTemplate.getTemplateId()).build();
                result.add(commonAmisVo);
            }
            return result;
        } catch (Exception e) {
            log.error("OfficialAccountController#queryList fail:{}", Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }


    /**
     * 根据账号Id和模板ID获取模板列表
     *
     * @return
     */
    @PostMapping("/detailTemplate")
    @ApiOperation("/根据账号Id和模板ID获取模板列表")
    @AustinResult
    public CommonAmisVo queryDetailList(Integer id, String wxTemplateId) {
        if (Objects.isNull(id) || Objects.isNull(wxTemplateId)) {
            log.info("id || wxTemplateId null! id:{},wxTemplateId:{}", id, wxTemplateId);
            return CommonAmisVo.builder().build();
        }
        try {
            WxMpService wxMpService = accountUtils.getAccountById(id, WxMpService.class);
            List<WxMpTemplate> allPrivateTemplate = wxMpService.getTemplateMsgService().getAllPrivateTemplate();
            return Convert4Amis.getWxMpTemplateParam(wxTemplateId, allPrivateTemplate);
        } catch (Exception e) {
            log.error("OfficialAccountController#queryDetailList fail:{}", Throwables.getStackTraceAsString(e));
            throw new CommonException(RespStatusEnum.SERVICE_ERROR);
        }
    }


}
