package com.lvl6.info;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

public class TwoLeggedOAuth extends DefaultApi10a {

    @Override
    public String getAccessTokenEndpoint ()       { return ""; };
    @Override
    public String getRequestTokenEndpoint()       { return ""; };
    @Override
    public String getAuthorizationUrl(Token arg0) { return ""; };

}
