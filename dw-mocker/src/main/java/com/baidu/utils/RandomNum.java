package com.baidu.utils;

import java.util.Random;

/**
 * @author Mr.Xu
 * @description:
 * @create 2020-02-23 15:46
 */
public class RandomNum {
    public static final int getRandInt(int fromNum, int toNum) {
        return fromNum + new Random().nextInt(toNum - fromNum + 1);
    }
}

