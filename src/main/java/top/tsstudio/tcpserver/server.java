package top.tsstudio.tcpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {

    private Set<connectionHandler> connectionHandlers = new HashSet<>();
    public boolean running = true;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private ServerSocket serverSocket;
    public String currentPGM, currentPVW;

    public void push_message(String message, String type) {
        System.out.println("Pushing message: " + message + " type: " + type);
        if (type.equals("PGM")) {
            this.currentPGM = message;
        }
        if (type.equals("PVW")) {
            this.currentPVW = message;
        }
        for (connectionHandler handler : connectionHandlers) {
            System.out.println("Pushing message to client" + handler.socket.getInetAddress());
            if (!handler.alive) {
                continue;
            }
            handler.messagePusher(message, type);
        }
    }

    public void start_server() {
        try {
            this.serverSocket = new ServerSocket(38383);
            System.out.println("Waiting for connection...");
            this.Service();
        } catch (IOException e) {
            e.printStackTrace();
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
        System.out.println("Server is shutting down...");
    }
}
