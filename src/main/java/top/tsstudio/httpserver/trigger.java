package top.tsstudio.httpserver;

import top.tsstudio.configurateHelper;
import top.tsstudio.tcpserver.server;

public class trigger {
    private server tcpServer;
    private configurateHelper config;

    public void trigger_reload() {
        this.config.load(this.config.last_filepath);
        this.tcpServer.reload_push_message_by_scene();
    }

    public void trigger_reload(String filepath) {
        this.config.load(filepath);
    }


    trigger(server tcpServer, configurateHelper config) {
        this.tcpServer = tcpServer;
        this.config = config;
    }
}
