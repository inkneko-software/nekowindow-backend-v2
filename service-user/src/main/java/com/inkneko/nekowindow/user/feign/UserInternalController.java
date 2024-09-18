package com.inkneko.nekowindow.user.feign;

import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.service.UserService;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class UserInternalController implements UserFeignClient {

    @Autowired
    UserService userService;

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
}
