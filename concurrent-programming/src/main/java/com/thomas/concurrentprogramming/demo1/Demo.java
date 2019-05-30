package com.thomas.concurrentprogramming.demo1;

/**
 * @Author: lf
 * @Date: 2019/5/29 14:16
 * Thread 简单应用
 */
public class Demo {
    PrintProcessor printProcessor;

    public Demo() {
        SaveProcessor saveProcessor=new SaveProcessor();
        saveProcessor.start();
        printProcessor=new PrintProcessor(saveProcessor);
        printProcessor.start();
    }

    public static void main(String[] args) {
        Request request=new Request();
        request.setName("发哥");
        new Demo().doTest(request);
    }
    public void doTest(Request request){
        printProcessor.processorRequest(request);
    }
}
