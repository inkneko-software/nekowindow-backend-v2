package com.inkneko.nekowindow.user.vo;

import com.inkneko.nekowindow.user.entity.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailVO {
    private Long uid;
    private String username;
    private String sign;
    private Integer exp;
    private String gender;
    private Date birth;
    private String avatarUrl;
    private String bannerUrl;
    private Integer fans;
    private Integer subscribes;

    public UserDetailVO(UserDetail userDetail){
        this.uid = userDetail.getUid();
        this.username = userDetail.getUsername();
        this.sign = userDetail.getSign();
        this.exp = userDetail.getExp();
        this.gender = userDetail.getGender();
        this.birth = userDetail.getBirth();
        this.avatarUrl = userDetail.getAvatarUrl();
        this.bannerUrl = userDetail.getBannerUrl();
        this.fans = userDetail.getFans();
        this.subscribes = userDetail.getSubscribes();
    }
}
