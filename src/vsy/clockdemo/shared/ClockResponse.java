package vsy.clockdemo.shared;

public class ClockResponse {
    private int _command;
    private boolean _succeeded;
    private long _returnValue;
    private String _exceptionMessage;

    public ClockResponse(int command, boolean succeeded, long returnValue, String exceptionMessage) {
        _command = command;
        _succeeded = succeeded;
        _returnValue = returnValue;
        _exceptionMessage = exceptionMessage;
    }

    public int getCommand(){
        return _command;
    }

    public boolean isSucceeded() {
        return _succeeded;
    }

    public long getReturnValue() {
        return _returnValue;
    }

    public String getExceptionMessage() {
        return _exceptionMessage;
    }

    public String getMessage() {
        return String.format("%d:%b:%d:%s", _command, _succeeded, _returnValue, _exceptionMessage);
    }

    public static ClockResponse parse(String message) {
        if (message == null)
            throw new IllegalArgumentException("message can not be null");

        String parts[] = message.split(":");
        if (parts.length != 4)
            throw new IllegalArgumentException("Message has not correct format");

        return new ClockResponse(
                Integer.parseInt(parts[0]),
                Boolean.parseBoolean(parts[1]),
                Long.parseLong(parts[2]),
                parts[3]);
    }
}
