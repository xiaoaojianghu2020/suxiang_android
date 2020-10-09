package com.sx.enjoy.modules.market

import com.sx.enjoy.R
import com.sx.enjoy.base.BaseActivity
import com.sx.enjoy.constans.C
import com.sx.enjoy.event.MarketSellSuccessEvent
import com.sx.enjoy.modules.mine.TransactionDetailsActivity
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.view.dialog.NoticeDialog
import kotlinx.android.synthetic.main.activity_sell_rice.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class SellRiceActivity : BaseActivity() ,SXContract.View{

    private lateinit var noticeDialog: NoticeDialog

    private lateinit var present: SXPresent

    override fun getTitleType() = PublicTitleData (C.TITLE_NORMAL,"我要卖出")

    override fun getLayoutResource() = R.layout.activity_sell_rice

    override fun beForSetContentView() {
        present = SXPresent(this)
    }

    override fun initView() {
        noticeDialog = NoticeDialog(this)

        val type = intent.getIntExtra("type",C.MARKET_ORDER_STATUS_BUY)
        val marketId = intent.getStringExtra("marketId")
        val amount = intent.getStringExtra("amount")
        val buyNum = intent.getStringExtra("buyNum")
        val orderNo = intent.getStringExtra("orderNo")

        tv_sell_rice.setOnClickListener {
            if(et_ali_number.text.isEmpty()){
                toast("请输入支付宝账号")
                return@setOnClickListener
            }
            present.createMarketOrder(C.USER_ID,type.toString(),amount,buyNum,et_ali_number.text.toString(),orderNo)
        }

        noticeDialog.setOnDismissListener {
            startActivity<TransactionDetailsActivity>(Pair("marketId",marketId),Pair("type",1))
            finish()
        }
    }

    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.CREATEMARKETORDER -> {
                    noticeDialog.showNotice(6)
                    EventBus.getDefault().post(MarketSellSuccessEvent(1))
                }
                else -> {

                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        toast(string!!)
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        if(boolean){
            toast("请检查网络连接")
        }
    }

}