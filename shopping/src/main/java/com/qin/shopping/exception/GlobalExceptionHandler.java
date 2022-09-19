package com.qin.shopping.exception;

import com.qin.shopping.dto.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author qcb
 * @date 2022/09/17 21:03.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result handlerBusinessException(Exception e){
        logger.error(e.getMessage(), e);
        if(e instanceof BusinessException){
            BusinessException businessException = (BusinessException) e;
            return Result.fail(businessException.getMessage());
        }
        return Result.fail(e.getMessage());
    }
}
