package com.example.moneycounter4.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.moneycounter4.R
import com.example.moneycounter4.bean.ItemAccount
import com.example.moneycounter4.bean.TalkItem
import com.example.moneycounter4.utils.HttpUtilCallback
import com.example.moneycounter4.utils.HttpUtils.HttpUtil
import com.example.moneycounter4.utils.JSonEvalUtils.JSonArray
import com.example.moneycounter4.utils.JSonEvalUtils.JSonEval
import com.example.moneycounter4.view.adapter.TalkRecyclerViewAdapter
import com.example.moneycounter4.viewmodel.MainApplication
import com.example.moneycounter4.viewmodel.MainViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_mine.*


class FragmentMine : Fragment() {
    lateinit var viewModel: MainViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        return inflater.inflate(R.layout.fragment_mine, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val isMine = arguments?.getBoolean("isMine")
        val accountNumS = arguments?.getString("accountNum")

        isMine?.let {
            if (!isMine) {
                layoutCondition.visibility = View.GONE
                imageViewSetting.visibility = View.GONE
            } else {
                textViewTag.text = "我的主题"
            }
        }

        val listener = SwipeRefreshLayout.OnRefreshListener {
            HttpUtil.getInstance().httpGet(
                (activity?.application as MainApplication).connectionUrlMain,
                object : HttpUtilCallback {
                    override fun doSomething(respond: String?) {
                        var account: ItemAccount? = null
                        try {
                            account = JSonEval.getInstance()
                                .fromJson(respond, ItemAccount::class.java)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        activity?.runOnUiThread {
                            account?.let {
                                textViewAccountNum?.text = "账号：" + it.accountNum
                                textViewText?.text = it.text
                                textViewUsrName?.text = it.usrName
                                when (it.sex) {
                                    "男" -> imageViewSex?.setImageResource(R.drawable.ic_man)
                                    "女" -> imageViewSex?.setImageResource(R.drawable.ic_woman)
                                }
                                textViewLikes?.text = it.likes.toString()
                                //ImageLoader.with(activity).load(account.picUrl).into(imageViewUsrPic)
                                imageViewUsrPic?.let { it2 ->
                                    Glide.with(requireActivity()).load(it.picUrl).into(it2)
                                }
                            }
                            swipeRefreshLayout?.isRefreshing = false
                        }

                    }

                    override fun error() {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "加载失败，请检查网络连接呀", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout?.isRefreshing = false
                        }

                    }
                },
                activity,

                "action",
                "getaccountinfo",
                "accountnum",
                accountNumS
            )


            HttpUtil.getInstance().httpGet(
                (activity?.application as MainApplication).connectionUrlMain,
                object : HttpUtilCallback {
                    override fun doSomething(respond: String?) {
                        val list = ArrayList<TalkItem>()
                        try {
                            val jsonArray = JSonArray(respond)
                            for (i in 0 until jsonArray.size()) {
                                val gson = Gson()
                                list.add(
                                    gson.fromJson(
                                        jsonArray.get(i),
                                        TalkItem::class.javaObjectType
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        activity?.runOnUiThread {
                            if (activity?.application == null) {
                                return@runOnUiThread
                            }
                            val adapter = TalkRecyclerViewAdapter(
                                activity,
                                context ?: MainApplication.context,
                                list,
                                (activity?.application as MainApplication).connectionUrlMain,
                                viewModel.accountNum.get(),
                                viewModel.token
                            )
                            textViewTalkCount?.text = list.size.toString()
                            recyclerViewTalkLog?.adapter = adapter
                            recyclerViewTalkLog?.layoutManager = LinearLayoutManager(context)
                        }

                    }

                    override fun error() {
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "加载用户帖子失败，请检查网络连接呀", Toast.LENGTH_SHORT).show()
                            swipeRefreshLayout?.isRefreshing = false
                        }

                    }
                },
                activity,

                "action",
                "getaccounttalk",
                "accountnum",
                accountNumS
            )


        }

        swipeRefreshLayout.setOnRefreshListener(listener)
        listener.onRefresh()
        swipeRefreshLayout.isRefreshing = true

        imageViewSetting.setOnClickListener {
            findNavController().navigate(R.id.action_global_fragmentSetting)
        }

    }


}