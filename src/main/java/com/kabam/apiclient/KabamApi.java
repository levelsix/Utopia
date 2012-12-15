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

    /**
     * This endpoint will return a NAID to a 3rd party mobile game developer that has its own
     * account management system. <br>
     * As long as the the user_id is consistent and unique per account this endpoint will always
     * return the same naid for this user_id and game. <br>
     * The naid returned from this endpoint can be used to interact with other kabam systems.<br>
     * Naid uniquely identifies a user. <br>
     * <br>
     * Possible Return Codes: <br>
     *     257 : InternalError. Generic internal error. <br>
     *     4097: MissingOrIncorrectArguments. udid or network parameter missing or incorrect. <br>
     *     4100: WrongApiKey. This network does not exists in our database. <br>
     *
     * @param clientId The app/game id of the client making the request. <br>
     *                  Ask API team to get one! This is specific to environment.
     *                  (sometimes referred as app_id )
     * @param userId Any unique persistent identifier for this (logical) game account. <br>
     *               Email addresses to database id fields, anything is acceptable as long as it
     *               is consistent and unique per account.
     * @param platform The network the call is made from. For Mobile Service, it should be either
     *                "iphone" or "android"
     * @param biParams These params will be used for bi purposes or advertisement tracking. The <br>
     *                 format for this parameter should be JSON. Keys for some values are: <br>
     *                 <b>open_udid</b> : Check here https://github.com/ylechelle/OpenUDID. This is
     *                 the only required parameter. Others are optional.<br>
     *                 <b>mac_hash</b> : MD5 has of the mac address <br>
     *                 <b>device_id</b> : Android device id. (android id) <br>
     *                 <b>advertiser_id</b> : IOS6 advertiser id <br>
     * @param userCreationDate When is this user created on the 3rd party game's side. This is used
     *                         to differentiate between old and new accounts in 3rd party mobile
     *                         games. This is in unix timestamp format. <br>
     *                         If not passed it will default to current unix time.
     * @return Returns the following:<br>
     *         access_token - The oauth token that can be used for other API calls<br>
     *         naid - The naid returned back<br>
     */
    public MobileNaidResponse mobileGetNaid (String userId, int clientId, String platform, 
    		String biParams, Long userCreationDate)  throws Exception {
        String service = "/mobile/naid";
        Map<String, String> pathParams = new HashMap<String, String>();
        pathParams.put("client_id", "" + clientId);
        Map<String, String> bodyParams = new HashMap<String, String>();
        bodyParams.put("user_id", userId);
        bodyParams.put("platform", platform);
        bodyParams.put("bi_params", biParams);
        bodyParams.put("creation_date", userCreationDate.toString());
        
        String response = post(service, secret, host, port, pathParams, null, bodyParams);
        return (MobileNaidResponse) response(response, MobileNaidResponse.class);
    }
}


