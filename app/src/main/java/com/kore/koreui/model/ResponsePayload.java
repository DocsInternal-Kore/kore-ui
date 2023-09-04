package com.kore.koreui.model;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("UnknownNullness")
public class ResponsePayload {

    private boolean download;
    private String fileName;
    private String url;

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public boolean isDownload() {
        return download;
    }
}
