package com.leyou.common.advice;

import com.leyou.common.exception.LyException;
import com.leyou.common.result.ExceptionResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/28
 * @描述 异常通知类
 */
@ControllerAdvice
@Slf4j
public class BasicExceptionAdvice {
    //捕获异常处理 RuntimeException
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(RuntimeException e){
        // 我们暂定返回状态码为400， 然后从异常中获取友好提示信息
        return ResponseEntity.status(400).body(e.getMessage());
    }
    //捕获异常处理 自定义异常
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> LyException(LyException le){
        // 我们暂定返回状态码为400， 然后从异常中获取友好提示信息
        return ResponseEntity.status(le.getStatus()).body(new ExceptionResult(le));
    }


}
