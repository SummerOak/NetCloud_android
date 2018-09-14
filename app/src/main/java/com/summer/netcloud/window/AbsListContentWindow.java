package com.summer.netcloud.window;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.summer.netcloud.R;
import com.summer.netcloud.message.IMsgListener;
import com.summer.netcloud.message.Messege;
import com.summer.netcloud.message.MsgDispatcher;
import com.summer.netcloud.utils.ResTools;
import com.summer.netcloud.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by summer on 07/07/2018.
 */

public abstract class AbsListContentWindow<D,V extends View> extends AbsWindow implements IMsgListener {

    private FrameLayout mContentView;
    private TextView mEmptyView;
    private TextView mLoading;
    private ListView mListView;
    private MyAdapter mAdapter;

    private List<D> mData = new ArrayList<>();
    private SparseArray<Integer> mID2Pos = new SparseArray<>();
    private SparseArray<Integer> mPos2ID = new SparseArray<>();

    public AbsListContentWindow(Context context) {
        super(context);
    }

    private void initView(Context context){

        mContentView = new FrameLayout(context);

        mListView = new ListView(context);
        mListView.setDividerHeight(ScreenUtils.dp2px(1.5f));
        mListView.setDivider(new ColorDrawable(Color.TRANSPARENT));
        mListView.setSelector(R.drawable.list_item_bg);
        mListView.setBackgroundColor(Color.TRANSPARENT);
        mAdapter = new MyAdapter();

        mContentView.addView(mListView);

        mEmptyView = new TextView(context);
        mEmptyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResTools.getDimen(R.dimen.textsize2));
        mEmptyView.setTextColor(ResTools.getColor(R.color.text));
        mEmptyView.setGravity(Gravity.CENTER);
        mEmptyView.setText("empty");
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = (int)(ScreenUtils.getScreenSize().y*0.38f);
        mContentView.addView(mEmptyView, lp);

        mLoading = new TextView(context);
        mLoading.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResTools.getDimen(R.dimen.textsize2));
        mLoading.setTextColor(ResTools.getColor(R.color.text));
        mLoading.setText("loading...");
        lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = ScreenUtils.getScreenSize().y/3;
        mContentView.addView(mLoading, lp);
    }

    protected void setEmptyDescryption(String desc){
        if(mEmptyView != null){
            mEmptyView.setText(desc);
        }
    }

    @Override
    protected View getContentView() {
        if(mContentView == null){
            initView(getContext());
        }

        return mContentView;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessage(int msgId, Object arg) {
        switch (msgId){
            case Messege.APP_ON_RESUME:{
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void preSwitchIn() {
        super.preSwitchIn();

        MsgDispatcher.get().registerMsg(Messege.APP_ON_RESUME, this);
        mListView.setAdapter(mAdapter);
    }

    @Override
    protected void postSwitchOut() {
        super.postSwitchOut();
        MsgDispatcher.get().unregisterMsg(Messege.APP_ON_RESUME, this);
    }

    @Override
    public Object onSyncMessage(int msgId, Object arg) {
        return null;
    }

    protected List<D> getData(){
        return mData;
    }

    protected void updateData(List<D> data){
        synchronized (mAdapter){
            mData.clear();

            if(data != null){
                mData.addAll(data);
            }
        }

        update();
    }

    protected void showLoading(){
        mListView.setVisibility(View.INVISIBLE);
        mEmptyView.setVisibility(View.INVISIBLE);
        mLoading.setVisibility(View.VISIBLE);
    }

    protected void update(){
        if(mData.isEmpty()){
            mEmptyView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.INVISIBLE);
            mLoading.setVisibility(View.INVISIBLE);
        }else{
            mEmptyView.setVisibility(View.INVISIBLE);
            mListView.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.INVISIBLE);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void updateItem(D item){
        if(!isShowing()){
            return;
        }

        int id = getItemId(item);
        int pos = mID2Pos.get(id, -1);
        if (pos >= 0) {
            V itemView = (V)mListView.getChildAt(pos - mListView.getFirstVisiblePosition());
            if(itemView != null){
                bindItem(item, itemView);
            }
        }
    }


    protected abstract int getItemId(D item);
    protected abstract V createItemView(int position);
    protected abstract void bindItem(D item, V view);

    private class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return AbsListContentWindow.this.getItemId((D)getItem(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = createItemView(position);
            }

            V itemView = (V)convertView;
            D info = (D)getItem(position);

            int oldID = mPos2ID.get(position, -1);

            if(info != null){
                bindItem(info, itemView);
                int id = AbsListContentWindow.this.getItemId(info);
                mID2Pos.remove(oldID);
                mID2Pos.put(id, position);
                mPos2ID.put(position, id);
            }else{
                mID2Pos.remove(oldID);
                mPos2ID.remove(position);
            }

            return convertView;
        }
    }

}
