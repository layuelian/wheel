package com.thomas.concurrentprogramming.demo1;

/**
 * @Author: lf
 * @Date: 2019/5/29 10:27
 */
public class Request {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Request{" +
                "name='" + name + '\'' +
                '}';
    }
}
