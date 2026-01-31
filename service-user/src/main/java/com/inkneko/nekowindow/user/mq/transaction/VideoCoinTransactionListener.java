package com.inkneko.nekowindow.user.mq.transaction;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.mq.PostVideoCoinDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.user.entity.CoinHistory;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.mapper.CoinHistoryMapper;
import com.inkneko.nekowindow.user.mapper.UserDetailMapper;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RocketMQTransactionListener(rocketMQTemplateBeanName = "videoCoinExtRocketMQTemplate")
public class VideoCoinTransactionListener implements RocketMQLocalTransactionListener {

    private final UserDetailMapper userDetailMapper;
    CoinHistoryMapper coinHistoryMapper;
    RedissonClient redissonClient;
    public VideoCoinTransactionListener(
            CoinHistoryMapper coinHistoryMapper,
            RedissonClient redissonClient, UserDetailMapper userDetailMapper) {
        this.coinHistoryMapper = coinHistoryMapper;
        this.redissonClient = redissonClient;
        this.userDetailMapper = userDetailMapper;
    }

    @Override
    @Transactional
    public RocketMQLocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        ObjectMapper objectMapper = new ObjectMapper();
        PostVideoCoinDTO dto = null;
        try {
            dto = objectMapper.readValue((byte[]) msg.getPayload(), PostVideoCoinDTO.class);
            if (dto == null || dto.getUserId() == null || dto.getNkid() == null) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (IOException e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }

        // 对同一用户的视频投币操作加锁，防止并发问题
        RLock lock = redissonClient.getLock("UserService::VideoCoinTransactionListener::UserPostVideoCoinLock:%d".formatted(dto.getUserId()));
        boolean isLocked = false;
        try{
            isLocked = lock.tryLock(1, 5, TimeUnit.SECONDS);
            if (!isLocked) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 幂等性校验
            CoinHistory coinHistory = coinHistoryMapper.selectOne(
                    Wrappers.<CoinHistory>lambdaQuery()
                            .eq(CoinHistory::getBizKey, dto.getOrderId())
            );

            if (coinHistory != null) {
                return RocketMQLocalTransactionState.COMMIT;
            }

            // 校验用户余额
            UserDetail userDetail = userDetailMapper.selectOne(Wrappers.<UserDetail>lambdaQuery().eq(UserDetail::getUid, dto.getUserId()));
            if (userDetail.getCoins() < dto.getCoinNum()) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            // 校验是否投币达到上限
            List<CoinHistory> coinHistoryList = coinHistoryMapper.selectList(
                    Wrappers.<CoinHistory>lambdaQuery()
                            .eq(CoinHistory::getUid, dto.getUserId())
                            .eq(CoinHistory::getBizId, dto.getNkid())
                            .eq(CoinHistory::getBizType, "VIDEO")
            );
            int postedCoinNum = coinHistoryList.stream().mapToInt(CoinHistory::getNum).sum();
            if (postedCoinNum + dto.getCoinNum() > 2){
                return RocketMQLocalTransactionState.ROLLBACK;
            }

            // 未达上限则插入数据库
            coinHistory = new CoinHistory();
            coinHistory.setUid(dto.getUserId());
            // 硬币支出
            coinHistory.setNum(-dto.getCoinNum());
            coinHistory.setBizId(dto.getNkid());
            coinHistory.setBizType("VIDEO");
            coinHistory.setBizKey(dto.getOrderId());
            coinHistoryMapper.insert(coinHistory);

            userDetailMapper.update(
                    null,
                    Wrappers.<UserDetail>lambdaUpdate()
                    .eq(UserDetail::getUid, dto.getUserId())
                    .setSql("coins = coins - %d".formatted(dto.getCoinNum()))
            );
            return RocketMQLocalTransactionState.COMMIT;

        }catch (InterruptedException e){
            return RocketMQLocalTransactionState.ROLLBACK;
        }finally {
            if (isLocked){
                lock.unlock();
            }
        }
    }

    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message msg) {
        ObjectMapper objectMapper = new ObjectMapper();
        PostVideoCoinDTO dto = null;
        try {
            dto = objectMapper.readValue(msg.getPayload().toString(), PostVideoCoinDTO.class);
            if (dto == null || dto.getUserId() == null || dto.getNkid() == null) {
                return RocketMQLocalTransactionState.ROLLBACK;
            }
        } catch (JsonProcessingException e) {
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        CoinHistory coinHistory = coinHistoryMapper.selectOne(
                Wrappers.<CoinHistory>lambdaQuery()
                        .eq(CoinHistory::getBizKey, dto.getOrderId())
        );
        return coinHistory != null ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.ROLLBACK;
    }
}