package com.sx.enjoy.modules.store

import android.content.Intent
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.sx.enjoy.R
import com.sx.enjoy.adapter.ShopCartAdapter
import com.sx.enjoy.base.BaseActivity
import com.sx.enjoy.bean.CreateOrderBean
import com.sx.enjoy.bean.OrderSendBean
import com.sx.enjoy.bean.ShopCartBean
import com.sx.enjoy.constans.C
import com.sx.enjoy.event.ShopCartChangeEvent
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.view.SwipeItemLayout
import com.sx.enjoy.view.dialog.ReminderDialog
import kotlinx.android.synthetic.main.activity_shop_cart.*
import kotlinx.android.synthetic.main.empty_public_network.view.*
import kotlinx.android.synthetic.main.title_public_view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast

class ShopCartActivity : BaseActivity() ,SXContract.View{

    private lateinit var present: SXPresent

    private lateinit var reminderDialog: ReminderDialog

    private lateinit var emptyView : View
    private lateinit var errorView : View

    private lateinit var mAdapter: ShopCartAdapter

    private var isEdit = false
    private var isAllChecked = false

    override fun getTitleType() = PublicTitleData (C.TITLE_RIGHT_TEXT,"购物车","编辑",0,R.color.color_3A3A3A)

    override fun getLayoutResource() = R.layout.activity_shop_cart

    override fun beForSetContentView() {
        present = SXPresent(this)
    }

    override fun initView() {
        reminderDialog = ReminderDialog(this)

        mAdapter = ShopCartAdapter()
        rcy_shop_cart.layoutManager = LinearLayoutManager(this)
        rcy_shop_cart.adapter = mAdapter
        rcy_shop_cart.addOnItemTouchListener(object: SwipeItemLayout.OnSwipeItemTouchListener(this){})

        emptyView = View.inflate(this,R.layout.empty_public_view,null)
        errorView = View.inflate(this,R.layout.empty_public_network,null)
        mAdapter.isUseEmpty(false)

        initEvent()
        getShopCartList()
    }

    private fun initEvent(){
        tv_public_right.setOnClickListener {
            isEdit = !isEdit
            if(isEdit){
                tv_public_right.text = "完成"
                tv_shop_confirm.text = "删除"
                tv_total_money.visibility = View.INVISIBLE
            }else{
                tv_public_right.text = "编辑"
                tv_shop_confirm.text = "结算"
                tv_total_money.visibility = View.VISIBLE
            }
        }
        ll_all_checked.setOnClickListener {
            if(mAdapter.data.isEmpty()){
                return@setOnClickListener
            }
            isAllChecked = !isAllChecked
            tb_all_checked.isChecked = isAllChecked
            mAdapter.data.forEach {
                it.isSelected = isAllChecked
            }
            mAdapter.notifyDataSetChanged()
            selectTotalPrice()
        }

        swipe_refresh_layout.setOnRefreshListener {
            getShopCartList()
        }

        tv_shop_confirm.setOnClickListener {
            if(isEdit){
                var isSelected = false
                mAdapter.data.forEach {
                    if(it.isSelected){
                        isSelected = true
                        return@forEach
                    }
                }
                if(isSelected){
                    reminderDialog.showReminder(3)
                }
            }else{
                val ol = arrayListOf<OrderSendBean>()
                mAdapter.data.forEach {
                    if(it.isSelected){
                        val os =  OrderSendBean()
                        os.shoppingCarId = it.id
                        os.name = it.goodsName
                        os.price = String.format("%.2f", it.price)
                        os.number = it.goodsNumber.toString()
                        os.image = it.firstImage
                        os.specName = it.constitute
                        ol.add(os)
                    }
                }
                if(ol.isNotEmpty()){
                    val order = CreateOrderBean(ol)
                    startActivityForResult<OrderConfirmActivity>(4001,Pair("shopList",order))
                }
            }
        }

        mAdapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id){
                R.id.ll_shop_cart -> {
                    startActivity<CommodityActivity>(Pair("commodityId",mAdapter.data[position].goodsId))
                }
                R.id.ll_shop_checked -> {
                    mAdapter.data[position].isSelected = !mAdapter.data[position].isSelected
                    for(i in mAdapter.data.indices){
                        if(mAdapter.data[i].isSelected != isAllChecked){
                            isAllChecked = !isAllChecked
                            break
                        }
                    }
                    mAdapter.notifyDataSetChanged()
                    tb_all_checked.isChecked = isAllChecked
                    selectTotalPrice()
                }
                R.id.iv_shop_add -> {
                    present.addCommodityNumber(mAdapter.data[position].id,(mAdapter.data[position].goodsNumber+1).toString(),position,1)
                }
                R.id.iv_shop_sub -> {
                    if(mAdapter.data[position].goodsNumber>1){
                        present.addCommodityNumber(mAdapter.data[position].id,(mAdapter.data[position].goodsNumber-1).toString(),position,0)
                    }else{
                        toast("不能再减少了").setGravity(Gravity.CENTER, 0, 0)
                    }
                }
                R.id.tv_cart_delete -> {
                    present.deleteCommodityFromShopCart(arrayListOf(mAdapter.data[position].id))
                }
            }
        }
        reminderDialog.setOnNoticeConfirmListener(object :ReminderDialog.OnNoticeConfirmListener{
            override fun onConfirm() {
                val ids = arrayListOf<String>()
                mAdapter.data.forEach {
                    if(it.isSelected){
                        ids.add(it.id)
                    }
                }
                present.deleteCommodityFromShopCart(ids)
            }
        })

        errorView.iv_network_error.setOnClickListener {
            getShopCartList()
        }

    }

    private fun getShopCartList(){
        present.getMyShopCart(C.USER_ID)
    }

    private fun selectTotalPrice(){
        var totalPrice = 0.0
        var isSelect = false
        mAdapter.data.forEach {
            if(it.isSelected){
                totalPrice+=(it.goodsNumber*it.price)
                isSelect = true
            }
        }
        tv_total_money.text = "合计：¥${String.format("%.2f", totalPrice)}"
        tv_shop_confirm.setBackgroundColor(resources.getColor(if(isSelect) R.color.main_color else R.color.main_color_1))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK&&requestCode == 4001){
            getShopCartList()
        }
    }

    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.GETMYSHOPCART -> {
                    data?.let {
                        data as List<ShopCartBean>
                        mAdapter.isUseEmpty(true)
                        mAdapter.emptyView = emptyView
                        swipe_refresh_layout.finishRefresh()
                        mAdapter.setNewData(data)
                        tv_total_money.text = "合计：¥0.00"
                        isAllChecked = false
                        tb_all_checked.isChecked = false
                        tv_shop_confirm.setBackgroundColor(resources.getColor(R.color.main_color_1))
                    }
                }
                SXContract.DELETECOMMODITYFROMSHOPCART -> {
                    getShopCartList()
                    EventBus.getDefault().post(ShopCartChangeEvent(0))
                }
                else -> {

                }
            }
            if(flag.startsWith(SXContract.ADDCOMMODITYNUMBER)){
                val nData = flag.replace(SXContract.ADDCOMMODITYNUMBER,"")
                val tp = nData.split("-")
                if(tp[1].toInt() == 1){
                    mAdapter.data[tp[0].toInt()].goodsNumber++
                }else{
                    mAdapter.data[tp[0].toInt()].goodsNumber--
                }
                mAdapter.notifyDataSetChanged()
                if(mAdapter.data[tp[0].toInt()].isSelected){
                    selectTotalPrice()
                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        mAdapter.isUseEmpty(true)
        mAdapter.emptyView = emptyView
        swipe_refresh_layout.finishRefresh()
        toast(string!!).setGravity(Gravity.CENTER, 0, 0)
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        if(isRefreshList){
            swipe_refresh_layout.finishRefresh()
            mAdapter.isUseEmpty(true)
            mAdapter.emptyView = errorView
        }else{
            toast("请检查网络连接").setGravity(Gravity.CENTER, 0, 0)
        }
    }

}