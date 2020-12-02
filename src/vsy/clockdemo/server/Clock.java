package vsy.clockdemo.server;

import vsy.clockdemo.shared.ClockManager;
import vsy.clockdemo.shared.IllegalCmdException;

class Clock extends ClockManager {
    // clock states
    final static int ST_READY = 0;
    final static int ST_RUNNING = 1;
    final static int ST_HALTED = 2;
    final static int ST_EXIT = 3;

    // state variables
    int state;
    long startTime;
    long elapsedTime;

    public Clock() {
        state = ST_READY;
        startTime = 0;
        elapsedTime = 0;
    }

    public void start() throws IllegalCmdException {
        if (state != ST_READY) {
            throw new IllegalCmdException("'start' not allowed when clock is not ready");
        }

        startTime = System.currentTimeMillis();
        state = ST_RUNNING;
    }

    public void reset() throws IllegalCmdException {
        if (state != ST_HALTED && state != ST_RUNNING)
            throw new IllegalCmdException("'reset' not allowed");

        startTime = 0;
        elapsedTime = 0;
        state = ST_READY;
    }

    public long getTime() throws IllegalCmdException {
        if (state != ST_HALTED && state != ST_RUNNING)
            throw new IllegalCmdException("'getTime' not allowed");

        if (state == ST_RUNNING)
            elapsedTime = System.currentTimeMillis() - startTime;

        return elapsedTime;
    }

    public void waitTime(long time) throws IllegalCmdException {
        if (state != ST_RUNNING)
            throw new IllegalCmdException("'wait' not allowed");

        try {
            Thread.sleep(time);
        } catch (Exception ignored) {
        }
    }

    public long halt() throws IllegalCmdException {
        if (state != ST_RUNNING)
            throw new IllegalCmdException("'halt' not allowed");

        elapsedTime = System.currentTimeMillis() - startTime;

        state = ST_HALTED;
        return elapsedTime;
    }

    public void resume() throws IllegalCmdException {
        if (state != ST_HALTED)
            throw new IllegalCmdException("'resume' not allowed");

        startTime = System.currentTimeMillis() - elapsedTime;
        state = ST_RUNNING;
    }

    public void exit() throws IllegalCmdException {
        if (state != ST_READY)
            throw new IllegalCmdException("'exit' not allowed");

        state = ST_EXIT;
    }
}
