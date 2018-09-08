package com.cdkj.wzcd.module.work.join_approval.page;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cdkj.baselibrary.base.BaseLazyFragment;
import com.cdkj.baselibrary.utils.CameraHelper;
import com.cdkj.baselibrary.utils.QiNiuHelper;
import com.cdkj.wzcd.R;
import com.cdkj.wzcd.databinding.FragmentJoinStep6Binding;
import com.cdkj.wzcd.model.NodeListModel;
import com.cdkj.wzcd.module.work.join_approval.JoinApplyActivity;
import com.cdkj.wzcd.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.cdkj.baselibrary.appmanager.CdRouteHelper.DATA_SIGN;

/**
 * Created by cdkj on 2018/6/7.
 */

public class JoinStep6Fragment extends BaseLazyFragment {

    private String code;
    private FragmentJoinStep6Binding mBinding;

    private NodeListModel data;

    public static JoinStep6Fragment getInstance(String code) {
        JoinStep6Fragment fragment = new JoinStep6Fragment();
        Bundle bundle = new Bundle();
        bundle.putString(DATA_SIGN, code);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_join_step6, null, false);

        if (getArguments() != null) {
            code = getArguments().getString(DATA_SIGN);
        }

        data = ((JoinApplyActivity) mActivity).mData;

        initView();
        setView();

        return mBinding.getRoot();
    }

    @Override
    protected void lazyLoad() {

    }

    @Override
    protected void onInvisible() {

    }

    private void initView() {
        mBinding.myMlMarryDivorce.build(mActivity, 10, 0);
        mBinding.myMlApplyUserHkb.build(mActivity, 10, 1);
        mBinding.myMlBankBillPdf.build(mActivity, 10, 2);
        mBinding.myMlSingleProvePdf.build(mActivity, 10, 3);
        mBinding.myMlIncomeProvePdf.build(mActivity, 10, 4);
        mBinding.myMlLiveProvePdf.build(mActivity, 10, 5);
        mBinding.myMlHouseInvoice.build(mActivity, 10, 6);
        mBinding.myMlBuildProvePdf.build(mActivity, 10, 7);
        mBinding.myMlHkbFirstPage.build(mActivity, 10, 8);
        mBinding.myMlHkbMainPage.build(mActivity, 10, 9);
        mBinding.myMlGuarantor1IdNo.build(mActivity, 10, 10);
        mBinding.myMlGuarantor1Hkb.build(mActivity, 10, 11);
        mBinding.myMlGuarantor2IdNo.build(mActivity, 10, 12);
        mBinding.myMlGuarantor2Hkb.build(mActivity, 10, 13);
        mBinding.myMlGhHkb.build(mActivity, 10, 14);
        mBinding.myMlGhID.build(mActivity, 10, 15);
    }

    private void setView() {
        if (((JoinApplyActivity) mActivity).isDetails) {

            //结婚离婚证
            mBinding.myMlMarryDivorce.addListRequest(StringUtils.splitPIC(data.getMarryDivorce()));
            //户口本
            mBinding.myMlApplyUserHkb.addListRequest(StringUtils.splitPIC(data.getApplyUserHkb()));
            //银行流水
            mBinding.myMlBankBillPdf.addListRequest(StringUtils.splitPIC(data.getBankBillPdf()));
            //单身证明
            mBinding.myMlSingleProvePdf.addListRequest(StringUtils.splitPIC(data.getSingleProvePdf()));
            //收入证明
            mBinding.myMlIncomeProvePdf.addListRequest(StringUtils.splitPIC(data.getIncomeProvePdf()));
            //居住证明
            mBinding.myMlLiveProvePdf.addListRequest(StringUtils.splitPIC(data.getLiveProvePdf()));
            //购房发票
            mBinding.myMlHouseInvoice.addListRequest(StringUtils.splitPIC(data.getHouseInvoice()));
            //自建房证明
            mBinding.myMlBuildProvePdf.addListRequest(StringUtils.splitPIC(data.getBuildProvePdf()));
            //户口本首页
            mBinding.myMlHkbFirstPage.addListRequest(StringUtils.splitPIC(data.getHkbFirstPage()));
            //户口本主页
            mBinding.myMlHkbMainPage.addListRequest(StringUtils.splitPIC(data.getHkbMainPage()));
            //担保人1省份证
            mBinding.myMlGuarantor1IdNo.addListRequest(StringUtils.splitPIC(data.getGuarantor1IdNo()));
            //担保人1户口本
            mBinding.myMlGuarantor1Hkb.addListRequest(StringUtils.splitPIC(data.getGuarantor1Hkb()));
            //担保人2省份证
            mBinding.myMlGuarantor2IdNo.addListRequest(StringUtils.splitPIC(data.getGuarantor2IdNo()));
            //担保人2户口本
            mBinding.myMlGuarantor2Hkb.addListRequest(StringUtils.splitPIC(data.getGuarantor2Hkb()));
            //共还人身份证  ghIdNo
            mBinding.myMlGhID.addListRequest(StringUtils.splitPIC(data.getGhIdNo()));
            //共还人户口本
            mBinding.myMlGhHkb.addListRequest(StringUtils.splitPIC(data.getGhHkb()));


        } else {

            if (data.getCredit() != null) {
                List<NodeListModel.Credit.CreditUserListBean> creditUserList = data.getCredit().getCreditUserList();
                if (creditUserList != null && creditUserList.size() != 0) {
                    List<NodeListModel.Credit.CreditUserListBean> danbaorenList = new ArrayList<>();

                    for (NodeListModel.Credit.CreditUserListBean item : creditUserList) {
//                    if (true)
//                        return;
                        if (TextUtils.equals(item.getLoanRole(), "1")) {
                            //申请人本人
//                        mBinding.myMlApplyUserHkb

                        } else if (TextUtils.equals(item.getLoanRole(), "2")) {
                            //共同还款人
                            mBinding.myMlGhID.addList(item.getIdNoFront());
                            mBinding.myMlGhID.addList(item.getIdNoReverse());
                            mBinding.myMlGhID.addList(item.getInterviewPic());

                        } else if (TextUtils.equals(item.getLoanRole(), "3")) {
                            //担保人
                            danbaorenList.add(item);
                        }
                    }

                    if (danbaorenList.size() != 0) {
                        for (int i = 0; i < danbaorenList.size(); i++) {
                            if (i == 0) {
                                //担保人1
                                mBinding.myMlGuarantor1IdNo.addList(danbaorenList.get(i).getIdNoFront());
                                mBinding.myMlGuarantor1IdNo.addList(danbaorenList.get(i).getIdNoReverse());
                                mBinding.myMlGuarantor1IdNo.addList(danbaorenList.get(i).getInterviewPic());
                            } else if (i == 1) {
                                //担保人2
                                mBinding.myMlGuarantor2IdNo.addList(danbaorenList.get(i).getIdNoFront());
                                mBinding.myMlGuarantor2IdNo.addList(danbaorenList.get(i).getIdNoReverse());
                                mBinding.myMlGuarantor2IdNo.addList(danbaorenList.get(i).getInterviewPic());

                            } else {
                                break;
                            }
                        }
                    }
                }
            }

        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        String path = data.getStringExtra(CameraHelper.staticPath);
        showLoadingDialog();
        new QiNiuHelper(mActivity).uploadSinglePic(new QiNiuHelper.QiNiuCallBack() {
            @Override
            public void onSuccess(String key) {

                if (requestCode == mBinding.myMlMarryDivorce.getRequestCode()) {
                    mBinding.myMlMarryDivorce.addList(key);
                }

                if (requestCode == mBinding.myMlApplyUserHkb.getRequestCode()) {
                    mBinding.myMlApplyUserHkb.addList(key);
                }

                if (requestCode == mBinding.myMlBankBillPdf.getRequestCode()) {
                    mBinding.myMlBankBillPdf.addList(key);
                }

                if (requestCode == mBinding.myMlSingleProvePdf.getRequestCode()) {
                    mBinding.myMlSingleProvePdf.addList(key);
                }

                if (requestCode == mBinding.myMlIncomeProvePdf.getRequestCode()) {
                    mBinding.myMlIncomeProvePdf.addList(key);
                }

                if (requestCode == mBinding.myMlLiveProvePdf.getRequestCode()) {
                    mBinding.myMlLiveProvePdf.addList(key);
                }

                if (requestCode == mBinding.myMlHouseInvoice.getRequestCode()) {
                    mBinding.myMlHouseInvoice.addList(key);
                }

                if (requestCode == mBinding.myMlBuildProvePdf.getRequestCode()) {
                    mBinding.myMlBuildProvePdf.addList(key);
                }

                if (requestCode == mBinding.myMlHkbFirstPage.getRequestCode()) {
                    mBinding.myMlHkbFirstPage.addList(key);
                }

                if (requestCode == mBinding.myMlHkbMainPage.getRequestCode()) {
                    mBinding.myMlHkbMainPage.addList(key);
                }

                if (requestCode == mBinding.myMlGuarantor1IdNo.getRequestCode()) {
                    mBinding.myMlGuarantor1IdNo.addList(key);
                }

                if (requestCode == mBinding.myMlGuarantor1Hkb.getRequestCode()) {
                    mBinding.myMlGuarantor1Hkb.addList(key);
                }

                if (requestCode == mBinding.myMlGuarantor2IdNo.getRequestCode()) {
                    mBinding.myMlGuarantor2IdNo.addList(key);
                }

                if (requestCode == mBinding.myMlGuarantor2Hkb.getRequestCode()) {
                    mBinding.myMlGuarantor2Hkb.addList(key);
                }

                if (requestCode == mBinding.myMlGhHkb.getRequestCode()) {
                    mBinding.myMlGhHkb.addList(key);
                }
                if (requestCode == mBinding.myMlGhID.getRequestCode()) {
                    mBinding.myMlGhID.addList(key);
                }

                disMissLoading();

            }

            @Override
            public void onFal(String info) {
                disMissLoading();
            }
        }, path);
    }

    public Map<String, Object> getData() {

        Map<String, Object> map = new HashMap<>();

        map.put("marryDivorce", mBinding.myMlMarryDivorce.getListData());
        map.put("applyUserHkb", mBinding.myMlApplyUserHkb.getListData());
        map.put("bankBillPdf", mBinding.myMlBankBillPdf.getListData());
        map.put("singleProvePdf", mBinding.myMlSingleProvePdf.getListData());
        map.put("incomeProvePdf", mBinding.myMlIncomeProvePdf.getListData());
        map.put("liveProvePdf", mBinding.myMlLiveProvePdf.getListData());
        map.put("houseInvoice", mBinding.myMlHouseInvoice.getListData());
        map.put("buildProvePdf", mBinding.myMlBuildProvePdf.getListData());
        map.put("hkbFirstPage", mBinding.myMlHkbFirstPage.getListData());
        map.put("hkbMainPage", mBinding.myMlHkbMainPage.getListData());
        map.put("guarantor1IdNo", mBinding.myMlGuarantor1IdNo.getListData());
        map.put("guarantor1Hkb", mBinding.myMlGuarantor1Hkb.getListData());
        map.put("guarantor2IdNo", mBinding.myMlGuarantor2IdNo.getListData());
        map.put("guarantor2Hkb", mBinding.myMlGuarantor2Hkb.getListData());
        map.put("ghHkb", mBinding.myMlGhHkb.getListData());
        //自己加的 共还人身份证
        map.put("ghIdNo", mBinding.myMlGhID.getListData());

        return map;
    }
}
