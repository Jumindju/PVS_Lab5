package vsy.clockdemo.server;

import vsy.clockdemo.shared.ClockRequest;
import vsy.clockdemo.shared.ClockResponse;
import vsy.clockdemo.shared.IllegalCmdException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import static vsy.clockdemo.shared.ClockCommands.*;
import static vsy.clockdemo.shared.ClockCommands.CMD_EXIT;

public class ServerHelper {
    private static final String _charSet = "UTF-8";

    public static void RunClockLogic(Socket talkSocket, Clock clock) throws IOException {
        BufferedReader fromClient;
        OutputStreamWriter toClient;

        // incoming messages
        fromClient = new BufferedReader(new InputStreamReader(talkSocket.getInputStream(), _charSet));
        // outgoing messages
        toClient = new OutputStreamWriter(talkSocket.getOutputStream(), _charSet);

        requestLoop:
        while (true) {
            String rawRequest = fromClient.readLine();

            ProcessClockResult processClockResult = ProcessClockLogic(rawRequest, clock);
            String responseMessage = processClockResult.getResponseMessage();

            toClient.write(responseMessage);
            toClient.flush();

            if (processClockResult.shouldConnectionClose()) {
                toClient.close();
                fromClient.close();
                talkSocket.close();

                System.out.println("Close clock connection!");
                break requestLoop;
            }
        }
    }

    public static ProcessClockResult ProcessClockLogic(String command, Clock clock) {
        ClockRequest request = ClockRequest.parseRequest(command);

        boolean succeeded = true;
        long returnValue = 0;
        String exceptionMessage = null;
        try {
            switch (request.get_command()) {
                case CMD_START:
                    clock.start();
                    break;
                case CMD_RESET:
                    clock.reset();
                    break;
                case CMD_GETTIME:
                    returnValue = clock.getTime();
                    break;
                case CMD_WAIT:
                    clock.waitTime(request.get_parameter());
                    break;
                case CMD_HALT:
                    returnValue = clock.halt();
                    break;
                case CMD_CONTINUE:
                    clock.resume();
                    break;
                case CMD_EXIT:
                    clock.exit();
                    break;
            }
        } catch (IllegalCmdException ex) {
            succeeded = false;
            exceptionMessage = ex.getMessage();
        }

        ClockResponse response = new ClockResponse(request.get_command(), succeeded, returnValue, exceptionMessage);
        String responseMessage = response.getMessage() + '\n';

        return new ProcessClockResult(responseMessage, request.get_command() == CMD_EXIT && succeeded);
    }
}
