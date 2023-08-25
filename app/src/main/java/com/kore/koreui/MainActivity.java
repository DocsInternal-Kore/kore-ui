package com.kore.koreui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import kore.botssdk.activity.BotChatActivity;
//import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.net.SdkConfig;

public class MainActivity extends AppCompatActivity {

    private Button btnBotConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnBotConnect = findViewById(R.id.btnBotConnect);

//        SdkConfig.setCustomTemplateView("link", new LinkTemplateView(MainActivity.this));
//        SdkConfig.setCustomTemplateView("button", new BotButtonView(MainActivity.this));

        SdkConfig.intialize("st-b9889c46-218c-58f7-838f-73ae9203488c", "Sudheer Bot", "cs-1e845b00-81ad-5757-a1e7-d0f6fea227e9", "5OcBSQtH/k6Q/S6A3bseYfOee02YjjLLTNoT1qZDBso=", "anilkumar.routhu@kore.com");

        btnBotConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, BotChatActivity.class);
                startActivity(intent);
            }
        });

    }
}