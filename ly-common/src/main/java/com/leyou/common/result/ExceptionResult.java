package com.leyou.common.result;

import com.leyou.common.exception.LyException;
import lombok.Getter;
import org.joda.time.DateTime;

/**
 * @创建人 zhenzekun
 * @创建时间 2019/8/28
 * @描述 自定义异常返回结果
 */

@Getter
public class ExceptionResult {
    private int status;
    private String message;
    private String timestamp;

    public ExceptionResult(LyException e) {
        this.status = e.getStatus();
        this.message = e.getMessage();
        this.timestamp = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}
