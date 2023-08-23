package kore.botssdk.net;

import android.view.View;

public class SdkConfig
{
    public static void setCustomTemplateView(String templateName, View templateView)
    {
        SDKConfiguration.setCustomTemplateView(templateName, templateView);
    }

    public static void intialize(String botId, String botName, String clientId, String clientSecret, String identity)
    {
        SDKConfiguration.Client.bot_id = botId;
        SDKConfiguration.Client.bot_name = botName;
        SDKConfiguration.Client.client_id = clientId;
        SDKConfiguration.Client.client_secret = clientSecret;
        SDKConfiguration.Client.identity = identity;
    }
}
