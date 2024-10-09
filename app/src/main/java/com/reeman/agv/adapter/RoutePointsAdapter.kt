package com.reeman.agv.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.reeman.agv.R
import com.reeman.dao.repository.entities.PointsVO
import timber.log.Timber


class RoutePointsAdapter(
    val context: Context,
    val pointsVOList: MutableList<PointsVO>,
    private val routePointsAdapterClickListener: OnRoutePointsAdapterClickListener
) :
    RecyclerView.Adapter<RoutePointsAdapter.ItemViewHolder>() {

    private var selectedItemIndex = -1

    fun getCurrentItem(): PointsVO? {
        return if (selectedItemIndex == -1) {
            null
        } else {
            pointsVOList[selectedItemIndex]
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_point, parent, false)
        return ItemViewHolder(itemView)
    }

    fun remove(position: Int) {
        pointsVOList.removeAt(position)
        notifyItemRemoved(position)

        if (selectedItemIndex == position) {
            selectedItemIndex = -1
            routePointsAdapterClickListener.onItemClick(-1, null)
        } else if (selectedItemIndex > position) {
            selectedItemIndex -= 1
            routePointsAdapterClickListener.onItemClick(
                selectedItemIndex,
                pointsVOList[selectedItemIndex]
            )
        }

        notifyItemRangeChanged(position, pointsVOList.size)
    }


    fun addItem(pointsVO: PointsVO) {
        pointsVOList.add(pointsVO)
        val lastIndex = selectedItemIndex
        selectedItemIndex = pointsVOList.size - 1
        notifyItemInserted(selectedItemIndex)
        if (lastIndex != -1) {
            notifyItemChanged(lastIndex)
        }
        routePointsAdapterClickListener.onItemClick(
            selectedItemIndex,
            pointsVO
        )
    }

    fun updateItem(pointsVO: PointsVO) {
        if (selectedItemIndex == -1) return
        pointsVOList[selectedItemIndex] = pointsVO
        notifyItemChanged(selectedItemIndex)
    }

    override fun getItemCount() =
        pointsVOList.size

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val pointsVO = pointsVOList[position]
        holder.tvPoint.text = pointsVO.point
        holder.tvWaitingTime.text = if (pointsVO.isOpenWaitingTime) {
            pointsVO.waitingTime.toString()
        } else {
            context.getString(R.string.text_closed)
        }
        Timber.w("position : $position , pointsVO: $pointsVO , selectedItemIndex: $selectedItemIndex")
        if (selectedItemIndex == position) {
            holder.itemView.setBackgroundResource(R.drawable.bg_text)
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE)
        }
        holder.tvPoint.setOnClickListener {
            updateSelectionState(position, pointsVO)
        }
        holder.tvWaitingTime.setOnClickListener {
            updateSelectionState(position, pointsVO)
        }
        holder.ivDelete.setOnClickListener {
            routePointsAdapterClickListener.onDeleteClick(position, pointsVO)
        }
    }

    private fun updateSelectionState(position: Int, pointsVO: PointsVO) {
        if (selectedItemIndex != -1) {
            notifyItemChanged(selectedItemIndex)
        }
        selectedItemIndex = position
        routePointsAdapterClickListener.onItemClick(position, pointsVO)
        notifyItemChanged(position)
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvPoint: TextView
        val tvWaitingTime: TextView
        val ivDelete: AppCompatImageView

        init {
            tvPoint = itemView.findViewById(R.id.tv_point_name)
            tvWaitingTime = itemView.findViewById(R.id.tv_waiting_time)
            ivDelete = itemView.findViewById(R.id.iv_delete_point)
        }
    }

    interface OnRoutePointsAdapterClickListener {
        fun onDeleteClick(position: Int, pointsVO: PointsVO)

        fun onItemClick(position: Int, pointsVO: PointsVO?)
    }
}