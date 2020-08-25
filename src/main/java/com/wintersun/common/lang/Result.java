package com.wintersun.common.lang;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result implements Serializable {

    // 0成功，-1失败
    private int status;
    private String msg;
    private Object data;
    private String action;

    public static Result success() {
        return Result.success("操作成功", null);
    }

    public static Result success(Object data) {
        return Result.success("操作成功", data);
    }



    public static Result success(String msg, Object data){
      Result result = new Result();
      result.setStatus(0);
      result.setData(data);
      result.setMsg(msg);

      return result;
  }

    public static Result fail() {
        return Result.fail("操作失败");
    }


    public static Result fail(String msg) {
        Result result = new Result();
        result.status = -1;
        result.data = null;
        result.msg = msg;
        return result;
    }

    public Result action(String action){
        this.action = action;
        return this;
    }


}
