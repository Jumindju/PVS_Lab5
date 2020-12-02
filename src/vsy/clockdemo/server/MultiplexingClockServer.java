package vsy.clockdemo.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;

public class MultiplexingClockServer {
    Selector _events = null;
    ServerSocketChannel _listenChannel = null;

    private boolean _useSharedClock;

    private Clock _sharedClock;
    private HashMap<String, Clock> _clockHashMap;

    public MultiplexingClockServer(int port, boolean useSharedClock) throws IOException {
        _events = Selector.open();

        _listenChannel = ServerSocketChannel.open();
        _listenChannel.configureBlocking(false);
        _listenChannel.socket().bind(new InetSocketAddress(port));
        _listenChannel.register(_events, SelectionKey.OP_ACCEPT);

        _useSharedClock = useSharedClock;
        if (useSharedClock)
            _sharedClock = new Clock();

        _clockHashMap = new HashMap<>();
    }

    public static void main(String args[]) {
        try {
            new MultiplexingClockServer(2112, false).serverLoop();
        } catch (Exception ex) {
            System.out.println("Could not start server! " + ex.getMessage());
            ex.printStackTrace();
        }

        System.out.println("Server finished");
    }

    public void serverLoop() throws IOException {
        Iterator<SelectionKey> selKeys;

        System.out.println("Start multiplexing server!");

        serverLoop:
        while (true) {
            _events.select();

            selKeys = _events.selectedKeys().iterator();

            while (selKeys.hasNext()) {
                SelectionKey selKey = selKeys.next();
                selKeys.remove();

                if (selKey.isReadable()) {
                    if (processRead(selKey)) {
                        break serverLoop;
                    }
                } else if (selKey.isAcceptable()) {
                    processAccept(selKey);
                } else {
                    System.out.println("SelKey unknown: " + selKey.toString());
                }
            }
        }
    }

    private boolean processRead(SelectionKey selKey) {
        SocketChannel talkChannel = null;

        try {
            talkChannel = (SocketChannel) selKey.channel();

            String ipAddress = talkChannel.getRemoteAddress().toString();
            Clock clock;
            if(_useSharedClock){
                clock= _sharedClock;
            } else {
                if (_clockHashMap.containsKey(ipAddress)) {
                    clock = _clockHashMap.get(ipAddress);
                } else {
                    clock = new Clock();
                    _clockHashMap.put(ipAddress, clock);
                    System.out.println("Add new clock: " + ipAddress);
                }
            }

            String message = recvTextMessage(talkChannel);
            ProcessClockResult processClockResult = ServerHelper.ProcessClockLogic(message, clock);

            sendTextMessage(talkChannel, processClockResult.getResponseMessage());

            return processClockResult.shouldConnectionClose();
        } catch (IOException ex) {
            System.out.println("Error while processing read: " + ex.getMessage());

            if (talkChannel != null) {
                try {
                    talkChannel.close();
                } catch (IOException e) {
                    System.out.println("Could not force close talkChannel " + ex.getMessage());
                }
            }

            return false;
        }
    }

    private void processAccept(SelectionKey selKey) {
        SocketChannel talkChannel = null;

        try {
            talkChannel = _listenChannel.accept();

            talkChannel.configureBlocking(false);
            talkChannel.register(_events, SelectionKey.OP_READ);
        } catch (IOException ex) {
            System.out.println("Could not accept: " + ex.getMessage());

            try {
                talkChannel.close();
            } catch (IOException e) {
                System.out.println("Could not force close talkChannel on accept: " + e.getMessage());
            }
        }
    }

    private void sendTextMessage(SocketChannel sChannel, String msg) throws IOException {
        if (msg.charAt(msg.length() - 1) != '\n')
            msg += '\n';

        sChannel.write(ByteBuffer.wrap(msg.getBytes("UTF-8")));
    }

    private String recvTextMessage(SocketChannel socketChannel) throws IOException {
        ByteBuffer recvBuffer = ByteBuffer.allocate(1024);
        int numBytesRead = socketChannel.read(recvBuffer);

        switch (numBytesRead) {
            case -1:
                throw new IOException("Connection unexpectly closed");
            case 0:
                return "";
            default:
                if (recvBuffer.get(numBytesRead - 1) != 10)
                    throw new IOException("Message frame error");

                return new String(recvBuffer.array(), "UTF-8").trim();
        }
    }
}
