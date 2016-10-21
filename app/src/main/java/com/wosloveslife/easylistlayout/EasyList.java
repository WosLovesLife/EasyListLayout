package com.wosloveslife.easylistlayout;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wosloveslife.easylistlayout.adapter.BaseRecyclerViewAdapter;
import com.wosloveslife.easylistlayout.viewHolder.BaseRecyclerViewHolder;

import java.util.Arrays;

/**
 * Created by YesingBeijing on 2016/9/14.
 */
public class EasyList extends SwipeRefreshLayout {

    //===========Views
    private RecyclerView mRecyclerView;
    private View mLoadMoreView;
    private TextView mTvFooterHint;
    private ProgressBar mPbFooterLoading;

    //===========管理器
    private RecyclerView.LayoutManager mLayoutManager;

    //===========适配器
    private final Adapter mAdapterHelper = new Adapter();
    RecyclerView.Adapter mAdapter;

    //===========监听器
    /** 监听器，下拉刷新或上拉加载时回调对应方法 */
    private OnRefreshListener mOnRefreshListener;
    /** 监听器, 当布局尺寸发生变化时回调相关方法 */
    private OnSizeChangeListener mOnSizeChangeListener;

    //===========变量
    /** 为true时, 滑动到最后一条时加载更多 */
    private boolean mLoadMoreEnable;
    /** 为true时说明数据大于一页, 则可以触发onLoadMore */
    private boolean mPullUp;
    /** 控件初始化完成后改变值为 true 表示可以正常的显示 LoadingBar */
    private boolean mCanShow;
    private boolean mLoadMoreFooterEnable;

    public EasyList(Context context) {
        this(context, null);
    }

    public EasyList(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangeListener != null) {
            mOnSizeChangeListener.onSizeChanged(w, h, oldw, oldh);
        }

        /* 如果过早的调动显示方法, 而刷新控件还没有完全初始化好,就会导致刷新Bar不显示,
         * 因此监听控件的初始化 改变变量状态, 表示可以显示刷新 */
        mCanShow = true;
    }

    /**
     * 初始化
     */
    private void init() {
        mRecyclerView = new RecyclerView(getContext());
        addView(mRecyclerView);

        initRefreshLayout();
        initRecyclerView();
    }

    /**
     * 初始化刷新控件
     */
    private void initRefreshLayout() {
        setColorSchemeColors(Color.rgb(51, 181, 168), Color.rgb(64, 92, 113));
        setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
            }
        });
    }

    /**
     * 初始化列表控件
     */
    private void initRecyclerView() {
        mRecyclerView.setAdapter(mAdapterHelper);

        setLoadMoreFooterEnable(true);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;
                if (mAdapterHelper.getItemCount() < 1) return;

                /* 双重判断, 如果用户是向上的滑动操作, 并且处于最后一条,且开启了loadMore且监听不为null */
                if (!mLoadMoreEnable || !mPullUp) return;
                if (isLast() && mOnRefreshListener != null) {
                    showLoadMoreLoading();
                    mOnRefreshListener.onLoadMore();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mPullUp = dy > 0;
            }
        });
    }

    class Adapter extends BaseRecyclerViewAdapter {

        /** 忽略 */
        @Override
        public BaseRecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            BaseRecyclerViewHolder holder = super.onCreateViewHolder(parent, viewType);
            if (holder == null) {
                return create(parent, viewType);
            } else {
                return holder;
            }
        }

        /** 忽略 */
        @Override
        protected BaseRecyclerViewHolder onCreateItemViewHolder(ViewGroup parent) {
            return null;
        }

        @Override
        public int getRealItemCount() {
            return mAdapter == null ? 0 : mAdapter.getItemCount();
        }

        private BaseRecyclerViewHolder create(ViewGroup parent, int viewType) {
            final RecyclerView.ViewHolder holder = mAdapter.onCreateViewHolder(parent, viewType);
            return new BaseRecyclerViewHolder(holder.itemView) {
                @Override
                public void onBind(Object o, int position) {
                    mAdapter.onBindViewHolder(holder, position);
                }
            };
        }
    }

    //=======================================================
    //========================控制方法=======================
    //=======================================================

    //=====================================Build构造===================================
    public static class Builder {
        private EasyList mEasyList;

        public Builder(Context context) {
            mEasyList = new EasyList(context);
        }

        public Builder setLayoutManager(RecyclerView.LayoutManager layoutManager) {
            mEasyList.setLayoutManager(layoutManager);
            return this;
        }

//        public Builder setAdapter(BaseRecyclerViewAdapter adapter) {
//            mEasyList.setAdapter(adapter);
//            return this;
//        }

        public EasyList build() {
            return mEasyList;
        }
    }


    //=====================================对于列表管理的关键方法===================================

    /**
     * 设置RecyclerView数据适配器
     *
     * @param adapter 适配器
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        mAdapter = adapter;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mLayoutManager = layoutManager;
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public Adapter getAdapterHelper() {
        return mAdapterHelper;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    //=====================================对于列表数据的控制方法===================================

    public void addHeader(View v) {
        mAdapterHelper.addHeaderView(v);
    }

    public void addFooter(View v) {
        boolean enable = mLoadMoreFooterEnable;
        setLoadMoreFooterEnable(false);

        mAdapterHelper.addFooterView(v);

        if (enable) {
            setLoadMoreFooterEnable(true);
        }
    }

    //=====================================关于列表底部状态条的方法=================================

    /**
     * 显示底部状态条为 加载更多()
     */
    public void showLoadMoreLoading() {
        showLoadMoreLoading(null);
    }

    public void showLoadMoreLoading(@Nullable String msg) {
        mTvFooterHint.setText(msg == null ? "正在获取数据..." : msg);
        mTvFooterHint.setClickable(false);
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(VISIBLE);
    }

    public void showEndLine() {
        showEndLine(null);
    }

    public void showEndLine(@Nullable String msg) {
        mTvFooterHint.setText(msg == null ? "— 已经到底了 —" : msg);
        mTvFooterHint.setClickable(false);
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    public void showRetry() {
        showRetry(null, new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnRefreshListener.onLoadMore();
            }
        });
    }

    public void showRetry(@Nullable String msg, @Nullable OnClickListener onClickListener) {
        mTvFooterHint.setText(msg == null ? "加载失败,点击重试" : msg);
        mTvFooterHint.setClickable(true);
        mTvFooterHint.setOnClickListener(onClickListener != null ? onClickListener : new OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnRefreshListener.onLoadMore();
            }
        });
        mTvFooterHint.setVisibility(VISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    public void hideEndLine() {
        mTvFooterHint.setVisibility(INVISIBLE);
        mPbFooterLoading.setVisibility(INVISIBLE);
    }

    /**
     * 为true时,会在列表底部显示当前的列表状态, 例如,当列表数据全部加载完时,可以改变状态为已加载完数据 显示 "已经到底了"<br/>
     * 或者当正在加载更多数据时,可以改变状态为正在加载数据 显示 "正在获取数据..."<br/>
     * 另外, 当获取数据时没有网络可以改变状态为 点击重试 这样用户可以通过点击底部条激活 加载更多<br/>
     * 默认为{@link Boolean#TRUE}.
     * <p/>
     * 关于改变状态的方法:<br/>
     * {@link EasyList#showEndLine()}显示到底状态,
     * 或者它的重载{@link EasyList#showEndLine(String)}自定义提示语句.<br/>
     * {@link EasyList#showLoadMoreLoading()} 显示正在加载数据,
     * 或者它的重载{@link EasyList#showLoadMoreLoading(String)}自定义提示语句.<br/>
     * {@link EasyList#showRetry()} 显示重新加载,并默认用户点击后触发 加载更多 {@link OnRefreshListener#onLoadMore()},
     * 或者它的重载{@link EasyList#showRetry(String, OnClickListener)}自定义提示语句 和 点击事件监听.<br/>
     * {@link EasyList#hideEndLine()} 隐藏底部状态条.
     * <p/>
     * 注意: 当列表数据小于一页时, 不显示底部状态条.
     *
     * @param enable 是否开启列表底部状态条
     */
    public void setLoadMoreFooterEnable(boolean enable) {
        if (mAdapterHelper == null) return;

        mLoadMoreFooterEnable = enable;

        if (enable) {
            if (mLoadMoreView != null) return;

            mLoadMoreView = LayoutInflater.from(getContext()).inflate(R.layout.refresh_recyclerview_footer_view, this, false);
            mTvFooterHint = (TextView) mLoadMoreView.findViewById(R.id.tv_hint);
            mPbFooterLoading = (ProgressBar) mLoadMoreView.findViewById(R.id.pb_loading);
            mAdapterHelper.addFooterView(mLoadMoreView);
        } else {
            if (mLoadMoreView != null) {
                mAdapterHelper.removeFooter(mLoadMoreView);
                mLoadMoreView = null;
            }
        }
    }

    //======================================控制刷新Bar的显示/隐藏==================================

    /**
     * 开启刷新
     */
    public void startRefreshing() {
        if (!isRefreshing()) {
            if (mCanShow) {
                setRefreshing(true);
            } else {
                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        setRefreshEnable(true);
                    }
                });
            }
        }
    }

    /**
     * 停止刷新
     */
    public void refreshingComplete() {
        if (isRefreshing()) {
            setRefreshing(false);
        }
    }

    /**
     * 停止刷新
     */
    public void refreshingComplete(int bottomBarState) {
        if (isRefreshing()) {
            setRefreshing(false);
        }

        switch (bottomBarState) {
            case 0:
                showEndLine();
                break;
            case 1:
                showRetry();
                break;
            case 2:
                hideEndLine();
                break;
        }
    }

    //======================================启用/禁用刷新/加载更多==================================

    /**
     * 是否启用下拉刷新
     *
     * @param enable 为true时启用, 默认为true
     */
    public void setRefreshEnable(boolean enable) {
        setEnabled(enable);
    }

    /**
     * 是否启用加载更多(当滑动到最后一条时触发)
     *
     * @param enable 为true时启用, 默认为false
     */
    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }

    public void setBothEnable(boolean enable) {
        setRefreshEnable(enable);
        setLoadMoreEnable(enable);
    }

    //======================================各类事件监听和监听接口==================================
    public interface OnRefreshListener {
        void onRefresh();

        void onLoadMore();
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mOnRefreshListener = listener;
    }


    public interface OnSizeChangeListener {
        void onSizeChanged(int w, int h, int oldW, int oldH);
    }

    public void setOnSizeChangedListener(OnSizeChangeListener listener) {
        mOnSizeChangeListener = listener;
    }

    //======================================关于控件尺寸的控制方法==================================
    public void setListPadding(int left, int top, int right, int bottom) {
        mRecyclerView.setPadding(left, top, right, bottom);
    }

    public int getListWidth() {
        return (mRecyclerView.getMeasuredWidth()
                - mRecyclerView.getPaddingLeft()
                - mRecyclerView.getPaddingRight())
                / getSpanCount();
    }

    public int getSpanCount() {
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) mLayoutManager).getSpanCount();
        } else if (mLayoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) mLayoutManager).getSpanCount();
        } else {
            return 1;
        }
    }

    //====================================================================

    private boolean isLast() {
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            int[] positions = ((StaggeredGridLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPositions(null);
            /* 这个数组中的position表示整个元素序列中,处于最靠近底边的元素所在的下标.如果列表有两列, 则数组length=2 */
            Arrays.sort(positions);
            return positions[positions.length - 1] >= mAdapterHelper.getItemCount() - 1;
        } else if (mLayoutManager instanceof LinearLayoutManager) {
            int position = ((LinearLayoutManager) mLayoutManager).findLastCompletelyVisibleItemPosition();
            return position >= mAdapterHelper.getItemCount() - 1;
        } else {
            return true;
        }
    }
}
