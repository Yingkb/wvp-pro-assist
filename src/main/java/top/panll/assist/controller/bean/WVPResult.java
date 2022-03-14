package top.panll.assist.controller.bean;

import lombok.Data;
import lombok.experimental.Accessors;
import top.panll.assist.enums.ErrorMsgEnum;
import top.panll.assist.enums.SuccessMsgEnum;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class WVPResult<T> implements Serializable {

    private boolean success = false;
    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
        this.success = true;
    }

    public void setError(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void setError(ErrorMsgEnum error) {
        this.code = error.getCode();
        this.msg = error.getMsg();
    }


    public static <T> WVPResult<T> cloneError(WVPResult src) {
        WVPResult<T> resultDTO = new WVPResult<>();
        resultDTO.setError(src.getCode(), src.getMsg());
        return resultDTO;
    }

    public static <T> WVPResult<T> buildSuccessResult(T data) {
        WVPResult<T> resultDTO = new WVPResult<>();
        resultDTO.setData(data);
        resultDTO.setCode(SuccessMsgEnum.SUCCESS.getCode());
        resultDTO.setMsg(SuccessMsgEnum.SUCCESS.getMsg());
        return resultDTO;
    }

    public static <T> WVPResult<T> buildSuccessResult() {
        WVPResult<T> resultDTO = new WVPResult<>();
        resultDTO.setSuccess(true);
        resultDTO.setCode(SuccessMsgEnum.SUCCESS.getCode());
        resultDTO.setMsg(SuccessMsgEnum.SUCCESS.getMsg());
        return resultDTO;
    }

    public static <T> WVPResult<T> buildErrorResult(ErrorMsgEnum error) {
        WVPResult<T> resultDTO = new WVPResult<>();
        resultDTO.setError(error);
        return resultDTO;
    }

    public static <T> WVPResult<T> buildErrorResult(int errCode, String errMsg) {
        WVPResult<T> resultDTO = new WVPResult<>();
        resultDTO.setError(errCode, errMsg);
        return resultDTO;
    }


}
