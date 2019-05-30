package com.thomas.concurrentprogramming.demo1;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Author: lf
 * @Date: 2019/5/29 10:37
 */
public class SaveProcessor extends Thread implements RequestProcessor {
    LinkedBlockingDeque<Request> linkedBlockingDeque=new LinkedBlockingDeque<>();
    @Override
    public void run() {
        while (true){
            try {
                Request request=linkedBlockingDeque.take();
                System.out.println("save data:"+request);
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
