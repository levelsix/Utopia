package com.lvl6.properties;

public class Globals {
    /** size of ByteBuffer for reading/writing from channels */
    public static final int NET_BUFFER_SIZE=16384*4;

    /** maximum event size in bytes */
    public static final int MAX_EVENT_SIZE=16384*4;

    /** interval to sleep between attempts to write to a channel. */
    public static final long CHANNEL_WRITE_SLEEP = 10L;

    /** number of worker threads for EventWriter */
    public static final int EVENT_WRITER_WORKERS = 5;
    
    /** number of worker threads for APNSWriter */
    public static final int APNS_WRITER_WORKERS = 5;

    /** default number of workers for GameControllers */
    public static final int DEFAULT_CONTROLLER_WORKERS = 2;

    public static final boolean IS_SANDBOX = true;

    public static final int NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME = 10;
}