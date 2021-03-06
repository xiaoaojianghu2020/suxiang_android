package com.sx.enjoy.modules.store

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gyf.immersionbar.ImmersionBar
import com.likai.lib.base.BaseFragment
import com.sx.enjoy.R
import com.sx.enjoy.adapter.TypeContentAdapter
import com.sx.enjoy.adapter.TypeMenuAdapter
import com.sx.enjoy.bean.StoreCategoryBean
import com.sx.enjoy.net.SXContract
import com.sx.enjoy.net.SXPresent
import kotlinx.android.synthetic.main.empty_public_network.view.*
import kotlinx.android.synthetic.main.fragment_store.*
import kotlinx.android.synthetic.main.header_type_title.view.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.toast

class StoreFragment : BaseFragment(),SXContract.View{

    private lateinit var present : SXPresent

    private lateinit var menuAdapter: TypeMenuAdapter
    private lateinit var contentAdapter: TypeContentAdapter

    private lateinit var emptyView : View
    private lateinit var errorView : View

    private lateinit var headTitleView : View

    private var selectId = ""

    override fun getLayoutResource() = R.layout.fragment_store

    override fun beForInitView() {
        present = SXPresent(this)
    }

    override fun initView() {
        ImmersionBar.with(activity!!).statusBarDarkFont(true).titleBar(tb_store_title).init()

        menuAdapter = TypeMenuAdapter()
        rcy_menu_list.layoutManager = LinearLayoutManager(activity)
        rcy_menu_list.adapter = menuAdapter

        contentAdapter = TypeContentAdapter()
        rcy_type_list.layoutManager = GridLayoutManager(activity,3)
        rcy_type_list.adapter = contentAdapter

        headTitleView = View.inflate(activity,R.layout.header_type_title,null)

        emptyView = View.inflate(activity,R.layout.empty_public_view,null)
        errorView = View.inflate(activity,R.layout.empty_public_network,null)
        contentAdapter.isUseEmpty(false)

        present.getStoreCategory("0")

        initEvent()
    }

    override fun refreshData() {
        present.getStoreCategory("0")
    }

    private fun initEvent(){
        swipe_refresh_layout.setOnRefreshListener {
            present.getStoreCategory("0")
        }
        menuAdapter.setOnItemClickListener { adapter, view, position ->
            menuAdapter.selectItem(position)
            selectId = menuAdapter.data[position].id
            headTitleView.tv_type_title.text = menuAdapter.data[position].cateName
            contentAdapter.setNewData(menuAdapter.data[position].categoryVoList)
        }
        contentAdapter.setOnItemClickListener { adapter, view, position ->
            activity?.startActivity<CommodityListActivity>(Pair("cartId",contentAdapter.data[position].id))
        }
        tv_commodity_search.setOnClickListener {
            activity?.startActivity<CommodityListActivity>(Pair("key",et_commodity_search.text.toString()))
        }
        errorView.iv_network_error.setOnClickListener {
            present.getStoreCategory("0")
        }
    }


    override fun onSuccess(flag: String?, data: Any?) {
        flag?.let {
            when (flag) {
                SXContract.GETSTORECATEGORY -> {
                    data?.let {
                        data as List<StoreCategoryBean>
                        isLoadComplete = true
                        contentAdapter.isUseEmpty(true)
                        contentAdapter.emptyView = emptyView
                        swipe_refresh_layout.finishRefresh()
                        if(data.isNotEmpty()){
                            var selectPosition = 0
                            for(i in data.indices) {
                                if(data[i].id == selectId){
                                    selectPosition = i
                                    break
                                }
                            }
                            data[selectPosition].isSelected = true
                            menuAdapter.setNewData(data)
                            contentAdapter.removeAllHeaderView()
                            contentAdapter.addHeaderView(headTitleView)
                            headTitleView.tv_type_title.text = data[selectPosition].cateName
                            contentAdapter.setNewData(data[selectPosition].categoryVoList)
                        }
                    }
                }
                else -> {

                }
            }
        }
    }


    override fun onFailed(string: String?,isRefreshList:Boolean) {
        swipe_refresh_layout.finishRefresh()
        contentAdapter.isUseEmpty(true)
        contentAdapter.emptyView = emptyView
        activity?.toast(string!!)?.setGravity(Gravity.CENTER, 0, 0)
    }

    override fun onNetError(boolean: Boolean,isRefreshList:Boolean) {
        contentAdapter.isUseEmpty(true)
        contentAdapter.emptyView = errorView
        swipe_refresh_layout.finishRefresh()
    }

    companion object {
        fun newInstance(): StoreFragment {
            val fragment = StoreFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

}