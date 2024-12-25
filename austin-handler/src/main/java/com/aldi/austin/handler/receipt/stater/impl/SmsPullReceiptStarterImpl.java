package com.aldi.austin.handler.receipt.stater.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Throwables;
import com.aldi.austin.common.constant.CommonConstant;
import com.aldi.austin.common.dto.account.sms.SmsAccount;
import com.aldi.austin.common.enums.ChannelType;
import com.aldi.austin.handler.receipt.stater.ReceiptMessageStater;
import com.aldi.austin.handler.script.SmsScript;
import com.aldi.austin.support.dao.ChannelAccountDao;
import com.aldi.austin.support.dao.SmsRecordDao;
import com.aldi.austin.support.domain.ChannelAccount;
import com.aldi.austin.support.domain.SmsRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


/**
 * 拉取短信回执信息
 *
 * @author 3y
 */
@Component
@Slf4j
public class SmsPullReceiptStarterImpl implements ReceiptMessageStater {

    @Autowired
    private ChannelAccountDao channelAccountDao;

    @Autowired
    private Map<String, SmsScript> scriptMap;

    @Autowired
    private SmsRecordDao smsRecordDao;

    /**
     * 拉取消息并入库
     */
    @Override
    public void start() {
        try {
            List<ChannelAccount> channelAccountList = channelAccountDao.findAllByIsDeletedEqualsAndSendChannelEquals(CommonConstant.FALSE, ChannelType.SMS.getCode());
            for (ChannelAccount channelAccount : channelAccountList) {
                SmsAccount smsAccount = JSON.parseObject(channelAccount.getAccountConfig(), SmsAccount.class);
                List<SmsRecord> smsRecordList = scriptMap.get(smsAccount.getScriptName()).pull(channelAccount.getId().intValue());
                if (CollUtil.isNotEmpty(smsRecordList)) {
                    smsRecordDao.saveAll(smsRecordList);
                }
            }
        } catch (Exception e) {
            log.error("SmsPullReceiptStarter#start fail:{}", Throwables.getStackTraceAsString(e));

        }

    }
}
