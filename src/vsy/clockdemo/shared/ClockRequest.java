package vsy.clockdemo.shared;

public class ClockRequest {
    private int _command;
    private long _parameter;

    public ClockRequest(int cmd) {
        this._command = cmd;
    }

    public ClockRequest(int cmd, long parameter) {
        this._command = cmd;
        this._parameter = parameter;
    }

    public String getRequestMessage() {
        return String.format("%d:%d", _command, _parameter);
    }

    public static ClockRequest parseRequest(String rawRequest) {
        if (rawRequest == null)
            throw new IllegalArgumentException("Request is null");

        rawRequest = rawRequest.trim();

        String parts[] = rawRequest.split(":");
        if (parts.length != 2)
            throw new IllegalArgumentException("Request has invalid format");

        return new ClockRequest(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
    }

    public int get_command() {
        return _command;
    }

    public long get_parameter() {
        return _parameter;
    }

    public void set_command(int _command) {
        this._command = _command;
    }
}
