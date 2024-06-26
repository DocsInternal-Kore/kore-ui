package kore.botssdk.view.viewUtils;

import android.database.DataSetObserver;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

public class InfiniteCyclePagerAdapter extends PagerAdapter {

    private final static int VIRTUAL_ITEM_COUNT = 10_000_000;

    private final PagerAdapter mPagerAdapter;
    private OnNotifyDataSetChangedListener mOnNotifyDataSetChangedListener;

    public InfiniteCyclePagerAdapter(final PagerAdapter pagerAdapter) {
        mPagerAdapter = pagerAdapter;
    }

    public PagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    public void setOnNotifyDataSetChangedListener(final OnNotifyDataSetChangedListener onNotifyDataSetChangedListener) {
        mOnNotifyDataSetChangedListener = onNotifyDataSetChangedListener;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mPagerAdapter.destroyItem(container, getVirtualPosition(position), object);
    }

    @Override
    public void finishUpdate(ViewGroup container) {
        mPagerAdapter.finishUpdate(container);
    }

    @Override
    public int getCount() {
        if (mPagerAdapter.getCount() == 0) return 0;
        return VIRTUAL_ITEM_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPagerAdapter.getPageTitle(getVirtualPosition(position));
    }

    @Override
    public float getPageWidth(int position) {
        return mPagerAdapter.getPageWidth(getVirtualPosition(position));
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return mPagerAdapter.isViewFromObject(view, o);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return mPagerAdapter.instantiateItem(container, getVirtualPosition(position));
    }

    @Override
    public Parcelable saveState() {
        return mPagerAdapter.saveState();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        mPagerAdapter.restoreState(state, loader);
    }

    @Override
    public void startUpdate(ViewGroup container) {
        mPagerAdapter.startUpdate(container);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mPagerAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mPagerAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void notifyDataSetChanged() {
        mPagerAdapter.notifyDataSetChanged();
        // Callback for invalidating transformer position
        if (mOnNotifyDataSetChangedListener != null) mOnNotifyDataSetChangedListener.onChanged();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        mPagerAdapter.setPrimaryItem(container, position, object);
    }

    @Override
    public int getItemPosition(Object object) {
        return mPagerAdapter.getItemPosition(object);
    }

    // Main feature of this adapter which return virtual position
    // relative to virtual count and original count
    protected int getVirtualPosition(final int realPosition) {
        return realPosition % mPagerAdapter.getCount();
    }

    protected interface OnNotifyDataSetChangedListener {
        void onChanged();
    }
}
