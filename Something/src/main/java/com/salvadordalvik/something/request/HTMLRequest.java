package com.salvadordalvik.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.request.FastRequest;
import com.salvadordalvik.something.util.Constants;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by matthewshepard on 1/17/14.
 */
public abstract class HTMLRequest<T> extends FastRequest<T> {
    public HTMLRequest(String baseUrl, int method, Response.Listener<T> success, Response.ErrorListener error) {
        super(baseUrl, method, success, error);
    }

    @Override
    public T parseResponse(NetworkResponse response) throws Exception {
        return parseHtmlResponse(response, parseDocument(response));
    }

    public abstract T parseHtmlResponse(NetworkResponse response, Document document) throws Exception;

    private static Document parseDocument(NetworkResponse response) throws IOException {
        return Jsoup.parse(new ByteArrayInputStream(response.data), "CP1252", Constants.BASE_URL);
    }

    protected static String getFirstTextByClass(Element parent, String htmlClass, String fallback){
        Elements targets = parent.getElementsByClass(htmlClass);
        if(targets.size() > 0){
            return targets.first().text().trim();
        }
        return fallback;
    }

    protected static int stripParseInt(String str){
        return Integer.parseInt(str.replaceAll("\\D", ""));
    }
}
