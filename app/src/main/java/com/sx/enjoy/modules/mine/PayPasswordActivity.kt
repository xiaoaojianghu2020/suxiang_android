package com.sx.enjoy.modules.mine

import android.view.Gravity
import com.sx.enjoy.R
import com.sx.enjoy.base.BaseActivity
import com.sx.enjoy.bean.UserBean
import com.sx.enjoy.constans.C
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.utils.EncryptionUtil
import com.sx.enjoy.view.dialog.NoticeDialog
import kotlinx.android.synthetic.main.activity_pay_password.*
import org.jetbrains.anko.toast
import org.litepal.LitePal

class PayPasswordActivity : BaseActivity()  , SXContract.View{

    private lateinit var noticeDialog: NoticeDialog

    private lateinit var present : SXPresent

    override fun getTitleType() = PublicTitleData(C.TITLE_NORMAL,"交易密码")

    override fun getLayoutResource() = R.layout.activity_pay_password

    override fun beForSetContentView() {
        present = SXPresent(this)
    }

    override fun initView() {
        noticeDialog = NoticeDialog(this)

        noticeDialog.setOnDismissListener {
            finish()
        }
        tv_password_save.setOnClickListener {
            if(et_new_password_1.text.isEmpty()){
                toast("请输入新密码").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            if(et_new_password_2.text.isEmpty()){
                toast("请再次输入新密码").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            if(et_new_password_1.text.toString()!=et_new_password_2.text.toString()){
                toast("密码不一致").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            if(et_old_password.text.isNotEmpty()&&et_old_password.text.toString() == et_new_password_1.text.toString()){
                toast("新密码不可与旧密码一致").setGravity(Gravity.CENTER, 0, 0)
                return@setOnClickListener
            }
            present.updatePayPassword(C.USER_ID,
                EncryptionUtil.MD5(et_old_password.text.toString()),EncryptionUtil.MD5(et_new_password_1.text.toString()),
                EncryptionUtil.MD5(et_new_password_2.text.toString()))
        }
    }

    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.UPDATEPAYPASSWORD -> {
                    val user = LitePal.findLast(UserBean::class.java)
                    if(user.isPayPwd != 1){
                        user.isPayPwd = 1
                        user.updateAll("userId = ?", user.userId)
                    }
                    noticeDialog.showNotice(2)
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