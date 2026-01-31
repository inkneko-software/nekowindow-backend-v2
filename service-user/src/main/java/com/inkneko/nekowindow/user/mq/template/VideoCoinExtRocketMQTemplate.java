package com.inkneko.nekowindow.user.mq.template;

import org.apache.rocketmq.spring.annotation.ExtRocketMQTemplateConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@ExtRocketMQTemplateConfiguration(nameServer = "10.200.0.10:9876", group = "video-coin-tx-producer-group", tlsEnable =  "false")
public class VideoCoinExtRocketMQTemplate extends RocketMQTemplate{
}
