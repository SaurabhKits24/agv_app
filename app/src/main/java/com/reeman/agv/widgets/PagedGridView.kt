package com.reeman.agv.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reeman.agv.R
import com.reeman.agv.adapter.DeliveryPointsAdapter
import com.reeman.agv.adapter.PageGroupAdapter
import com.reeman.points.model.custom.GenericPoint
import kotlin.math.ceil
import kotlin.math.min

class PagedGridView(context: Context, attrs: AttributeSet?) : LinearLayout(
    context, attrs
), DeliveryPointsAdapter.OnDeliveryPointClickListener,
    PageGroupAdapter.OnPageGroupItemClickListener {
    private var gvPoints: GridView
    private var layoutPaginationContainer: LinearLayout
    private var rvPage: RecyclerView
    private var pointsAdapter: DeliveryPointsAdapter
    private var pageAdapter: PageGroupAdapter

    private var currentMap: String? = null
    private var pointList: MutableList<GenericPoint>? = null
    private val pointListMap = mutableMapOf<Int, List<GenericPoint>>()
    private var isScrollMode = false

    private var currentPage = 1
    private val numColumns = 4
    private val itemsPerPage =
        numColumns * (if (resources.displayMetrics.heightPixels >= 800) 6 else 5)

    private var pointCheckedListener: OnPointCheckedListener? = null

    init {
        inflate(context, R.layout.layout_paged_grid_view, this)
        gvPoints = findViewById(R.id.gv_points)
        layoutPaginationContainer = findViewById(R.id.layout_pagination_container)
        rvPage = findViewById(R.id.rv_page)
        pointsAdapter = DeliveryPointsAdapter(this)
        pageAdapter = PageGroupAdapter(this)
        gvPoints.numColumns = numColumns
        gvPoints.horizontalSpacing = 8
        gvPoints.verticalSpacing = 5
        gvPoints.adapter = pointsAdapter
        rvPage.adapter = pageAdapter
        rvPage.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun initData(
        currentMap: String?,
        pointList: MutableList<GenericPoint>,
        isScrollMode: Boolean = true,
        pointCheckedListener: OnPointCheckedListener
    ) {
        this.isScrollMode = isScrollMode
        this.pointCheckedListener = pointCheckedListener
        setData(currentMap, pointList)

    }

    fun setData(currentMap: String?, pointList: List<GenericPoint>) {
        this.currentMap = currentMap
        this.pointList = pointList.toMutableList()
        val totalPage = ceil(this.pointList!!.size / itemsPerPage.toDouble()).toInt()
        pointListMap.clear()
        for (i in 0 until totalPage) {
            val startIndex = i * itemsPerPage
            pointListMap[i + 1] =
                this.pointList!!.subList(
                    startIndex,
                    min(startIndex + itemsPerPage, this.pointList!!.size)
                )
        }
        if (isScrollMode || pointListMap.size == 1) {
            switchToScrollingMode()
        } else {
            switchToPagingMode()
        }
    }

    private fun switchToPagingMode() {
        layoutPaginationContainer.visibility = View.VISIBLE
        pageAdapter.setPageList(pointListMap.map { it.key })
        pointsAdapter.updatePoints(currentMap, pointListMap[currentPage])
    }

    private fun switchToScrollingMode() {
        layoutPaginationContainer.visibility = View.GONE
        pointsAdapter.updatePoints(currentMap, pointList)
    }

    override fun onDeliveryPointClick(checkedPoint: GenericPoint) {
        pointCheckedListener?.onPointChecked(checkedPoint)
    }

    override fun onEleDeliveryPointClick(map: String, checkedPoint: GenericPoint) {
        pointCheckedListener?.onPointWithMapChecked(map, checkedPoint)
    }

    override fun onMapGroupItemClick(index: Int) {
        currentPage = index + 1
        pointsAdapter.updatePoints(currentMap, pointListMap[currentPage])
    }

    interface OnPointCheckedListener {
        fun onPointChecked(checkedPoint: GenericPoint)

        fun onPointWithMapChecked(map: String, checkedPoint: GenericPoint)
    }
}