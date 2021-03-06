package me.wavever.ganklock.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.activeandroid.query.Select;
import java.util.ArrayList;
import java.util.List;
import me.wavever.ganklock.R;
import me.wavever.ganklock.event.ClickEvent;
import me.wavever.ganklock.event.RxBus;
import me.wavever.ganklock.model.bean.Gank;
import me.wavever.ganklock.presenter.LikePresenter;
import me.wavever.ganklock.ui.activity.WebViewActivity;
import me.wavever.ganklock.ui.adapter.LikeRecyclerViewAdapter;
import me.wavever.ganklock.utils.LogUtil;
import me.wavever.ganklock.view.ILikeView;
import rx.functions.Action1;

/**
 * Created by wavever on 2016/8/12.
 */
public class LikeFragment extends BaseFragment<ILikeView, LikePresenter> implements ILikeView {

    private static final String TAG = LikeFragment.class.getSimpleName() + "-->";

    private RecyclerView mRecyclerView;
    private LikeRecyclerViewAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mTips;
    private List<Gank> mList;

    @Override
    protected int loadView() {
        return R.layout.fragment_like;
    }


    @Override
    protected void initView() {
        mRecyclerView = (RecyclerView) mContext.findViewById(R.id.recycler_view_like_fragment);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mAdapter = new LikeRecyclerViewAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
        mTips = (TextView) mContext.findViewById(R.id.empty_tip_like_fragment);
        mProgressBar = (ProgressBar) mContext.findViewById(R.id.progress_bar_like_fragment);
        mProgressBar.setVisibility(View.VISIBLE);
        mList = new ArrayList<>();
    }


    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadLikeData();
        LogUtil.d(TAG + "onActivityCreated执行");
        mSubscrition = RxBus.getInstance().tObservable(ClickEvent.class).subscribe(
            new Action1<ClickEvent>() {
                @Override public void call(ClickEvent clickEvent) {
                    if(clickEvent.eventType == ClickEvent.CLICK_TYPE_LIKE){
                        Intent intent = new Intent(mContext, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.KEY_TITLE, mList.get(clickEvent.position).getDesc());
                        intent.putExtra(WebViewActivity.KEY_URL, mList.get(clickEvent.position).getUrl());
                        startActivity(intent);
                    }
                }
            });
    }


    public void loadLikeData() {
        getPresenter().getLikeList();
    }


    @Override
    public LikePresenter createPresenter() {
        return new LikePresenter();
    }


    public void showLikeData(List<Gank> list) {
        if(!mList.isEmpty()){
            mList.clear();
        }
        mList = list;
        mAdapter.setDataList(list);
        mAdapter.notifyDataSetChanged();
        mProgressBar.setVisibility(View.GONE);
        mTips.setVisibility(View.GONE);
    }


    @Override public void refreshLikeData(List<Gank> list) {
    }


    public void showEmptyTip() {
        mProgressBar.setVisibility(View.GONE);
        mTips.setVisibility(View.VISIBLE);
    }


    /**
     * 每次状态改变都会调用
     */
    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            LogUtil.d(TAG + "onHiddenChanged执行--》展示");
            int dbCount = new Select().from(Gank.class).count();
            if (mTips.getVisibility() == View.VISIBLE && dbCount > 0) {//此时的Like为空
                loadLikeData();
            } else {
                if (dbCount > mAdapter.getItemCount()) {
                    loadLikeData();
                }
            }
        } else {
            LogUtil.d(TAG + "onHiddenChanged执行--》隐藏");
        }
    }
}
