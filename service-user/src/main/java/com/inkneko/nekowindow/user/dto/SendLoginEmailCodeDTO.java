package com.inkneko.nekowindow.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendLoginEmailCodeDTO {
    @Pattern(regexp = "[0-9a-zA-Z_-]+?@(qq\\.com|vip\\.qq\\.com|163\\.com|gmail\\.com|foxmail\\.com|126\\.com|sina\\.com|outlook\\.com)",
             message = "邮箱格式不正确，仅支持腾讯/新浪/网易/微软/谷歌邮箱")
    private String email;
}
