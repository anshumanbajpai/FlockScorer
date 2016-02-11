package co.flock;

import com.squareup.okhttp.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlockMessagePoster {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String FLOCK_INCOMING_WEBHOOK_URL = "https://api.flock.co/hooks/sendMessage/e46b89ae-bddc-4763-8877-5df3ef74d1ea";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void Post(String text) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text",text);
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());
        Request request = new Request.Builder()
                .url(FLOCK_INCOMING_WEBHOOK_URL)
                .post(body)
                .build();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    client.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
