package vsy.clockdemo.client;

import vsy.clockdemo.shared.ClockRequest;
import vsy.clockdemo.shared.ClockResponse;
import vsy.clockdemo.shared.IllegalCmdException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static vsy.clockdemo.shared.ClockCommands.*;

public class ClockClient {
    private static boolean _stopClient = false;

    public static void main(String[] args) {
        ClockClient client = new ClockClient();
        client.run();
    }

    public static void display(String msg) {
        System.out.println("  " + msg);
    }

    public void prompt(String msg) {
        System.out.println(msg);
    }

    public ClockRequest getCommand() {
        Pattern commandSyntax = Pattern.compile("s|c|h|r|e|g|w +[1-9][0-9]*");

        String cmdText = null;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            try {
                prompt("command [s|c|h|r|e|g|w]: ");
                cmdText = in.readLine();
                if (!commandSyntax.matcher(cmdText).matches())
                    throw new Exception();
                break; // leave loop here if correct command detected
            } catch (Exception e) {
                display("command syntax error");
            }
        }

        StringTokenizer st = new StringTokenizer(cmdText);
        switch (st.nextToken().charAt(0)) // first token=command
        {
            case 'c':
                return new ClockRequest(CMD_CONTINUE);
            case 'g':
                return new ClockRequest(CMD_GETTIME);
            case 's':
                return new ClockRequest(CMD_START);
            case 'w':
                return new ClockRequest(CMD_WAIT,
                        Long.parseLong(st.nextToken()));
            // second token = parameter
            case 'h':
                return new ClockRequest(CMD_HALT);
            case 'r':
                return new ClockRequest(CMD_RESET);
            default:
                return new ClockRequest(CMD_EXIT);
            // case 'e' and any other char
        }
    }

    public void run() {
        ClockStub clockStub = null;
        try {
            clockStub = new ClockStub("localhost", 2112);
        } catch (IOException e) {
            display("Can not connect to server");
            e.printStackTrace();
        }

        display("accepted commands:");
        display("s[tart] h[old] c[ontinue] r[eset])");
        display("g[et time] e[xit] w[ait] 4711\n");
        ClockRequest command;
        while (true) {
            command = getCommand(); // from console

            try {
                switch (command.get_command()) {
                    case CMD_CONTINUE:
                        clockStub.resume();
                        break;
                    case CMD_GETTIME:
                        clockStub.getTime();
                        break;
                    case CMD_START:
                        clockStub.start();
                        break;
                    case CMD_WAIT:
                        clockStub.waitTime(command.get_parameter());
                        break;
                    case CMD_HALT:
                        clockStub.halt();
                        break;
                    case CMD_RESET:
                        clockStub.reset();
                        break;
                    case CMD_EXIT:
                        clockStub.exit(); // clean up
                        break;
                    default:
                        display("Illegal command");
                }
            } catch (IllegalCmdException | IOException e) {
                display(e.getMessage());
                command.set_command(CMD_NOT_EXECUTED);
            }
        }
    }

    public static void onClientReceive(ClockResponse response) {
        if (!response.isSucceeded()) {
            System.out.println("Received error from server: " + response.getExceptionMessage());
            return;
        }

        System.out.println("Received from server: ");

        switch (response.getCommand()) {
            case CMD_CONTINUE:
                display("clock continued");
                break;
            case CMD_GETTIME:
                display("elapsed time = " + response.getReturnValue() + "ms");
                break;
            case CMD_START:
                display("clock started");
                break;
            case CMD_WAIT:
                display("wait finished");
                break;
            case CMD_HALT:
                display("clock halted, elapsed time = "
                        + response.getReturnValue() + "ms");
                break;
            case CMD_RESET:
                display("clock resetted");
                break;
            case CMD_EXIT:
                display("program stop");
                _stopClient = true;
                break;
            default:
                display("Received unknown command from server!");
        }
    }

    public static void onServerClosed() {
        System.out.println("Server was closed!");
        System.exit(0);
    }
}
