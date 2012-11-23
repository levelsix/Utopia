package com.kabam.apiclient;

import org.codehaus.jackson.annotate.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Response {

    @JsonProperty("returnCode")
    protected int returnCode = 0;

    @JsonProperty("error")
    protected String error;

    public int getReturnCode() {
        return this.returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
