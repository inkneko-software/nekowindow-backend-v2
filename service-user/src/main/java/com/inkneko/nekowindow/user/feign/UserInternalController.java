package com.inkneko.nekowindow.user.feign;

import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.service.UserInternalService;
import com.inkneko.nekowindow.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Hidden
@RestController
public class UserInternalController implements UserFeignClient {

    UserService userService;
    UserInternalService userInternalService;

    public UserInternalController(UserService userService, UserInternalService userInternalService) {
        this.userService = userService;
        this.userInternalService = userInternalService;
    }

    @Override
    public UserVo get(Long userId) {
        UserDetail userDetail = userService.getUserDetail(userId);
        return  new UserVo(
                userDetail.getUid(),
                userDetail.getUsername(),
                userDetail.getSign(),
                userDetail.getExp(),
                userDetail.getGender(),
                userDetail.getBirth(),
                userDetail.getAvatarUrl(),
                userDetail.getBannerUrl(),
                userDetail.getFans(),
                userDetail.getSubscribes());
    }

    @Override
    public List<UserVo> getSubscribeList(Long userId) {
        return userInternalService.getUserSubscribeList(userId);
    }

    @Override
    public List<UserVo> getFanList(Long userId) {
        return userInternalService.getUserFansList(userId);
    }
}
