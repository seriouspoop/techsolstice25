package com.example.financetracker;

import android.content.Context;

import com.google.mediapipe.tasks.genai.llminference.*;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Gemma{

    public static LlmInference activeLlmInferenceInstance;

    public void initLlm(android.content.Context context) throws IOException, ExceptionInInitializerError{

        if(activeLlmInferenceInstance != null){
            throw new ExceptionInInitializerError("ALREADY_INITIALIZED");
        }

//        String modelPath;
//        modelPath = getModelPath(context,"");

        LlmInference.LlmInferenceOptions initOptions = LlmInference.LlmInferenceOptions.builder()
                .setModelPath("/data/local/tmp/llm/gemma.task")
                //.setModelPath("gemma.task")
                .setMaxTopK(64)
                .build();

        LlmInference llmInference = LlmInference.createFromOptions(context, initOptions);
        activeLlmInferenceInstance = llmInference;
        return;
    }
    public String inferFromString(String inputText) throws IllegalStateException{
        if(activeLlmInferenceInstance == null){
            throw new IllegalStateException("NOT_INITIALIZED_USE_initLlm()");
        }

        String response = activeLlmInferenceInstance.generateResponse(inputText);
        return response;
    }

    private static String getModelPath(Context context, String assetFileName) throws IOException {
        File file = new File(context.getCacheDir(), assetFileName);
        if (!file.exists()) {
            InputStream inputStream = context.getAssets().open(assetFileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            inputStream.close();
            outputStream.close();
        }
        return file.getAbsolutePath();
    }


}
