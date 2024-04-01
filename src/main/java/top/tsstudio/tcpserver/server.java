package top.tsstudio.tcpserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.tsstudio.configurateHelper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    private final Logger logger = LogManager.getLogger("Main");

    private Set<connectionHandler> connectionHandlers = new HashSet<>();
    public boolean running = true;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    public String currentPGM, currentPVW;
    public configurateHelper config;

    public void push_message(String message, String type) {
        logger.info("Pushing message: " + message + " type: " + type);
        if (type.equals("PGM")) {
            this.currentPGM = message;
        }
        if (type.equals("PVW")) {
            this.currentPVW = message;
        }
        for (connectionHandler handler : connectionHandlers) {
            if (!handler.alive) {
                continue;
            }
            handler.messagePusher(message, type);
        }
    }

    public void start_server(configurateHelper config) {
        try {
            this.config = config;
            this.serverSocket = new ServerSocket(config.tcpPort);
            logger.info("Server started on port " + config.tcpPort);
            this.Service();
        } catch (IOException e) {
            //e.printStackTrace();
            logger.error("Failed to start server, shutting down...", e);
        }
    }

    public void Service() throws IOException {
        while (this.running) {
            Socket socket = null;
            socket = serverSocket.accept();//将服务器和客户端的通信交给线程池处理
            connectionHandler handler = new connectionHandler(socket, this);
            connectionHandlers.add(handler);
            executorService.execute(handler);
        }
        logger.error("Server stopped");
    }
}
