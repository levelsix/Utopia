package com.kabam.apiclient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

class KabamApiBase {

    protected String getBaseString(Map<String, String> sigData, Map<String, String> pathParams,
            Map<String, String> queryParams, Map<String, String> bodyParams) {

        TreeMap<String, String> sortedParams = new TreeMap<String, String>(sigData);

        // Path params
        if (pathParams != null) {
            for (Map.Entry<String, String> entry : pathParams.entrySet()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        // Query params
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        // Body params
        if (bodyParams != null) {
            for (Map.Entry<String, String> entry : bodyParams.entrySet()) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        return serializeKeyPairs(sortedParams, '=', '&');
    }

    protected Map<String, String> generateSignature(String service, String secret,
            Map<String, String> pathParams, Map<String, String> queryParams,
            Map<String, String> bodyParams) throws KabamException {

        if (secret.length() != 32) {
            throw new KabamException("secret key must be of length 32");
        }

        Map<String, String> sigData = new HashMap<String, String>();
        sigData.put("nonce", UUID.randomUUID().toString().replace("-", ""));
        sigData.put("ts", "" + (System.currentTimeMillis() / 1000L));
        sigData.put("version", "1");

        String baseString = getBaseString(sigData, pathParams, queryParams, bodyParams);
        String signature = sign(baseString, secret);
        sigData.put("sig", signature);

        return sigData;
    }

    protected String generateUrl(String service, String host, int port,
            Map<String, String> pathParams, Map<String, String> queryParams) {

        String url = host + ":" + port + service;
        if (pathParams != null) {
            url += "/" + serializeKeyPairs(pathParams, '/', '/');
        }

        if (queryParams != null) {
            url += "?" + serializeKeyPairs(queryParams, '=', '&');
        }

        return url;
    }

    public String post(String service, String secret, String host, int port,
            Map<String, String> pathParams, Map<String, String> queryParams,
            Map<String, String> bodyParams) throws KabamException {

        Map<String, String> sigData = generateSignature(service, secret,
                pathParams, queryParams, bodyParams);

        Map<String, String> queryParamsWithSig = new HashMap<String, String>(sigData);
        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                queryParamsWithSig.put(entry.getKey(), entry.getValue());
            }
        }

        String url = generateUrl(service, host, port, pathParams, queryParamsWithSig);
        String body = serializeKeyPairs(bodyParams, '=', '&');

        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");

        try {
            StringEntity params = new StringEntity(body);
            httpPost.setEntity(params);

            HttpClient httpClient = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            String response = httpClient.execute(httpPost, responseHandler);
            return response;
        } catch (Exception e) {
            httpPost.abort();
            throw new KabamException(e.getMessage());
        }
    }

    protected Response response(String json, Class<? extends Response> classz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return (Response) mapper.readValue(json, classz);
        } catch (IOException e) {
            Response response = new Response();
            response.setError("Failed to reach API servers");
            return response;
        }
    }

    protected String serializeKeyPairs(Map<String, String> params,
            char keyValueSep, char groupSep) {

        StringBuffer buffer = new StringBuffer();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                buffer.append(groupSep);
            }

            buffer.append(entry.getKey());
            buffer.append(keyValueSep);
            try {
                buffer.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // We silenty discard the value
            }
        }

        return buffer.toString().trim();
    }

    public String sign(String baseString, String secret) {
        Mac mac = null;
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");

            // get an hmac_sha1 Mac instance and initialize with the signing key
            mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(baseString.getBytes());
            return Base16Encoder.encode(rawHmac);
        } catch (Exception e) {
            return "";
        } finally {
            if (mac != null) {
                mac.reset();
            }
        }
    }
}
