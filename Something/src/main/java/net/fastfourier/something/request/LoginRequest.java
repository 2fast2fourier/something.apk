package net.fastfourier.something.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import net.fastfourier.something.util.OkHttpStack;

import org.jsoup.nodes.Document;

import java.net.CookieManager;
import java.net.CookiePolicy;

/**
 * Created by matthewshepard on 1/31/14.
 */
public class LoginRequest extends HTMLRequest<Boolean> {
    public LoginRequest(String username, String password, Response.Listener<Boolean> success, Response.ErrorListener error) {
        super("https://forums.somethingawful.com/account.php", Request.Method.POST, success, error);
        addParam("action", "login");
        addParam("username", username);
        addParam("password", password);

        CookieManager.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
    }

    @Override
    public Boolean parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        if(OkHttpStack.saveCookies()){
            return true;
        }
        throw new VolleyError("Failed to Login");
    }
}
