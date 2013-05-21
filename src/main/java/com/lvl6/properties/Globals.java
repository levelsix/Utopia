package com.lvl6.properties;


import com.lvl6.spring.AppContext;


public class Globals {
	
	
	protected String appleBundleId;
	protected String appStoreUrl;
	protected String reviewPageUrl;
	protected boolean kabamEnabled = true;
	protected boolean sandbox = true;
	protected boolean iddictionOn = true;
	protected float versionNumber = 1.0f;
	protected int healthCheckTimeoutSeconds = 6;
	
	
	
	public boolean isKabamEnabled() {
		return kabamEnabled;
	}

	public void setKabamEnabled(boolean kabamEnabled) {
		this.kabamEnabled = kabamEnabled;
	}

	public String getAppleBundleId() {
		return appleBundleId;
	}

	public void setAppleBundleId(String appleBundleId) {
		this.appleBundleId = appleBundleId;
	}

	public String getAppStoreUrl() {
		return appStoreUrl;
	}

	public void setAppStoreUrl(String appStoreUrl) {
		this.appStoreUrl = appStoreUrl;
	}

	public String getReviewPageUrl() {
		return reviewPageUrl;
	}

	public void setReviewPageUrl(String reviewPageUrl) {
		this.reviewPageUrl = reviewPageUrl;
	}

    public int getHealthCheckTimeoutSeconds() {
		return healthCheckTimeoutSeconds;
	}

	public void setHealthCheckTimeoutSeconds(int healthCheckTimeoutSeconds) {
		this.healthCheckTimeoutSeconds = healthCheckTimeoutSeconds;
	}

	public float getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(float versionNumber) {
		this.versionNumber = versionNumber;
	}

	public boolean isIddictionOn() {
		return iddictionOn;
	}

	public void setIddictionOn(boolean iddictionOn) {
		this.iddictionOn = iddictionOn;
	}

	public boolean getSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean isSandbox) {
		this.sandbox = isSandbox;
	}

	/** size of ByteBuffer for reading/writing from channels */

    public static final int NET_BUFFER_SIZE=16384*32;

    /** maximum event size in bytes */
    public static final int MAX_EVENT_SIZE=16384*32;

    /** interval to sleep between attempts to write to a channel. */
    public static final long CHANNEL_WRITE_SLEEP = 10L;

    /** number of worker threads for EventWriter */
    public static final int EVENT_WRITER_WORKERS = 5;
    
    /** number of worker threads for APNSWriter */
    public static final int APNS_WRITER_WORKERS = 5;

    /** default number of workers for GameControllers */
    public static final int DEFAULT_CONTROLLER_WORKERS = 2;

    public static final boolean IS_SANDBOX(){return AppContext.getApplicationContext().getBean(Globals.class).getSandbox();};

    public static final int NUM_MINUTES_DIFFERENCE_LEEWAY_FOR_CLIENT_TIME = 10;
    
    public static final String APP_STORE_URL() {return AppContext.getApplicationContext().getBean(Globals.class).getAppStoreUrl();};//"itms-apps://itunes.com/apps/ageofchaos";
    public static final String REVIEW_PAGE_URL() {return AppContext.getApplicationContext().getBean(Globals.class).getReviewPageUrl();};//= "itms-apps://itunes.apple.com/WebObjects/MZStore.woa/wa/viewContentsUserReviews?id=548520325&pageNumber=0&sortOrdering=1&type=Purple+Software";
    public static final String REVIEW_PAGE_CONFIRMATION_MESSAGE = "Awesome! Rate us 5 Stars in the App Store to keep the updates coming!";
    
    //public static final Level LOG_LEVEL = Level.INFO;
    
    public static final int NUM_SECONDS_FOR_CONTROLLER_PROCESS_EVENT_LONGTIME_LOG_WARNING = 1;
    
    public static final String APPLE_BUNDLE_ID(){return AppContext.getApplicationContext().getBean(Globals.class).getAppleBundleId();};// = "com.lvl6.kingdom";

    public static final float VERSION_NUMBER(){return AppContext.getApplicationContext().getBean(Globals.class).getVersionNumber();};

    public static int HEALTH_CHECK_TIMEOUT() {return AppContext.getApplicationContext().getBean(Globals.class).getHealthCheckTimeoutSeconds();};
    
    



    public static final boolean IDDICTION_ON(){return AppContext.getApplicationContext().getBean(Globals.class).isIddictionOn();};;

}