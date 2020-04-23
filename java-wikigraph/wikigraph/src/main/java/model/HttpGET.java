package model;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HttpGET {
    private Optional<String> base = Optional.empty();
    private final Map<String, String> par;
    private boolean invalid = false;
    private int timeout = 5000;

    public HttpGET() {
        this.par = new HashMap<>();
    }

    private HttpGET(final Map<String, String> map) {
        this.par = map;
    }

    private void checkValidity() {
        if (invalid) throw new IllegalStateException("Http request already done.");
    }

    public HttpGET setBaseURL(final String base) {
        checkValidity();
        this.base = Optional.of(base);
        return this;
    }

    public HttpGET addParameter(final String key, final String value) {
        checkValidity();
        this.par.put(key, value);
        return this;
    }

    public HttpGET setTimeout(final int millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        this.timeout = millis;
        return this;
    }

    public String send() {
        checkValidity();
        this.invalid = true;
        final HttpURLConnection con;
        if (this.base.isPresent()) {
            try {
                final URL url = new URL(this.base.get());
                con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setDoOutput(true);
                final DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(getParamsString(this.par));
                out.flush();
                out.close();
                //con.setRequestProperty("Content-Type", "application/json");
                con.setConnectTimeout(this.timeout);
                con.setReadTimeout(this.timeout);
                con.setInstanceFollowRedirects(true);
                final int status = con.getResponseCode();
                final BufferedReader in;
                if (status > 299) {
                    in = new BufferedReader(
                            new InputStreamReader(con.getErrorStream()));
                } else {
                    in = new BufferedReader(
                            new InputStreamReader(con.getInputStream()));
                }
                String inputLine;
                final StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                con.disconnect();
                return content.toString();
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Malformed URL: " + this.base.get(), e);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException("Base not provided");
        }
    }

    public static String getParamsString(final Map<String, String> params) {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public HttpGET copy() {
        HttpGET h = new HttpGET(new HashMap<>(this.par));
        this.base.ifPresent(h::setBaseURL);
        return h.setTimeout(this.timeout);
    }
}
