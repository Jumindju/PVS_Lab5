package vsy.clockdemo.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ParallelClockServer {

    private ServerSocket _listenSocket;
    private HashMap<String, Clock> _clockMap;

    public ParallelClockServer(int port) throws IOException {
        _listenSocket = new ServerSocket(port);
        _clockMap = new HashMap<>();
    }

    public static void main(String args[]) {
        try {
            new ParallelClockServer(2112).run();
        } catch (Exception ex) {
            System.out.println("Could not start server!");
            ex.printStackTrace();
        }
    }

    public void run() throws IOException {
        System.out.println("Server started!");
        while (true) {
            Socket clientSocket = _listenSocket.accept();
            System.out.println("New Client");

            (new Thread(() -> {
                service(clientSocket);
            })).start();
        }
    }

    private void service(Socket clientSocket) {
        try {
            String ipAddress = clientSocket.getInetAddress().toString();
            Clock clock;

            if (_clockMap.containsKey(ipAddress)) {
                clock = _clockMap.get(ipAddress);
            } else {
                clock = new Clock();
                _clockMap.put(ipAddress, clock);
                System.out.println("Add new clock: " + ipAddress);
            }

            ServerHelper.RunClockLogic(clientSocket, clock);
        } catch (IOException e) {
            if (e.getMessage() == "Connection reset") {
                System.out.println("Client closed connection");
                return;
            }

            System.out.println("Error while running clock process");
            e.printStackTrace();
        }
    }
}
