package com.sx.enjoy.modules.market

import android.app.Activity
import android.view.Gravity
import com.sx.enjoy.R
import com.sx.enjoy.base.BaseActivity
import com.sx.enjoy.constans.C
import com.sx.enjoy.event.MarketSellSuccessEvent
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.view.dialog.NoticeDialog
import kotlinx.android.synthetic.main.activity_buy_in.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast

class BuyInActivity : BaseActivity() ,SXContract.View{

    private lateinit var present: SXPresent

    private lateinit var noticeDialog : NoticeDialog

    private var isSend = false

    override fun getTitleType() = PublicTitleData(C.TITLE_NORMAL,"我要买入")

    override fun getLayoutResource() = R.layout.activity_buy_in

    override fun initView() {
        noticeDialog = NoticeDialog(this)

        present = SXPresent(this)

        initEvent()
    }

    private fun initEvent(){
        tv_submit.setOnClickListener {
            if(et_rice_count.text.isEmpty()){
                toast("请输入买入数量").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            if(et_rice_price.text.isEmpty()){
                toast("请输入买入单价").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            if(!isSend){
                isSend = true
                present.publishMarketInfo(C.USER_ID,"0",et_rice_price.text.toString(),et_rice_count.text.toString(),"")
            }
        }

        noticeDialog.setOnDismissListener {
            finish()
        }

    }

    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.PUBLISHMARKETINFO -> {
                    isSend = false
                    noticeDialog.showNotice(3)
                    EventBus.getDefault().post(MarketSellSuccessEvent(0))
                }
                else -> {

                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        isSend = false
        toast(string!!).setGravity(Gravity.CENTER, 0, 0)
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        isSend = false
        if(boolean){
            toast("请检查网络连接").setGravity(Gravity.CENTER, 0, 0)
        }
    }

}