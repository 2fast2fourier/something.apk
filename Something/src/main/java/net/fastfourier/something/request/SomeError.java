package net.fastfourier.something.request;

import com.android.volley.VolleyError;

/**
 * Created by matthewshepard on 3/8/14.
 */
public class SomeError extends VolleyError {
    public SomeError(String exceptionMessage) {
        super(exceptionMessage);
    }
}
