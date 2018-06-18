package com.cdkj.baselibrary.interfaces;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

/**
 * 根据需求自己定义
 * Created by cdkj on 2017/8/8.
 */
public interface RefreshInterface<T> {

    View getRefreshLayout();

    RecyclerView getRecyclerView();

    RecyclerView.Adapter getAdapter(List<T> listData);

    View getEmptyView();

    void showErrorState(String errorMsg,@DrawableRes int errorImg);

    void showEmptyState(String errorMsg,@DrawableRes int errorImg);

    void onRefresh(int pageIndex, int limit);

    void onLoadMore(int pageIndex, int limit);

    void getListDataRequest(int pageIndex, int limit, boolean isShowDialog);

    void onDestroy();

}
