package com.inkneko.nekowindow.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkneko.nekowindow.api.auth.client.AuthClient;
import com.inkneko.nekowindow.api.auth.vo.AuthVo;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.user.config.UserServiceConfig;
import com.inkneko.nekowindow.user.dto.EmailLoginDTO;
import com.inkneko.nekowindow.user.dto.SendLoginEmailCodeDTO;
import com.inkneko.nekowindow.user.entity.Relation;
import com.inkneko.nekowindow.user.entity.UserCredential;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.mapper.*;
import com.inkneko.nekowindow.user.service.UserService;
import com.inkneko.nekowindow.user.util.AsyncMailSender;
import com.inkneko.nekowindow.user.vo.DailyBonusVO;
import com.inkneko.nekowindow.user.vo.LoginVO;
import com.inkneko.nekowindow.user.vo.MyUserDetailVO;
import com.inkneko.nekowindow.user.vo.UserDetailVO;
import org.apache.commons.lang3.time.DateUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private final AsyncMailSender asyncMailSender;
    private final SecureRandom secureRandom;

    private final UserServiceConfig userServiceConfig;
    private final UserCredentialMapper userCredentialMapper;
    private final UserDetailMapper userDetailMapper;
    private final RelationMapper relationMapper;
    private final PrivateMessageMapper privateMessageMapper;

    private final RedissonClient redissonClient;
    private final RMapCache<String, String> loginEmailCodeMap;
    private final RMapCache<String, String> registerEmailCodeMap;

    private final AuthClient authClient;

    public UserServiceImpl(
            AsyncMailSender asyncMailSender,
            UserCredentialMapper userCredentialMapper,
            UserServiceConfig userServiceConfig,
            UserDetailMapper userDetailMapper,
            RedissonClient redissonClient,
            AuthClient authClient,
            RelationMapper relationMapper,
            PrivateMessageMapper privateMessageMapper
    ) {
        this.asyncMailSender = asyncMailSender;
        this.secureRandom = new SecureRandom();
        this.userCredentialMapper = userCredentialMapper;
        this.userDetailMapper = userDetailMapper;
        this.redissonClient = redissonClient;
        this.loginEmailCodeMap = redissonClient.getMapCache("user-login-email-code");
        this.registerEmailCodeMap = redissonClient.getMapCache("user-register-email-code");
        this.authClient = authClient;
        this.userServiceConfig = userServiceConfig;
        this.relationMapper = relationMapper;
        this.privateMessageMapper = privateMessageMapper;
    }

    private String genEmailCode() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }

    @Override
    public void sendLoginEmailCode(@Validated SendLoginEmailCodeDTO dto) {
        UserCredential userCredential = userCredentialMapper.selectOne(new LambdaQueryWrapper<UserCredential>().eq(UserCredential::getEmail, dto.getEmail()));
        String code = genEmailCode();

        if (userCredential != null) {
            if (loginEmailCodeMap.remainTimeToLive(dto.getEmail()) > 240 * 1000) {
                throw new ServiceException(400, "注册验证码请求过于频繁");
            }
            loginEmailCodeMap.put(dto.getEmail(), code, 5, TimeUnit.MINUTES);
            asyncMailSender.send(
                    String.format("NekoWindow <%s>", userServiceConfig.getEmailFrom()),
                    dto.getEmail(),
                    "【墨云视窗】登录验证",
                    String.format("您的登录验证码为<span style='color: #3a62bf;'>%s</span>, 5分钟内有效", code),
                    true
            );
        } else {
            registerEmailCodeMap.put(dto.getEmail(), code, 5, TimeUnit.MINUTES);
            asyncMailSender.send(
                    "NekoWindow <%s>".formatted(userServiceConfig.getEmailFrom()),
                    dto.getEmail(),
                    "【墨云视窗】注册验证",
                    String.format("您的注册验证码为<span style='color: #3a62bf;'>%s</span>, 5分钟内有效", code),
                    true
            );
        }
    }


    @Override
    @Transactional
    public LoginVO login(EmailLoginDTO dto) {
        UserCredential userCredential = userCredentialMapper.selectOne(new LambdaQueryWrapper<UserCredential>().eq(UserCredential::getEmail, dto.getEmail()));
        if (userCredential == null) {
            String code = registerEmailCodeMap.get(dto.getEmail());
            if (code == null || !code.equals(dto.getCode())) {
                throw new ServiceException(403, "验证码错误或已失效");
            }
            if (registerEmailCodeMap.remove(dto.getEmail()) == null){
                throw new ServiceException(403, "验证码已被使用");
            }
            userCredential = new UserCredential();
            userCredential.setEmail(dto.getEmail());
            userCredential.setAuthSalt("-");
            userCredential.setAuthHash("-");
            userCredentialMapper.insert(userCredential);
            UserDetail userDetail = new UserDetail();
            userDetail.setUid(userCredential.getUid());
            userDetail.setGender("保密");
            userDetail.setUsername(String.format("neko_%d", userCredential.getUid()));
            userDetailMapper.insert(userDetail);
        } else {
            String code = loginEmailCodeMap.get(dto.getEmail());
            if (code == null || !code.equals(dto.getCode())) {
                throw new ServiceException(403, "验证码错误或已失效");
            }
            loginEmailCodeMap.remove(dto.getEmail());
        }

        AuthVo authVo = authClient.newSession(userCredential.getUid());

        return new LoginVO(authVo.getSessionToken(), authVo.getUserId());
    }


    @Override
    public UserDetail getUserDetail(Long userId) {
        UserDetail userDetail = userDetailMapper.selectById(userId);
        if (userDetail == null) {
            throw new ServiceException(404, "用户不存在");
        }
        return userDetail;
    }

    /**
     * 查询指定userId用户的详细资料，同时并尝试获取该用户的登录奖励
     *
     * @param userId 用户id
     * @return 用户详细资料以及登录奖励获得信息
     */
    @Override
    public MyUserDetailVO getMyUserDetail(Long userId) {
        //使用isolation=读已提交与select for update实现行级锁
        LambdaQueryWrapper<UserDetail> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(UserDetail::getUid, userId);
        lambdaQueryWrapper.last("for update");
        UserDetail userDetail = userDetailMapper.selectOne(lambdaQueryWrapper);

        if (userDetail == null) {
            throw new ServiceException(404, "用户不存在");
        }
        //检查是否为同一天
        boolean isDifferentDay = !LocalDate.now().isEqual(userDetail.getLastLogin().toLocalDate());
        if (isDifferentDay) {
            userDetail.setLastLogin(ZonedDateTime.now());
            userDetail.setCoins(userDetail.getCoins() + 1);
            userDetailMapper.updateById(userDetail);
        }
        return new MyUserDetailVO(userDetail, isDifferentDay);
    }

    @Override
    public void updateUserDetail(UserDetail userDetail) {
        String username = userDetail.getUsername();
        if (username != null && username.trim().isEmpty()){
            throw new ServiceException(400, "用户名不能为空");
        }
        userDetailMapper.updateById(userDetail);
    }

    /**
     * 关注用户
     *
     * @param fromUserId 发起者的用户ID
     * @param toUserId   被关注者的用户ID
     */
    @Override
    @Transactional
    public void subscribeUser(Long fromUserId, Long toUserId) {
        if (fromUserId.equals(toUserId)) {
            throw new ServiceException(400, "用户不能关注自己");
        }
        if (userCredentialMapper.selectById(fromUserId) == null) {
            throw new ServiceException(400, "关注发起用户不存在");
        }
        if (userCredentialMapper.selectById(toUserId) == null) {
            throw new ServiceException(400, "关注目标用户不存在");
        }

        try {
            //添加关注
            Relation relation = new Relation();
            relation.setFollowerUid(fromUserId);
            relation.setTargetUid(toUserId);
            relationMapper.insert(relation);
            //当且仅当两个用户同时互相关注时，才会发生死锁。交给超时处理，自动回滚
            //关注者的关注数+1
            userDetailMapper.updateUserSubscribesNum(fromUserId, 1);
            //被关注者的粉丝数+1
            userDetailMapper.updateUserFansNum(toUserId, 1);
        } catch (DataIntegrityViolationException e) {
            throw new ServiceException(400, "已关注该用户");
        }
    }

    /**
     * 取消关注用户
     *
     * @param fromUserId 发起者的用户ID
     * @param toUserId   被关注者的用户ID
     */
    @Override
    @Transactional
    public void unsubscribeUser(Long fromUserId, Long toUserId) {
        int affectedRows = relationMapper.delete(
                new LambdaQueryWrapper<Relation>()
                        .eq(Relation::getFollowerUid, fromUserId)
                        .eq(Relation::getTargetUid, toUserId)
        );
        if (affectedRows != 0) {
            //关注者的关注数-1
            userDetailMapper.updateUserSubscribesNum(fromUserId, -1);
            //被关注者的粉丝数-1
            userDetailMapper.updateUserFansNum(toUserId, -1);
        }
    }

    /**
     * 查询用户的关注列表
     *
     * @param userId 用户ID
     * @param page   页数，从1开始
     * @param size   页面大小
     * @return 指定用户的关注列表
     */
    @Override
    public List<UserDetailVO> getUserSubscribeList(Long userId, Integer page, Integer size) {
        if (page <= 0){
            throw new ServiceException(400, "起始页数需大于等于1");
        }
        if (size > 50){
            throw new ServiceException(400, "请求的页面过大");
        }
        List<Relation> userRelationList = relationMapper.selectList(
                new LambdaQueryWrapper<Relation>()
                        .eq(Relation::getFollowerUid, userId)
                        .last("LIMIT %d, %d".formatted((page - 1) * size, size))
        );
        List<UserDetailVO> result = new ArrayList<>();
        for (Relation userRelation : userRelationList) {
            UserDetail targetUserDetail = userDetailMapper.selectById(userRelation.getTargetUid());
            result.add(new UserDetailVO(targetUserDetail));
        }
        return result;
    }

    /**
     * 查询指定用户的粉丝列表
     *
     * @param userId 用户ID
     * @param page   页数，从1开始
     * @param size   页面大小
     * @return 指定用户的粉丝列表
     */
    @Override
    public List<UserDetailVO> getUserFollowerList(Long userId, Integer page, Integer size) {
        if (page <= 0){
            throw new ServiceException(400, "起始页数需大于等于1");
        }
        if (size > 50){
            throw new ServiceException(400, "请求的页面过大");
        }
        List<Relation> userRelationList = relationMapper.selectList(
                new LambdaQueryWrapper<Relation>()
                        .eq(Relation::getTargetUid, userId)
                        .last("LIMIT %d, %d".formatted((page - 1) * size, size))
        );
        List<UserDetailVO> result = new ArrayList<>();
        for (Relation userRelation : userRelationList) {
            UserDetail targetUserDetail = userDetailMapper.selectById(userRelation.getTargetUid());
            result.add(new UserDetailVO(targetUserDetail));
        }
        return result;
    }
}
