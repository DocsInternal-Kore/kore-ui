package kore.botssdk.net;

import android.view.View;

import androidx.annotation.NonNull;

public class SDKConfig {

    public static void setCustomTemplateView(@NonNull String templateName, @NonNull View templateView)
    {
        SDKConfiguration.setCustomTemplateView(templateName, templateView);
    }
}
