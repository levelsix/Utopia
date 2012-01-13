package com.lvl6.properties;

public class Globals {
    /** size of ByteBuffer for reading/writing from channels */
    public static final int NET_BUFFER_SIZE=8192;

    /** maximum event size in bytes */
    public static final int MAX_EVENT_SIZE=4096;

    /** interval to sleep between attempts to write to a channel. */
    public static final long CHANNEL_WRITE_SLEEP = 10L;

    /** number of worker threads for EventWriter */
    public static final int EVENT_WRITER_WORKERS = 5;

    /** default number of workers for GameControllers */
    public static final int DEFAULT_CONTROLLER_WORKERS = 5;
}