package kore.botssdk.net;

import android.annotation.SuppressLint;
import android.view.View;

import androidx.annotation.NonNull;
@SuppressLint("UnknownNullness")
public class SDKConfig {

    public static void setCustomTemplateView(@NonNull String templateName, @NonNull View templateView)
    {
        SDKConfiguration.setCustomTemplateView(templateName, templateView);
    }

    public static void initialize(String botId, String botName, String clientId, String clientName, String identity)
    {
        SDKConfiguration.Client.setBot_id(botId);
        SDKConfiguration.Client.setBot_id(botName);
        SDKConfiguration.Client.setBot_id(clientId);
        SDKConfiguration.Client.setBot_id(clientName);
        SDKConfiguration.Client.setBot_id(identity);
    }

    public static void setServerUrl(String url)
    {
        SDKConfiguration.Server.setServerUrl(url);
        SDKConfiguration.Server.setKoreBotServerUrl(url);
    }

    public static void setBrandingUrl(String url)
    {
        SDKConfiguration.Server.setBrandingUrl(url);
    }

    public static void setTokenUrl(String url)
    {
        SDKConfiguration.Server.setTokenUrl(url);
    }

    public static void setJWTUrl(String url)
    {
        SDKConfiguration.JWTServer.setJwtServerUrl(url);
    }

}
