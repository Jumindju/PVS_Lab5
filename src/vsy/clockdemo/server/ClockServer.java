package vsy.clockdemo.server;

import vsy.clockdemo.shared.ClockManager;
import vsy.clockdemo.shared.ClockRequest;
import vsy.clockdemo.shared.ClockResponse;
import vsy.clockdemo.shared.IllegalCmdException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static vsy.clockdemo.shared.ClockCommands.*;

public class ClockServer {
    private static final int PORT = 2112;

    public static void main(String args[]) {
        Socket talkSocket;

        try {
            ServerSocket listenSocket = new ServerSocket(PORT);

            System.out.println("Server started!");

            while (true) {
                talkSocket = listenSocket.accept();

                ServerHelper.RunClockLogic(talkSocket, new Clock());
            }
        } catch (IOException e) {
            System.out.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
