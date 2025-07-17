package com.example.entity;

import lombok.Data;

@Data
public class RestBean<T> {
    private int status;
    private boolean success;
    private T message;

    public RestBean(int status, boolean success, T message) {
        this.status = status;
        this.success = success;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getMessage() {
        return message;
    }

    public void setMessage(T message) {
        this.message = message;
    }

    public static <T> RestBean<T> success() {
        return new RestBean<>(200,true,null);
    }

    public static <T> RestBean<T> success(T data) {
        return new RestBean<>(200,true,data);
    }

    public static <T> RestBean<T> failure(int status) {
        return new RestBean<>(status,false,null);
    }

    public static <T> RestBean<T> failure(int status, T data) {
        return new RestBean<>(status,false,data);
    }
}
