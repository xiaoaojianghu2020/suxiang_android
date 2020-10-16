package com.sx.enjoy.modules.home

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bumptech.glide.Glide
import com.gyf.immersionbar.ImmersionBar
import com.likai.lib.base.BaseFragment
import com.likai.lib.commonutils.DensityUtils
import com.likai.lib.commonutils.SharedPreferencesUtil
import com.sx.enjoy.R
import com.sx.enjoy.adapter.HomeListAdapter
import com.sx.enjoy.bean.*
import com.sx.enjoy.constans.C
import com.sx.enjoy.event.RiceRefreshEvent
import com.sx.enjoy.event.StepRefreshEvent
import com.sx.enjoy.modules.login.LoginActivity
import com.sx.enjoy.modules.mine.WebUrlActivity
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import com.sx.enjoy.utils.GlideImageLoader
import com.sx.enjoy.view.dialog.SignDialog
import com.sx.enjoy.view.dialog.SignOverDialog
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.header_home_view.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast
import org.litepal.LitePal


class HomeFragment : BaseFragment(),SXContract.View{

    private lateinit var signOverDialog: SignOverDialog

    private lateinit var present: SXPresent

    private lateinit var headView:View
    private lateinit var mAdapter:HomeListAdapter

    private var noticeList = arrayListOf<NoticeListBean>()

    private var pager = 1
    private var titleType = 0
    private var targetWalk = 0


    override fun getLayoutResource() = R.layout.fragment_home

    override fun beForInitView() {
        present = SXPresent(this)
    }

    override fun initView() {
        ImmersionBar.with(activity!!).statusBarDarkFont(true).titleBar(tb_home_title).init()

        signOverDialog = SignOverDialog(activity!!)

        mAdapter = HomeListAdapter()
        rcy_home_list.layoutManager = LinearLayoutManager(activity)
        rcy_home_list.adapter = mAdapter

        headView = View.inflate(activity,R.layout.header_home_view,null)
        mAdapter.addHeaderView(headView)


        present.getHomeBanner()
        present.getHomeNotice()
        getNewsList(true)

        iv_home_head.setImageResource(R.mipmap.ic_home_bg_walk)
        Glide.with(activity!!).load(R.mipmap.ic_step_run).into(iv_note_type)

        val step = SharedPreferencesUtil.getCommonInt(activity,"step")
        val minStep = SharedPreferencesUtil.getCommonInt(activity,"minStep")

        headView.tv_today_step.text = (step+minStep).toString()

        initEvent()
    }


    fun initUser(){
        val user = LitePal.findLast(UserBean::class.java)
        if(user!=null){
            headView.tv_user_activity.text = String.format("%.2f", user.userActivity)
            headView.tv_user_contribution.text = String.format("%.2f", user.userContrib)
            headView.tv_user_rice.text = String.format("%.2f", user.riceGrains)
        }else{
            headView.tv_user_activity.text = "0"
            headView.tv_user_contribution.text = "0"
            headView.tv_user_rice.text = "0"
        }
    }

    fun initStepAndDayRice(){
        initStep(0)
        initDayRice(null)
        getStepAndDayRice()
    }

    fun getStepAndDayRice(){
        if(C.USER_ID.isNotEmpty()){
            present.getRiceFromStep(C.USER_ID,"123","0","0","0","0")
        }
    }

    fun initStep(step:Int){
        headView.tv_today_step.text = step.toString()
        if(targetWalk != 0){
            headView.cp_walk_progress.value = ((step)/targetWalk.toFloat())*100
        }
    }

    fun initDayRice(rice:RiceRefreshEvent?){
        if(rice == null){
            headView.tv_step_rice.text = "0.00"
            headView.tv_car_rice.text = "0.00"
            headView.tv_distances.text = "0"
            headView.tv_target_step.text = "今日目标0步"
            headView.cp_walk_progress.value = 0f
        }else{
            targetWalk = rice.targetWalk
            headView.tv_distances.text = rice.mileage
            headView.tv_step_rice.text = rice.walkRiceGrains
            headView.tv_car_rice.text = rice.drivingRiceGrains
            headView.tv_target_step.text = "今日目标${rice.targetWalk}步"
            if(rice.targetWalk>0){
                headView.cp_walk_progress.value = ((rice.rotateMinStep+rice.minStep)/rice.targetWalk.toFloat())*100
            }
        }
    }

    private fun initEvent(){
        iv_home_sign.setOnClickListener {
            if(C.USER_ID.isEmpty()){
                activity?.startActivity<LoginActivity>()
            }else{
                present.getSignResult(C.USER_ID,true)
            }
        }
        tv_note_list.setOnClickListener {
            if(C.USER_ID.isEmpty()){
                activity?.startActivity<LoginActivity>()
            }else{
                activity?.startActivity<WalkHistoryActivity>(Pair("titleType",titleType))
            }
        }
        hst_title.setOnSortSelectListener {
            titleType = it
            when(it){
                0 -> {
                    headView.rl_step_count.visibility = View.VISIBLE
                    headView.ll_step_count.visibility = View.GONE
                    headView.ll_step_rice.visibility = View.VISIBLE
                    headView.ll_car_rice.visibility = View.GONE
                    tv_note_list.text = "历史步数"
                    iv_home_head.setImageResource(R.mipmap.ic_home_bg_walk)
                    Glide.with(activity!!).load(R.mipmap.ic_step_run).into(iv_note_type)
                }
                1 -> {
                    headView.rl_step_count.visibility = View.GONE
                    headView.ll_step_count.visibility = View.VISIBLE
                    headView.ll_step_rice.visibility = View.GONE
                    headView.ll_car_rice.visibility = View.VISIBLE
                    tv_note_list.text = "车行记录"
                    iv_home_head.setImageResource(R.mipmap.ic_home_bg_car)
                    Glide.with(activity!!).load(R.mipmap.ic_car).into(iv_note_type)
                }
            }
        }

        swipe_refresh_layout.setOnRefreshListener {
            present.getHomeBanner()
            present.getHomeNotice()
            getNewsList(true)
        }

        mAdapter.setOnLoadMoreListener {
            getNewsList(false)
        }

        rcy_home_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val position: Int = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                if(position<=0){
                    val bottom = DensityUtils.dp2px(activity,225f)
                    val curIndex = rcy_home_list.getChildAt(0).top
                    when {
                        curIndex>=0 -> {
                            v_home_title.alpha = 0f
                            v_status_bar.alpha = 0f
                        }
                        bottom+curIndex<0 -> {
                            v_home_title.alpha = 1f
                            v_status_bar.alpha = 1f
                        }
                        else -> {
                            v_home_title.alpha = (bottom-(bottom+curIndex))/bottom
                            v_status_bar.alpha = (bottom-(bottom+curIndex))/bottom
                        }
                    }
                }else{
                    v_home_title.alpha = 1f
                    v_status_bar.alpha = 1f
                }
            }
        })
        headView.tb_home_notice.setItemOnClickListener { data, position ->
            activity?.startActivity<NoticeDetailsActivity>(Pair("nid",noticeList[position].id))
        }
        mAdapter.setOnItemClickListener { adapter, view, position ->
            activity?.startActivity<NewsDetailsActivity>(Pair("nid",mAdapter.data[position].id))
        }

        headView.ban_top_list.setOnBannerListener {

        }

    }

    private fun getNewsList(isRefreshList: Boolean){
        if(isRefreshList){
            pager = 1
            mAdapter.loadMoreComplete()
            mAdapter.setEnableLoadMore(false)
        }else{
            pager++
            swipe_refresh_layout.finishRefresh()
        }
        present.getHomeNews(C.PUBLIC_PAGER_NUMBER,pager.toString())
    }

    override fun onResume() {
        super.onResume()
        headView.tb_home_notice.startViewAnimator()
        headView.ban_top_list.startAutoPlay()
        headView. ban_bottom_list.startAutoPlay()
    }



    override fun onStop() {
        super.onStop()
        headView.tb_home_notice.stopViewAnimator()
        headView.ban_top_list.stopAutoPlay()
        headView.ban_bottom_list.stopAutoPlay()
    }



    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.GETSIGNRESULT -> {
                    data?.let {
                        data as Boolean
                        if(data){
                            signOverDialog.show()
                        }else{
                            activity?.startActivity<SignAnswerActivity>()
                        }
                    }
                }
                SXContract.GETHOMEBANNER -> {
                    data?.let {
                        data as BannerListBean
                        if(data.shuffList.isNotEmpty()){
                            headView.ban_top_list.visibility = View.VISIBLE
                            val bannerList = arrayListOf<String>()
                            data.shuffList.forEach {
                                bannerList.add(it.bannerImg)
                            }
                            headView.ban_top_list.setImageLoader(GlideImageLoader())
                            headView.ban_top_list.setImages(bannerList)
                            headView.ban_top_list.start()
                            headView.ban_top_list.setOnBannerListener {
                                activity?.startActivity<WebUrlActivity>(Pair("title",data.shuffList[it].title),Pair("url",data.shuffList[it].url))
                            }
                        }else{
                            headView.ban_top_list.visibility = View.GONE
                        }
                        if(data.advertList.isNotEmpty()){
                            headView.ban_bottom_list.visibility = View.VISIBLE
                            val advertList = arrayListOf<String>()
                            data.advertList.forEach {
                                advertList.add(it.bannerImg)
                            }
                            headView.ban_bottom_list.setImageLoader(GlideImageLoader())
                            headView.ban_bottom_list.setImages(advertList)
                            headView.ban_bottom_list.start()
                            headView.ban_bottom_list.setOnBannerListener {
                                activity?.startActivity<WebUrlActivity>(Pair("title",data.advertList[it].title),Pair("url",data.advertList[it].url))
                            }
                        }else{
                            headView.ban_bottom_list.visibility = View.GONE
                        }
                    }
                }
                SXContract.GETHOMENOTICE -> {
                    data?.let {
                        swipe_refresh_layout.finishRefresh()
                        data as List<NoticeListBean>
                        noticeList.clear()
                        if(data.isNotEmpty()){
                            noticeList.addAll(data)
                            headView.ll_home_notice.visibility = View.VISIBLE
                            val noticeList = arrayListOf<String>()
                            data.forEach {
                                noticeList.add(it.title)
                            }
                            headView.tb_home_notice.setDatas(noticeList)
                        }else{
                            headView.ll_home_notice.visibility = View.GONE
                        }

                    }
                }
                SXContract.GETHOMENEWS -> {
                    data?.let {
                        data as List<NewsListBean>
                        if(pager<=1){
                            swipe_refresh_layout.finishRefresh()
                            mAdapter.setEnableLoadMore(true)
                            mAdapter.setNewData(data)
                        }else{
                            if(data.isEmpty()){
                                mAdapter.loadMoreEnd()
                            }else{
                                mAdapter.addData(data)
                                mAdapter.loadMoreComplete()
                            }
                        }
                    }
                }
                SXContract.GETRICEFROMSTEP -> {
                    data?.let {
                        data as StepRiceBean
                        if(C.USER_ID.isNotEmpty()){
                            targetWalk = data.targetWalk
                            val sdf = activity!!.getSharedPreferences(SharedPreferencesUtil.SP_COMMON_NAME,
                                Context.MODE_PRIVATE)
                            val editor = sdf.edit()
                            var step = sdf.getInt("step",0)
                            var minStep = sdf.getInt("minStep",0)
                            if(data.rotateMinStep>step){
                                step = data.rotateMinStep
                                editor.putInt("step",data.rotateMinStep)
                            }
                            if(data.minStep>minStep){
                                minStep = data.minStep
                                editor.putInt("minStep",data.minStep)
                            }
                            editor.apply()

                            headView.tv_distances.text = data.mileage
                            headView.tv_step_rice.text = data.walkRiceGrains
                            headView.tv_car_rice.text = data.drivingRiceGrains
                            headView.tv_target_step.text = "今日目标${data.targetWalk}步"
                            if(data.targetWalk>0){
                                headView.cp_walk_progress.value = ((step+minStep)/data.targetWalk.toFloat())*100
                            }
                            initStep(step+minStep)
                        }
                    }
                }
                else -> {

                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        activity?.toast(string!!)
        if(isRefreshList){
            if(pager<=1){
                swipe_refresh_layout.finishRefresh()
                mAdapter.setEnableLoadMore(true)
            }else{
                mAdapter.loadMoreFail()
            }
        }
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        activity?.toast("请检查网络连接")
        if(isRefreshList){
            if(pager<=1){
                swipe_refresh_layout.finishRefresh()
                mAdapter.setEnableLoadMore(true)
            }else{
                mAdapter.loadMoreFail()
            }
        }
    }


    companion object {
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

}