package com.sx.enjoy.modules.mine

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import com.likai.lib.commonutils.LoadingDialog
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.sx.enjoy.R
import com.sx.enjoy.base.BaseActivity
import com.sx.enjoy.bean.TransactionOrderBean
import com.sx.enjoy.bean.UpLoadImageData
import com.sx.enjoy.bean.UpLoadImageList
import com.sx.enjoy.constans.C
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.utils.GlideEngine
import com.sx.enjoy.utils.ImageLoaderUtil
import com.sx.enjoy.utils.UpLoadImageUtil
import com.sx.enjoy.view.dialog.NoticeDialog
import com.sx.enjoy.view.dialog.SingleImageShowDialog
import com.yanzhenjie.permission.AndPermission
import kotlinx.android.synthetic.main.activity_transaction_details.*
import org.jetbrains.anko.toast

class TransactionDetailsActivity : BaseActivity() ,SXContract.View{

    private lateinit var noticeDialog : NoticeDialog
    private lateinit var loadingDialog : LoadingDialog
    private lateinit var singelImageDialog : SingleImageShowDialog


    private lateinit var present: SXPresent

    private var uploadTask : UpLoadImageUtil? = null

    private var transaction: TransactionOrderBean? = null

    private var type = 0
    private var marketId = ""
    private var photo = ""

    override fun beForSetContentView() {
        present = SXPresent(this)
    }

    override fun getTitleType() = PublicTitleData (C.TITLE_NORMAL,if(type == C.MARKET_ORDER_STATUS_BUY) "买入详情" else "卖出详情")

    override fun getLayoutResource() = R.layout.activity_transaction_details

    override fun initView() {
        type = intent.getIntExtra("type",0)
        marketId = intent.getStringExtra("marketId")

        noticeDialog = NoticeDialog(this)
        loadingDialog = LoadingDialog(this)
        singelImageDialog = SingleImageShowDialog(this)

        uploadTask = UpLoadImageUtil(this,present)

        present.getTransactionOrderDetails(marketId)

        initEvent()
    }

    private fun initEvent(){
        iv_upload_documents.setOnClickListener {
            if(type == C.MARKET_ORDER_STATUS_BUY){
                when(transaction?.status){
                    1 -> {
                        PictureSelector.create(this)
                            .openGallery(PictureMimeType.ofImage())
                            .imageSpanCount(3)
                            .selectionMode(PictureConfig.MULTIPLE)
                            .maxSelectNum(1)
                            .isCamera(true)
                            .loadImageEngine(GlideEngine.createGlideEngine())
                            .compress(true)
                            .hideBottomControls(true)
                            .minimumCompressSize(1024)
                            .forResult(1001)
                    }
                    2 ,3 -> {
                        singelImageDialog.showImage(transaction?.transaction)
                    }
                }
            }else{
                if(transaction?.status == 2||transaction?.status == 3){
                    singelImageDialog.showImage(transaction?.transaction)
                }
            }
        }

        tv_submit.setOnClickListener {
            if(type == C.MARKET_ORDER_STATUS_BUY){
                if(transaction?.status == 1){
                    if(transaction == null){
                        return@setOnClickListener
                    }
                    if(photo.isEmpty()){
                        toast("请上传支付凭证").setGravity(Gravity.CENTER, 0, 0)
                    }else{
                        val imageList = arrayListOf<UpLoadImageData>()
                        val logoImage = arrayListOf<UpLoadImageList>()
                        logoImage.add(UpLoadImageList(photo))
                        imageList.add(UpLoadImageData(logoImage,1,"支付凭证"))
                        uploadTask?.addImagesToSources(imageList)
                        uploadTask?.start()
                        loadingDialog.showLoading("上传中...")
                    }
                }
            }else{
                if(transaction?.status == 2){
                    present.confirmMarketOrder(C.USER_ID,transaction!!.richUserId,transaction!!.buyNum,transaction!!.orderNo,transaction!!.type.toString())
                }
            }
        }

        uploadTask?.setOnUploadImageResultListener { result, sources ->
            loadingDialog.dismiss()
            if(result){
                present.payMarketOrder(transaction!!.orderNo,sources[0].imageList[0].netPath)
            }
        }

        ll_other_phone.setOnClickListener {
            checkPermission(tv_other_phone.text.toString())
        }

        ll_sell_phone.setOnClickListener {
            checkPermission(tv_sell_phone.text.toString())
        }
    }

    @SuppressLint("MissingPermission")
    private fun checkPermission(tel:String){
        AndPermission.with(this)
            .runtime()
            .permission(Manifest.permission.CALL_PHONE)
            .onGranted { permissions ->
                val intent = Intent(Intent.ACTION_CALL)
                val data = Uri.parse("tel:$tel")
                intent.data = data
                startActivity(intent)
            }
            .onDenied { permissions ->

            }
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1001 -> {
                    val selectList = PictureSelector.obtainMultipleResult(data)
                    photo = selectList[0].compressPath
                    ImageLoaderUtil().displayImage(this,selectList[0].compressPath,iv_upload_documents)
                }
            }
        }
    }

    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.GETTRANSACTIONORDERDETAILS -> {
                    data?.let {
                        data as TransactionOrderBean
                        transaction = data
                        tv_market_price.text = "¥${data.amount}"
                        tv_market_count.text = data.buyNum
                        tv_total_money.text = "¥${data.buyAmountSum}"

                        if(type == C.MARKET_ORDER_STATUS_BUY){
                            when(data.status){
                                0 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_un_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.color_666666))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.color_666666))
                                    ll_sell_info.visibility = View.GONE
                                    ll_zfb_number.visibility = View.GONE
                                    ll_sell_phone.visibility = View.GONE
                                    ll_ali_upload.visibility = View.GONE
                                    ll_order_time.visibility = View.GONE
                                    ll_photo_upload.visibility = View.GONE
                                    ll_buy_user.visibility = View.GONE
                                    ll_upload_documents.visibility = View.GONE
                                    tv_submit.visibility = View.GONE
                                }
                                1 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    ImageLoaderUtil().displayHeadImage(this,data.sellUserImg,iv_user_head)
                                    tv_user_name.text = data.sellUserName
                                    tv_market_time.text = data.createTime
                                    tv_zfb_number.text = data.payNumber
                                    tv_sell_phone.text = data.sellUserPhone
                                    tv_zfb_upload.text = data.respTime
                                    tv_submit.text = "提交"

                                    ll_sell_info.visibility = View.VISIBLE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_order_time.visibility = View.GONE
                                    ll_photo_upload.visibility = View.GONE
                                    ll_buy_user.visibility = View.GONE
                                    ll_upload_documents.visibility = View.VISIBLE
                                    tv_submit.visibility = View.VISIBLE
                                }
                                2 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    ImageLoaderUtil().displayHeadImage(this,data.sellUserImg,iv_user_head)
                                    ImageLoaderUtil().displayImage(this,data.transaction,iv_upload_documents)
                                    tv_user_name.text = data.sellUserName
                                    tv_market_time.text = data.createTime
                                    tv_zfb_number.text = data.payNumber
                                    tv_sell_phone.text = data.sellUserPhone
                                    tv_zfb_upload.text = data.respTime
                                    tv_photo_upload.text = data.payTime

                                    ll_sell_info.visibility = View.VISIBLE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_order_time.visibility = View.GONE
                                    ll_photo_upload.visibility = View.VISIBLE
                                    ll_buy_user.visibility = View.GONE
                                    ll_upload_documents.visibility = View.VISIBLE
                                    tv_submit.visibility = View.GONE
                                }
                                3 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_70DC7D))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    ImageLoaderUtil().displayHeadImage(this,data.sellUserImg,iv_user_head)
                                    ImageLoaderUtil().displayImage(this,data.transaction,iv_upload_documents)
                                    tv_user_name.text = data.sellUserName
                                    tv_market_time.text = data.createTime
                                    tv_zfb_number.text = data.payNumber
                                    tv_sell_phone.text = data.sellUserPhone
                                    tv_zfb_upload.text = data.respTime
                                    tv_photo_upload.text = data.payTime

                                    ll_sell_info.visibility = View.VISIBLE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_order_time.visibility = View.GONE
                                    ll_photo_upload.visibility = View.VISIBLE
                                    ll_buy_user.visibility = View.GONE
                                    ll_upload_documents.visibility = View.VISIBLE
                                    tv_submit.visibility = View.GONE
                                }
                            }
                        }else{
                            when(data.status){
                                0 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_un_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.color_666666))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.color_666666))
                                    tv_zfb_number.text = data.payNumber
                                    tv_order_time.text = data.createTime


                                    ll_sell_info.visibility = View.GONE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.GONE
                                    ll_order_time.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.GONE
                                    ll_photo_upload.visibility = View.GONE
                                    ll_buy_user.visibility = View.GONE
                                    ll_upload_documents.visibility = View.GONE
                                    tv_submit.visibility = View.GONE
                                }
                                1 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    tv_zfb_number.text = data.payNumber
                                    tv_order_time.text = data.createTime
                                    tv_zfb_upload.text = if(data.type == data.orderType) data.createTime else data.respTime
                                    tv_photo_upload.text = data.payTime
                                    ImageLoaderUtil().displayHeadImage(this,data.headImage,iv_other_head)
                                    tv_other_name.text = data.userName
                                    tv_other_phone.text = data.userPhone

                                    ll_sell_info.visibility = View.GONE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.GONE
                                    ll_order_time.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_photo_upload.visibility = View.GONE
                                    ll_buy_user.visibility = View.VISIBLE
                                    ll_upload_documents.visibility = View.GONE
                                    tv_submit.visibility = View.GONE
                                }
                                2 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_un_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_666666))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    ImageLoaderUtil().displayHeadImage(this,data.headImage,iv_other_head)
                                    ImageLoaderUtil().displayImage(this,data.transaction,iv_upload_documents)
                                    tv_zfb_number.text = data.payNumber
                                    tv_order_time.text = data.createTime
                                    tv_zfb_upload.text = if(data.type == data.orderType) data.createTime else data.respTime
                                    tv_photo_upload.text = data.payTime
                                    tv_other_name.text = data.userName
                                    tv_other_phone.text = data.userPhone
                                    tv_submit.text = "确认完成"

                                    ll_sell_info.visibility = View.GONE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.GONE
                                    ll_order_time.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_photo_upload.visibility = View.VISIBLE
                                    ll_buy_user.visibility = View.VISIBLE
                                    ll_upload_documents.visibility = View.VISIBLE
                                    tv_submit.visibility = View.VISIBLE
                                }
                                3 -> {
                                    iv_transaction_status_1.setImageResource(R.mipmap.ic_market_graphed)
                                    iv_transaction_status_2.setImageResource(R.mipmap.ic_market_complete)
                                    tv_transaction_status_1.setTextColor(resources.getColor(R.color.main_color))
                                    tv_transaction_status_2.setTextColor(resources.getColor(R.color.color_70DC7D))
                                    v_transaction_status.setBackgroundColor(resources.getColor(R.color.main_color))
                                    ImageLoaderUtil().displayHeadImage(this,data.headImage,iv_other_head)
                                    ImageLoaderUtil().displayImage(this,data.transaction,iv_upload_documents)
                                    tv_zfb_number.text = data.payNumber
                                    tv_order_time.text = data.createTime
                                    tv_zfb_upload.text = if(data.type == data.orderType) data.createTime else data.respTime
                                    tv_photo_upload.text = data.payTime
                                    tv_other_name.text = data.userName
                                    tv_other_phone.text = data.userPhone

                                    ll_sell_info.visibility = View.GONE
                                    ll_zfb_number.visibility = View.VISIBLE
                                    ll_sell_phone.visibility = View.GONE
                                    ll_order_time.visibility = View.VISIBLE
                                    ll_ali_upload.visibility = View.VISIBLE
                                    ll_photo_upload.visibility = View.VISIBLE
                                    ll_buy_user.visibility = View.VISIBLE
                                    ll_upload_documents.visibility = View.VISIBLE
                                    tv_submit.visibility = View.GONE
                                }
                            }
                        }
                    }
                }
                SXContract.PAYMARKETORDER -> {
                    noticeDialog.showNotice(7)
                    present.getTransactionOrderDetails(marketId)
                }
                SXContract.CONFIRMMARKETORDER -> {
                    noticeDialog.showNotice(9)
                    present.getTransactionOrderDetails(marketId)
                    setResult(RESULT_OK)
                }
                else -> {

                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        toast(string!!).setGravity(Gravity.CENTER, 0, 0)
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        if(boolean){
            toast("请检查网络连接").setGravity(Gravity.CENTER, 0, 0)
        }
    }

}