package com.cdkj.wzcd.module.work.join_approval.page;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdkj.baselibrary.base.BaseLazyFragment;
import com.cdkj.baselibrary.model.DataDictionary;
import com.cdkj.baselibrary.nets.BaseResponseListCallBack;
import com.cdkj.baselibrary.nets.RetrofitUtils;
import com.cdkj.baselibrary.utils.LogUtil;
import com.cdkj.baselibrary.utils.MoneyUtils;
import com.cdkj.baselibrary.utils.StringUtils;
import com.cdkj.wzcd.R;
import com.cdkj.wzcd.adpter.JoinApplyGpsAdapter;
import com.cdkj.wzcd.api.MyApiServer;
import com.cdkj.wzcd.databinding.FragmentJoinStep1Binding;
import com.cdkj.wzcd.model.DealersModel;
import com.cdkj.wzcd.model.GpsInstallModel;
import com.cdkj.wzcd.model.GpsInstallReplaceModel;
import com.cdkj.wzcd.model.NodeListModel;
import com.cdkj.wzcd.model.event.BudgetCheckModel;
import com.cdkj.wzcd.model.event.ChangeRePointList1Model;
import com.cdkj.wzcd.module.work.join_approval.GPSAddActivity;
import com.cdkj.wzcd.module.work.join_approval.JoinApplyActivity;
import com.cdkj.wzcd.util.DataDictionaryHelper;
import com.cdkj.wzcd.util.RequestUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

import static com.cdkj.baselibrary.appmanager.CdRouteHelper.DATA_SIGN;
import static com.cdkj.wzcd.util.DataDictionaryHelper.rate_type;

/**
 * Created by cdkj on 2018/6/7.
 */

public class JoinStep1Fragment extends BaseLazyFragment {

    private String code;
    private FragmentJoinStep1Binding mBinding;

    public boolean isOutside;
    private NodeListModel data;

    private JoinApplyGpsAdapter mAdapter;
    private List<GpsInstallModel> mList = new ArrayList<>();

    // 已选择的经销商
    private DealersModel mDealersModel;

    public static JoinStep1Fragment getInstance(String code) {
        JoinStep1Fragment fragment = new JoinStep1Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_SIGN, code);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_join_step1, null, false);

        if (getArguments() != null) {
            code = getArguments().getString(DATA_SIGN);
        }

        data = ((JoinApplyActivity) mActivity).mData;
        isOutside = ((JoinApplyActivity) mActivity).isOutside;

        setDealers();

        initAdapter();
        initView();
        setView();

        initTextWatcher();

        return mBinding.getRoot();
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    protected void onInvisible() {

    }

    private void initAdapter() {
        mAdapter = new JoinApplyGpsAdapter(mList);
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            GPSAddActivity.open(mActivity, mAdapter.getItem(position), position);
        });
    }


    /**
     * 需要初始化的布局：
     * 下拉框布局SelectLayout
     */
    private void initView() {
        List<DataDictionary> rateList = new ArrayList<>();

        List<String> periodList = new ArrayList<>();
        for (NodeListModel.BankSubbranchBean.BankBean.BankRateListBean bean : data.getBankSubbranch().getBank().getBankRateList()){

            if(!periodList.contains(bean.getPeriod()+"")){
                periodList.add(bean.getPeriod()+"");
            }

        }

        List<DataDictionary> period = new ArrayList<>();
        for (String str : periodList){
            period.add(new DataDictionary().setDkey(str).setDvalue(str+"期"));
        }
        mBinding.mySlLoanPeriod.setData(period,(dialog, which) -> {
            rateList.clear();
            for (NodeListModel.BankSubbranchBean.BankBean.BankRateListBean bean : data.getBankSubbranch().getBank().getBankRateList()){
                if (TextUtils.isEmpty(periodList.get(which))){
                    rateList.add(new DataDictionary().setDkey(bean.getRate()+"").setDvalue(bean.getRate()+""));
                }else {

                    if (TextUtils.equals(periodList.get(which), bean.getPeriod()+"")){
                        rateList.add(new DataDictionary().setDkey(bean.getRate()+"").setDvalue(bean.getRate()+""));
                    }

                }

            }
        });

        mBinding.mySlRateType.setData(DataDictionaryHelper.getListByParentKey(rate_type),(dialog, which) -> {

            if (which == 0){ // 如果利率类型为传统利率，则厂家贴息自动设为0
                mBinding.myElCarDealerSubsidy.setText("0");

            }

        });
        mBinding.mySlUserType.setData("1", "个人", "2", "企业", null);
        mBinding.mySlIsNeedCheck.setData("1", "是", "0", "否", null);
        mBinding.mySlIsAdvanceFund.setData("1", "是", "0", "否", (dialog, which) -> {
            EventBus.getDefault().post(new ChangeRePointList1Model());
        });


        for (NodeListModel.BankSubbranchBean.BankBean.BankRateListBean bean : data.getBankSubbranch().getBank().getBankRateList()){
            rateList.add(new DataDictionary().setDkey(bean.getRate()+"").setDvalue(bean.getRate()+""));
        }
        mBinding.myESlBankRate.setData(rateList,(dialog, which) -> {

            if (!TextUtils.isEmpty(mBinding.myESlBankRate.getDataKey())){

                // 贷款金额不为空且不为0
                if (RequestUtil.isEmpty(mBinding.myElLoanAmount.getText())){
                    return;
                }

                // 服务费不为空
                if (!TextUtils.isEmpty(mBinding.myElServicePrice.getText())){
                    return;
                }

                try {
                    String str = RequestUtil.div(mBinding.myElServicePrice.getText(), mBinding.myElLoanAmount.getText(), 2);
                    mBinding.myNlGlobalRate.setText(RequestUtil.add(str, mBinding.myESlBankRate.getDataKey()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }

        });

        mBinding.myRlGpsList.build(mAdapter, true, view -> {
            GPSAddActivity.open(mActivity);
        });

    }

    private void setDealers(){

        if (isOutside){

            mBinding.myElDealers.setVisibility(View.VISIBLE);
            mBinding.mySlDealers.setVisibility(View.GONE);

        }else {

            mBinding.myElDealers.setVisibility(View.GONE);
            mBinding.mySlDealers.setVisibility(View.VISIBLE);

            Map<String, String> map = RetrofitUtils.getNodeListMap();
            map.put("curNodeCode", "006_02");
            showLoadingDialog();
            Call call = RetrofitUtils.createApi(MyApiServer.class).getDealersList("632067", StringUtils.getJsonToString(map));
            addCall(call);

            call.enqueue(new BaseResponseListCallBack<DealersModel>(mActivity) {
                @Override
                protected void onSuccess(List<DealersModel> data, String SucMessage) {

                    if (data == null || data.size() == 0)
                        return;

                    List<DataDictionary> list = new ArrayList<>();
                    for (DealersModel model : data){
                        list.add(new DataDictionary().setDkey(model.getCode()).setDvalue(model.getParentGroup()+"-"+model.getAbbrName()));
                    }

                    mBinding.mySlDealers.setData(list, (dialog, which) -> {

                        mDealersModel = data.get(which);

                        EventBus.getDefault().post(new ChangeRePointList1Model());

                    });

                }

                @Override
                protected void onFinish() {
                    disMissLoading();
                }
            });

        }

    }

    private void setView() {

        mBinding.myNlName.setText(data.getCustomerName());
        mBinding.myNlCode.setText(data.getCode());
        mBinding.myNlBank.setText(data.getLoanBankName());
        mBinding.myNlType.setText(DataDictionaryHelper.getBizTypeBuyKey(data.getShopWay()));
        mBinding.myNlBankLoanCs.setText(MoneyUtils.doubleFormatMoney(data.getBankLoanCs()));
        mBinding.myNlCompanyLoanCs.setText(MoneyUtils.doubleFormatMoney(data.getCompanyLoanCs()));

        // setContentByKey 需要现设置SlLayout的数据List
        mBinding.mySlUserType.setContentByKey(data.getCustomerType());
        mBinding.mySlLoanPeriod.setContentByKey(data.getLoanPeriods());
        mBinding.mySlRateType.setContentByKey(data.getRateType());
        mBinding.mySlIsNeedCheck.setContentByKey(data.getIsSurvey());
        mBinding.mySlIsAdvanceFund.setContentByKey(data.getIsAdvanceFund());
        mBinding.myESlBankRate.setContentByKey(data.getBankRate()+"");

        mBinding.myElDealers.setText(data.getCarDealerName());
        mBinding.myElOriginalPrice.setMoneyText(data.getOriginalPrice());
        mBinding.myElCarNumber.setText(data.getCarModel());
        mBinding.myElBillPrice.setMoneyText(data.getInvoicePrice());
        mBinding.myElLoanAmount.setMoneyText(data.getLoanAmount());
        mBinding.myElServicePrice.setMoneyText(data.getFee());
        mBinding.myElCarDealerSubsidy.setMoneyText(data.getCarDealerSubsidy());

        if (data.getBudgetOrderGpsList() == null || data.getBudgetOrderGpsList().size() ==0)
            return;

        mList.clear();
        for (NodeListModel.BudgetOrderGpsListBean bean : data.getBudgetOrderGpsList()){

            GpsInstallModel model = new GpsInstallModel();
            model.setCode(bean.getCode());
            model.setGpsDevNo(bean.getGpsDevNo());
            model.setAzLocation(bean.getAzLocation());

            mList.add(model);
        }
        mAdapter.notifyDataSetChanged();
    }



    @Subscribe
    public void doAddCreditPerson(GpsInstallModel model){
        mList.add(model);
        mAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void doReplaceCreditPerson(GpsInstallReplaceModel model){

        mList.set(model.getLocation(), model.getGpsInstallModel());
        mAdapter.notifyDataSetChanged();

    }

    private String checkFail;

    public boolean check(){
        if (mBinding.mySlUserType.check()){
            checkFail = mBinding.mySlUserType.getTitle();
            return false;
        }

        if (mBinding.mySlLoanPeriod.check()){
            checkFail = mBinding.mySlLoanPeriod.getTitle();
            return false;
        }

        if (mBinding.mySlRateType.check()){
            checkFail = mBinding.mySlRateType.getTitle();
            return false;
        }

        if (mBinding.mySlIsNeedCheck.check()){
            checkFail = mBinding.mySlIsNeedCheck.getTitle();
            return false;
        }

        if (mBinding.myESlBankRate.check()){
            checkFail = mBinding.myESlBankRate.getTitle();
            return false;
        }

        if (mBinding.mySlIsAdvanceFund.check()){
            checkFail = mBinding.mySlIsAdvanceFund.getTitle();
            return false;
        }

        if (isOutside){

            if (mBinding.myElDealers.check()){
                checkFail = mBinding.myElDealers.getTitle();
                return false;
            }

        }else {

            if (mBinding.mySlDealers.check()){
                checkFail = mBinding.myElDealers.getTitle();
                return false;
            }
        }


        if (mBinding.myElOriginalPrice.check()){
            checkFail = mBinding.myElOriginalPrice.getTitle();
            return false;
        }

        if (mBinding.myElCarNumber.check()){
            checkFail = mBinding.myElCarNumber.getTitle();
            return false;
        }

        if (mBinding.myElFrameNo.check()){
            checkFail = mBinding.myElFrameNo.getTitle();
            return false;
        }

        if (mBinding.myElBillPrice.check()){
            checkFail = mBinding.myElBillPrice.getTitle();
            return false;
        }

        if (mBinding.myElLoanAmount.check()){
            checkFail = mBinding.myElLoanAmount.getTitle();
            return false;
        }

        if (mBinding.myElServicePrice.check()){
            checkFail = mBinding.myElServicePrice.getTitle();
            return false;
        }

        if (mBinding.myElCarDealerSubsidy.check()){
            checkFail = mBinding.myElCarDealerSubsidy.getTitle();
            return false;
        }

        return true;
    }


    @Subscribe
    public void doCheck(BudgetCheckModel model){

        if (model == null)
            return;

        // 检查未通过，由Activity提示，不往下check
        if (!model.isCheckResult()){
            return;
        }

        if (model.getCheckIndex() == 0){
            if (check()){

                EventBus.getDefault().post(new BudgetCheckModel()
                        .setCheckIndex(model.getCheckIndex()+1)
                        .setCheckResult(true)
                        .setCheckFail(null));

            }else {

                EventBus.getDefault().post(new BudgetCheckModel()
                        .setCheckIndex(model.getCheckIndex())
                        .setCheckResult(false)
                        .setCheckFail(checkFail));

            }
        }

    }

    public Map<String, Object> getData(){

        Map<String, Object> map = new HashMap<>();

        map.put("customerType",mBinding.mySlUserType.getDataKey());
        map.put("loanPeriods",mBinding.mySlLoanPeriod.getDataKey());
        map.put("rateType",mBinding.mySlRateType.getDataKey());
        map.put("isSurvey",mBinding.mySlIsNeedCheck.getDataKey());
        map.put("bankRate",mBinding.myESlBankRate.getDataKey());
        map.put("isAdvanceFund",mBinding.mySlIsAdvanceFund.getDataKey());

        map.put("bankLoanCs",mBinding.myNlBankLoanCs.getText());
        map.put("companyLoanCs",mBinding.myNlCompanyLoanCs.getText());
        map.put("globalRate",mBinding.myNlGlobalRate.getText());

        if (isOutside){
            map.put("outCarDealerName",mBinding.myElDealers.getText());
        }else {
            map.put("carDealerCode",mBinding.mySlDealers.getDataKey());
        }

        map.put("originalPrice",mBinding.myElOriginalPrice.getMoneyText());
        map.put("carModel",mBinding.myElCarNumber.getText());
        map.put("frameNo",mBinding.myElFrameNo.getText());
        map.put("invoicePrice",mBinding.myElBillPrice.getMoneyText());
        map.put("loanAmount",mBinding.myElLoanAmount.getMoneyText());
        map.put("fee",mBinding.myElServicePrice.getMoneyText());
        map.put("carDealerSubsidy",mBinding.myElCarDealerSubsidy.getMoneyText());
        map.put("gpsList",mList);

        return map;
    }


    private void initTextWatcher(){

        mBinding.myElLoanAmount.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!TextUtils.isEmpty(editable.toString())){

                    // 发票金额不为空且不为0
                    if (!RequestUtil.isEmpty(mBinding.myElBillPrice.getText())){
                        try {
                            mBinding.myNlCompanyLoanCs.setText(RequestUtil.div(editable.toString(), mBinding.myElBillPrice.getText(), 2));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    // 服务费不为空
                    if (!TextUtils.isEmpty(mBinding.myElServicePrice.getText())){

                        // 发票金额不为空
                        if (!RequestUtil.isEmpty(mBinding.myElBillPrice.getText())){
                            try {

                                String str = RequestUtil.add(mBinding.myElServicePrice.getText(), editable.toString());
                                mBinding.myNlBankLoanCs.setText(RequestUtil.div(str, mBinding.myElBillPrice.getText(), 2));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }

                if (!RequestUtil.isEmpty(editable.toString())){
                    // 服务费不为空
                    if (!TextUtils.isEmpty(mBinding.myElServicePrice.getText())){

                        // 银行利率不为空
                        if (!RequestUtil.isEmpty(mBinding.myESlBankRate.getDataKey())){

                            try {
                                String str = RequestUtil.div(mBinding.myElServicePrice.getText(), editable.toString(), 2);

                                LogUtil.E("str = " + str);
                                LogUtil.E("BankRate = " + mBinding.myESlBankRate.getDataKey());

                                mBinding.myNlGlobalRate.setText(RequestUtil.add(str, mBinding.myESlBankRate.getDataKey()));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                        }

                    }


                }

            }
        });

        mBinding.myElBillPrice.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!RequestUtil.isEmpty(editable.toString())){

                    // 贷款金额不为空
                    if (!TextUtils.isEmpty(mBinding.myElLoanAmount.getText())){

                        try {
                            mBinding.myNlCompanyLoanCs.setText(RequestUtil.div(mBinding.myElLoanAmount.getText(), editable.toString(), 2));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }


                        // 服务费金额不为空
                        if (!TextUtils.isEmpty(mBinding.myElServicePrice.getText())){
                            try {
                                String str = RequestUtil.add(mBinding.myElLoanAmount.getText(), mBinding.myElServicePrice.getText());
                                mBinding.myNlBankLoanCs.setText(RequestUtil.div(str, editable.toString(), 2));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }

                    }

                }

            }
        });

        mBinding.myElServicePrice.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if (!TextUtils.isEmpty(editable)){

                    // 发票金额不为空且不为0
                    if (!RequestUtil.isEmpty(mBinding.myElBillPrice.getText())){
                        // 贷款金额不为空
                        if (TextUtils.isEmpty(mBinding.myElLoanAmount.getText())){
                            return;
                        }

                        try {
                            String str = RequestUtil.add(mBinding.myElLoanAmount.getText(), editable.toString());
                            mBinding.myNlBankLoanCs.setText(RequestUtil.div(str, mBinding.myElBillPrice.getText(), 2));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }

                    // 贷款金额不为空且不为0
                    if (!RequestUtil.isEmpty(mBinding.myElLoanAmount.getText())){
                        // 银行利率不为空
                        if (TextUtils.isEmpty(mBinding.myESlBankRate.getDataKey())){
                            return;
                        }

                        try {
                            String str = RequestUtil.div(editable.toString(), mBinding.myElLoanAmount.getText(), 2);
                            mBinding.myNlGlobalRate.setText(RequestUtil.add(str, mBinding.myESlBankRate.getDataKey()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }


                }

            }
        });

        mBinding.myElCarDealerSubsidy.setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!TextUtils.isEmpty(editable)){

                    if (isOutside){

                        if (!TextUtils.isEmpty(mBinding.myElDealers.getText()))
                            EventBus.getDefault().post(new ChangeRePointList1Model());

                    }else {

                        if (!TextUtils.isEmpty(mBinding.mySlDealers.getDataKey()))
                            EventBus.getDefault().post(new ChangeRePointList1Model());
                    }

                }
            }
        });

    }


    public String getCarDealerSubsidy(){
        return TextUtils.isEmpty(mBinding.myElCarDealerSubsidy.getMoneyText()) ? "0" : mBinding.myElCarDealerSubsidy.getMoneyText();
    }

    public DealersModel getDealers(){
        return mDealersModel;
    }

    public Boolean getIsAdvanceFund(){

        if (mBinding.mySlIsAdvanceFund.getSelectIndex() == -1){
            return true;
        }else {

            if (TextUtils.equals(mBinding.mySlIsAdvanceFund.getDataKey(), "1")){
                return true;
            }else {
                return false;
            }

        }

    }
}