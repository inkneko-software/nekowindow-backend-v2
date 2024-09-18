package com.inkneko.nekowindow.user.dto;

import lombok.Data;

@Data
public class EmailLoginDTO {
    private String email;
    private String code;
}
