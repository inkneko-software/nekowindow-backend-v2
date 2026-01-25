package com.inkneko.nekowindow.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailPasswordLoginDTO {
    private String email;
    private String password;
}
