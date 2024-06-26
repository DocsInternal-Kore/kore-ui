package kore.botssdk.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import kore.botssdk.R;
import kore.botssdk.listener.ComposeFooterInterface;
import kore.botssdk.listener.InvokeGenericWebViewInterface;
import kore.botssdk.models.BaseBotMessage;
import kore.botssdk.models.BotRequest;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.ComponentModel;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.models.PayloadOuter;
import kore.botssdk.net.SDKConfiguration;
import kore.botssdk.utils.SelectionUtils;
import kore.botssdk.utils.StringUtils;
import kore.botssdk.view.KaBaseBubbleContainer;
import kore.botssdk.view.KaBaseBubbleLayout;
import kore.botssdk.view.viewUtils.BubbleViewUtil;

/**
 * edit : Only for bots SDK
 */
@SuppressLint("UnknownNullness")
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder>  {

    private int lastPosition = -1;
//    private static String LOG_TAG = ChatAdapter.class.getSimpleName();
final Context context;
    private Activity activityContext;
    private final LayoutInflater ownLayoutInflater;
    private final HashMap<String, Integer> headersMap = new HashMap<>();
    private boolean isAlpha = false;
    int selectedItem = -1;


    public ComposeFooterInterface getComposeFooterInterface() {
        return composeFooterInterface;
    }

    public void setComposeFooterInterface(ComposeFooterInterface composeFooterInterface) {
        this.composeFooterInterface = composeFooterInterface;
    }

    public InvokeGenericWebViewInterface getInvokeGenericWebViewInterface() {
        return invokeGenericWebViewInterface;
    }

    public void setInvokeGenericWebViewInterface(InvokeGenericWebViewInterface invokeGenericWebViewInterface) {
        this.invokeGenericWebViewInterface = invokeGenericWebViewInterface;
    }

    ComposeFooterInterface composeFooterInterface;
    private InvokeGenericWebViewInterface invokeGenericWebViewInterface;
    private final int BUBBLE_CONTENT_LAYOUT_WIDTH;
    private final int BUBBLE_CONTENT_LAYOUT_HEIGHT;


    public ArrayList<BaseBotMessage> getBaseBotMessageArrayList() {
        return baseBotMessageArrayList;
    }

    private final ArrayList<BaseBotMessage> baseBotMessageArrayList;

    private static final int BUBBLE_LEFT_LAYOUT = 0;
    private static final int BUBBLE_RIGHT_LAYOUT = BUBBLE_LEFT_LAYOUT + 1;

    public ChatAdapter(Context context) {
        this.context = context;
        ownLayoutInflater = LayoutInflater.from(context);
        BUBBLE_CONTENT_LAYOUT_WIDTH = BubbleViewUtil.getBubbleContentWidth();
        BUBBLE_CONTENT_LAYOUT_HEIGHT = BubbleViewUtil.getBubbleContentHeight();
        baseBotMessageArrayList = new ArrayList<>();
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(ownLayoutInflater.inflate(i == BUBBLE_RIGHT_LAYOUT ? R.layout.ka_bubble_layout_right : R.layout.ka_bubble_layout_left, null), i);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.baseBubbleContainer.setAlpha(isAlpha && position != getItemCount() -1 ? 0.4f : 1.0f);
        holder.baseBubbleContainer.setViewActive(!isAlpha || position == getItemCount()-1);
        holder.baseBubbleContainer.setDimensions(BUBBLE_CONTENT_LAYOUT_WIDTH, BUBBLE_CONTENT_LAYOUT_HEIGHT);
        holder.baseBubbleLayout.setContinuousMessage(position == 0 || checkIsContinuous(position));
        holder.baseBubbleLayout.setGroupMessage(false);
        holder.baseBubbleLayout.setComposeFooterInterface(composeFooterInterface);
        holder.baseBubbleLayout.setInvokeGenericWebViewInterface(invokeGenericWebViewInterface);
        holder.baseBubbleLayout.setActivityContext(activityContext);
        holder.baseBubbleLayout.fillBubbleLayout(position,position == getItemCount() -1 , getItem(position));
        holder.textView.setText(getItem(position).getFormattedDate());

        if(headersMap.isEmpty()) {
            prepareHeaderMap();
        }
        boolean fDate = false;
        try {
            fDate = headersMap.get(getItem(holder.getBindingAdapterPosition()).getFormattedDate()) == holder.getBindingAdapterPosition();
        }catch (Exception e){
            e.printStackTrace();
        }
        holder.headerView.setVisibility(getItem(holder.getBindingAdapterPosition()) != null && fDate ? View.VISIBLE : View.GONE);
        if(selectedItem == holder.getBindingAdapterPosition()){
            holder.baseBubbleLayout.setTimeStampVisible();
        }

        if(getItemViewType(holder.getBindingAdapterPosition()) == BUBBLE_RIGHT_LAYOUT)
        {
            // call Animation function
            setAnimation(holder.itemView, position);

            holder.baseBubbleLayout.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(selectedItem != -1){
                        notifyItemChanged(selectedItem);
                    }
                    selectedItem = holder.getBindingAdapterPosition();
                    holder.baseBubbleLayout.setTimeStampVisible();
                    return true;
                }
            });
            holder.baseBubbleLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BotRequest botRequest = (BotRequest) getItem(holder.getBindingAdapterPosition());
                    if(composeFooterInterface != null)  composeFooterInterface.copyMessageToComposer((String) botRequest.getMessage().getBody(), false);
                }
            });
        }
        else
            setLeftAnimation(holder.itemView, position);

    }

    private boolean checkIsContinuous(int position) {
        if(getItem(position).isSend() && getItem(position-1).isSend()){
            return true;
        }else return !getItem(position).isSend() && !getItem(position - 1).isSend();
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.REVERSE, 1.0f, Animation.REVERSE, 1.0f);
            anim.setDuration(800);//to make duration random number between [0,501)
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }
    private void setLeftAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation anim = new ScaleAnimation(-0.5f, 1.0f, -0.5f, 1.0f, Animation.REVERSE, 0.0f, Animation.REVERSE, 0.5f);
            anim.setDuration(1000);
            viewToAnimate.startAnimation(anim);
            lastPosition = position;
        }
    }


    @Override
    public int getItemViewType(int position) {

        BaseBotMessage baseBotMessage = getItem(position);
        if (SDKConfiguration.BubbleColors.isArabic != baseBotMessage.isSend()) {
            return BUBBLE_RIGHT_LAYOUT;
        } else {
            return BUBBLE_LEFT_LAYOUT;
        }
    }

    public BaseBotMessage getItem(int position) {
        if (baseBotMessageArrayList != null && position <= baseBotMessageArrayList.size() - 1 && position != -1) {
            return baseBotMessageArrayList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return baseBotMessageArrayList.size();
    }

    public boolean isAlpha() {
        return isAlpha;
    }

    public void setAlpha(boolean alpha) {
        isAlpha = alpha;
    }



    public static class ViewHolder extends RecyclerView.ViewHolder{
        final KaBaseBubbleContainer baseBubbleContainer;
        final KaBaseBubbleLayout baseBubbleLayout;
        final View headerView;
        final TextView textView;
        public ViewHolder(View view,int viewType){
            super(view);
            if ( viewType== BUBBLE_RIGHT_LAYOUT) {
                // Right Side
                baseBubbleLayout = view.findViewById(R.id.sendBubbleLayout);
                baseBubbleContainer = view.findViewById(R.id.send_bubble_layout_container);
            } else {
                // Left Side
                baseBubbleLayout = view.findViewById(R.id.receivedBubbleLayout);
                baseBubbleContainer = view.findViewById(R.id.received_bubble_layout_container);
            }
            headerView = view.findViewById(R.id.headerLayout);
            textView = view.findViewById(R.id.filesSectionHeader);

        }
    }

    public void addBaseBotMessage(BaseBotMessage baseBotMessage) {
        if(!baseBotMessageArrayList.contains(baseBotMessage))
            baseBotMessageArrayList.add(baseBotMessage);

        if (headersMap.get(baseBotMessage.getFormattedDate()) == null) {
            headersMap.put(baseBotMessage.getFormattedDate(), baseBotMessageArrayList.size() -1);
        }
        SelectionUtils.resetSelectionTasks();
        SelectionUtils.resetSelectionSlots();
        isAlpha = false;
        notifyItemInserted(baseBotMessageArrayList.size() -1);
    }



    public void addBaseBotMessages(ArrayList<BaseBotMessage> list) {
        baseBotMessageArrayList.addAll(0, list);
        prepareHeaderMap();
        if(selectedItem != -1) {
            selectedItem = selectedItem + list.size()-1;
        }
        notifyItemRangeInserted(0, list.size() - 1);
    }



    public void setActivityContext(Activity activityContext) {
        this.activityContext = activityContext;
    }

    private void prepareHeaderMap() {
        int i = 0;
        headersMap.clear();
        for (i = 0; i < baseBotMessageArrayList.size(); i++) {
            BaseBotMessage baseBotMessage = baseBotMessageArrayList.get(i);
            if (headersMap.get(baseBotMessage.getFormattedDate()) == null) {
                headersMap.put(baseBotMessage.getFormattedDate(), i);
            }
        }

    }
}
