package kore.botssdk.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import kore.botssdk.R;
import kore.botssdk.adapter.FeedbackRatingScaleAdapter;
import kore.botssdk.dialogs.FeedbackActionSheetFragment;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.listener.ListClickListner;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.utils.KaFontUtils;

@SuppressLint("UnknownNullness")
public class FeedbackTemplateView extends LinearLayout implements View.OnClickListener, ListClickListner
{
    private TextView tvfeedback_template_title;
    private ImageView icon_1, icon_2, icon_3, icon_4, icon_5;
    private final Context mContext;
    int position, ratingPosition = -1;
    PayloadInner payloadInner;
    private RatingBar rbFeedback;
    private LinearLayout emojis;
    private RelativeLayout rlViewNPS;
    private RecyclerView rvRatingScale;
    private FeedbackRatingScaleAdapter feedbackRatingScaleAdapter;

    public FeedbackTemplateView(Context context) {
        super(context);
        this.mContext = context;
        init();
    }

    public FeedbackTemplateView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        init();
    }

    public FeedbackTemplateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        init();
    }


    public InvokeGenericWebViewInterface getInvokeGenericWebViewInterface() {
        return invokeGenericWebViewInterface;
    }


    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    public ComposeFooterInterface getComposeFooterInterface() {
        return composeFooterInterface;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private ComposeFooterInterface composeFooterInterface;


    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.feedback_template_view, this, true);
        KaFontUtils.applyCustomFont(getContext(), view);
        tvfeedback_template_title = view.findViewById(R.id.tvfeedback_template_title);
        rbFeedback = view.findViewById(R.id.rbFeedback);
        emojis = view.findViewById(R.id.emojis);
        rlViewNPS = view.findViewById(R.id.rlViewNPS);
        rvRatingScale = view.findViewById(R.id.rvRatingScale);

        icon_1 = view.findViewById(R.id.icon_1);
        icon_2 = view.findViewById(R.id.icon_2);
        icon_3 = view.findViewById(R.id.icon_3);
        icon_4 = view.findViewById(R.id.icon_4);
        icon_5 = view.findViewById(R.id.icon_5);

        icon_1.setOnClickListener(this);
        icon_2.setOnClickListener(this);
        icon_3.setOnClickListener(this);
        icon_4.setOnClickListener(this);
        icon_5.setOnClickListener(this);

        rbFeedback.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                position = (int) rating;
                payloadInner.setEmojiPosition(position);

                if(fromUser)
                    updateData();
            }
        });

    }

    public void populateData(final PayloadInner payloadInnerOut) {

        if (payloadInnerOut != null)
        {
            this.payloadInner = payloadInnerOut;
            tvfeedback_template_title.setText(payloadInner.getText());

            if(payloadInner.getView().equalsIgnoreCase(BotResponse.VIEW_STAR))
            {
                emojis.setVisibility(GONE);
                rbFeedback.setVisibility(VISIBLE);
                rlViewNPS.setVisibility(GONE);
                rbFeedback.setRating(payloadInner.getEmojiPosition());
            }
            else if(payloadInner.getView().equalsIgnoreCase(BotResponse.VIEW_NPS)
                    && payloadInner.getNumbersArrays() != null && payloadInner.getNumbersArrays().size() > 0)
            {
                emojis.setVisibility(GONE);
                rbFeedback.setVisibility(GONE);
                rlViewNPS.setVisibility(VISIBLE);

                rvRatingScale.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
                rvRatingScale.setAdapter(feedbackRatingScaleAdapter = new FeedbackRatingScaleAdapter(mContext, payloadInner.getNumbersArrays(), composeFooterInterface, FeedbackTemplateView.this, ratingPosition));
            }
            else
            {
                emojis.setVisibility(VISIBLE);
                rbFeedback.setVisibility(GONE);
                rlViewNPS.setVisibility(GONE);

                resetAll();
                loademojis(payloadInner.getEmojiPosition());
            }

            if(payloadInner.getSliderView() && !payloadInner.getDialogCancel())
            {
                payloadInner.setDialogCancel(true);
                FeedbackActionSheetFragment bottomSheetDialog = new FeedbackActionSheetFragment();
                bottomSheetDialog.setSkillName("skillName","trigger");
                bottomSheetDialog.setData(payloadInner);
                bottomSheetDialog.setComposeFooterInterface(composeFooterInterface);
                bottomSheetDialog.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
                bottomSheetDialog.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "add_tags");
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        int id = v.getId();
        if (id == R.id.icon_1) {
            resetAll();
            //  icon_4.setImageResource(R.drawable.feedbac_ic_emo_4);
            loademojis(0);
            position = 1;
            updateData();
        } else if (id == R.id.icon_2) {
            resetAll();
            loademojis(1);
            position = 2;
            updateData();
        } else if (id == R.id.icon_3) {
            resetAll();
            loademojis(2);
            position = 3;
            updateData();
        } else if (id == R.id.icon_4) {
            resetAll();
            loademojis(3);
            position = 4;
            updateData();
        } else if (id == R.id.icon_5) {
            resetAll();
            loademojis(4);
            position = 5;
            updateData();
        }
    }

    void updateData()
    {
        composeFooterInterface.onSendClick((position)+"", (position)+"", false);
    }

    private void resetAll() {
        icon_1.setImageResource(R.drawable.feedback_icon_1);
        icon_2.setImageResource(R.drawable.feedback_icon_2);
        icon_3.setImageResource(R.drawable.feedback_icon_3);
        icon_4.setImageResource(R.drawable.feedback_icon_4);
        icon_5.setImageResource(R.drawable.feedback_icon_5);
    }

    public void loademojis(int position) {
        this.payloadInner.setEmojiPosition(position);
        switch (position) {
            case 0:
                Glide.with(mContext).load(R.drawable.feedback_icon_2).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new DrawableImageViewTarget(icon_1));
                break;
            case 1:
                Glide.with(mContext).load(R.drawable.feedback_icon_2).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new DrawableImageViewTarget(icon_2));
                break;
            case 2:
                Glide.with(mContext).load(R.drawable.feedback_icon_3).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new DrawableImageViewTarget(icon_3));
                break;
            case 3:
                Glide.with(mContext).load(R.drawable.feedback_icon_4).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new DrawableImageViewTarget(icon_4));
                break;
            case 4:
                Glide.with(mContext).load(R.drawable.feedback_icon_5).apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE)).into(new DrawableImageViewTarget(icon_5));
                break;
        }
    }

    @Override
    public void listItemClicked(int position) {
        ratingPosition = position;
        if(feedbackRatingScaleAdapter != null)
            feedbackRatingScaleAdapter.setEnabled(ratingPosition);
    }
}
