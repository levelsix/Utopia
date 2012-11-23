package com.kabam.apiclient;

import java.util.HashMap;
import java.util.Map;

public class KabamApi extends KabamApiBase {

    private String host;
    private int port;
    private String secret;

    public KabamApi(String host, int port, String secret) {
        this.host = host;
        this.port = port;
        this.secret = secret;
    }

    public RegisterGuestResponse mobileRegisterGuest(
            String deviceId, int clientId, String network) {
        String service = "/mobile/registerguest";
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("client_id", "" + clientId);
        Map<String, String> bodyParams = new HashMap<String, String>();
        bodyParams.put("udid", deviceId);
        bodyParams.put("type", network);

        try {
            String response = post(service, secret, host, port, pathParams, null, bodyParams);
            RegisterGuestResponse registerGuestResponse =
                (RegisterGuestResponse) response(response, RegisterGuestResponse.class);
            return registerGuestResponse;
        } catch (Exception e) {
            return new RegisterGuestResponse();
        }
    }
}


