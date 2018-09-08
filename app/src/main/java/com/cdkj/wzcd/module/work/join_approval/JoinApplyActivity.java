package com.cdkj.wzcd.module.work.join_approval;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.cdkj.baselibrary.appmanager.SPUtilHelper;
import com.cdkj.baselibrary.base.AbsBaseLoadActivity;
import com.cdkj.baselibrary.dialog.UITipDialog;
import com.cdkj.baselibrary.model.IsSuccessModes;
import com.cdkj.baselibrary.nets.BaseResponseModelCallBack;
import com.cdkj.baselibrary.nets.RetrofitUtils;
import com.cdkj.baselibrary.utils.StringUtils;
import com.cdkj.baselibrary.utils.ToastUtil;
import com.cdkj.wzcd.R;
import com.cdkj.wzcd.api.MyApiServer;
import com.cdkj.wzcd.databinding.ActivityJoinApplyBinding;
import com.cdkj.wzcd.model.DealersModel;
import com.cdkj.wzcd.model.NodeListModel;
import com.cdkj.wzcd.model.event.BudgetCheckModel;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep10Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep11Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep1Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep2Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep3Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep4Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep5Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep6Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep7Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep8Fragment;
import com.cdkj.wzcd.module.work.join_approval.page.JoinStep9Fragment;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

import static com.cdkj.baselibrary.appmanager.CdRouteHelper.DATA_SIGN;

/**
 * Created by cdkj on 2018/6/7.
 */

public class JoinApplyActivity extends AbsBaseLoadActivity {

    private ActivityJoinApplyBinding mBinding;

    private String code;

    private int pageIndex = 0;

    private String[] pageList;

    public NodeListModel mData;

    private List<Fragment> fragments = new ArrayList<>();

    public boolean isOutside;
    /**
     * 是否是查看详情
     */
    public boolean isDetails;
    /**
     * 保存
     */
    public static String SAVEALL = "saveAll";

    /**
     * @param context
     * @param code
     * @param isDetails 是查看详情 还是申请
     */
    public static void open(boolean isDetails, Context context, String code) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, JoinApplyActivity.class);
        intent.putExtra(DATA_SIGN, code);
        intent.putExtra("isDetails", isDetails);
        context.startActivity(intent);
    }

    /**
     * @param context
     * @param code
     * @param isOutside 是否是外单
     */
    public static void open(Context context, String code, boolean isOutside) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, JoinApplyActivity.class);
        intent.putExtra(DATA_SIGN, code);
        intent.putExtra("isOutside", isOutside);
        context.startActivity(intent);
    }


    @Override
    public View addMainView() {
        mBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_join_apply, null, false);
        return mBinding.getRoot();
    }

    @Override
    public void afterCreate(Bundle savedInstanceState) {
        initListener();

        if (getIntent() == null)
            return;

        isOutside = getIntent().getBooleanExtra("isOutside", false);
        isDetails = getIntent().getBooleanExtra("isDetails", false);
        code = getIntent().getStringExtra(DATA_SIGN);
        getNode();
    }

    private void initListener() {
        mBaseBinding.titleView.setMidFraClickListener(view -> {

            new AlertDialog.Builder(this).setTitle("前往->").setSingleChoiceItems(
                    pageList, pageIndex, (dialog, which) -> {

                        pageIndex = which;
                        setCurrent(pageIndex);

                        dialog.dismiss();
                    }).setNegativeButton("取消", null).show();

        });

        mBaseBinding.titleView.setLeftFraClickListener(view -> {
            if (pageIndex == 0) {
                finish();
            } else {
                pageIndex -= 1;
                setCurrent(pageIndex);
            }
        });

        mBaseBinding.titleView.setRightFraClickListener(view -> {
            int limit = 9;
            if (TextUtils.equals(mData.getShopWay(), "2")) { //二手车
                limit = 10;
            }

            if (pageIndex != limit) {
                pageIndex += 1;
                setCurrent(pageIndex);
            } else {

//                apply();

                EventBus.getDefault().post(new BudgetCheckModel()
                        .setCheckIndex(0)
                        .setCheckResult(true)
                        .setCheckFail(null));
            }
        });

    }


    public void getNode() {
        if (TextUtils.isEmpty(code))
            return;

        Map<String, String> map = RetrofitUtils.getRequestMap();

        map.put("code", code);

        showLoadingDialog();

        Call call = RetrofitUtils.createApi(MyApiServer.class).getNode("632146", StringUtils.getJsonToString(map));
        addCall(call);

        call.enqueue(new BaseResponseModelCallBack<NodeListModel>(this) {
            @Override
            protected void onSuccess(NodeListModel data, String SucMessage) {

                mData = data;

                init();
                initViewPager();
                setCurrent(pageIndex);
            }

            @Override
            protected void onFinish() {
                disMissLoading();
            }
        });
    }

    private void init() {

        if (TextUtils.equals(mData.getShopWay(), "2")) { //二手车
            pageList = new String[]{"预算单信息", "职业及收入情况", "资产情况", "紧急联系人", "其他情况", "费用情况"
                    , "贷款材料", "家访材料", "企业照片", "二手车照片", "其他材料"};

        } else {
            pageList = new String[]{"预算单信息", "职业及收入情况", "资产情况", "紧急联系人", "其他情况", "费用情况"
                    , "贷款材料", "家访材料", "企业照片", "其他材料"};
        }

    }


    private void initViewPager() {

        fragments.add(JoinStep1Fragment.getInstance(code));
        fragments.add(JoinStep2Fragment.getInstance(code));
        fragments.add(JoinStep3Fragment.getInstance(code));


        fragments.add(JoinStep11Fragment.getInstance(code));


        fragments.add(JoinStep4Fragment.getInstance(code));
        fragments.add(JoinStep5Fragment.getInstance(code));
        fragments.add(JoinStep6Fragment.getInstance(code));
        fragments.add(JoinStep7Fragment.getInstance(code));
        fragments.add(JoinStep8Fragment.getInstance(code));

        if (TextUtils.equals(mData.getShopWay(), "2")) { //二手车
            // 二手车
            fragments.add(JoinStep9Fragment.getInstance(code));

        }

        fragments.add(JoinStep10Fragment.getInstance(code));

        FragmentPagerAdapter mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };

        mBinding.vpStep.setAdapter(mAdapter);
        mBinding.vpStep.setOffscreenPageLimit(11);

        mBaseBinding.titleView.setMidImgVisible(true);
    }

    private void setCurrent(int current) {
        pageIndex = current;

        mBinding.vpStep.setCurrentItem(current, true);
        changeTitleView(current);
    }

    private void changeTitleView(int current) {

        mBaseBinding.titleView.setLeftImg(0);
        mBaseBinding.titleView.setLeftTitle("上一页");
        mBaseBinding.titleView.setRightTitle("下一页");
        mBaseBinding.titleView.setMidTitle(pageList[current]);

        if (current == 0) {
            mBaseBinding.titleView.setLeftTitle(null);
            mBaseBinding.titleView.setLeftImg(com.cdkj.baselibrary.R.drawable.back_img);
        }

        if (TextUtils.equals(mData.getShopWay(), "2")) { //二手车
            if (current == 10) {
                //增加了一个  紧急联系人  所以各+1
                if (isDetails) {
                    //查看详情  不能进行申请操作
                    mBaseBinding.titleView.setRightTitle("");
                } else {

                    mBaseBinding.titleView.setRightTitle("申请");
                }
            }

        } else {
            if (current == 9) {
                if (isDetails) {
                    //查看详情  不能进行申请操作
                    mBaseBinding.titleView.setRightTitle("");
                } else {
                    mBaseBinding.titleView.setRightTitle("申请");
                }
            }
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = fragments.get(pageIndex);
        fragment.onActivityResult(requestCode, resultCode, data);

    }

    /**
     * 在最后一个界面点击了保存按钮去 保存
     *
     * @param asveAll
     */
    @Subscribe
    public void doCheckTip(String asveAll) {
        if (TextUtils.equals(SAVEALL, asveAll)) {
            apply("0");
        }
    }

    @Subscribe
    public void doCheckTip(BudgetCheckModel model) {
        if (model == null)
            return;


        if (model.isCheckResult()) {

            if (model.getCheckIndex() == 6) { // 表示所有需要检查的元素已经全部检查通过

                apply("1");
            }

        } else {

            showDoubleWarnListen("请将《" + pageList[0] + "》里的\n" + model.getCheckFail() + "\n内容补充完整", view1 -> {
                pageIndex = model.getCheckIndex();
                setCurrent(pageIndex);
            });

        }

    }

    /**
     * 处理类型(0 保存 1 发送)
     * @param dealType
     */
    private void apply(String dealType) {
        Map<String, Object> map = new HashMap<>();

        map.put("budgetOrderCode", code);
        map.put("creditCode", mData.getCreditCode());
        map.put("operator", SPUtilHelper.getUserId());

        map.putAll(((JoinStep1Fragment) fragments.get(0)).getData());
        map.putAll(((JoinStep2Fragment) fragments.get(1)).getData());
        map.putAll(((JoinStep3Fragment) fragments.get(2)).getData());
        map.putAll(((JoinStep11Fragment) fragments.get(3)).getData());
        map.putAll(((JoinStep4Fragment) fragments.get(4)).getData());
        map.putAll(((JoinStep5Fragment) fragments.get(5)).getData());
        map.putAll(((JoinStep6Fragment) fragments.get(6)).getData());
        map.putAll(((JoinStep7Fragment) fragments.get(7)).getData());
        map.putAll(((JoinStep8Fragment) fragments.get(8)).getData());


        if (TextUtils.equals(mData.getShopWay(), "2")) { //二手车
            map.putAll(((JoinStep9Fragment) fragments.get(9)).getData());
            map.putAll(((JoinStep10Fragment) fragments.get(10)).getData());
        } else {
            map.putAll(((JoinStep10Fragment) fragments.get(9)).getData());
        }

        map.put("type", isOutside ? "2" : "1");
        map.put("dealType", dealType);//处理类型(0 保存 1 发送)

        //打印map
//        Iterator itor = map.keySet().iterator();
//        while (itor.hasNext()) {
//            String key = (String) itor.next();
//            Object value = map.get(key);
//            Log.i("pppppp", "apply: key->" + key + "  value->" + value);
//        }
        showLoadingDialog();

        mSubscription.add(Observable.just("").
                subscribeOn(Schedulers.newThread())
                .map(json -> {
                    return StringUtils.getJsonToString(map);
//                    try {
//                        //下面这个可供出错时测试使用
//                        Exclude ex = new Exclude();
//                        Gson gson = new GsonBuilder().addDeserializationExclusionStrategy(ex).addSerializationExclusionStrategy(ex).create();
//                        String jsonString = gson.toJson(map);
//                        Log.i("pppppp", "apply: 哈哈哈" +jsonString);
//                    } catch (Exception e) {
//                        LogUtil.E("转换出错" + e.getMessage());
//                        return e.getMessage();
//                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(json -> {

                    Call call = RetrofitUtils.getBaseAPiService().successRequest("632120", json);
                    addCall(call);

                    call.enqueue(new BaseResponseModelCallBack<IsSuccessModes>(JoinApplyActivity.this) {
                        @Override
                        protected void onSuccess(IsSuccessModes data, String SucMessage) {
                            if (data.isSuccess()) {
                                UITipDialog.showSuccess(JoinApplyActivity.this, "操作成功", dialogInterface -> {
                                    finish();
                                });
                            } else {
                                UITipDialog.showFail(JoinApplyActivity.this, "操作失败");
                            }
                        }

                        @Override
                        protected void onReqFailure(String errorCode, String errorMessage) {
//                            super.onReqFailure(errorCode, errorMessage);
                            ToastUtil.show(JoinApplyActivity.this, "失败" + errorMessage);
                        }

                        @Override
                        protected void onFinish() {
                            disMissLoading();
                        }
                    });
                }, throwable -> {
                    Log.e("ppppp", "apply: 转换失败了" + throwable.getMessage());
                    UITipDialog.showInfo(this, "未知异常");
                    disMissLoading();
                }));


    }


    public String getCjtx() {
        return ((JoinStep1Fragment) fragments.get(0)).getCarDealerSubsidy();
    }

    public DealersModel getDealers() {
        return ((JoinStep1Fragment) fragments.get(0)).getDealers();
    }

    public Boolean getIsAdvanceFund() {
        return ((JoinStep1Fragment) fragments.get(0)).getIsAdvanceFund();
    }


    class Exclude implements ExclusionStrategy {

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes field) {
            SerializedName ns = field.getAnnotation(SerializedName.class);
            if (ns != null)
                return false;
            return true;
        }
    }

}
