package kore.botssdk.fragment;

import static kore.botssdk.view.viewUtils.DimensionUtil.dp1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kore.botssdk.R;
import kore.botssdk.activity.BotChatActivity;
import kore.botssdk.adapter.ChatAdapter;
import kore.botssdk.listener.BotContentFragmentUpdate;
import kore.botssdk.listener.BotSocketConnectionManager;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.TTSUpdate;
import kore.botssdk.listener.ThemeChangeListener;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotBrandingModel;
import kore.botssdk.models.BotHistory;
import kore.botssdk.models.BotHistoryMessage;
import kore.botssdk.models.BotInfoModel;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.BrandingBodyModel;
import kore.botssdk.models.BrandingHeaderModel;
import kore.botssdk.models.Component;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.models.QuickReplyTemplate;
import kore.botssdk.net.RestBuilder;
import kore.botssdk.net.RestResponse;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.net.SDKConfiguration.Client;
import kore.botssdk.net.WebHookRestBuilder;
import kore.botssdk.retroresponse.ServerBotMsgResponse;
import kore.botssdk.utils.BundleConstants;
import kore.botssdk.utils.BundleUtils;
import kore.botssdk.utils.DateUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.utils.Utils;
import kore.botssdk.view.CircularProfileView;
import kore.botssdk.view.QuickReplyView;
import kore.botssdk.view.viewUtils.RoundedCornersTransform;
import kore.botssdk.views.DotsTextView;
import kore.botssdk.views.LoadingDots;
import kore.botssdk.websocket.SocketWrapper;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Pradeep Mahato on 31-May-16.
 * Copyright (c) 2014 Kore Inc. All rights reserved.
 */
public class BotContentFragment extends Fragment implements BotContentFragmentUpdate {
    RelativeLayout rvChatContent, rlBody;
    RecyclerView botsBubblesListView;
    ChatAdapter botsChatAdapter;
    QuickReplyView quickReplyView;
    LinearLayout botTypingStatusRl;
    LoadingDots ldDots;
    CircularProfileView botTypingStatusIcon;
    DotsTextView typingStatusItemDots;
    ComposeFooterInterface composeFooterInterface;
    InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    ThemeChangeListener themeChangeListener;
    boolean shallShowProfilePic;
    String mChannelIconURL;
    String mBotNameInitials;
    TTSUpdate ttsUpdate;
    int mBotIconId;
    boolean fetching = false;
    boolean hasMore = true;
    TextView headerView, tvTheme1, tvTheme2;
    final Gson gson = new Gson();
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager mLayoutManager;
    int offset = 0;
    SharedPreferences sharedPreferences;
    //Date Range
    long today;
    long nextMonth;
    long janThisYear;
    long decThisYear;
    long oneYearForward;
    Pair<Long, Long> todayPair;
    Pair<Long, Long> nextMonthPair;
    PopupWindow popupWindow;
    View popUpView;
    String jwt;
    RelativeLayout llBotHeader;
    BotBrandingModel botBrandingModel = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = View.inflate(requireActivity(), R.layout.bot_content_layout, null);
        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popUpView = View.inflate(requireActivity(), R.layout.theme_change_layout, null);
        popupWindow = new PopupWindow(popUpView, width, height, focusable);
        getBundleInfo();
        findViews(view);
        findThemeViews(popUpView);
        initializeBotTypingStatus(view, mChannelIconURL);
        setupAdapter();

        if (!Client.isWebHook) loadChatHistory(0, limit);
        else loadWebHookChatHistory(limit);
        return view;
    }

    private void findViews(View view) {
        rvChatContent = view.findViewById(R.id.rvChatContent);
        botsBubblesListView = view.findViewById(R.id.chatContentListView);
        mLayoutManager = (LinearLayoutManager) botsBubblesListView.getLayoutManager();
        headerView = view.findViewById(R.id.filesSectionHeader);
        swipeRefreshLayout = view.findViewById(R.id.swipeContainerChat);
        quickReplyView = view.findViewById(R.id.quick_reply_view);
        llBotHeader = view.findViewById(R.id.llBotHeader);
        rlBody = view.findViewById(R.id.rlBody);

        headerView.setVisibility(View.GONE);
        sharedPreferences = requireActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE);
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!Client.isWebHook) {
                    if (botsChatAdapter != null) loadChatHistory(botsChatAdapter.getItemCount(), limit);
                    else loadChatHistory(0, limit);
                } else {
                    fetching = false;

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }

                }
            }
        });
    }

    public void findThemeViews(View view) {
        tvTheme1 = view.findViewById(R.id.tvTheme1);
        tvTheme2 = view.findViewById(R.id.tvTheme2);

        tvTheme1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                SharedPreferences.Editor editor = requireActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(BotResponse.APPLY_THEME_NAME, BotResponse.THEME_NAME_1);
                editor.apply();

                themeChangeListener.onThemeChangeClicked(BotResponse.THEME_NAME_1);
            }
        });

        tvTheme2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(BotResponse.THEME_NAME, Context.MODE_PRIVATE).edit();
                editor.putString(BotResponse.APPLY_THEME_NAME, BotResponse.THEME_NAME_2);
                editor.apply();

                themeChangeListener.onThemeChangeClicked(BotResponse.THEME_NAME_2);
            }
        });
    }

    public void changeThemeAndLaunch() {
        Intent intent = new Intent(getActivity(), BotChatActivity.class);
        startActivity(intent);
    }

    public void setJwtTokenForWebHook(String jwt) {
        if (!StringUtils.isNullOrEmpty(jwt)) this.jwt = jwt;
    }

    @Override
    public void onResume() {
        super.onResume();
//        chatBgColor = sharedPreferences.getString(BotResponse.WIDGET_BG_COLOR, "#f3f3f5");
//        chatTextColor = sharedPreferences.getString(BotResponse.WIDGET_TXT_COLOR, SDKConfiguration.BubbleColors.leftBubbleSelected);
//        rvChatContent.setBackgroundColor(Color.parseColor(chatBgColor));
//
//        GradientDrawable gradientDrawable = (GradientDrawable) headerView.getBackground();
//        gradientDrawable.setColor(Color.parseColor(chatBgColor));
//        headerView.setTextColor(Color.parseColor(chatTextColor));

    }

    public void changeThemeBackGround(String bgColor, String textColor) {
        if (!StringUtils.isNullOrEmpty(bgColor)) {
            rvChatContent.setBackgroundColor(Color.parseColor(bgColor));
            GradientDrawable gradientDrawable = (GradientDrawable) headerView.getBackground();
            gradientDrawable.setColor(Color.parseColor(bgColor));
        }

        headerView.setTextColor(Color.parseColor(textColor));
    }

    private void setupAdapter() {
        botsChatAdapter = new ChatAdapter(getActivity());
        botsChatAdapter.setComposeFooterInterface(composeFooterInterface);
        botsChatAdapter.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        botsChatAdapter.setActivityContext(getActivity());
        botsBubblesListView.setAdapter(botsChatAdapter);
//        botsChatAdapter.setShallShowProfilePic(shallShowProfilePic);
        // botsBubblesListView.setOnScrollListener(onScrollListener);
//        quickReplyView = new QuickReplyView(getContext());
        quickReplyView.setComposeFooterInterface(composeFooterInterface);
        quickReplyView.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
    }

    public void setTtsUpdate(TTSUpdate ttsUpdate) {
        this.ttsUpdate = ttsUpdate;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public void setThemeChangeInterface(ThemeChangeListener themeChangeListener) {
        this.themeChangeListener = themeChangeListener;
    }

    private void getBundleInfo() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            shallShowProfilePic = bundle.getBoolean(BundleUtils.SHOW_PROFILE_PIC, false);
            mChannelIconURL = bundle.getString(BundleUtils.CHANNEL_ICON_URL);
            mBotNameInitials = bundle.getString(BundleUtils.BOT_NAME_INITIALS, "B");
            mBotIconId = bundle.getInt(BundleUtils.BOT_ICON_ID, -1);
        }
    }

    public void setBotBrandingModel(BotBrandingModel botBrandingModel) {
        this.botBrandingModel = botBrandingModel;

        if (botBrandingModel != null) {
            BrandingHeaderModel headerModel = botBrandingModel.getHeader();

            if (headerModel != null) {
                if (headerModel.getSize().equalsIgnoreCase(BundleUtils.COMPACT))
                    llBotHeader.addView(View.inflate(requireActivity(), R.layout.bot_header, null));
                else if (headerModel.getSize().equalsIgnoreCase(BundleUtils.LAYOUT_LARGE))
                    llBotHeader.addView(View.inflate(requireActivity(), R.layout.bot_header_3, null));
                else llBotHeader.addView(View.inflate(requireActivity(), R.layout.bot_header_2, null));

                if (!StringUtils.isNullOrEmpty(headerModel.getBg_color()))
                    llBotHeader.setBackgroundColor(Color.parseColor(headerModel.getBg_color()));
            } else llBotHeader.addView(View.inflate(requireActivity(), R.layout.bot_header, null));

            TextView tvBotTitle = llBotHeader.findViewById(R.id.tvBotTitle);
            TextView tvBotDesc = llBotHeader.findViewById(R.id.tvBotDesc);
            ImageView ivBotAvatar = llBotHeader.findViewById(R.id.ivBotAvatar);
            LinearLayout llBotAvatar = llBotHeader.findViewById(R.id.llBotAvatar);
            ImageView ivBotHelp = llBotHeader.findViewById(R.id.ivBotHelp);
            ImageView ivBotSupport = llBotHeader.findViewById(R.id.ivBotSupport);
            ImageView ivBotClose = llBotHeader.findViewById(R.id.ivBotClose);
            ImageView ivBotArrowBack = llBotHeader.findViewById(R.id.ivBotArrowBack);

            ivBotAvatar.setVisibility(View.GONE);
            ivBotHelp.setVisibility(View.GONE);
            ivBotSupport.setVisibility(View.GONE);
            ivBotClose.setVisibility(View.GONE);

            ivBotArrowBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    composeFooterInterface.sendWithSomeDelay(BundleUtils.OPEN_WELCOME, "", 0, false);
                }
            });

            if (headerModel != null) {
                if (headerModel.getTitle() != null && !StringUtils.isNullOrEmpty(headerModel.getTitle().getName())) {
                    tvBotTitle.setText(headerModel.getTitle().getName());
                    if (!StringUtils.isNullOrEmpty(headerModel.getTitle().getColor())) {
                        tvBotTitle.setTextColor(Color.parseColor(headerModel.getTitle().getColor()));
                    }
                }

                if (headerModel.getSub_title() != null && !StringUtils.isNullOrEmpty(headerModel.getSub_title().getName())) {
                    tvBotDesc.setText(headerModel.getSub_title().getName());
                    if (!StringUtils.isNullOrEmpty(headerModel.getSub_title().getColor())) {
                        tvBotDesc.setTextColor(Color.parseColor(headerModel.getSub_title().getColor()));
                    }
                }

                if (headerModel.getIcon() != null && headerModel.getIcon().isShow()) {
                    ivBotAvatar.setVisibility(View.VISIBLE);

                    if (headerModel.getIcon().getType().equalsIgnoreCase(BundleUtils.CUSTOM)) {
                        llBotAvatar.setBackgroundResource(0);
                        Picasso.get().load(headerModel.getIcon().getIcon_url()).transform(new RoundedCornersTransform()).into(ivBotAvatar);
                        ivBotAvatar.setLayoutParams(new LinearLayout.LayoutParams((int) (40 * dp1), (int) (40 * dp1)));
                    } else {
                        switch (headerModel.getIcon().getIcon_url()) {
                            case "icon-1":
                                ivBotAvatar.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.ic_icon_1, requireActivity().getTheme()));
                                break;
                            case "icon-2":
                                ivBotAvatar.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.ic_icon_2, requireActivity().getTheme()));
                                break;
                            case "icon-3":
                                ivBotAvatar.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.ic_icon_3, requireActivity().getTheme()));
                                break;
                            case "icon-4":
                                ivBotAvatar.setImageDrawable(ResourcesCompat.getDrawable(requireActivity().getResources(), R.drawable.ic_icon_4, requireActivity().getTheme()));
                                break;
                        }

                        if (botBrandingModel.getGeneral() != null && botBrandingModel.getGeneral().getColors() != null && botBrandingModel.getGeneral().getColors().isUseColorPaletteOnly()) {
                            llBotAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(botBrandingModel.getGeneral().getColors().getPrimary())));
                            ivBotAvatar.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(botBrandingModel.getGeneral().getColors().getSecondary_text())));
                        }
                    }
                }

                if (headerModel.getButtons() != null) {
                    if (headerModel.getButtons().getHelp() != null && headerModel.getButtons().getHelp().isShow()) {
                        ivBotHelp.setVisibility(View.VISIBLE);

                        if (!StringUtils.isNullOrEmpty(headerModel.getIcons_color()))
                            ivBotHelp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(headerModel.getIcons_color())));

                        ivBotHelp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (headerModel.getButtons().getHelp().getAction() != null && !StringUtils.isNullOrEmpty(headerModel.getButtons().getHelp().getAction().getType())) {
                                    if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(headerModel.getButtons().getHelp().getAction().getType()) || BundleConstants.BUTTON_TYPE_URL.equalsIgnoreCase(headerModel.getButtons().getHelp().getAction().getType())) {
                                        invokeGenericWebViewInterface.invokeGenericWebView(headerModel.getButtons().getHelp().getAction().getValue());
                                    } else if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(headerModel.getButtons().getHelp().getAction().getType())) {
                                        if (!StringUtils.isNullOrEmpty(headerModel.getButtons().getHelp().getAction().getValue()))
                                            composeFooterInterface.onSendClick(headerModel.getButtons().getHelp().getAction().getValue(), false);
                                        else if (!StringUtils.isNullOrEmpty(headerModel.getButtons().getHelp().getAction().getTitle())) {
                                            composeFooterInterface.onSendClick(headerModel.getButtons().getHelp().getAction().getTitle(), false);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    if (headerModel.getButtons().getLive_agent() != null && headerModel.getButtons().getLive_agent().isShow()) {
                        ivBotSupport.setVisibility(View.VISIBLE);

                        if (!StringUtils.isNullOrEmpty(headerModel.getIcons_color()))
                            ivBotSupport.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(headerModel.getIcons_color())));

                        ivBotSupport.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (headerModel.getButtons().getLive_agent().getAction() != null && !StringUtils.isNullOrEmpty(headerModel.getButtons().getLive_agent().getAction().getType())) {
                                    if (BundleConstants.BUTTON_TYPE_WEB_URL.equalsIgnoreCase(headerModel.getButtons().getLive_agent().getAction().getType()) || BundleConstants.BUTTON_TYPE_URL.equalsIgnoreCase(headerModel.getButtons().getLive_agent().getAction().getType())) {
                                        invokeGenericWebViewInterface.invokeGenericWebView(headerModel.getButtons().getLive_agent().getAction().getValue());
                                    } else if (BundleConstants.BUTTON_TYPE_POSTBACK.equalsIgnoreCase(headerModel.getButtons().getLive_agent().getAction().getType())) {
                                        if (!StringUtils.isNullOrEmpty(headerModel.getButtons().getLive_agent().getAction().getValue()))
                                            composeFooterInterface.onSendClick(headerModel.getButtons().getLive_agent().getAction().getValue(), false);
                                        else if (!StringUtils.isNullOrEmpty(headerModel.getButtons().getLive_agent().getAction().getTitle())) {
                                            composeFooterInterface.onSendClick(headerModel.getButtons().getLive_agent().getAction().getTitle(), false);
                                        }
                                    }
                                }
                            }
                        });
                    }

                    if (headerModel.getButtons().getClose() != null && headerModel.getButtons().getClose().isShow()) {
                        ivBotClose.setVisibility(View.VISIBLE);

                        if (!StringUtils.isNullOrEmpty(headerModel.getIcons_color()))
                            ivBotClose.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(headerModel.getIcons_color())));

                        ivBotClose.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BotSocketConnectionManager.killInstance();
                                requireActivity().finish();
                            }
                        });
                    }
                }

                if (!StringUtils.isNullOrEmpty(headerModel.getIcons_color()))
                    ivBotArrowBack.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(headerModel.getIcons_color())));

            }

            if (botBrandingModel.getBody() != null) {
                BrandingBodyModel bodyModel = botBrandingModel.getBody();

                if (bodyModel != null) {
                    if (bodyModel.getBackground() != null && !StringUtils.isNullOrEmpty(bodyModel.getBackground().getType())) {
                        if (BundleConstants.COLOR.equalsIgnoreCase(bodyModel.getBackground().getType()) && !StringUtils.isNullOrEmpty(bodyModel.getBackground().getColor())) {
                            rlBody.setBackgroundColor(Color.parseColor(bodyModel.getBackground().getColor()));
                        } else if (!StringUtils.isNullOrEmpty(bodyModel.getBackground().getImg())) {
                            Glide.with(requireActivity()).load(bodyModel.getBackground().getImg()).into(new CustomTarget<Drawable>() {
                                @Override
                                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                    rlBody.setBackground(resource);
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                }
                            });
                        }
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (bodyModel.getTime_stamp() != null) {
                        if (!StringUtils.isNullOrEmpty(bodyModel.getTime_stamp().getColor())) {
                            editor.putString(BotResponse.TIME_STAMP_TXT_COLOR, bodyModel.getTime_stamp().getColor());
                        }

                        Client.timeStampBottom = !bodyModel.getTime_stamp().getPosition().equalsIgnoreCase(BundleUtils.TOP);
                        SDKConfiguration.setTimeStampsRequired(bodyModel.getTime_stamp().isShow());
                    }

                    editor.apply();
                }
            }
        }
    }

    public void showTypingStatus(BotResponse botResponse) {
        if (botTypingStatusRl != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            botTypingStatusRl.setVisibility(View.VISIBLE);
            if (StringUtils.isNullOrEmpty(mChannelIconURL) && !StringUtils.isNullOrEmpty(botResponse.getIcon())) {
                mChannelIconURL = botResponse.getIcon();
                botTypingStatusIcon.populateLayout(mBotNameInitials, mChannelIconURL, null, -1, Color.parseColor(SDKConfiguration.BubbleColors.quickReplyColor), true);
            }
        }
    }

    public void setQuickRepliesIntoFooter(BotResponse botResponse) {
        ArrayList<QuickReplyTemplate> quickReplyTemplates = getQuickReplies(botResponse);
        quickReplyView.populateQuickReplyView(quickReplyTemplates);
    }

    public void showCalendarIntoFooter(BotResponse botResponse) {
        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter.getPayload();
                    if (payInner != null && BotResponse.TEMPLATE_TYPE_DATE.equalsIgnoreCase(payInner.getTemplate_type())) {
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(MaterialDatePicker.todayInUtcMilliseconds());
                        int strYear = cal.get(Calendar.YEAR);
                        int strMonth = cal.get(Calendar.MONTH);
                        int strDay = cal.get(Calendar.DAY_OF_MONTH);
                        String minDate = strMonth + "-" + strDay + "-" + strYear;

                        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
//                        builder.setCalendarConstraints(minRange(minDate, payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);

                        try {
                            MaterialDatePicker<Long> picker = builder.build();
                            picker.show(getFragmentManager(), picker.toString());

                            picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
                                @Override
                                public void onPositiveButtonClick(Long selection) {
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(selection);
                                    int stYear = calendar.get(Calendar.YEAR);
                                    int stMonth = calendar.get(Calendar.MONTH);
                                    int stDay = calendar.get(Calendar.DAY_OF_MONTH);

                                    String formatedDate = "";
                                    formatedDate = ((stMonth + 1) > 9 ? (stMonth + 1) : "0" + (stMonth + 1)) + "-" + (stDay > 9 ? stDay : "0" + stDay) + "-" + stYear;

                                    composeFooterInterface.onSendClick(formatedDate, false);
                                }
                            });
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    } else if (payInner != null && BotResponse.TEMPLATE_TYPE_DATE_RANGE.equalsIgnoreCase(payInner.getTemplate_type())) {
                        initSettings();
                        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
                        builder.setTitleText(payInner.getTitle());
                        builder.setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR);
//                        builder.setCalendarConstraints(limitRange(payInner.getEndDate(), payInner.getFormat()).build());
                        builder.setTheme(R.style.MyMaterialCalendarTheme);

                        try {
                            MaterialDatePicker<Pair<Long, Long>> picker = builder.build();
                            picker.show(getFragmentManager(), picker.toString());
                            picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                                @Override
                                public void onPositiveButtonClick(Pair<Long, Long> selection) {
                                    Long startDate = selection.first;
                                    Long endDate = selection.second;

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTimeInMillis(startDate);
                                    int strYear = cal.get(Calendar.YEAR);
                                    int strMonth = cal.get(Calendar.MONTH);
                                    int strDay = cal.get(Calendar.DAY_OF_MONTH);

                                    cal.setTimeInMillis(endDate);
                                    int endYear = cal.get(Calendar.YEAR);
                                    int endMonth = cal.get(Calendar.MONTH);
                                    int endDay = cal.get(Calendar.DAY_OF_MONTH);

                                    String formatedDate = "";
                                    formatedDate = DateUtils.getMonthName(strMonth) + " " + strDay + DateUtils.getDayOfMonthSuffix(strDay) + ", " + strYear;
                                    formatedDate = formatedDate + " to " + DateUtils.getMonthName(endMonth) + " " + endDay + DateUtils.getDayOfMonthSuffix(endDay) + ", " + endYear;


                                    composeFooterInterface.onSendClick(formatedDate, false);
                                }
                            });
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
            return typedValue.data;
        }
        throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
    }

    private ArrayList<QuickReplyTemplate> getQuickReplies(BotResponse botResponse) {

        ArrayList<QuickReplyTemplate> quickReplyTemplates = null;

        if (botResponse != null && botResponse.getMessage() != null && !botResponse.getMessage().isEmpty()) {
            ComponentModel compModel = botResponse.getMessage().get(0).getComponent();
            if (compModel != null) {
                String compType = compModel.getType();
                if (BotResponse.COMPONENT_TYPE_TEMPLATE.equalsIgnoreCase(compType)) {
                    PayloadOuter payOuter = compModel.getPayload();
                    PayloadInner payInner = payOuter.getPayload();
                    if (payInner != null && BotResponse.TEMPLATE_TYPE_QUICK_REPLIES.equalsIgnoreCase(payInner.getTemplate_type())) {
                        quickReplyTemplates = payInner.getQuick_replies();
                    }
                }
            }
        }

        return quickReplyTemplates;
    }

    public void addMessageToBotChatAdapter(BotResponse botResponse) {
        botsChatAdapter.addBaseBotMessage(botResponse);
        botTypingStatusRl.setVisibility(View.GONE);
        botsBubblesListView.smoothScrollToPosition(botsChatAdapter.getItemCount());
    }

    protected void initializeBotTypingStatus(View view, String mChannelIconURL) {
        botTypingStatusRl = view.findViewById(R.id.botTypingStatus);
        botTypingStatusIcon = view.findViewById(R.id.typing_status_item_cpv);
        ldDots = view.findViewById(R.id.ldDots);
        botTypingStatusIcon.populateLayout(mBotNameInitials, mChannelIconURL, null, mBotIconId, Color.parseColor(SDKConfiguration.BubbleColors.quickReplyColor), true);
    }

    private void scrollToBottom() {
        final int count = botsChatAdapter.getItemCount();
        botsBubblesListView.post(new Runnable() {
            @Override
            public void run() {
                botsBubblesListView.smoothScrollToPosition(count - 1);
            }
        });
    }


    private final int limit = 10;

    public void addMessagesToBotChatAdapter(ArrayList<BaseBotMessage> list, boolean scrollToBottom) {
        botsChatAdapter.addBaseBotMessages(list);
        if (scrollToBottom) {
            scrollToBottom();
        }
    }

    public void updateContentListOnSend(BotRequest botRequest) {
        if (botRequest.getMessage() != null) {
            if (botsChatAdapter != null) {
                botsChatAdapter.addBaseBotMessage(botRequest);
                quickReplyView.populateQuickReplyView(null);
                scrollToBottom();
            }
        }
    }


    private Observable<ServerBotMsgResponse> getHistoryRequest(final int _offset, final int limit) {
        return Observable.create(new ObservableOnSubscribe<ServerBotMsgResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ServerBotMsgResponse> emitter) throws Exception {
                try {
                    ServerBotMsgResponse re = new ServerBotMsgResponse();

                    Call<BotHistory> _resp = RestBuilder.getRestAPI().getBotHistory("bearer " + SocketWrapper.getInstance(getActivity().getApplicationContext()).getAccessToken(), Client.bot_id, limit, _offset, true);
                    Response<BotHistory> rBody = _resp.execute();
                    BotHistory history = rBody.body();

                    if (rBody.isSuccessful()) {
                        List<BotHistoryMessage> messages = history.getMessages();
                        ArrayList<BaseBotMessage> msgs = null;
                        if (messages != null && messages.size() > 0) {
                            msgs = new ArrayList<>();
                            for (int index = 0; index < messages.size(); index++) {
                                BotHistoryMessage msg = messages.get(index);
                                if (msg.getType().equals(BotResponse.MESSAGE_TYPE_OUTGOING)) {
                                    List<Component> components = msg.getComponents();
                                    String data = components.get(0).getData().getText();
                                    try {
                                        PayloadOuter outer = gson.fromJson(data, PayloadOuter.class);
                                        BotResponse r = Utils.buildBotMessage(outer, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    } catch (com.google.gson.JsonSyntaxException ex) {
                                        BotResponse r = Utils.buildBotMessage(data, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    }
                                } else {
                                    try {
                                        String message = msg.getComponents().get(0).getData().getText();
                                        RestResponse.BotMessage botMessage = new RestResponse.BotMessage(message);
                                        RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
                                        botPayLoad.setMessage(botMessage);
                                        BotInfoModel botInfo = new BotInfoModel(Client.bot_name, Client.bot_id, null);
                                        botPayLoad.setBotInfo(botInfo);
                                        Gson gson = new Gson();
                                        String jsonPayload = gson.toJson(botPayLoad);

                                        BotRequest botRequest = gson.fromJson(jsonPayload, BotRequest.class);
                                        long cTime = DateUtils.isoFormatter.parse(msg.getCreatedOn()).getTime() + TimeZone.getDefault().getRawOffset();
                                        String createdTime = DateUtils.isoFormatter.format(new Date(cTime));
                                        botRequest.setCreatedOn(createdTime);
                                        msgs.add(botRequest);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            re.setBotMessages(msgs);
                            re.setOriginalSize(messages != null ? messages.size() : 0);
                        }
                    }

                    emitter.onNext(re);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }

    private Observable<ServerBotMsgResponse> getWebHookHistoryRequest(final int limit) {
        return Observable.create(new ObservableOnSubscribe<ServerBotMsgResponse>() {
            @Override
            public void subscribe(ObservableEmitter<ServerBotMsgResponse> emitter) throws Exception {
                try {
                    ServerBotMsgResponse re = new ServerBotMsgResponse();

                    Call<BotHistory> _resp = WebHookRestBuilder.getRestAPI().getWebHookBotHistory("bearer " + jwt, Client.webHook_bot_id, Client.webHook_bot_id, limit);
                    Response<BotHistory> rBody = _resp.execute();
                    BotHistory history = rBody.body();

                    if (rBody.isSuccessful()) {
                        List<BotHistoryMessage> messages = history.getMessages();
                        ArrayList<BaseBotMessage> msgs = null;
                        if (messages != null && messages.size() > 0) {
                            msgs = new ArrayList<>();
                            for (int index = 0; index < messages.size(); index++) {
                                BotHistoryMessage msg = messages.get(index);
                                if (msg.getType().equals(BotResponse.MESSAGE_TYPE_OUTGOING)) {
                                    List<Component> components = msg.getComponents();
                                    String data = components.get(0).getData().getText();
                                    try {
                                        PayloadOuter outer = gson.fromJson(data, PayloadOuter.class);
                                        BotResponse r = Utils.buildBotMessage(outer, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    } catch (com.google.gson.JsonSyntaxException ex) {
                                        BotResponse r = Utils.buildBotMessage(data, msg.getBotId(), Client.bot_name, msg.getCreatedOn(), msg.getId());
                                        r.setType(msg.getType());
                                        msgs.add(r);
                                    }
                                } else {
                                    try {
                                        String message = msg.getComponents().get(0).getData().getText();
                                        RestResponse.BotMessage botMessage = new RestResponse.BotMessage(message);
                                        RestResponse.BotPayLoad botPayLoad = new RestResponse.BotPayLoad();
                                        botPayLoad.setMessage(botMessage);
                                        BotInfoModel botInfo = new BotInfoModel(Client.bot_name, Client.bot_id, null);
                                        botPayLoad.setBotInfo(botInfo);
                                        Gson gson = new Gson();
                                        String jsonPayload = gson.toJson(botPayLoad);

                                        BotRequest botRequest = gson.fromJson(jsonPayload, BotRequest.class);
                                        long cTime = DateUtils.isoFormatter.parse(msg.getCreatedOn()).getTime() + TimeZone.getDefault().getRawOffset();
                                        String createdTime = DateUtils.isoFormatter.format(new Date(cTime));
                                        botRequest.setCreatedOn(createdTime);
                                        msgs.add(botRequest);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            re.setBotMessages(msgs);
                            re.setOriginalSize(messages != null ? messages.size() : 0);
                        }
                    }

                    emitter.onNext(re);
                    emitter.onComplete();
                } catch (Exception e) {
                    emitter.onError(e);
                }
            }
        });
    }

    private void initSettings() {
        today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar calendar = getClearedUtc();
        calendar.setTimeInMillis(today);
        calendar.roll(Calendar.MONTH, 1);
        nextMonth = calendar.getTimeInMillis();

        calendar.setTimeInMillis(today);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        janThisYear = calendar.getTimeInMillis();
        calendar.setTimeInMillis(today);
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        decThisYear = calendar.getTimeInMillis();

        calendar.setTimeInMillis(today);
        calendar.roll(Calendar.YEAR, 1);
        oneYearForward = calendar.getTimeInMillis();

        todayPair = new Pair<>(today, today);
        nextMonthPair = new Pair<>(nextMonth, nextMonth);
    }

    private static Calendar getClearedUtc() {
        Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        utc.clear();
        return utc;
    }

    private void loadWebHookChatHistory(final int limit) {
        if (fetching) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        fetching = true;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        getWebHookHistoryRequest(limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ServerBotMsgResponse>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ServerBotMsgResponse re) {
                fetching = false;

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                ArrayList<BaseBotMessage> list = null;
                if (re != null) {
                    list = re.getBotMessages();
                    offset = re.getOriginalSize();
                }

                if (list != null && list.size() > 0) {
                    addMessagesToBotChatAdapter(list, true);
                }

                if ((list == null || list.size() < limit) && offset != 0) {
                    hasMore = false;
                }
            }

            @Override
            public void onError(Throwable e) {
                fetching = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onComplete() {
                fetching = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    void loadChatHistory(final int _offset, final int limit) {
        if (fetching) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        fetching = true;
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        getHistoryRequest(_offset, limit).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ServerBotMsgResponse>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ServerBotMsgResponse re) {
                fetching = false;

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                ArrayList<BaseBotMessage> list = null;
                if (re != null) {
                    list = re.getBotMessages();
                    offset = _offset + re.getOriginalSize();
                }

                if (list != null && list.size() > 0) {
                    addMessagesToBotChatAdapter(list, offset == 0);
                }

                if ((list == null || list.size() < limit) && offset != 0) {
                    hasMore = false;
                }
            }

            @Override
            public void onError(Throwable e) {
                fetching = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onComplete() {
                fetching = false;
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }
}
