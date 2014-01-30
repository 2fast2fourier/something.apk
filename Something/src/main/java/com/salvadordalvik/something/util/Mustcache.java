package com.salvadordalvik.something.util;

import android.content.Context;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by matthewshepard on 1/29/14.
 */
public class MustCache {
    public static String threadHeader;
    private static Template postTemplate, footerTemplate;


    public static void init(final Context context){
        new Thread(){
            @Override
            public void run() {
                generatePostTemplates(context);
            }
        }.start();
    }

    public synchronized static void applyPostTemplate(StringBuilder builder, Map<String, String> post){
        builder.append(postTemplate.execute(post));
    }

    public synchronized static void applyFooterTemplate(StringBuilder builder, Map<String, String> args){
        builder.append(footerTemplate.execute(args));
    }

    private synchronized static void generatePostTemplates(Context context){
        try {
            InputStreamReader postMustReader = new InputStreamReader(context.getResources().getAssets().open("mustache/post.mustache"), Charsets.UTF_8);
            postTemplate = Mustache.compiler().compile(postMustReader);

            InputStream headerInput = context.getResources().getAssets().open("mustache/header.html");
            threadHeader = CharStreams.toString(new InputStreamReader(headerInput, Charsets.UTF_8));

            InputStreamReader footerInput = new InputStreamReader(context.getResources().getAssets().open("mustache/footer.mustache"), Charsets.UTF_8);
            footerTemplate = Mustache.compiler().compile(footerInput);

            Closeables.close(headerInput, true);
            Closeables.close(footerInput, true);
            Closeables.close(postMustReader, true);
        } catch (IOException e) {
            e.printStackTrace();
            //should never happen, log via bugsense just in case
            //TODO add bugsense tracking
        }
    }
}
