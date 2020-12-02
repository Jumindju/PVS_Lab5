package vsy.clockdemo.server;

public class ProcessClockResult {
    private String _responseMessage;
    private boolean shouldConnectionClose;

    public ProcessClockResult(String responseMessage, boolean closeConnection) {
        _responseMessage = responseMessage;
        shouldConnectionClose = closeConnection;
    }

    public boolean shouldConnectionClose() {
        return shouldConnectionClose;
    }

    public String getResponseMessage() {
        return _responseMessage;
    }
}
