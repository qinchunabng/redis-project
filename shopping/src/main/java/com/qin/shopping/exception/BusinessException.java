package com.qin.shopping.exception;

import lombok.Data;

/**
 * description
 *
 * @author qcb
 * @date 2022/09/17 21:00.
 */
@Data
public class BusinessException extends RuntimeException{

    public BusinessException(String errorMsg) {
        super(errorMsg);
    }

}
