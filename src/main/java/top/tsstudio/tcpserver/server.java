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
    public String currentPGMScene, currentPVWScene;
    public configurateHelper config;

    public void push_illuminance_message_by_machine(String machine, int illuminance) {
        logger.info("Pushing illuminance message: " + machine + " " + illuminance);
        for (connectionHandler handler : connectionHandlers) {
            if (!handler.alive) {
                continue;
            }
            handler.illuminancePusher(machine, illuminance);
        }
    }

    public void push_illuminance_message_by_scene(String scene, int illuminance) {
        logger.info("Pushing illuminance message: " + scene + " " + illuminance);
        String machine = config.scenes.get(scene);
        for (connectionHandler handler : connectionHandlers) {
            if (!handler.alive) {
                continue;
            }
            handler.illuminancePusher(machine, illuminance);
        }
    }

    public void push_message_by_scene(String scene, String type) {
        logger.info("Pushing scene: " + scene + " type: " + type);
        //check if scene is in config.scenes' keys
        String message = config.scenes.get(scene);
        if (message == null) {
            logger.error("Scene not found in config: " + scene);
            return;
        }
        if (type.equals("PGM")) {
            this.currentPGMScene = scene;
        }
        if (type.equals("PVW")) {
            this.currentPVWScene = scene;
        }
        for (connectionHandler handler : connectionHandlers) {
            if (!handler.alive) {
                continue;
            }
            handler.messagePusher(message, type);
        }
    }

    public void reload_push_message_by_scene() {
        this.push_message_by_scene(this.currentPGMScene, "PGM");
        this.push_message_by_scene(this.currentPVWScene, "PVW");
    }

    /**
     * @deprecated Use push_message_by_scene instead
     */
    public void push_message(String message, String type) {
        logger.info("Pushing message: " + message + " type: " + type);
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
