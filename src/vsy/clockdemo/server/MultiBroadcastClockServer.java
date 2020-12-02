package vsy.clockdemo.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MultiBroadcastClockServer {
    private static MulticastSocket socket;
    private static final int PORT = 81;
    private static InetAddress _group;
    private static Clock _sharedClock;

    public static void main(String args[]) {
        try {
            socket = new MulticastSocket(PORT);
            _group = InetAddress.getByName("239.0.0.0");
            socket.joinGroup(_group);

            _sharedClock = new Clock();
        } catch (IOException e) {
            System.out.println("Could not start multiplexing server: " + e.getMessage());
            return;
        }

        serverLoop();

        try {
            socket.leaveGroup(_group);
        } catch (IOException e) {
            System.out.println("Could not leave group: " + e.getMessage());
        }
        socket.close();
    }

    private static void serverLoop() {
        System.out.println("Server started!");

        try {
            while (true) {
                DatagramPacket recvPacket = new DatagramPacket(new byte[500], 500);
                DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, _group, 2112);

                socket.receive(recvPacket);

                String requestMessage = new String(recvPacket.getData(), 0, recvPacket.getLength(), "UTF-8");
                ProcessClockResult result = ServerHelper.ProcessClockLogic(requestMessage, _sharedClock);

                sendPacket.setData(result.getResponseMessage().getBytes("UTF-8"));
                socket.send(sendPacket);

                if (result.shouldConnectionClose()) {
                    socket.leaveGroup(_group);
                    socket.close();

                    System.out.println("Server stopped!");
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println("Error in server loop: " + ex.getMessage());
        }

    }
}
