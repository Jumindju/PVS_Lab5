package vsy.clockdemo.client;

import vsy.clockdemo.shared.ClockRequest;
import vsy.clockdemo.shared.IllegalCmdException;
import vsy.clockdemo.shared.ClockManager;
import vsy.clockdemo.shared.ClockResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.nio.charset.Charset;

import static vsy.clockdemo.shared.ClockCommands.*;

public class ClockStub {
    private MulticastSocket socket;

    private String _host;
    private int _port;
    private InetAddress _group;

    public ClockStub(String host, int port) throws IOException {
        _host = host;
        _port = port;
        _group = InetAddress.getByName("239.0.0.0");

        socket = new MulticastSocket(port);

        new Thread(() -> recvMsgAsync(socket)).start();

        socket.joinGroup(_group);
        //socket.setLoopbackMode(true);
    }

    public void start() throws IOException, IllegalCmdException {
        sendMessage(CMD_START);
    }

    public void reset() throws IOException, IllegalCmdException {
        sendMessage(CMD_RESET);
    }

    public void getTime() throws IOException, IllegalCmdException {
        sendMessage(CMD_GETTIME);
    }

    public void waitTime(long time) throws IOException, IllegalCmdException {
        sendMessage(CMD_WAIT, time);
    }

    public void halt() throws IOException, IllegalCmdException {
        sendMessage(CMD_HALT);
    }

    public void resume() throws IOException, IllegalCmdException {
        sendMessage(CMD_CONTINUE);
    }

    public void exit() throws IOException, IllegalCmdException {
        sendMessage(CMD_EXIT);
    }

    private void sendMessage(int command) throws IOException, IllegalCmdException {
        sendMessage(command, 0);
    }

    private void sendMessage(int command, long parameter) throws IOException, IllegalCmdException {
        DatagramPacket sendPacket = new DatagramPacket(new byte[0], 0, InetAddress.getByName(_host), 81);

        // Send message to server
        ClockRequest request = new ClockRequest(command, parameter);
        String requestMessage = request.getRequestMessage() + '\n';

        sendPacket.setData(requestMessage.getBytes("UTF-8"));
        socket.send(sendPacket);
    }

    private void recvMsgAsync(MulticastSocket socket) {
        DatagramPacket recvPacket = new DatagramPacket(new byte[500], 500);

        try {
            while (true) {
                // Receive response
                socket.receive(recvPacket);
                String rawResponse = new String(recvPacket.getData(), 0, recvPacket.getLength(), "UTF-8");
                ClockResponse response = ClockResponse.parse(rawResponse);

                if (response.getCommand() == CMD_EXIT && response.isSucceeded()) {
                    socket.close();
                    socket.leaveGroup(_group);
                }

                ClockClient.onClientReceive(response);
            }
        } catch (IOException e) {
            ClockClient.onServerClosed();
        }
    }
}
