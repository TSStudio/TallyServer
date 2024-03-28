package top.tsstudio;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.message.event.scenes.CurrentPreviewSceneChangedEvent;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import top.tsstudio.obsclient.client;
import top.tsstudio.tcpserver.server;

public class Main {

    public static void main(String[] args) {
        server tcpServer = new server();
        configurateHelper config;

        if (args.length >= 1) {
            config = new configurateHelper(args[0]);
        } else if (System.getenv("TALLY_CONFIG_PATH") != null) {
            System.out.println("Using config file from environment variable");
            config = new configurateHelper(System.getenv("TALLY_CONFIG_PATH"));
        } else {
            System.out.println("No config file provided, using default config");
            config = new configurateHelper();
        }

        client client = new client(config, tcpServer);

        System.out.println("Initializing TCP Server...");
        Thread serverThread = new Thread(tcpServer::start_server);
        serverThread.start();
        System.out.println("Initializing OBS Connection...");
        client.connect();
    }
}