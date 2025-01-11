package com.inkneko.nekowindow.encode.config;

import lombok.Data;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class EncodeConfig {
    public static final String NK_TOPIC_EXCHANGE_NAME = "heimusic-topic-exchange";
    public static final String NK_PROBE_QUEUE_NAME = "nekowindow-probe-queue";
    public static final String NK_ENCODE_VIDEO_QUEUE_NAME = "nekowindow-encode-video-queue";
    public static final String NK_ENCODE_AUDIO_QUEUE_NAME = "nekowindow-encode-audio-queue";
    public static final String NK_CONCAT_QUEUE_NAME = "nekowindow-concat-queue";

    /**
     * GOP（关键帧）大小，默认为5秒
     */
    @Value("${encode.gop-size:5}")
    private int gopSize;

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    TopicExchange topicExchange(){
        return new TopicExchange(NK_TOPIC_EXCHANGE_NAME);
    }

    @Bean
    Queue probeQueue(){
        return new Queue(NK_PROBE_QUEUE_NAME, true);
    }

    @Bean
    Queue encodeVideoQueue(){
        return new Queue(NK_ENCODE_VIDEO_QUEUE_NAME, true);
    }

    @Bean
    Queue encodeAudioQueue(){
        return new Queue(NK_ENCODE_AUDIO_QUEUE_NAME, true);
    }

    @Bean
    Queue concatQueue(){
        return new Queue(NK_CONCAT_QUEUE_NAME, true);
    }

    @Bean
    Binding probeBinding(@Qualifier("probeQueue") Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("probe");
    }

    @Bean
    Binding encodeVideoBinding(@Qualifier("encodeVideoQueue") Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("encode.video");
    }

    @Bean
    Binding encodeAudioBinding(@Qualifier("encodeAudioQueue") Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("encode.audio");
    }

    @Bean
    Binding concatBinding(@Qualifier("concatQueue") Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with("concat");
    }

}
