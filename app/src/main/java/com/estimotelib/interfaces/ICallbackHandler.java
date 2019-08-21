package com.estimotelib.interfaces;

public interface ICallbackHandler<T> {

    void response(T response);

    void isError(String errorMsg);
}