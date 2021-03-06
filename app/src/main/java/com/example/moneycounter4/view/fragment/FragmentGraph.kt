package com.example.moneycounter4.view.fragment

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.builder.TimePickerBuilder
import com.bigkoo.pickerview.view.OptionsPickerView
import com.example.moneycounter4.R
import com.example.moneycounter4.base.BaseViewModelFragment
import com.example.moneycounter4.model.DataReader
import com.example.moneycounter4.model.dao.getByDuration
import com.example.moneycounter4.network.setSchedulers
import com.example.moneycounter4.utils.CalendarUtil
import com.example.moneycounter4.utils.getYear
import com.example.moneycounter4.view.adapter.ChartAdapter
import com.example.moneycounter4.view.costom.DataItem
import com.example.moneycounter4.viewmodel.GraphViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.reactivex.Observable
import kotlinx.android.synthetic.main.fragment_graph.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class FragmentGraph : BaseViewModelFragment<GraphViewModel>() {

    companion object {
        lateinit var tfLight: Typeface
    }


    private var year = 0
    private val adapter = ChartAdapter(listOf())


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        tfLight = Typeface.createFromAsset(requireContext().assets, "alibaba_light.ttf")
        year = Calendar.getInstance().getYear()
        return inflater.inflate(R.layout.fragment_graph, container, false)
    }



    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        textViewTimeYear.text = "${year}年"
        constraintDate.setOnClickListener {
            val pvTime =
                TimePickerBuilder(
                    requireContext()
                ) { date, _ ->
                    year = date.year + 1900
                    drag_head_view.refresh()
                }
                    .setType(booleanArrayOf(true, false, false, false, false, false))
                    .build()
            pvTime.show()
        }


        constraintInOrOut.setOnClickListener {
            val options1Items = listOf("支出", "收入", "结余")
            val pvOptions: OptionsPickerView<String> =
                OptionsPickerBuilder(
                    requireContext()
                ) { options1, _, _, _ ->
                    val tx: String = options1Items[options1]
                    textViewExpend.text = tx
                    drag_head_view.refresh()
                }.build()
            pvOptions.setPicker(options1Items)
            pvOptions.setSelectOptions(
                when (textViewExpend.text.toString()) {
                    "支出" -> 0
                    "收入" -> 1
                    "结余" -> 2
                    else -> 0
                }
            )
            pvOptions.show()
        }

        rv_graph.adapter = adapter
        rv_graph.layoutManager = LinearLayoutManager(requireContext())
        val listener = {
            refresh()
        }

        drag_head_view.onRefreshAction = listener
        drag_head_view.refresh()

        tv_export.setOnClickListener {
            viewModel.export(requireContext())
        }

    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun refresh() {


        textViewTimeYear.text = "${year}年"

        val o = Observable.create<ArrayList<LineData>> {
            val chara = listOf("一", "二", "三", "四", "五", "六", "日")
            var week = 1
            val option = when (textViewExpend.text.toString()) {
                "支出" -> DataReader.OPTION_EXPEND
                "收入" -> DataReader.OPTION_INCOME
                "结余" -> DataReader.OPTION_LAST
                else -> DataReader.OPTION_EXPEND
            }
            val list = ArrayList<LineData>()
            CalendarUtil.getEveryFirstDayOfWeek(year).forEach {
                val t = ArrayList<DataItem>()
                var weekMoney = 0.0


                val entries: ArrayList<Entry> = ArrayList()
                for (i in 1..7) {
                    var m = 0.0
                    DataReader.db?.counterDao()
                        ?.getByDuration(it, 86400000L * (i - 1), 86400000L * i, option)
                        ?.forEach { cit ->
                            cit.money?.let { money ->
                                m += money
                            }

                        }
                    if (m < 0.0 && option == DataReader.OPTION_LAST) {
                        m = 0.0
                    }
                    weekMoney += m
                    t.add(DataItem(chara[i - 1], abs(m), it, 86400000L * 7))
                    entries.add(Entry(i.toFloat(), abs(m).toFloat()))
                }
                if (weekMoney != 0.0) {
//                    val d = BarDataSet(entries, "第${week}周")
//                    d.setDrawValues(true)
////                    d.valueTextSize = context?.sp(10)?.toFloat()?: 0f
//                    d.colors = ColorTemplate.VORDIPLOM_COLORS.toList()
//                    d.barShadowColor = Color.rgb(203, 203, 203)
//                    val sets: ArrayList<IBarDataSet> = ArrayList()
//                    sets.add(d)
//                    val cd = BarData(sets)
//                    cd.barWidth = 0.9f

                    val lineDataSet = LineDataSet(entries, "第${week}周 - 起始日期：${it}")
                    lineDataSet.valueTextSize = 12f
                    val lineData = LineData(lineDataSet)

                    list.add(lineData)
                }
                week++
            }


            it.onNext(list)
        }.setSchedulers().subscribe {
            adapter.refresh(it)
            drag_head_view?.finishRefresh()
        }


    }



    //========
//    private class ChartDataAdapter internal constructor(
//        context: Context?,
//        objects: List<BarData?>?
//    ) :
//        ArrayAdapter<BarData?>(context!!, 0, objects!!) {
//
//        @SuppressLint("InflateParams")
//        override fun getView(
//            position: Int,
//            convertView: View?,
//            parent: ViewGroup
//        ): View {
//
//
//            var convertView1 = convertView
//            val data: BarData? = getItem(position)
//            val holder: ViewHolder
//            if (convertView1 == null) {
//                holder = ViewHolder()
//                convertView1 =
//                    LayoutInflater.from(context).inflate(R.layout.item_list_barchart, null)
//                holder.chart = convertView1.findViewById(R.id.chart)
//                holder.textViewTag = convertView1.findViewById(R.id.textViewTag)
//                convertView1.tag = holder
//            } else {
//                holder = convertView1.tag as ViewHolder
//            }
//
//
//            holder.chart?.setTouchEnabled(false)
//
//            // apply styling
//            if (data != null) {
//                data.setValueTypeface(tfLight)
//                data.setValueTextColor(Color.BLACK)
//            }
//            holder.chart?.description?.isEnabled = false
//            holder.chart?.setDrawGridBackground(false)
//            val xAxis: XAxis? = holder.chart?.xAxis
//            xAxis?.position = XAxis.XAxisPosition.BOTTOM
//            xAxis?.typeface = tfLight
//            xAxis?.setDrawGridLines(false)
//            val leftAxis: YAxis? = holder.chart?.axisLeft
//            leftAxis?.typeface = tfLight
//            leftAxis?.setLabelCount(5, false)
//            leftAxis?.spaceTop = 15f
//            val rightAxis: YAxis? = holder.chart?.axisRight
//            rightAxis?.typeface = tfLight
//            rightAxis?.setLabelCount(5, false)
//            rightAxis?.spaceTop = 15f
//
//            // set data
//            holder.chart?.data = data
//            holder.chart?.setFitBars(true)
//
//            // do not forget to refresh the chart
////            holder.chart.invalidate();
//            holder.chart?.animateY(700)
//
//            return convertView1!!
//        }
//
//        private inner class ViewHolder {
//            var chart: BarChart? = null
//            var textViewTag: TextView? = null
//        }
//    }
//
//    @RequiresApi(Build.VERSION_CODES.N)
//    private fun getData(month: Int): Pair<BarData?, Boolean> {
//        val entries: ArrayList<BarEntry> = ArrayList()
//        var b = false
//
//        val calendar = Calendar.getInstance()
//        calendar.set(year, month, 0)
//        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
//        val option = when (textViewExpend.text.toString()) {
//            "支出" -> DataReader.OPTION_EXPEND
//            "收入" -> DataReader.OPTION_INCOME
//            "结余" -> DataReader.OPTION_LAST
//            else -> DataReader.OPTION_EXPEND
//        }
//        LogW.d("$year $month")
//        for (day in 1..dayOfMonth) {
//
//            val list = DataReader.getCounterItems(year, month, day, DataReader.OPTION_BY_DAY)
//            val money = DataReader.count(list, option)
//            if (money != 0.0) {
//                b = true
//            }
//            entries.add(BarEntry(day.toFloat(), money.toFloat()))
//        }
//        val d = BarDataSet(entries, "${month}月")
//        d.setDrawValues(false)
//        d.colors = ColorTemplate.VORDIPLOM_COLORS.toList()
//        d.barShadowColor = Color.rgb(203, 203, 203)
//        val sets: ArrayList<IBarDataSet> = ArrayList()
//        sets.add(d)
//        val cd = BarData(sets)
//        cd.barWidth = 0.9f
//        return Pair(cd, b)
//    }

}