package kore.botssdk.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;

import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;

import kore.botssdk.R;
import kore.botssdk.models.BotResponse;
import kore.botssdk.models.PayloadInner;
import kore.botssdk.utils.LogUtils;
import kore.botssdk.view.tableview.TableExpandView;
import kore.botssdk.view.tableview.adapters.BotRespExpandTableAdapter;
import kore.botssdk.view.tableview.model.MiniTableModel;
import kore.botssdk.view.tableview.model.TableColumnWeightModel;
import kore.botssdk.view.tableview.toolkit.SimpleTableHeaderAdapter;
import kore.botssdk.view.tableview.toolkit.TableDataRowBackgroundProviders;
import kore.botssdk.view.viewUtils.DimensionUtil;
import kore.botssdk.view.viewUtils.LayoutUtils;

public class BotResponsiveExpandTableView extends TableExpandView<MiniTableModel> {

    private Context context;
    private String[]  headers;
    int dp1;

    public BotResponsiveExpandTableView(final Context context) {
        this(context, null);
        this.context = context;
        this.dp1 = (int) DimensionUtil.dp1;

    }

    public BotResponsiveExpandTableView(final Context context, final AttributeSet attributes) {
        this(context, attributes, android.R.attr.listViewStyle);

    }

    public BotResponsiveExpandTableView(final Context context, final AttributeSet attributes, final int styleAttributes){
        super(context,attributes,styleAttributes);

    }
    public String[] addHeaderAdapter( List<List<String>> primary){
        headers = new String[primary.size()];
        String[] alignment = new String[primary.size()];
        String defaultAlign = "left";
        for(int i=0; i<primary.size();i++){
            headers[i] = primary.get(i).get(0);
            if(primary.get(i).size() > 1)
                alignment[i] = primary.get(i).get(1);
            else
                alignment[i] = defaultAlign;
        }
        final SimpleTableHeaderAdapter simpleTableHeaderAdapter = new SimpleTableHeaderAdapter(context, headers,alignment);
        simpleTableHeaderAdapter.setTextColor(context.getResources().getColor(R.color.primaryDark));
        setHeaderAdapter(simpleTableHeaderAdapter);
        setHeaderVisible(false, 100);

        final int rowColorEven =context.getResources().getColor(R.color.table_data_row_even);
        setDataRowBackgroundProvider(TableDataRowBackgroundProviders.alternatingRowColors(rowColorEven, rowColorEven));

        final TableColumnWeightModel tableColumnWeightModel = new TableColumnWeightModel(headers.length);
        for(int index=0;index<headers.length;index++) {
            tableColumnWeightModel.setColumnWeight(index, 3);
        }
        setColumnCount(headers.length);
        setColumnModel(tableColumnWeightModel);
        return alignment;
    }

    public void setData(PayloadInner payloadInner){
        if(payloadInner != null) {
            String[] alignment = addHeaderAdapter(payloadInner.getColumns());
            addDataAdapterForTable(payloadInner, alignment);
        }
    }

    public void addDataAdapterForTable(PayloadInner data, String[] alignment) {

        List<MiniTableModel> lists = new ArrayList<>();
        int size = ((ArrayList<?>) data.getElements()).size();
        for (int j = 0; j < size; j++) {
            MiniTableModel model = new MiniTableModel();
            model.setElements(((ArrayList)(((LinkedTreeMap<?, ?>)((ArrayList) data.getElements()).get(j))).get("Values")));
            lists.add(model);
        }

        BotRespExpandTableAdapter botRespExpandTableAdapter = new BotRespExpandTableAdapter(context, lists, alignment, headers, data);
        setDataAdapter(botRespExpandTableAdapter);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int parentWidth = getMeasuredWidth();

        //get the available size of child view
        int childLeft = 0;//this.getPaddingLeft();
        int childTop = 0;//this.getPaddingTop();

        //walk through each child, and arrange it from left to right
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                LayoutUtils.layoutChild(child, childLeft, childTop);
                childTop += child.getMeasuredHeight();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)  {
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        // Unspecified means that the ViewPager is in a ScrollView WRAP_CONTENT.
        // At Most means that the ViewPager is not in a ScrollView WRAP_CONTENT.
        if (mode == MeasureSpec.UNSPECIFIED || mode == MeasureSpec.AT_MOST) {
            // super has to be called in the beginning so the child views can be initialized.
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int height;
            height = 75 * dp1 * tableDataView.getAdapter().getCount();
            height += tableHeaderView.getMeasuredHeight();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height+getPaddingTop(), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
