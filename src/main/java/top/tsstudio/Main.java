package top.tsstudio;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.tsstudio.obsclient.client;
import top.tsstudio.tcpserver.server;

public class Main {

    public static void main(String[] args) {
        Logger logger = LogManager.getLogger("Main");
        server tcpServer = new server();
        configurateHelper config;

        if (args.length >= 1) {
            config = new configurateHelper(args[0]);
        } else if (System.getenv("TALLY_CONFIG_PATH") != null) {
            logger.info("Using config file from environment variable");
            config = new configurateHelper(System.getenv("TALLY_CONFIG_PATH"));
        } else {
            logger.warn("No config file provided, using default config, note this doesn't contain machine info so it can only run but not helpful.");
            config = new configurateHelper();
        }

        client client = new client(config, tcpServer);

        logger.info("Initializing TCP Server...");
        Thread serverThread = new Thread(() -> {
            tcpServer.start_server(config);
        });
        serverThread.start();
        logger.info("Initializing OBS Connection...");
        client.connect();
    }
}