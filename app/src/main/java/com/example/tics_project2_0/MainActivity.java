package com.example.tics_project2_0;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText inputText;
    MaterialButton generateBtn;
    ProgressBar progressBar;
    ImageView imageView;
    TextView storyText;
    Button copyBtn;


    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.input_text);
        generateBtn = findViewById(R.id.generate_btn);
        progressBar = findViewById(R.id.progress_bar);
        imageView = findViewById(R.id.image_view);
        copyBtn = findViewById(R.id.copy_btn);
        storyText = findViewById(R.id.story);

        generateBtn.setOnClickListener((v)->{
            String text = inputText.getText().toString().trim();
            if(text.isEmpty()){
                inputText.setError("Text can't be empty");
                return;
            }
            callImageAPI(text);
            callStoryAPI(text);
        });

        copyBtn.setOnClickListener(v -> {
            copyText();
        });
    }

    public void callStoryAPI(String text) {
        setInProgress(true);
        String combinedText = "Create a short story on" + " " + text + " " +" of three to five paragraph of maximum 230 characters";
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("model", "gpt-3.5-turbo-instruct");
            jsonBody.put("prompt", combinedText);
            jsonBody.put("temperature", 0.8);
            jsonBody.put("max_tokens", 280);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody requestBody = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/completions")
                .header("Authorization", "Bearer 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")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Failed to generate image", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String completion = jsonObject.getJSONArray("choices").getJSONObject(0).getString("text");
                    loadStory(completion);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void callImageAPI(String text){
        setInProgress(true);
        String combinedText = "theme image of story:" + " " + text;
        JSONObject jsonBody = new JSONObject();
        try{
            jsonBody.put("prompt",combinedText);
            jsonBody.put("size","256x256");
        }catch (Exception e){
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization","Bearer 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")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Failed to generate image", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try{
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String imageUrl = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    loadImage(imageUrl);
                    setInProgress(false);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

    void setInProgress(boolean inProgress){
        runOnUiThread(()->{
            if(inProgress){
                progressBar.setVisibility(View.VISIBLE);
                generateBtn.setVisibility(View.GONE);
            }else{
                progressBar.setVisibility(View.GONE);
                generateBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    void loadStory(String story){
        final String trimmedStory = story.trim();
        runOnUiThread(() -> {

            storyText.setText(trimmedStory);
            copyBtn.setVisibility(trimmedStory.isEmpty() ? View.GONE : View.VISIBLE);
        });
    }

    void loadImage(String url){
        runOnUiThread(()->{
            Picasso.get().load(url).into(imageView);
        });
    }

    public void copyText() {
        String storyContent = storyText.getText().toString();
        if (!storyContent.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Story", storyContent);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Story copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Nothing to copy", Toast.LENGTH_SHORT).show();
        }
    }
}

