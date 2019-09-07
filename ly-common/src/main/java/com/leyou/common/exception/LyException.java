package com.leyou.common.exception;

import com.leyou.common.enums.ExceptionEnum;
import lombok.Getter;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/28
 * @描述 自定义异常
 */
@Getter
public class LyException extends RuntimeException{
    private int status;

    public LyException(ExceptionEnum em) {
        super(em.getMessage());
        this.status = em.getStatus();
    }

    public LyException(ExceptionEnum em, Throwable cause) {
        super(em.getMessage(),cause);
        this.status = em.getStatus();
    }
}
