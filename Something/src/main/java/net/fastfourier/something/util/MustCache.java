package net.fastfourier.something.util;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;
import com.google.common.base.Charsets;
import com.google.common.io.Closeables;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by matthewshepard on 1/29/14.
 */
public class MustCache {
    private static Template postTemplate, footerTemplate, headerTemplate, pmTemplate;


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

    public synchronized static void applyHeaderTemplate(StringBuilder builder, Map<String, String> args){
        builder.append(headerTemplate.execute(args));
    }

    public synchronized static void applyPMTemplate(StringBuilder builder, Map<String, String> args){
        builder.append(pmTemplate.execute(args));
    }

    private synchronized static void generatePostTemplates(Context context){
        try {
            InputStreamReader postMustReader = new InputStreamReader(context.getResources().getAssets().open("mustache/post.mustache"), Charsets.UTF_8);
            postTemplate = Mustache.compiler().compile(postMustReader);

            InputStreamReader pmMustReader = new InputStreamReader(context.getResources().getAssets().open("mustache/pm.mustache"), Charsets.UTF_8);
            pmTemplate = Mustache.compiler().compile(pmMustReader);

            InputStreamReader headerInput = new InputStreamReader(context.getResources().getAssets().open("mustache/header.mustache"), Charsets.UTF_8);
            headerTemplate = Mustache.compiler().compile(headerInput);

            InputStreamReader footerInput = new InputStreamReader(context.getResources().getAssets().open("mustache/footer.mustache"), Charsets.UTF_8);
            footerTemplate = Mustache.compiler().compile(footerInput);

            Closeables.close(pmMustReader, true);
            Closeables.close(headerInput, true);
            Closeables.close(footerInput, true);
            Closeables.close(postMustReader, true);
        } catch (IOException e) {
            e.printStackTrace();
            //should never happen, log via bugsense just in case
            BugSenseHandler.sendException(e);
        }
    }
}
