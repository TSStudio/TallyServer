package top.tsstudio.obsclient;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.OBSRemoteControllerBuilder;
import io.obswebsocket.community.client.message.event.scenes.CurrentPreviewSceneChangedEvent;
import io.obswebsocket.community.client.message.event.scenes.CurrentProgramSceneChangedEvent;
import top.tsstudio.configurateHelper;
import top.tsstudio.tcpserver.server;

public class client {
    private OBSRemoteController controller;

    public client(configurateHelper config, server tcpServer) {
        OBSRemoteControllerBuilder controllerBuilder = OBSRemoteController.builder();
        controllerBuilder.host(config.obsAddress);
        controllerBuilder.port(config.obsPort);
        controllerBuilder.connectionTimeout(config.obsTimeout);
        controllerBuilder.registerEventListener(CurrentProgramSceneChangedEvent.class, event -> {
            tcpServer.push_message(config.scenes.get(event.getSceneName()), "PGM");
        });
        controllerBuilder.registerEventListener(CurrentPreviewSceneChangedEvent.class, event -> {
            tcpServer.push_message(config.scenes.get(event.getSceneName()), "PVW");
        });
        if (config.obsPasswordEnabled) {
            System.out.println("Password is enabled");
            controllerBuilder.password(config.obsPassword);
        }
        controllerBuilder.lifecycle().onClose((e) -> {
            System.out.println("Disconnected from OBS");
            tcpServer.running = false;
            System.exit(1);
        });
        controllerBuilder.lifecycle().onReady(() -> {
            System.out.println("Connected to OBS");
            this.controller.getCurrentPreviewScene(
                    (response) -> tcpServer.push_message(config.scenes.get(response.getCurrentPreviewSceneName()), "PVW")
            );
            this.controller.getCurrentProgramScene(
                    (response) -> tcpServer.push_message(config.scenes.get(response.getCurrentProgramSceneName()), "PGM")
            );
        });
        this.controller = controllerBuilder.build();
    }

    public void connect() {
        controller.connect();
    }

}
