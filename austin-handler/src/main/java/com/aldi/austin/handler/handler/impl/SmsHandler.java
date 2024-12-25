package com.aldi.austin.handler.handler.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Throwables;
import com.aldi.austin.common.domain.RecallTaskInfo;
import com.aldi.austin.common.domain.TaskInfo;
import com.aldi.austin.common.dto.account.sms.SmsAccount;
import com.aldi.austin.common.dto.model.SmsContentModel;
import com.aldi.austin.common.enums.ChannelType;
import com.aldi.austin.handler.domain.sms.MessageTypeSmsConfig;
import com.aldi.austin.handler.domain.sms.SmsParam;
import com.aldi.austin.handler.enums.LoadBalancerStrategy;
import com.aldi.austin.handler.handler.BaseHandler;
import com.aldi.austin.handler.loadbalance.ServiceLoadBalancerFactory;
import com.aldi.austin.handler.script.SmsScript;
import com.aldi.austin.support.dao.SmsRecordDao;
import com.aldi.austin.support.domain.SmsRecord;
import com.aldi.austin.support.utils.AccountUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 短信发送处理
 *
 * @author 3y
 */
@Component
@Slf4j
public class SmsHandler extends BaseHandler{

    /**
     * 流量自动分配策略
     */
    private static final Integer AUTO_FLOW_RULE = 0;
    private static final String FLOW_KEY_PREFIX = "message_type_";

    /**
     * 默认负载均衡为随机加权, 待拓展读取配置, 不同Handler可绑定不同的负载均衡策略
     */
    private static final String loadBalancerStrategy = LoadBalancerStrategy.SERVICE_LOAD_BALANCER_RANDOM_WEIGHT_ENHANCED;

    @Autowired
    private SmsRecordDao smsRecordDao;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private AccountUtils accountUtils;
    @Value("${austin.msg-type-sms-config:[]}")
    private String msgTypeSmsConfig;
    @Autowired
    private ServiceLoadBalancerFactory<MessageTypeSmsConfig> serviceLoadBalancer;

    public SmsHandler() {
        channelCode = ChannelType.SMS.getCode();
    }

    @Override
    public boolean handler(TaskInfo taskInfo) {
        SmsParam smsParam = SmsParam.builder()
                .phones(taskInfo.getReceiver())
                .content(getSmsContent(taskInfo))
                .messageTemplateId(taskInfo.getMessageTemplateId())
                .build();
        try {
            /**
             * 1、动态配置做流量负载
             * 2、发送短信
             */
            List<MessageTypeSmsConfig> messageTypeSmsConfigs = serviceLoadBalancer.selectService(getMessageTypeSmsConfig(taskInfo), loadBalancerStrategy);
            for (MessageTypeSmsConfig messageTypeSmsConfig : messageTypeSmsConfigs) {
                smsParam.setScriptName(messageTypeSmsConfig.getScriptName());
                smsParam.setSendAccountId(messageTypeSmsConfig.getSendAccount());
                List<SmsRecord> recordList = applicationContext.getBean(messageTypeSmsConfig.getScriptName(), SmsScript.class).send(smsParam);
                if (CollUtil.isNotEmpty(recordList)) {
                    smsRecordDao.saveAll(recordList);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("SmsHandler#handler fail:{},params:{}", Throwables.getStackTraceAsString(e), JSON.toJSONString(smsParam));
        }
        return false;
    }

    /**
     * 如模板指定具体的明确账号，则优先发其账号，否则走到流量配置
     * <p>
     * 流量配置每种类型都会有其下发渠道账号的配置(流量占比也会配置里面)
     * <p>
     * 样例：
     * key：msgTypeSmsConfig
     * value：[{"message_type_10":[{"weights":80,"scriptName":"TencentSmsScript"},{"weights":20,"scriptName":"YunPianSmsScript"}]},{"message_type_20":[{"weights":20,"scriptName":"YunPianSmsScript"}]},{"message_type_30":[{"weights":20,"scriptName":"TencentSmsScript"}]},{"message_type_40":[{"weights":20,"scriptName":"TencentSmsScript"}]}]
     * 通知类短信有两个发送渠道 TencentSmsScript 占80%流量，YunPianSmsScript占20%流量
     * 营销类短信只有一个发送渠道 YunPianSmsScript
     * 验证码短信只有一个发送渠道 TencentSmsScript
     *
     * @param taskInfo
     * @return
     */
    private List<MessageTypeSmsConfig> getMessageTypeSmsConfig(TaskInfo taskInfo) {

        /**
         * 如果模板指定了账号，则优先使用具体的账号进行发送
         */
        if (!taskInfo.getSendAccount().equals(AUTO_FLOW_RULE)) {
            SmsAccount account = accountUtils.getAccountById(taskInfo.getSendAccount(), SmsAccount.class);
            return Collections.singletonList(MessageTypeSmsConfig.builder().sendAccount(taskInfo.getSendAccount()).scriptName(account.getScriptName()).weights(100).build());
        }

        /**
         * 读取流量配置
         */
        JSONArray jsonArray = JSON.parseArray(msgTypeSmsConfig);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONArray array = jsonArray.getJSONObject(i).getJSONArray(FLOW_KEY_PREFIX + taskInfo.getMsgType());
            if (CollUtil.isNotEmpty(array)) {
                return JSON.parseArray(JSON.toJSONString(array), MessageTypeSmsConfig.class);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 如果有输入链接，则把链接拼在文案后
     * <p>
     * PS: 这里可以考虑将链接 转 短链
     * PS: 如果是营销类的短信，需考虑拼接 回TD退订 之类的文案
     */
    private String getSmsContent(TaskInfo taskInfo) {
        SmsContentModel smsContentModel = (SmsContentModel) taskInfo.getContentModel();
        if (CharSequenceUtil.isNotBlank(smsContentModel.getUrl())) {
            return smsContentModel.getContent() + CharSequenceUtil.SPACE + smsContentModel.getUrl();
        } else {
            return smsContentModel.getContent();
        }
    }

    /**
     * 短信不支持撤回
     * 腾讯云文档 eg：https://cloud.tencent.com/document/product/382/52077
     * @param recallTaskInfo
     */
    @Override
    public void recall(RecallTaskInfo recallTaskInfo) {

    }
}