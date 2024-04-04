package top.tsstudio.obsclient;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.message.event.scenes.CurrentPreviewSceneChangedEvent;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.tsstudio.configurateHelper;
import top.tsstudio.tcpserver.server;

public class client {
    private OBSRemoteController controller;

    public client(configurateHelper config, server tcpServer, Thread serverThread) {
        Logger logger = LogManager.getLogger("Main");
        OBSRemoteControllerBuilder controllerBuilder = OBSRemoteController.builder();
        controllerBuilder.host(config.obsAddress);
        controllerBuilder.port(config.obsPort);
        controllerBuilder.connectionTimeout(config.obsTimeout);
        controllerBuilder.registerEventListener(CurrentProgramSceneChangedEvent.class, event -> {
            tcpServer.push_message_by_scene(event.getSceneName(), "PGM");
        });
        controllerBuilder.registerEventListener(CurrentPreviewSceneChangedEvent.class, event -> {
            tcpServer.push_message_by_scene(event.getSceneName(), "PVW");
        });
        if (config.obsPasswordEnabled) {
            logger.info("Using password for OBS connection");
            controllerBuilder.password(config.obsPassword);
        }
        controllerBuilder.lifecycle().onClose((e) -> {
            logger.error("Connection to OBS closed, shutting down...");
            tcpServer.running = false;
            serverThread.interrupt();
            System.exit(1);
        });
        controllerBuilder.lifecycle().onReady(() -> {
            logger.info("Connection to OBS established");
            this.controller.getCurrentPreviewScene(
                    (response) -> tcpServer.push_message_by_scene(response.getCurrentPreviewSceneName(), "PVW")
            );
            this.controller.getCurrentProgramScene(
                    (response) -> tcpServer.push_message_by_scene(response.getCurrentProgramSceneName(), "PGM")
            );
        });
        this.controller = controllerBuilder.build();
    }

    public void connect() {
        controller.connect();
    }

}
