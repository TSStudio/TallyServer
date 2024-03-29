package top.tsstudio.tcpserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class connectionHandler implements Runnable {
    private final Logger logger = LogManager.getLogger("Main");
    private OutputStream ostream;

    private int heartBeatCounter = 0;
    public Socket socket;
    public boolean alive = false;

    private server tcpServer;
    //private final Socket socket;

    public void messagePusher(String message, String type) {
        try {
            //make message 3 chars long
            if (message.length() < 3) {
                int length = message.length();
                message = message + "-".repeat(3 - length);
            }
            if (message.length() > 3) {
                message = message.substring(0, 3);
            }
            if (type.equals("PVW")) {
                this.ostream.write(("\002PVW" + message + "\003").getBytes());
                this.ostream.flush();
                return;
            }
            if (type.equals("PGM")) {
                this.ostream.write(("\002PGM" + message + "\003").getBytes());
                this.ostream.flush();
                return;
            }
        } catch (IOException e) {
            logger.error("Err", e);
        }
    }

    private void heartBeatResponder() {
        try {
            this.ostream.write("HEARTBEAT".getBytes());
            this.ostream.flush();
            heartBeatCounter++;
            if (heartBeatCounter == 1) {
                this.messagePusher(tcpServer.currentPVW, "PVW");
                this.messagePusher(tcpServer.currentPGM, "PGM");
            }
        } catch (IOException e) {
            logger.error("Err", e);
        }

    }

    public connectionHandler(Socket socket, server tcpServer) {
        this.socket = socket;
        this.tcpServer = tcpServer;
    }

    public void run() {
        logger.info("New connection accept:" + socket.getInetAddress());
        StringBuilder buffer = new StringBuilder();
        try {
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            this.alive = true;
            this.ostream = outputStream;
            while (true) {
                int c = inputStream.read();
                if (c == -1) {
                    break;
                } else if (c == 0) {
                    String message = buffer.toString();
                    if (message.equals("HEARTBEAT")) {
                        heartBeatResponder();
                    }
                    buffer.setLength(0);
                } else {
                    buffer.append((char) c);
                    if (buffer.length() > 1024000) {//1MB
                        //wtf when this happens?
                        logger.warn("Message too long. Discarding...");
                        break;
                    }
                }
            }

        } catch (IOException e) {
            logger.error("Err", e);
        } finally {
            this.alive = false;
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                logger.error("Err", e);
            }
        }
    }
}
