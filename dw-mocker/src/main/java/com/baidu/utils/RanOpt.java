package com.baidu.utils;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-02-23 15:45
 */
public class RanOpt<T>{
    T value ;
    int weight;

    public RanOpt ( T value, int weight ){
        this.value=value ;
        this.weight=weight;
    }

    public T getValue() {
        return value;
    }

    public int getWeight() {
        return weight;
    }
}

