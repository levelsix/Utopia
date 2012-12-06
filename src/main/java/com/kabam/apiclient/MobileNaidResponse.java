package com.kabam.apiclient;

import org.codehaus.jackson.annotate.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MobileNaidResponse extends Response {

    @JsonProperty("naid")
    private Integer naid;

    @JsonProperty("kabam_id")
    private Integer kabamId;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("code")
    private String code;

    @JsonProperty("redirectUrl")
    private String redirectUrl;

    @JsonProperty("expiry")
    private Long expiryTime;

    public Integer getNaid() {
        return naid;
    }

    public void setNaid(Integer naid) {
        this.naid = naid;
    }

    public Integer getKabamId() {
        return kabamId;
    }

    public void setKabamId(Integer kabamId) {
        this.kabamId = kabamId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public Long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(Long expiryTime) {
        this.expiryTime = expiryTime;
    }
}
