package com.inkneko.nekowindow.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordByEmailCodeDTO {
    private String email;
    private String code;
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d@$!%*?&_-]{8,20}$", message = "密码格式错误")
    private String newPassword;
}
