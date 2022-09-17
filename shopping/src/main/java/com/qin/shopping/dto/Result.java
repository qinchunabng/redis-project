package com.qin.shopping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    private Boolean success;
    private String errorMsg;
    private T data;
    private Long total;

    public static Result ok(){
        return new Result(true, null, null, null);
    }
    public static <T> Result ok(T data){
        return new Result(true, null, data, null);
    }
    public static <T> Result ok(List<T> data, Long total){
        return new Result(true, null, data, total);
    }
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null, null);
    }
}
