package com.xinzhe.aiassistant.common.result;

import lombok.Data;

/**
 * 全局统一返回类
 * 所有接口的返回结果都用这个类封装，保证格式统一
 * @param <T> 泛型，代表任意类型的业务数据
 */
@Data
public class Result<T> {
    // 状态码：200=成功，其他=失败
    private Integer code;
    // 提示信息：成功返回"操作成功"，失败返回具体错误原因
    private String msg;
    // 业务数据：接口要返回的具体内容，比如用户信息、聊天记录
    private T data;

    /**
     * 成功返回（带业务数据）
     * 比如查询用户信息成功，把用户对象放在data里返回
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 成功返回（不带业务数据）
     * 比如删除、修改成功，不需要返回数据
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 失败返回（只传提示信息）
     * 比如"用户名已存在""密码错误"
     */
    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    /**
     * 失败返回（自定义状态码+提示信息）
     * 比如401未登录、403无权限
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}