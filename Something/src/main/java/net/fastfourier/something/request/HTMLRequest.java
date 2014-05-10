package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.request.FastRequest;
import net.fastfourier.something.util.Constants;
import net.fastfourier.something.util.OkHttpStack;
import net.fastfourier.something.util.SomePreferences;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 1/17/14.
 */
public abstract class HTMLRequest<T> extends FastRequest<T> {
    public HTMLRequest(String baseUrl, int method, Response.Listener<T> success, Response.ErrorListener error) {
        super(baseUrl, method, success, error);
    }

    @Override
    public T parseResponse(Request<T> request, NetworkResponse response) throws Exception {
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
        return parseHtmlResponse(request, response, document);
    }

    public abstract T parseHtmlResponse(Request<T> request, NetworkResponse response, Document document) throws Exception;

    private static Document parseDocument(NetworkResponse response) throws IOException {
        return Jsoup.parse(new ByteArrayInputStream(response.data), "CP1252", Constants.BASE_URL);
    }

    @Override
    protected String getBodyCharset() {
        return "CP1252";
    }


    private static final Pattern unencodeCharactersPattern = Pattern.compile("&#(\\d+);");
    private static final Pattern encodeCharactersPattern = Pattern.compile("([^\\x00-\\xFF])");
    /**
     * HTML encoding routine pulled from Awful. Used for posting replies/PMs/ect.
     * Encodes non-CP1252 characters by manually wrapping them within HTML entities.
     * Probably fails for some character entities.
     * TODO replace this for the love of god
     * @param str regular string
     * @return encoded string
     */
    public static String encodeHtml(String str) {
        StringBuffer unencodedContent = new StringBuffer(str.length());
        Matcher fixCharMatch = encodeCharactersPattern.matcher(str);
        while (fixCharMatch.find()) {
            fixCharMatch.appendReplacement(unencodedContent, "&#" + fixCharMatch.group(1).codePointAt(0) + ";");
        }
        fixCharMatch.appendTail(unencodedContent);
        return unencodedContent.toString();
    }

    /**
     * HTML decoding routine pulled from Awful. Used for quotes/PM replies/ect.
     * Probably fails for some character entities.
     * TODO replace this for the love of god
     * @param html html-encoded string
     * @return regular string
     */
    public static String unencodeHtml(String html) {
        if (html == null) {
            return "";
        }
        String processed = StringEscapeUtils.unescapeHtml4(html);
        StringBuffer unencodedContent = new StringBuffer(processed.length());
        Matcher fixCharMatch = unencodeCharactersPattern.matcher(processed);
        while (fixCharMatch.find()) {
            fixCharMatch.appendReplacement(unencodedContent, Character.toString((char) Integer.parseInt(fixCharMatch.group(1))));
        }
        fixCharMatch.appendTail(unencodedContent);
        return unencodedContent.toString();
    }

    @Override
    protected Request<T> generateRequest(String url, Response.Listener<T> success, Response.ErrorListener error) {
        return new HTMLInternalRequest(url, success, error);
    }

    protected class HTMLInternalRequest extends FastInternalRequest implements OkHttpStack.Redirectable{
        private String redirectUrl;

        public HTMLInternalRequest(String url, Response.Listener<T> success, Response.ErrorListener error) {
            super(url, success, error);
        }

        @Override
        public void onRedirect(String redirectUrl) {
            this.redirectUrl = redirectUrl;
        }

        @Override
        public boolean hasRedirect() {
            return redirectUrl != null;
        }

        @Override
        public String getRedirectUrl() {
            return redirectUrl;
        }
    }
}
