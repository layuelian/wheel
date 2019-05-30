package com.thomas.concurrentprogramming.demo1;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Author: lf
 * @Date: 2019/5/29 10:31
 */
public class PrintProcessor extends Thread implements RequestProcessor {

    LinkedBlockingDeque<Request> linkedBlockingDeque=new LinkedBlockingDeque<>();

    private final RequestProcessor nextProcessor;

    public PrintProcessor(RequestProcessor nextProcessor){
        this.nextProcessor=nextProcessor;
    }
    @Override
    public void run() {
        while (true){
            try {
                Request request=linkedBlockingDeque.take();
                System.out.println("print data:"+request);
                nextProcessor.processorRequest(request);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processorRequest(Request request) {
        linkedBlockingDeque.add(request);
    }
}
