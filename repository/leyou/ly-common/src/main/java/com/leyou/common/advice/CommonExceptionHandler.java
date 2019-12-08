package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**通用异常处理
 * @author wangshiyu
 */
@ControllerAdvice
public class CommonExceptionHandler {

    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handlerException(LyException e){
        return ResponseEntity.status(e.getExceptionEnum().getCode()).body(new ExceptionResult(e.getExceptionEnum()));
    }

}
