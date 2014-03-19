package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.request.FastRequest;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.SomePreferences;

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
        //Check login cookie status, SA will automatically set them to "deleted" if the session expired.
        if(!SomePreferences.confirmLogin()){
            SomePreferences.clearAuthentication();
            throw new SessionError("Not Logged In");
        }
        Document document = parseDocument(response);
        if(document.getElementsByClass("notregistered").size() > 0){
            //Not logged in.
            SomePreferences.clearAuthentication();
            throw new SessionError("Not Logged In");
        }
        Element stdErr = document.getElementsByClass("standarderror").first();
        if(stdErr != null){
            //Generic SA error messages
            throw new SomeError(stdErr.getElementsByClass("standard").first().getElementsByClass("inner").first().ownText());
        }
        return parseHtmlResponse(response, document);
    }

    public abstract T parseHtmlResponse(NetworkResponse response, Document document) throws Exception;

    private static Document parseDocument(NetworkResponse response) throws IOException {
        return Jsoup.parse(new ByteArrayInputStream(response.data), "CP1252", Constants.BASE_URL);
    }

    @Override
    protected String getBodyCharset() {
        return "CP1252";
    }
}
