package kore.botssdk.activity;

import static android.view.View.VISIBLE;
import static kore.botssdk.activity.KaCaptureImageActivity.rotateIfNecessary;
import static kore.botssdk.fcm.FCMWrapper.GROUP_KEY_NOTIFICATIONS;
import static kore.botssdk.net.SDKConfiguration.Client.enable_ack_delivery;
import static kore.botssdk.utils.BundleConstants.CAPTURE_IMAGE_CHOOSE_FILES_BUNDLED_PREMISSION_REQUEST;
import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import io.reactivex.annotations.NonNull;
import kore.botssdk.R;
import kore.botssdk.adapter.PromotionsAdapter;
import kore.botssdk.adapter.WelcomeStarterButtonsAdapter;
import kore.botssdk.adapter.WelcomeStaticLinkListAdapter;
import kore.botssdk.adapter.WelcomeStaticLinksAdapter;
import kore.botssdk.application.BotApplication;
import kore.botssdk.bot.BotClient;
import kore.botssdk.event.KoreEventCenter;
import kore.botssdk.events.SocketDataTransferModel;
import kore.botssdk.fileupload.core.KoreWorker;
import kore.botssdk.fileupload.core.UploadBulkFile;
import kore.botssdk.fragment.BotContentFragment;
import kore.botssdk.fragment.ComposeFooterFragment;
import kore.botssdk.fragment.QuickReplyFragment;
import kore.botssdk.listener.BaseSocketConnectionManager;
import kore.botssdk.listener.BotContentFragmentUpdate;
import kore.botssdk.listener.BotSocketConnectionManager;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.ComposeFooterUpdate;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.SocketChatListener;
import kore.botssdk.listener.TTSUpdate;
import kore.botssdk.listener.ThemeChangeListener;
import kore.botssdk.models.BotActiveThemeModel;
import kore.botssdk.models.BotBrandingModel;
import kore.botssdk.models.BotButtonModel;
import kore.botssdk.models.BotInfoModel;
import kore.botssdk.models.BotMetaModel;
import kore.botssdk.models.BotOptionsModel;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.BotResponseMessage;
import kore.botssdk.models.BotResponsePayLoadText;
import kore.botssdk.models.BrandingWelcomeModel;
import kore.botssdk.models.CalEventsTemplateModel;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.ComponentModelPayloadText;
import kore.botssdk.models.EventMessageModel;
import kore.botssdk.models.EventModel;
import kore.botssdk.models.FormActionTemplate;
import kore.botssdk.models.KnowledgeCollectionModel;
import kore.botssdk.models.KoreComponentModel;
import kore.botssdk.models.KoreMedia;
import kore.botssdk.models.PayloadHeaderModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.models.WebHookRequestModel;
import kore.botssdk.models.WebHookResponseDataModel;
import kore.botssdk.net.BrandingRestBuilder;
import kore.botssdk.net.RestBuilder;
import kore.botssdk.net.RestResponse;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.net.WebHookRestBuilder;
import kore.botssdk.pushnotification.PushNotificationRegister;
import kore.botssdk.utils.AsyncTaskExecutor;
import kore.botssdk.utils.BitmapUtils;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.KaMediaUtils;
import kore.botssdk.utils.KaPermissionsHelper;
import kore.botssdk.utils.LogUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.TTSSynthesizer;
import kore.botssdk.view.AutoExpandListView;
import kore.botssdk.view.HeightAdjustableViewPager;
import kore.botssdk.view.viewUtils.RoundedCornersTransform;
import kore.botssdk.websocket.SocketWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
@SuppressLint("UnknownNullness")
public class BotChatActivity extends BotAppCompactActivity implements ComposeFooterInterface, QuickReplyFragment.QuickReplyInterface, TTSUpdate, InvokeGenericWebViewInterface, ThemeChangeListener {
    final String LOG_TAG = BotChatActivity.class.getSimpleName();
    FrameLayout chatLayoutFooterContainer;
    FrameLayout chatLayoutContentContainer;
    FrameLayout chatLayoutPanelContainer;
    ProgressBar taskProgressBar;
    FragmentTransaction fragmentTransaction;
    final Handler handler = new Handler();
    String chatBot, taskBotId, jwt;
    Handler actionBarTitleUpdateHandler;
    BotClient botClient;
    BotContentFragment botContentFragment;
    ComposeFooterFragment composeFooterFragment;
    TTSSynthesizer ttsSynthesizer;
    QuickReplyFragment quickReplyFragment;
    BotContentFragmentUpdate botContentFragmentUpdate;
    ComposeFooterUpdate composeFooterUpdate;
    boolean isItFirstConnect = true;
    final Gson gson = new Gson();
    //Fragment Approch
    FrameLayout composerView;
    RelativeLayout rlChatWindow;
    SharedPreferences sharedPreferences;
    private ImageView ivChaseBackground, ivChaseLogo;
    protected final int compressQualityInt = 100;
    final Handler messageHandler = new Handler();
    private String fileUrl;
    WebHookResponseDataModel webHookResponseDataModel;
    BotMetaModel botMetaModel;
    Runnable runnable;
    private final int poll_delay = 2000;
    String lastMsgId = "";
    Dialog progressBar, welcomeDialog;
    private static String uniqueID = null;
    BotBrandingModel botOptionsModel;
    BotActiveThemeModel botActiveThemeModel;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bot_chat_layout);

        findViews();
        getBundleInfo();
        getDataFromTxt();

        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        //Add Bot Content Fragment
        botContentFragment = new BotContentFragment();
        botContentFragment.setArguments(getIntent().getExtras());
        botContentFragment.setComposeFooterInterface(this);
        botContentFragment.setInvokeGenericWebViewInterface(this);
        botContentFragment.setThemeChangeInterface(this);
        fragmentTransaction.add(R.id.chatLayoutContentContainer, botContentFragment).commit();
        setBotContentFragmentUpdate(botContentFragment);

        //Add Suggestion Fragment
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        quickReplyFragment = new QuickReplyFragment();
        quickReplyFragment.setArguments(getIntent().getExtras());
        quickReplyFragment.setListener(BotChatActivity.this);
        fragmentTransaction.add(R.id.quickReplyLayoutFooterContainer, quickReplyFragment).commit();

        //Add Bot Compose Footer Fragment
        fragmentTransaction = getSupportFragmentManager().beginTransaction();
        composeFooterFragment = new ComposeFooterFragment();
        composeFooterFragment.setArguments(getIntent().getExtras());
        composeFooterFragment.setComposeFooterInterface(this);
        composeFooterFragment.setBottomOptionData(getDataFromTxt());
        fragmentTransaction.add(R.id.chatLayoutFooterContainer, composeFooterFragment).commit();
        setComposeFooterUpdate(composeFooterFragment);

        updateTitleBar();

        botClient = new BotClient(this);
        ttsSynthesizer = new TTSSynthesizer(this);
        setupTextToSpeech();
        KoreEventCenter.register(this);

        if (!SDKConfiguration.Client.isWebHook) {
            BotSocketConnectionManager.getInstance().setChatListener(sListener);
        } else BotSocketConnectionManager.getInstance().startAndInitiateConnectionWithConfig(getApplicationContext(), null);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isOnline()) {
                    BotSocketConnectionManager.killInstance();
                }
                finish();
            }
        });

    }

    final SocketChatListener sListener = new SocketChatListener() {
        @Override
        public void onMessage(BotResponse botResponse) {
            processPayload("", botResponse);
        }

        @Override
        public void onConnectionStateChanged(BaseSocketConnectionManager.CONNECTION_STATE state, boolean isReconnection) {
            if (state == BaseSocketConnectionManager.CONNECTION_STATE.CONNECTED) {
                getBrandingDetails();
//                getBrandingDataFromTxt();
            }

            new PushNotificationRegister().registerPushNotification(BotChatActivity.this, botClient.getUserId(), botClient.getAccessToken(), sharedPreferences.getString("FCMToken", getUniqueDeviceId(BotChatActivity.this)));

            updateTitleBar(state);
        }

        @Override
        public void onMessage(SocketDataTransferModel data) {
            if (data == null) return;
            if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_TEXT_MESSAGE)) {
                processPayload(data.getPayLoad(), null);

            } else if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_MESSAGE_UPDATE)) {
                if (botContentFragment != null) {
                    botContentFragment.updateContentListOnSend(data.getBotRequest());
                }
            }
        }
    };

    public void postNotification(String title, String pushMessage) {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder nBuilder = null;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel notificationChannel = new NotificationChannel("Kore_Push_Service", "Kore_Android", importance);
        mNotificationManager.createNotificationChannel(notificationChannel);
        nBuilder = new NotificationCompat.Builder(this, notificationChannel.getId());

        nBuilder.setContentTitle(title).setSmallIcon(R.mipmap.ic_launcher).setColor(Color.parseColor("#009dab")).setContentText(pushMessage).setGroup(GROUP_KEY_NOTIFICATIONS).setGroupSummary(true).setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_HIGH);
        if (alarmSound != null) {
            nBuilder.setSound(alarmSound);
        }

        Intent intent = new Intent(getApplicationContext(), BotChatActivity.class);
        Bundle bundle = new Bundle();
        //This should not be null
        bundle.putBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
        bundle.putString(BundleUtils.PICK_TYPE, "Notification");
        bundle.putString(BundleUtils.BOT_NAME_INITIALS, SDKConfiguration.Client.bot_name.charAt(0) + "");
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_MUTABLE);
        nBuilder.setContentIntent(pendingIntent);

        Notification notification = nBuilder.build();
        notification.ledARGB = 0xff0000FF;

        mNotificationManager.notify("YUIYUYIU", 237891, notification);
    }

    @Override
    protected void onDestroy() {
        botClient.disconnect();
        KoreEventCenter.unregister(this);

        if (progressBar != null) progressBar.dismiss();

        if (welcomeDialog != null) {
            welcomeDialog.hide();
        }

        super.onDestroy();
    }

    private void getBundleInfo() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
//            if (!bundle.getBoolean(BundleUtils.IS_FROM_WELCOME)) {
//                this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        new Handler().postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (composeFooterFragment != null) {
//                                    composeFooterFragment.setBotBrandingModel(botOptionsModel);
//                                }
//
//                                if (botContentFragment != null) {
//                                    botContentFragment.setBotBrandingModel(botOptionsModel);
//                                }
//
//                                showWelcomeDialog();
//                            }
//                        }, 5000);
//                    }
//                });
//            } else {
//                closeProgressDialogue();
//                rlChatWindow.setVisibility(VISIBLE);
//            }

            jwt = bundle.getString(BundleUtils.JWT_TKN, "");

        }

        chatBot = SDKConfiguration.Client.bot_name;
        taskBotId = SDKConfiguration.Client.bot_id;
    }

    private void findViews() {
        chatLayoutFooterContainer = findViewById(R.id.chatLayoutFooterContainer);
        chatLayoutContentContainer = findViewById(R.id.chatLayoutContentContainer);
        chatLayoutPanelContainer = findViewById(R.id.chatLayoutPanelContainer);
        rlChatWindow = findViewById(R.id.rlChatWindow);
        taskProgressBar = findViewById(R.id.taskProgressBar);
        ivChaseBackground = findViewById(R.id.ivChaseBackground);
        ivChaseLogo = findViewById(R.id.ivChaseLogo);
        sharedPreferences = getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        RestBuilder.setContext(BotChatActivity.this);
        WebHookRestBuilder.setContext(BotChatActivity.this);
        BrandingRestBuilder.setContext(BotChatActivity.this);
        showProgressDialogue("Please wait..");
    }

    void updateTitleBar() {
//        String botName = (chatBot != null && !chatBot.isEmpty()) ? chatBot : ((SDKConfiguration.Server.IS_ANONYMOUS_USER) ? chatBot + " - anonymous" : chatBot);
//        getSupportActionBar().setSubtitle(botName);
    }

    void updateTitleBar(BaseSocketConnectionManager.CONNECTION_STATE socketConnectionEvents) {

        switch (socketConnectionEvents) {
            case CONNECTING:
                taskProgressBar.setVisibility(View.VISIBLE);
                updateActionBar();
                break;
            case CONNECTED:
                /*if(isItFirstConnect)
                    botClient.sendMessage("welcomedialog");*/
                taskProgressBar.setVisibility(View.GONE);
                composeFooterFragment.enableSendButton();
                updateActionBar();

                break;
            case DISCONNECTED:
            case CONNECTED_BUT_DISCONNECTED:
                taskProgressBar.setVisibility(View.VISIBLE);
                composeFooterFragment.setDisabled(true);
                composeFooterFragment.updateUI();
                updateActionBar();
                break;

            default:
                taskProgressBar.setVisibility(View.GONE);
                updateActionBar();

        }
    }


    private void setupTextToSpeech() {
        composeFooterFragment.setTtsUpdate(BotSocketConnectionManager.getInstance());
        botContentFragment.setTtsUpdate(BotSocketConnectionManager.getInstance());
    }

    public void onEvent(String jwt) {
        this.jwt = jwt;

        if (SDKConfiguration.Client.isWebHook) {
            if (botContentFragment != null) botContentFragment.setJwtTokenForWebHook(jwt);

            if (composeFooterFragment != null) composeFooterFragment.setJwtToken(jwt);

            getWebHookMeta();
        }
    }

    public void onEvent(SocketDataTransferModel data) {
        if (data == null) return;
        if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_TEXT_MESSAGE)) {
            processPayload(data.getPayLoad(), null);
        } else if (data.getEvent_type().equals(BaseSocketConnectionManager.EVENT_TYPE.TYPE_MESSAGE_UPDATE)) {
            if (botContentFragment != null) {
                botContentFragment.updateContentListOnSend(data.getBotRequest());
            }
        }
    }

    public void onEvent(@androidx.annotation.NonNull BaseSocketConnectionManager.CONNECTION_STATE states) {
        updateTitleBar(states);
    }

    public void setButtonBranding(BotBrandingModel brandingModel) {
        if (brandingModel != null) {
            SharedPreferences.Editor editor = getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();

            if (brandingModel.getBody() != null && brandingModel.getBody().getBot_message() != null) {
                editor.putString(BotResponse.BUBBLE_LEFT_BG_COLOR, brandingModel.getBody().getBot_message().getBg_color());
                editor.putString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, brandingModel.getBody().getBot_message().getColor());
            }

            if (brandingModel.getBody() != null && brandingModel.getBody().getUser_message() != null) {

                editor.putString(BotResponse.BUBBLE_RIGHT_BG_COLOR, brandingModel.getBody().getUser_message().getBg_color());
                editor.putString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, brandingModel.getBody().getUser_message().getColor());
            }

            if (brandingModel.getGeneral() != null && brandingModel.getGeneral().getColors() != null && brandingModel.getGeneral().getColors().isUseColorPaletteOnly()) {
                editor.putString(BotResponse.BUTTON_ACTIVE_BG_COLOR, brandingModel.getGeneral().getColors().getPrimary());
                editor.putString(BotResponse.BUTTON_ACTIVE_TXT_COLOR, brandingModel.getGeneral().getColors().getSecondary_text());
                editor.putString(BotResponse.BUTTON_INACTIVE_BG_COLOR, brandingModel.getGeneral().getColors().getSecondary());
                editor.putString(BotResponse.BUTTON_INACTIVE_TXT_COLOR, brandingModel.getGeneral().getColors().getPrimary_text());
                SDKConfiguration.BubbleColors.quickReplyColor = brandingModel.getGeneral().getColors().getPrimary();
                SDKConfiguration.BubbleColors.quickReplyTextColor = brandingModel.getGeneral().getColors().getPrimary_text();
                editor.putString(BotResponse.BUBBLE_LEFT_BG_COLOR, brandingModel.getGeneral().getColors().getSecondary());
                editor.putString(BotResponse.BUBBLE_LEFT_TEXT_COLOR, brandingModel.getGeneral().getColors().getPrimary_text());
                editor.putString(BotResponse.BUBBLE_RIGHT_BG_COLOR, brandingModel.getGeneral().getColors().getPrimary());
                editor.putString(BotResponse.BUBBLE_RIGHT_TEXT_COLOR, brandingModel.getGeneral().getColors().getSecondary_text());
            }
            editor.apply();
        }
    }


    public void onEvent(BotResponse botResponse) {
        processPayload("", botResponse);
    }

    public void updateActionbar(boolean isSelected, String type, ArrayList<BotButtonModel> buttonModels) {

    }

    @Override
    public void lauchMeetingNotesAction(Context context, String mid, String eid) {

    }

    @Override
    public void showAfterOnboard(boolean isdiscard) {

    }

    @Override
    public void onPanelClicked(Object pModel, boolean isFirstLaunch) {

    }

    @Override
    public void knowledgeCollectionItemClick(KnowledgeCollectionModel.DataElements elements, String id) {

    }

    @Override
    public void externalReadWritePermission(String fileUrl) {
        this.fileUrl = fileUrl;
        if (!KaPermissionsHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            KaPermissionsHelper.requestForPermission(this, CAPTURE_IMAGE_CHOOSE_FILES_BUNDLED_PREMISSION_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onDeepLinkClicked(String url) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAPTURE_IMAGE_CHOOSE_FILES_BUNDLED_PREMISSION_REQUEST) {
            if (KaPermissionsHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE/*,Manifest.permission.RECORD_AUDIO*/)) {

                if (!StringUtils.isNullOrEmpty(fileUrl)) KaMediaUtils.saveFileFromUrlToKorePath(BotChatActivity.this, fileUrl);
            } else {
                Toast.makeText(getApplicationContext(), "Access denied. Operation failed !!", Toast.LENGTH_LONG).show();
            }
        }
    }

    void updateActionBar() {
        if (actionBarTitleUpdateHandler == null) {
            actionBarTitleUpdateHandler = new Handler();
        }

        actionBarTitleUpdateHandler.removeCallbacks(actionBarUpdateRunnable);
        actionBarTitleUpdateHandler.postDelayed(actionBarUpdateRunnable, 4000);

    }

    final Runnable actionBarUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateTitleBar();
        }
    };

    @Override
    protected void onPause() {
        BotApplication.activityPaused();
        ttsSynthesizer.stopTextToSpeech();
        super.onPause();
    }


    @Override
    public void onSendClick(String message, boolean isFromUtterance) {
        if (!StringUtils.isNullOrEmpty(message)) {
            closeWelcomeDialog();

            if (!SDKConfiguration.Client.isWebHook) BotSocketConnectionManager.getInstance().sendMessage(message, null);
            else {
                addSentMessageToChat(message);
                sendWebHookMessage(false, message, null);
                BotSocketConnectionManager.getInstance().stopTextToSpeech();
            }
        }
    }


    @Override
    public void onSendClick(String message, String payload, boolean isFromUtterance) {
        closeWelcomeDialog();
        if (!SDKConfiguration.Client.isWebHook) {
            if (payload != null) {
                BotSocketConnectionManager.getInstance().sendPayload(message, payload);
            } else {
                BotSocketConnectionManager.getInstance().sendMessage(message, "");
            }
        } else {
            BotSocketConnectionManager.getInstance().stopTextToSpeech();
            if (payload != null) {
                addSentMessageToChat(message);
                sendWebHookMessage(false, payload, null);
            } else {
                addSentMessageToChat(message);
                sendWebHookMessage(false, message, null);
            }
        }

        toggleQuickRepliesVisiblity();
    }

    @Override
    public void onSendClick(String message, ArrayList<HashMap<String, String>> attachments, boolean isFromUtterance) {
        if (attachments != null && attachments.size() > 0) {
            if (!SDKConfiguration.Client.isWebHook) BotSocketConnectionManager.getInstance().sendAttachmentMessage(message, attachments);
            else {
                addSentMessageToChat(message);
                sendWebHookMessage(false, message, attachments);
            }
        }
    }

    @Override
    public void onFormActionButtonClicked(FormActionTemplate fTemplate) {

    }

    @Override
    public void launchActivityWithBundle(String type, Bundle payload) {

    }

    @Override
    public void sendWithSomeDelay(String message, String payload, long time, boolean isScrollupNeeded) {
        if (message.equalsIgnoreCase(BundleUtils.OPEN_WELCOME)) {
            if (welcomeDialog != null) welcomeDialog.show();
        }
    }

    @Override
    public void copyMessageToComposer(String text, boolean isForOnboard) {
        composeFooterFragment.setComposeText(text);
    }

    @Override
    public void showMentionNarratorContainer(boolean show, String natxt, String cotext, String res, boolean isEnd, boolean showOverlay, String templateType) {

    }

    @Override
    public void openFullView(String templateType, String data, CalEventsTemplateModel.Duration duration, int position) {

    }


    public void setBotContentFragmentUpdate(BotContentFragmentUpdate botContentFragmentUpdate) {
        this.botContentFragmentUpdate = botContentFragmentUpdate;
    }

    public void setComposeFooterUpdate(ComposeFooterUpdate composeFooterUpdate) {
        this.composeFooterUpdate = composeFooterUpdate;
    }


    @Override
    public void onQuickReplyItemClicked(String text) {
        onSendClick(text, false);
    }

    /**
     * payload processing
     */

    void processPayload(String payload, BotResponse botLocalResponse) {
        if (botLocalResponse == null) BotSocketConnectionManager.getInstance().stopDelayMsgTimer();

        if (payload.contains("Form_Submitted")) {
            Intent intent = new Intent("finish_activity");
            sendBroadcast(intent);
        }

        try {
            final BotResponse botResponse = botLocalResponse != null ? botLocalResponse : gson.fromJson(payload, BotResponse.class);
            if (botResponse == null || botResponse.getMessage() == null || botResponse.getMessage().isEmpty()) {
                return;
            }

            if (!StringUtils.isNullOrEmpty(botResponse.getIcon()) && StringUtils.isNullOrEmpty(SDKConfiguration.BubbleColors.getIcon_url()))
                SDKConfiguration.BubbleColors.setIcon_url(botResponse.getIcon());

            if (botClient != null && enable_ack_delivery)
                botClient.sendMsgAcknowledgement(botResponse.getTimestamp(), botResponse.getKey());

            LogUtils.d(LOG_TAG, payload);

            PayloadOuter payOuter = null;
            if (!botResponse.getMessage().isEmpty()) {
                ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
                if (compModel != null) {
                    payOuter = compModel.getPayload();
                    if (payOuter != null) {
                        if (payOuter.getText() != null && payOuter.getText().contains("&quot")) {
                            Gson gson = new Gson();
                            payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                        } else if (payOuter.getText() != null && payOuter.getText().contains("*")) {
                            Gson gson = new Gson();
                            payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                        }

                    }
                }
            }

            final PayloadInner payloadInner = payOuter == null ? null : payOuter.getPayload();
            if (payloadInner != null && payloadInner.getTemplate_type() != null && "start_timer".equalsIgnoreCase(payloadInner.getTemplate_type())) {
                BotSocketConnectionManager.getInstance().startDelayMsgTimer();
            }
            botContentFragment.showTypingStatus(botResponse);
            if (payloadInner != null) {
                payloadInner.convertElementToAppropriate();
            }

            if (!BotApplication.isActivityVisible()) {
                postNotification("Kore Message", "Received new message.");
            }

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (botResponse.getMessageId() != null) lastMsgId = botResponse.getMessageId();

                    botContentFragment.addMessageToBotChatAdapter(botResponse);
                    botContentFragment.setQuickRepliesIntoFooter(botResponse);
                    botContentFragment.showCalendarIntoFooter(botResponse);
                }
            }, BundleConstants.TYPING_STATUS_TIME);
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof JsonSyntaxException) {
                try {
                    //This is the case Bot returning user sent message from another channel
                    if (botContentFragment != null) {
                        BotRequest botRequest = gson.fromJson(payload, BotRequest.class);
                        botRequest.setCreatedOn(DateUtils.isoFormatter.format(new Date()));
                        botContentFragment.updateContentListOnSend(botRequest);
                    }
                } catch (Exception e1) {
                    try {
                        final BotResponsePayLoadText botResponse = gson.fromJson(payload, BotResponsePayLoadText.class);
                        if (botResponse == null || botResponse.getMessage() == null || botResponse.getMessage().isEmpty()) {
                            return;
                        }
                        LogUtils.d(LOG_TAG, payload);
                        if (!botResponse.getMessage().isEmpty()) {
                            ComponentModelPayloadText compModel = botResponse.getMessage().get(0).getComponent();
                            if (compModel != null && !StringUtils.isNullOrEmpty(compModel.getPayload())) {
                                displayMessage(compModel.getPayload(), BotResponse.COMPONENT_TYPE_TEXT, botResponse.getMessageId());
                            }
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public void invokeGenericWebView(String url) {
        if (url != null && !url.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), GenericWebViewActivity.class);
            intent.putExtra("url", url);
            intent.putExtra("header", getResources().getString(R.string.app_name));
            startActivity(intent);
        }
    }

    @Override
    public void handleUserActions(String action, HashMap<String, Object> payload) {


    }

    @Override
    public void onStop() {
        BotSocketConnectionManager.getInstance().unSubscribe();
        super.onStop();
    }

    public BotOptionsModel getDataFromTxt() {
        BotOptionsModel botOptionsModel = null;

        try {
            InputStream is = getResources().openRawResource(R.raw.option);
            Reader reader = new InputStreamReader(is);
            botOptionsModel = gson.fromJson(reader, BotOptionsModel.class);
            LogUtils.e("Options Size", botOptionsModel.getTasks().size() + "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return botOptionsModel;
    }

    public void getBrandingDataFromTxt() {
        try {
            InputStream is = getResources().openRawResource(R.raw.branding_response);
            Reader reader = new InputStreamReader(is);
            BotActiveThemeModel botActiveThemeModel = gson.fromJson(reader, BotActiveThemeModel.class);
            botOptionsModel = botActiveThemeModel.getV3();

            setButtonBranding(botOptionsModel);

            if (composeFooterFragment != null) {
                composeFooterFragment.setBotBrandingModel(botOptionsModel);
            }

            if (botContentFragment != null) {
                botContentFragment.setBotBrandingModel(botOptionsModel);
            }

            if (botOptionsModel != null && botOptionsModel.getChat_bubble() != null && !StringUtils.isNullOrEmpty(botOptionsModel.getChat_bubble().getStyle())) {
                sharedPreferences.edit().putString(BundleConstants.BUBBLE_STYLE, botOptionsModel.getChat_bubble().getStyle()).apply();
            }

            if (botOptionsModel != null && botOptionsModel.getBody() != null && !StringUtils.isNullOrEmpty(botOptionsModel.getBody().getBubble_style())) {
                sharedPreferences.edit().putString(BundleConstants.BUBBLE_STYLE, botOptionsModel.getBody().getBubble_style()).apply();
            }

            if (botOptionsModel != null && botOptionsModel.getWelcome_screen() != null) {
                if (botOptionsModel.getWelcome_screen().isShow()) showWelcomeDialog();
                else {
                    closeProgressDialogue();
                    rlChatWindow.setVisibility(VISIBLE);
                }
            } else {
                closeProgressDialogue();
                rlChatWindow.setVisibility(VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayMessage(String text, String type, String messageId) {
        if (!lastMsgId.equalsIgnoreCase(messageId)) {
            try {
                PayloadOuter payloadOuter = gson.fromJson(text, PayloadOuter.class);

                if (StringUtils.isNullOrEmpty(payloadOuter.getType())) payloadOuter.setType(type);

                ComponentModel componentModel = new ComponentModel();
                componentModel.setType(payloadOuter.getType());
                componentModel.setPayload(payloadOuter);

                BotResponseMessage botResponseMessage = new BotResponseMessage();
                botResponseMessage.setType(componentModel.getType());
                botResponseMessage.setComponent(componentModel);

                ArrayList<BotResponseMessage> arrBotResponseMessages = new ArrayList<>();
                arrBotResponseMessages.add(botResponseMessage);

                BotResponse botResponse = new BotResponse();
                botResponse.setType(componentModel.getType());
                botResponse.setMessage(arrBotResponseMessages);
                botResponse.setMessageId(messageId);

                if (botMetaModel != null && !StringUtils.isNullOrEmpty(botMetaModel.getIcon())) botResponse.setIcon(botMetaModel.getIcon());

                processPayload("", botResponse);
            } catch (Exception e) {
                PayloadInner payloadInner = new PayloadInner();
                payloadInner.setTemplate_type("text");

                PayloadOuter payloadOuter = new PayloadOuter();
                payloadOuter.setText(text);
                payloadOuter.setType("text");
                payloadOuter.setPayload(payloadInner);

                ComponentModel componentModel = new ComponentModel();
                componentModel.setType("text");
                componentModel.setPayload(payloadOuter);

                BotResponseMessage botResponseMessage = new BotResponseMessage();
                botResponseMessage.setType("text");
                botResponseMessage.setComponent(componentModel);

                ArrayList<BotResponseMessage> arrBotResponseMessages = new ArrayList<>();
                arrBotResponseMessages.add(botResponseMessage);

                BotResponse botResponse = new BotResponse();
                botResponse.setType("text");
                botResponse.setMessage(arrBotResponseMessages);
                botResponse.setMessageId(messageId);

                if (botMetaModel != null && !StringUtils.isNullOrEmpty(botMetaModel.getIcon())) botResponse.setIcon(botMetaModel.getIcon());

                processPayload("", botResponse);
            }
        }

    }

    public void displayMessage(PayloadOuter payloadOuter) {
        try {
            if (payloadOuter != null && payloadOuter.getPayload() != null) {
                ComponentModel componentModel = new ComponentModel();
                componentModel.setType(payloadOuter.getType());
                componentModel.setPayload(payloadOuter);

                BotResponseMessage botResponseMessage = new BotResponseMessage();
                botResponseMessage.setType(componentModel.getType());
                botResponseMessage.setComponent(componentModel);

                ArrayList<BotResponseMessage> arrBotResponseMessages = new ArrayList<>();
                arrBotResponseMessages.add(botResponseMessage);

                BotResponse botResponse = new BotResponse();
                botResponse.setType(componentModel.getType());
                botResponse.setMessage(arrBotResponseMessages);

                if (botMetaModel != null && !StringUtils.isNullOrEmpty(botMetaModel.getIcon())) botResponse.setIcon(botMetaModel.getIcon());

                processPayload("", botResponse);
            } else if (payloadOuter != null && !StringUtils.isNullOrEmpty(payloadOuter.getText())) {
                displayMessage(payloadOuter.getText(), BotResponse.COMPONENT_TYPE_TEXT, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                BotSocketConnectionManager.getInstance().subscribe();
            }
        });
        super.onStart();
    }

    @Override
    protected void onResume() {
        BotApplication.activityResumed();
        if (!SDKConfiguration.Client.isWebHook) {
            BotSocketConnectionManager.getInstance().checkConnectionAndRetry(getApplicationContext(), false);
            updateTitleBar(BotSocketConnectionManager.getInstance().getConnection_state());
        }
        super.onResume();
    }

    @Override
    public void ttsUpdateListener(boolean isTTSEnabled) {
        stopTextToSpeech();
    }

    @Override
    public void ttsOnStop() {
        stopTextToSpeech();
    }

    public boolean isTTSEnabled() {
        if (composeFooterFragment != null) {
            return composeFooterFragment.isTTSEnabled();
        } else {
            LogUtils.e(BotChatActivity.class.getSimpleName(), "ComposeFooterFragment not found");
            return false;
        }
    }

    private void stopTextToSpeech() {
        try {
            ttsSynthesizer.stopTextToSpeech();
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
        }
    }

    void textToSpeech(BotResponse botResponse) {
        if (isTTSEnabled() && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            String botResponseTextualFormat = "";
            ComponentModel componentModel = botResponse.getMessage().get(0).getComponent();
            if (componentModel != null) {
                String compType = componentModel.getType();
                PayloadOuter payOuter = componentModel.getPayload();
                if (BotResponse.COMPONENT_TYPE_TEXT.equalsIgnoreCase(compType) || payOuter.getType() == null) {
                    botResponseTextualFormat = payOuter.getText();
                } else if (BotResponse.COMPONENT_TYPE_ERROR.equalsIgnoreCase(payOuter.getType())) {
                    botResponseTextualFormat = payOuter.getPayload().getText();
                } else if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(payOuter.getType()) || BotResponse.COMPONENT_TYPE_MESSAGE.equalsIgnoreCase(payOuter.getType())) {
                    PayloadInner payInner;
                    if (payOuter.getText() != null && payOuter.getText().contains("&quot")) {
                        Gson gson = new Gson();
                        payOuter = gson.fromJson(payOuter.getText().replace("&quot;", "\""), PayloadOuter.class);
                    }
                    payInner = payOuter.getPayload();

                    if (payInner.getSpeech_hint() != null) {
                        botResponseTextualFormat = payInner.getSpeech_hint();
//                        ttsSynthesizer.speak(botResponseTextualFormat);
                    } else if (BotResponse.TEMPLATE_TYPE_BUTTON.equalsIgnoreCase(payInner.getTemplate_type())) {
                        botResponseTextualFormat = payInner.getText();
                    } else if (BotResponse.TEMPLATE_TYPE_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())) {
                        botResponseTextualFormat = payInner.getText();
                    } else if (BotResponse.TEMPLATE_TYPE_CAROUSEL.equalsIgnoreCase(payInner.getTemplate_type())) {
                        botResponseTextualFormat = payInner.getText();
                    } else if (BotResponse.TEMPLATE_TYPE_CAROUSEL_ADV.equalsIgnoreCase(payInner.getTemplate_type())) {
                        botResponseTextualFormat = payInner.getText();
                    } else if (BotResponse.TEMPLATE_TYPE_LIST.equalsIgnoreCase(payInner.getTemplate_type())) {
                        botResponseTextualFormat = payInner.getText();
                    }
                }
            }
            if (BotSocketConnectionManager.getInstance().isTTSEnabled()) {
                BotSocketConnectionManager.getInstance().startSpeak(botResponseTextualFormat);
            }
        }
    }


    private void toggleQuickRepliesVisiblity() {
        quickReplyFragment.toggleQuickReplyContainer(View.GONE);
    }

    @SuppressLint("MissingPermission")
    protected boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Network nw = connectivityManager.getActiveNetwork();
        if (nw == null) return false;
        NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
        return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
    }

    @Override
    public void onThemeChangeClicked(String message) {
        if (message.equalsIgnoreCase(BotResponse.THEME_NAME_1)) {
            ivChaseLogo.setVisibility(View.VISIBLE);
            ivChaseBackground.setVisibility(View.GONE);
        } else {
            ivChaseBackground.setVisibility(VISIBLE);
            ivChaseLogo.setVisibility(View.GONE);
        }
    }

    public void sendImage(String fP, String fN, String fPT) {
        new SaveCapturedImageTask(fP, fN, fPT).executeAsync();
    }

    protected class SaveCapturedImageTask extends AsyncTaskExecutor<String> {
        private final String filePath;
        private final String fileName;
        private final String filePathThumbnail;
        private String orientation;
        private String extn = null;

        public SaveCapturedImageTask(String filePath, String fileName, String filePathThumbnail) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.filePathThumbnail = filePathThumbnail;
        }

        @Override
        protected void doInBackground(String... strings) {
            OutputStream fOut = null;
            if (filePath != null) {
                extn = filePath.substring(filePath.lastIndexOf(".") + 1);
                Bitmap thePic = BitmapUtils.decodeBitmapFromFile(filePath, 800, 600, false);
                if (thePic != null) {
                    try {
                        // compress the image
                        File _file = new File(filePath);

                        LogUtils.d(LOG_TAG, " file.exists() ---------------------------------------- " + _file.exists());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            fOut = Files.newOutputStream(_file.toPath());
                        } else fOut = new FileOutputStream(_file);

                        thePic.compress(Bitmap.CompressFormat.JPEG, compressQualityInt, fOut);
                        thePic = rotateIfNecessary(filePath, thePic);
                        orientation = thePic.getWidth() > thePic.getHeight() ? BitmapUtils.ORIENTATION_LS : BitmapUtils.ORIENTATION_PT;
                        fOut.flush();
                        fOut.close();
                    } catch (Exception e) {
                        LogUtils.e(LOG_TAG, e.toString());
                    } finally {
                        try {
                            assert fOut != null;
                            fOut.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        @Override
        protected void onPostExecute() {
            if (extn != null) {
                if (!SDKConfiguration.Client.isWebHook) {
                    KoreWorker.getInstance().addTask(new UploadBulkFile(fileName, filePath, "bearer " + SocketWrapper.getInstance(BotChatActivity.this).getAccessToken(), SocketWrapper.getInstance(BotChatActivity.this).getBotUserId(), "workflows", extn, KoreMedia.BUFFER_SIZE_IMAGE, new Messenger(messagesMediaUploadAcknowledgeHandler), filePathThumbnail, "AT_" + System.currentTimeMillis(), BotChatActivity.this, BitmapUtils.obtainMediaTypeOfExtn(extn), (!SDKConfiguration.Client.isWebHook ? SDKConfiguration.Server.SERVER_URL : SDKConfiguration.Server.koreAPIUrl), orientation, true, SDKConfiguration.Client.isWebHook, SDKConfiguration.Client.bot_id));
                } else {
                    KoreWorker.getInstance().addTask(new UploadBulkFile(fileName, filePath, "bearer " + jwt, SocketWrapper.getInstance(BotChatActivity.this).getBotUserId(), "workflows", extn, KoreMedia.BUFFER_SIZE_IMAGE, new Messenger(messagesMediaUploadAcknowledgeHandler), filePathThumbnail, "AT_" + System.currentTimeMillis(), BotChatActivity.this, BitmapUtils.obtainMediaTypeOfExtn(extn), (!SDKConfiguration.Client.isWebHook ? SDKConfiguration.Server.SERVER_URL : SDKConfiguration.Server.koreAPIUrl), orientation, true, SDKConfiguration.Client.isWebHook, SDKConfiguration.Client.webHook_bot_id));
                }
            } else {
                showToast("Unable to attach!");
            }
        }

        @Override
        protected void onCancelled() {
            // update UI on task cancelled
            showToast("Unable to attach!");
        }
    }

    @SuppressLint("HandlerLeak")
    final Handler messagesMediaUploadAcknowledgeHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public synchronized void handleMessage(Message msg) {
            Bundle reply = msg.getData();
            LogUtils.e("shri", reply + "------------------------------");
            if (reply.getBoolean("success", true)) {
                String mediaFilePath = reply.getString("filePath");
                String MEDIA_TYPE = reply.getString("fileExtn");
                String mediaFileId = reply.getString("fileId");
                String mediaFileName = reply.getString("fileName");
                String componentType = reply.getString("componentType");
                String thumbnailURL = reply.getString("thumbnailURL");
                String fileSize = reply.getString("fileSize");
                KoreComponentModel koreMedia = new KoreComponentModel();
                koreMedia.setMediaType(BitmapUtils.getAttachmentType(componentType));
                HashMap<String, Object> cmpData = new HashMap<>(1);
                cmpData.put("fileName", mediaFileName);

                koreMedia.setComponentData(cmpData);
                koreMedia.setMediaFileName(getComponentId(componentType));
                koreMedia.setMediaFilePath(mediaFilePath);
                koreMedia.setFileSize(fileSize);

                koreMedia.setMediafileId(mediaFileId);
                koreMedia.setMediaThumbnail(thumbnailURL);

                messageHandler.postDelayed(new Runnable() {
                    public void run() {
                        HashMap<String, String> attachmentKey = new HashMap<>();
                        attachmentKey.put("fileName", mediaFileName + "." + MEDIA_TYPE);
                        attachmentKey.put("fileType", componentType);
                        attachmentKey.put("fileId", mediaFileId);
                        attachmentKey.put("localFilePath", mediaFilePath);
                        attachmentKey.put("fileExtn", MEDIA_TYPE);
                        attachmentKey.put("thumbnailURL", thumbnailURL);
                        composeFooterFragment.addAttachmentToAdapter(attachmentKey);
                    }
                }, 400);
            } else {
                String errorMsg = reply.getString(UploadBulkFile.error_msz_key);
                if (!TextUtils.isEmpty(errorMsg)) {
                    LogUtils.i("File upload error", errorMsg);
                    showToast(errorMsg);
                }
            }
        }
    };

    public void mediaAttachment(HashMap<String, String> attachmentKey) {
        messageHandler.postDelayed(new Runnable() {
            public void run() {
                composeFooterFragment.addAttachmentToAdapter(attachmentKey);
            }
        }, 400);
    }

    String getComponentId(String componentType) {
        if (componentType != null) {
            if (componentType.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_IMAGE)) {
                return "image_" + System.currentTimeMillis();
            } else if (componentType.equalsIgnoreCase(KoreMedia.MEDIA_TYPE_VIDEO)) {
                return "video_" + System.currentTimeMillis();
            } else {
                return "doc_" + System.currentTimeMillis();
            }
        }
        return "";
    }

    void getBrandingDetails() {
        Call<BotActiveThemeModel> getBankingConfigService = BrandingRestBuilder.getRestAPI().getBrandingNewDetails(SDKConfiguration.Client.bot_id, "bearer " + SocketWrapper.getInstance(BotChatActivity.this).getAccessToken(), SDKConfiguration.Client.tenant_id, "published", "1", "en_US", SDKConfiguration.Client.bot_id);
        getBankingConfigService.enqueue(new Callback<BotActiveThemeModel>() {
            @Override
            public void onResponse(@NonNull Call<BotActiveThemeModel> call, @NonNull Response<BotActiveThemeModel> response) {
                if (response.isSuccessful()) {
                    botActiveThemeModel = response.body();

                    if (botActiveThemeModel != null && botActiveThemeModel.getV3() != null) {
                        botOptionsModel = botActiveThemeModel.getV3();

                        setButtonBranding(botOptionsModel);

                        if (botOptionsModel != null) {
                            if (botOptionsModel.getChat_bubble() != null && !StringUtils.isNullOrEmpty(botOptionsModel.getChat_bubble().getStyle())) {
                                sharedPreferences.edit().putString(BundleConstants.BUBBLE_STYLE, botOptionsModel.getChat_bubble().getStyle()).apply();
                            }

                            if (botOptionsModel.getBody() != null && !StringUtils.isNullOrEmpty(botOptionsModel.getBody().getBubble_style())) {
                                sharedPreferences.edit().putString(BundleConstants.BUBBLE_STYLE, botOptionsModel.getBody().getBubble_style()).apply();
                            }

                            if (botOptionsModel.getGeneral() != null && botOptionsModel.getGeneral().getColors() != null && botOptionsModel.getGeneral().getColors().isUseColorPaletteOnly()) {
                                botOptionsModel.getHeader().setBg_color(botOptionsModel.getGeneral().getColors().getSecondary());
                                botOptionsModel.getFooter().setBg_color(botOptionsModel.getGeneral().getColors().getSecondary());
                                botOptionsModel.getFooter().getCompose_bar().setOutline_color(botOptionsModel.getGeneral().getColors().getPrimary());
                                botOptionsModel.getFooter().getCompose_bar().setInline_color(botOptionsModel.getGeneral().getColors().getSecondary_text());
                                botOptionsModel.getHeader().setIcons_color(botOptionsModel.getGeneral().getColors().getPrimary());
                                botOptionsModel.getFooter().setIcons_color(botOptionsModel.getGeneral().getColors().getPrimary());
                                botOptionsModel.getHeader().getTitle().setColor(botOptionsModel.getGeneral().getColors().getPrimary());
                                botOptionsModel.getHeader().getSub_title().setColor(botOptionsModel.getGeneral().getColors().getPrimary());
                            }

                            if (botOptionsModel.getWelcome_screen() != null) {
                                if (botOptionsModel.getWelcome_screen().isShow()) showWelcomeDialog();
                                else {
                                    closeProgressDialogue();
                                    rlChatWindow.setVisibility(VISIBLE);
                                }
                            } else {
                                closeProgressDialogue();
                                rlChatWindow.setVisibility(VISIBLE);
                            }
                        }

                        if (composeFooterFragment != null) {
                            composeFooterFragment.setBotBrandingModel(botOptionsModel);
                        }

                        if (botContentFragment != null) {
                            botContentFragment.setBotBrandingModel(botOptionsModel);
                        }

                        if (isItFirstConnect) {
                            botClient.sendMessage("BotNotifications");
                            isItFirstConnect = false;
                        }
                    }
                } else {
                    if (isItFirstConnect) {
                        botClient.sendMessage("BotNotifications");
                        isItFirstConnect = false;
                    }

                    closeProgressDialogue();
                    rlChatWindow.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BotActiveThemeModel> call, @NonNull Throwable t) {
                LogUtils.e("getBrandingDetails", t.toString());

                if (isItFirstConnect) {
                    botClient.sendMessage("BotNotifications");
                    isItFirstConnect = false;
                }

                closeProgressDialogue();
                rlChatWindow.setVisibility(VISIBLE);
            }
        });
    }

    void sendWebHookMessage(boolean new_session, String msg, ArrayList<HashMap<String, String>> attachments) {
        Call<WebHookResponseDataModel> getBankingConfigService = WebHookRestBuilder.getRestAPI().sendWebHookMessage(SDKConfiguration.Client.webHook_bot_id, "bearer " + jwt, getJsonRequest(new_session, msg, attachments));
        getBankingConfigService.enqueue(new Callback<WebHookResponseDataModel>() {
            @Override
            public void onResponse(@NonNull Call<WebHookResponseDataModel> call, @NonNull Response<WebHookResponseDataModel> response) {
                if (response.isSuccessful()) {
                    webHookResponseDataModel = response.body();
                    taskProgressBar.setVisibility(View.GONE);
                    composeFooterFragment.enableSendButton();
                    updateActionBar();

                    if (webHookResponseDataModel != null && webHookResponseDataModel.getData() != null && webHookResponseDataModel.getData().size() > 0) {
                        for (int i = 0; i < webHookResponseDataModel.getData().size(); i++) {
                            if (webHookResponseDataModel.getData().get(i).getVal() instanceof String)
                                displayMessage(webHookResponseDataModel.getData().get(i).getVal().toString(), webHookResponseDataModel.getData().get(i).getType(), webHookResponseDataModel.getData().get(i).getMessageId());
                            else if (webHookResponseDataModel.getData().get(i).getVal() != null) {
                                try {
                                    String elementsAsString = gson.toJson(webHookResponseDataModel.getData().get(i).getVal());
                                    Type carouselType = new TypeToken<PayloadOuter>() {
                                    }.getType();
                                    PayloadOuter payloadOuter = gson.fromJson(elementsAsString, carouselType);
                                    displayMessage(payloadOuter);
                                } catch (Exception e) {
                                    try {
                                        String elementsAsString = gson.toJson(webHookResponseDataModel.getData().get(i).getVal());
                                        Type carouselType = new TypeToken<PayloadHeaderModel>() {
                                        }.getType();
                                        PayloadHeaderModel payloadOuter = gson.fromJson(elementsAsString, carouselType);
                                        if (payloadOuter != null && payloadOuter.getPayload() != null) {
                                            displayMessage(payloadOuter.getPayload().getTemplate_type(), BotResponse.COMPONENT_TYPE_TEXT, webHookResponseDataModel.getData().get(i).getMessageId());
                                        }
                                    } catch (Exception ex) {
                                        String elementsAsString = gson.toJson(webHookResponseDataModel.getData().get(i).getVal());
                                        displayMessage(elementsAsString, BotResponse.COMPONENT_TYPE_TEXT, webHookResponseDataModel.getData().get(i).getMessageId());
                                    }

                                }
                            }
                        }

                        if (!StringUtils.isNullOrEmpty(webHookResponseDataModel.getPollId()))
                            startSendingPo11(webHookResponseDataModel.getPollId());
                    }
                } else {
                    taskProgressBar.setVisibility(View.GONE);
                    composeFooterFragment.enableSendButton();
                    updateActionBar();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WebHookResponseDataModel> call, @NonNull Throwable t) {
            }
        });
    }

    private void getWebHookMeta() {
        Call<BotMetaModel> getBankingConfigService = WebHookRestBuilder.getRestAPI().getWebHookBotMeta("bearer " + jwt, SDKConfiguration.Client.webHook_bot_id);
        getBankingConfigService.enqueue(new Callback<BotMetaModel>() {
            @Override
            public void onResponse(@NonNull Call<BotMetaModel> call, @NonNull Response<BotMetaModel> response) {
                if (response.isSuccessful()) {
                    botMetaModel = response.body();
                    if (botMetaModel != null) SDKConfiguration.BubbleColors.setIcon_url(botMetaModel.getIcon());
                    sendWebHookMessage(true, "ON_CONNECT", null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BotMetaModel> call, @NonNull Throwable t) {
            }
        });
    }

    void postPollingData(String pollId) {
        Call<WebHookResponseDataModel> getBankingConfigService = WebHookRestBuilder.getRestAPI().getPollIdData("bearer " + jwt, SDKConfiguration.Client.webHook_bot_id, pollId);
        getBankingConfigService.enqueue(new Callback<WebHookResponseDataModel>() {
            @Override
            public void onResponse(@NonNull Call<WebHookResponseDataModel> call, @NonNull Response<WebHookResponseDataModel> response) {
                if (response.isSuccessful()) {
                    webHookResponseDataModel = response.body();
                    taskProgressBar.setVisibility(View.GONE);
                    composeFooterFragment.enableSendButton();
                    updateActionBar();
//                    getBrandingDetails();

                    if (webHookResponseDataModel != null && webHookResponseDataModel.getData() != null && webHookResponseDataModel.getData().size() > 0) {
                        for (int i = 0; i < webHookResponseDataModel.getData().size(); i++) {
                            if (webHookResponseDataModel.getData().get(i).getVal() instanceof String)
                                displayMessage(webHookResponseDataModel.getData().get(i).getVal().toString(), webHookResponseDataModel.getData().get(i).getType(), webHookResponseDataModel.getData().get(i).getMessageId());
                        }

                        stopSendingPolling();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<WebHookResponseDataModel> call, @NonNull Throwable t) {
            }
        });
    }

    private void addSentMessageToChat(String message) {
        //Update the bot content list with the send message
        RestResponse.BotMessage botMessage = new RestResponse.BotMessage(message);
        RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
        botPayLoad.setMessage(botMessage);
        BotInfoModel botInfo = new BotInfoModel(SDKConfiguration.Client.bot_name, SDKConfiguration.Client.bot_id, null);
        botPayLoad.setBotInfo(botInfo);
        Gson gson = new Gson();
        String jsonPayload = gson.toJson(botPayLoad);

        BotRequest botRequest = gson.fromJson(jsonPayload, BotRequest.class);
        botRequest.setCreatedOn(DateUtils.isoFormatter.format(new Date()));
        sListener.onMessage(new SocketDataTransferModel(BaseSocketConnectionManager.EVENT_TYPE.TYPE_MESSAGE_UPDATE, message, botRequest, false));
    }

    private HashMap<String, Object> getJsonRequest(boolean new_session, String msg, ArrayList<HashMap<String, String>> attachments) {
        HashMap<String, Object> hsh = new HashMap<>();

        try {
            WebHookRequestModel webHookRequestModel = new WebHookRequestModel();
            WebHookRequestModel.Session session = new WebHookRequestModel.Session();
            session.setNewSession(new_session);
            webHookRequestModel.setSession(session);
            hsh.put("session", session);

            WebHookRequestModel.Message message = new WebHookRequestModel.Message();
            message.setVal(msg);

            if (new_session) message.setType("event");
            else message.setType("text");

            webHookRequestModel.setMessage(message);
            hsh.put("message", message);

            WebHookRequestModel.From from = new WebHookRequestModel.From();
            from.setId(SDKConfiguration.Client.webHook_identity);
            WebHookRequestModel.From.WebHookUserInfo userInfo = new WebHookRequestModel.From.WebHookUserInfo();
            userInfo.setFirstName("");
            userInfo.setLastName("");
            userInfo.setEmail("");
            from.setUserInfo(userInfo);
            webHookRequestModel.setFrom(from);
            hsh.put("from", from);

            WebHookRequestModel.To to = new WebHookRequestModel.To();
            to.setId("Kore.ai");
            WebHookRequestModel.To.GroupInfo groupInfo = new WebHookRequestModel.To.GroupInfo();
            groupInfo.setId("");
            groupInfo.setName("");
            to.setGroupInfo(groupInfo);
            webHookRequestModel.setTo(to);
            hsh.put("to", to);

            WebHookRequestModel.Token token = new WebHookRequestModel.Token();
            hsh.put("token", token);

            if (attachments != null && attachments.size() > 0) hsh.put("attachments", attachments);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return hsh;
    }

    void startSendingPo11(String pollId) {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, poll_delay);
                postPollingData(pollId);
            }
        }, poll_delay);
    }

    public String getUniqueDeviceId(Context context) {
        if (uniqueID == null) {
            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {
                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.apply();
            }
        }
        return uniqueID;
    }

    void stopSendingPolling() {
        handler.removeCallbacks(runnable);
    }

    void showProgressDialogue(String msg) {
        progressBar = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.progress_bar_dialog, null);
        progressBar.setContentView(view);
        Objects.requireNonNull(progressBar.getWindow()).setLayout((int) (250 * dp1), (int) (100 * dp1));
        progressBar.setCancelable(false);
        progressBar.setCanceledOnTouchOutside(false);
        progressBar.show();
    }

    void showWelcomeDialog() {
        RelativeLayout llHeaderLayout = null;
        LinearLayout llOuterHeader, llStartConversation, llBottomPower, llStarterLogo;
        AutoExpandListView lvPromotions;
        ConstraintLayout clStarter;
        ScrollView svWelcome;
        ImageView ivStarterLogo;

        welcomeDialog = new Dialog(this, R.style.MyDialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.welcome_screen, null);
        llOuterHeader = view.findViewById(R.id.llOuterHeader);
        llStartConversation = view.findViewById(R.id.llStartConversation);
        lvPromotions = view.findViewById(R.id.lvPromotions);
        clStarter = view.findViewById(R.id.clStarter);
        llBottomPower = view.findViewById(R.id.llBottomPower);
        svWelcome = view.findViewById(R.id.svWelcome);
        llStarterLogo = view.findViewById(R.id.llStarterLogo);
        ivStarterLogo = view.findViewById(R.id.ivStarterLogo);

        welcomeDialog.setContentView(view);
        Objects.requireNonNull(welcomeDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        welcomeDialog.setCancelable(false);
        welcomeDialog.setCanceledOnTouchOutside(false);

        llStartConversation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWelcomeDialog();
            }
        });

        if (botOptionsModel != null && botOptionsModel.getWelcome_screen() != null) {
            BrandingWelcomeModel welcomeModel = botOptionsModel.getWelcome_screen();

            if (!StringUtils.isNullOrEmpty(welcomeModel.getLayout())) {
                if (welcomeModel.getLayout().equalsIgnoreCase(BundleUtils.LAYOUT_LARGE)) {
                    llHeaderLayout = (RelativeLayout) View.inflate(BotChatActivity.this, R.layout.welcome_header_2, null);
                } else if (welcomeModel.getLayout().equalsIgnoreCase(BundleUtils.LAYOUT_MEDIUM)) {
                    llHeaderLayout = (RelativeLayout) View.inflate(BotChatActivity.this, R.layout.welcome_header_3, null);
                } else llHeaderLayout = (RelativeLayout) View.inflate(BotChatActivity.this, R.layout.welcome_header, null);
            }

            if (llHeaderLayout != null) {
                RelativeLayout rlHeader = llHeaderLayout.findViewById(R.id.rlHeader);
                TextView tvWelcomeHeader = llHeaderLayout.findViewById(R.id.tvWelcomeHeader);
                TextView tvWelcomeTitle = llHeaderLayout.findViewById(R.id.tvWelcomeTitle);
                TextView tvWelcomeDescription = llHeaderLayout.findViewById(R.id.tvWelcomeDescription);
                ImageView ivWelcomeLogo = llHeaderLayout.findViewById(R.id.ivWelcomeLogo);
                ConstraintLayout llInnerHeader = llHeaderLayout.findViewById(R.id.llInnerHeader);

                if (!StringUtils.isNullOrEmpty(welcomeModel.getTitle().getName())) {
                    tvWelcomeHeader.setText(welcomeModel.getTitle().getName());
                }

                if (!StringUtils.isNullOrEmpty(welcomeModel.getSub_title().getName()))
                    tvWelcomeTitle.setText(welcomeModel.getSub_title().getName());

                if (!StringUtils.isNullOrEmpty(welcomeModel.getNote().getName()))
                    tvWelcomeDescription.setText(welcomeModel.getNote().getName());

                if (welcomeModel.getBackground() != null) {
                    if (!StringUtils.isNullOrEmpty(welcomeModel.getBackground().getType())) {
                        if (welcomeModel.getBackground().getType().equalsIgnoreCase(BundleUtils.COLOR)) {
                            llInnerHeader.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(welcomeModel.getBackground().getColor())));
                        } else if (!StringUtils.isNullOrEmpty(welcomeModel.getBackground().getImg())) {
                            Glide.with(BotChatActivity.this).load(welcomeModel.getBackground().getImg()).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@androidx.annotation.NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    llInnerHeader.setBackground(null);
                                    rlHeader.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                        }
                    }
                }

                if (welcomeModel.getLogo() != null && !StringUtils.isNullOrEmpty(welcomeModel.getLogo().getLogo_url())) {
                    Picasso.get().load(welcomeModel.getLogo().getLogo_url()).transform(new RoundedCornersTransform()).into(ivWelcomeLogo);
                }

                if (botOptionsModel.getHeader() != null && botOptionsModel.getHeader().getIcon() != null) {
                    if (botOptionsModel.getHeader().getIcon().getType().equalsIgnoreCase(BundleUtils.CUSTOM)) {
                        llStarterLogo.setBackgroundResource(0);
                        Picasso.get().load(botOptionsModel.getHeader().getIcon().getIcon_url()).transform(new RoundedCornersTransform()).into(ivStarterLogo);
                        ivStarterLogo.setLayoutParams(new LinearLayout.LayoutParams((int) (40 * dp1), (int) (40 * dp1)));
                    } else {
                        switch (botOptionsModel.getHeader().getIcon().getIcon_url()) {
                            case "icon-1":
                                ivStarterLogo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_icon_1, getTheme()));
                                break;
                            case "icon-2":
                                ivStarterLogo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_icon_2, getTheme()));
                                break;
                            case "icon-3":
                                ivStarterLogo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_icon_3, getTheme()));
                                break;
                            case "icon-4":
                                ivStarterLogo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_icon_4, getTheme()));
                                break;
                        }
                    }
                }

                if (welcomeModel.getTop_fonts() != null) {
                    tvWelcomeHeader.setTextColor(Color.parseColor(welcomeModel.getTop_fonts().getColor()));
                    tvWelcomeTitle.setTextColor(Color.parseColor(welcomeModel.getTop_fonts().getColor()));
                    tvWelcomeDescription.setTextColor(Color.parseColor(welcomeModel.getTop_fonts().getColor()));
                    ivStarterLogo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(welcomeModel.getTop_fonts().getColor())));
                }

                if (botOptionsModel.getGeneral() != null && botOptionsModel.getGeneral().getColors() != null && botOptionsModel.getGeneral().getColors().isUseColorPaletteOnly()) {
                    tvWelcomeHeader.setTextColor(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary_text()));
                    tvWelcomeTitle.setTextColor(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary_text()));
                    tvWelcomeDescription.setTextColor(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary_text()));
                    llStarterLogo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(botOptionsModel.getGeneral().getColors().getPrimary())));
                    ivStarterLogo.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary_text())));
                    llInnerHeader.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(botOptionsModel.getGeneral().getColors().getPrimary())));

                    if (!StringUtils.isNullOrEmpty(botOptionsModel.getGeneral().getColors().getSecondary()))
                        svWelcome.setBackgroundColor(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary()));
                }

                if (welcomeModel.getBottom_background() != null && !StringUtils.isNullOrEmpty(welcomeModel.getBottom_background().getColor())) {
                    llBottomPower.setBackgroundColor(Color.parseColor(welcomeModel.getBottom_background().getColor()));
                }
            }

            RecyclerView rvStarterButtons = view.findViewById(R.id.rvStarterButtons);
            HeightAdjustableViewPager hvpLinks = view.findViewById(R.id.hvpLinks);
            RecyclerView rvLinks = view.findViewById(R.id.rvLinks);
            TextView tvStarterTitle = view.findViewById(R.id.tvStarterTitle);
            TextView tvStarterDesc = view.findViewById(R.id.tvStarterDesc);
            TextView tvStartConversation = view.findViewById(R.id.tvStartConversation);
            RelativeLayout rlLinks = view.findViewById(R.id.rlLinks);

            rvLinks.setLayoutManager(new LinearLayoutManager(BotChatActivity.this, LinearLayoutManager.VERTICAL, false));

            llOuterHeader.addView(llHeaderLayout);

            if (welcomeModel.getStarter_box() != null) {
                if (welcomeModel.getStarter_box().isShow()) {
                    clStarter.setVisibility(View.VISIBLE);

                    if (!StringUtils.isNullOrEmpty(welcomeModel.getStarter_box().getTitle())) {
                        tvStarterTitle.setVisibility(View.VISIBLE);

                        tvStarterTitle.setText(welcomeModel.getStarter_box().getTitle());
                        tvStartConversation.setText(welcomeModel.getStarter_box().getTitle());
                    }

                    if (!StringUtils.isNullOrEmpty(welcomeModel.getStarter_box().getSub_text())) {
                        tvStarterDesc.setVisibility(View.VISIBLE);
                        tvStarterDesc.setText(welcomeModel.getStarter_box().getSub_text());
                    }

                    if (welcomeModel.getStarter_box().getStart_conv_button() != null) {
                        if (!StringUtils.isNullOrEmpty(welcomeModel.getStarter_box().getStart_conv_button().getColor())) {
                            StateListDrawable gradientDrawable = (StateListDrawable) llStartConversation.getBackground();
                            gradientDrawable.setTint(Color.parseColor(welcomeModel.getStarter_box().getStart_conv_button().getColor()));
                            if (botOptionsModel.getGeneral() != null && botOptionsModel.getGeneral().getColors() != null && botOptionsModel.getGeneral().getColors().isUseColorPaletteOnly()) {
                                gradientDrawable.setTint(Color.parseColor(botOptionsModel.getGeneral().getColors().getPrimary()));
                            }

                            llStartConversation.setBackground(gradientDrawable);
                        }
                    }

                    if (welcomeModel.getStarter_box().getStart_conv_text() != null) {
                        if (!StringUtils.isNullOrEmpty(welcomeModel.getStarter_box().getStart_conv_text().getColor())) {
                            tvStartConversation.setTextColor(Color.parseColor(welcomeModel.getStarter_box().getStart_conv_text().getColor()));

                            if (botOptionsModel.getGeneral() != null && botOptionsModel.getGeneral().getColors() != null && botOptionsModel.getGeneral().getColors().isUseColorPaletteOnly() && !StringUtils.isNullOrEmpty(botOptionsModel.getGeneral().getColors().getSecondary_text())) {
                                tvStartConversation.setTextColor(Color.parseColor(botOptionsModel.getGeneral().getColors().getSecondary_text()));
                            }
                        }
                    }

                    if (welcomeModel.getStarter_box().getQuick_start_buttons() != null && welcomeModel.getStarter_box().getQuick_start_buttons().getButtons() != null && welcomeModel.getStarter_box().getQuick_start_buttons().getButtons().size() > 0) {
                        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(BotChatActivity.this);

                        if (!StringUtils.isNullOrEmpty(welcomeModel.getStarter_box().getQuick_start_buttons().getStyle())) {
                            if (welcomeModel.getStarter_box().getQuick_start_buttons().getStyle().equalsIgnoreCase(BotResponse.TEMPLATE_TYPE_LIST)) {
                                layoutManager.setFlexDirection(FlexDirection.COLUMN);
                                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                                rvStarterButtons.setLayoutManager(layoutManager);

                                WelcomeStarterButtonsAdapter quickRepliesAdapter = new WelcomeStarterButtonsAdapter(BotChatActivity.this, BotResponse.TEMPLATE_TYPE_LIST, !StringUtils.isNullOrEmpty(botOptionsModel.getGeneral().getColors().getSecondary()) ? botOptionsModel.getGeneral().getColors().getSecondary() : "#a7b0be");
                                quickRepliesAdapter.setWelcomeStarterButtonsArrayList(welcomeModel.getStarter_box().getQuick_start_buttons().getButtons());
                                rvStarterButtons.setAdapter(quickRepliesAdapter);
                            } else {
                                layoutManager.setFlexDirection(FlexDirection.ROW);
                                layoutManager.setJustifyContent(JustifyContent.FLEX_START);
                                rvStarterButtons.setLayoutManager(layoutManager);

                                WelcomeStarterButtonsAdapter quickRepliesAdapter = new WelcomeStarterButtonsAdapter(BotChatActivity.this, BotResponse.TEMPLATE_TYPE_CAROUSEL, !StringUtils.isNullOrEmpty(botOptionsModel.getGeneral().getColors().getSecondary()) ? botOptionsModel.getGeneral().getColors().getSecondary() : "#a7b0be");
                                quickRepliesAdapter.setWelcomeStarterButtonsArrayList(welcomeModel.getStarter_box().getQuick_start_buttons().getButtons());
                                quickRepliesAdapter.setComposeFooterInterface(BotChatActivity.this);
                                quickRepliesAdapter.setInvokeGenericWebViewInterface(BotChatActivity.this);
                                rvStarterButtons.setAdapter(quickRepliesAdapter);
                            }
                        }
                    }
                }
            }

            if (welcomeModel.getPromotional_content().getPromotions() != null && welcomeModel.getPromotional_content().isShow() && welcomeModel.getPromotional_content().getPromotions().size() > 0) {
                lvPromotions.setVisibility(VISIBLE);
                lvPromotions.setAdapter(new PromotionsAdapter(BotChatActivity.this, welcomeModel.getPromotional_content().getPromotions()));
            }

            if (welcomeModel.getStatic_links() != null) {
                if (welcomeModel.getStarter_box().isShow()) {
                    if (welcomeModel.getStatic_links().getLinks() != null && welcomeModel.getStatic_links().getLinks().size() > 0) {
                        rlLinks.setVisibility(View.VISIBLE);

                        if (!StringUtils.isNullOrEmpty(welcomeModel.getStatic_links().getLayout()) && welcomeModel.getStatic_links().getLayout().equalsIgnoreCase(BotResponse.TEMPLATE_TYPE_CAROUSEL)) {
                            hvpLinks.setVisibility(View.VISIBLE);
                            WelcomeStaticLinksAdapter quickRepliesAdapter = new WelcomeStaticLinksAdapter(BotChatActivity.this, welcomeModel.getStatic_links().getLinks(), !StringUtils.isNullOrEmpty(botOptionsModel.getGeneral().getColors().getSecondary()) ? botOptionsModel.getGeneral().getColors().getSecondary() : "#a7b0be");
                            quickRepliesAdapter.setComposeFooterInterface(BotChatActivity.this);
                            quickRepliesAdapter.setInvokeGenericWebViewInterface(BotChatActivity.this);
                            hvpLinks.setAdapter(quickRepliesAdapter);
                        } else {
                            rvLinks.setVisibility(View.VISIBLE);
                            WelcomeStaticLinkListAdapter welcomeStaticLinkListAdapter = new WelcomeStaticLinkListAdapter(BotChatActivity.this, rvLinks);
                            welcomeStaticLinkListAdapter.setWelcomeStaticLinksArrayList(welcomeModel.getStatic_links().getLinks());
                            welcomeStaticLinkListAdapter.setComposeFooterInterface(BotChatActivity.this);
                            welcomeStaticLinkListAdapter.setInvokeGenericWebViewInterface(BotChatActivity.this);
                            rvLinks.setAdapter(welcomeStaticLinkListAdapter);
                        }
                    } else rlLinks.setVisibility(View.GONE);
                }
            }
        }

        welcomeDialog.show();
        rlChatWindow.setVisibility(VISIBLE);
    }

    void closeWelcomeDialog() {
        if (welcomeDialog != null && welcomeDialog.isShowing()) {
            welcomeDialog.hide();

            closeProgressDialogue();
        }
    }

    void closeProgressDialogue() {
        if (progressBar != null) progressBar.hide();
    }

    void showAlertDialog(EventModel eventModel) {
        Dialog alertDialog = new Dialog(BotChatActivity.this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.incoming_call_layout, null);
        alertDialog.setContentView(dialogView);
        alertDialog.setCancelable(false);

        TextView tvAgentName = dialogView.findViewById(R.id.tvAgentName);
        TextView tvCallType = dialogView.findViewById(R.id.tvTypeOfCall);
        TextView tvCallAccept = dialogView.findViewById(R.id.tvCallAccept);
        TextView tvCallReject = dialogView.findViewById(R.id.tvCallReject);

        tvAgentName.setText(eventModel.getMessage().getFirstName());
        tvCallType.setText(getString(R.string.incoming_audio_call));

        if (eventModel.getMessage().isVideoCall()) tvCallType.setText(getString(R.string.incoming_video_call));

        tvCallAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventModel.getMessage() != null) {
                    eventModel.getMessage().setType("call_agent_webrtc_accepted");
                    botClient.sendMessage(gson.toJsonTree(eventModel.getMessage()));

                    alertDialog.dismiss();
                }
            }
        });

        tvCallReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }
}
