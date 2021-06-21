package com.kuo.easychat;

import com.kuo.easychat.client.WebSocketClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

/**
 * Hello world!
 *
 * @author Fagan Wang
 */
public class ClientApplication implements CommandLineRunner {

    private WebSocketClient client;

    public static void main(String[] args) {
        new SpringApplication(ClientApplication.class).run(args);
    }


    @Override
    public void run(String... args) throws Exception {
        this.registeEvent();
        this.connect();
        this.handleCommand();
    }

    private void registeEvent() {
        // 注册登录响应
//        EventPool.getInstance().registe(ActionId);
    }

    private void connect() {}

    private void handleCommand() {}
}
