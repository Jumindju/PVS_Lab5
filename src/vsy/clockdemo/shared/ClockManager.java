package vsy.clockdemo.shared;

import vsy.clockdemo.shared.IllegalCmdException;

import java.io.IOException;

public abstract class ClockManager {
    protected final static String _charSet = "UTF-8";


    public abstract void start() throws IllegalCmdException, IOException;

    public abstract void reset() throws IllegalCmdException, IOException;

    public abstract long getTime() throws IllegalCmdException, IOException;

    public abstract void waitTime(long time) throws IllegalCmdException, IOException;

    public abstract long halt() throws IllegalCmdException, IOException;

    public abstract void resume() throws IllegalCmdException, IOException;

    public abstract void exit() throws IllegalCmdException, IOException;
}
