package com.inkneko.nekowindow.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response<T>{
    private Integer code;
    private String message;
    private T data;

    public Response(String message){
        this.message = message;
        this.code = 0;
    }

    public Response(String message, T data){
        this.code = 0;
        this.message = message;
        this.data = data;
    }

    public Response(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public Response(Integer code, String message, T data){
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
