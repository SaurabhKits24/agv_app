package com.reeman.agv.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.reeman.agv.R

class QRCodePointBindingAdapter(
    val list: List<Pair<Pair<String,String>, Pair<String,String>>>
) : RecyclerView.Adapter<QRCodePointBindingAdapter.ItemViewHolder>() {
    private var QRCodeModelArrayList: MutableList<Pair<Pair<String,String>, Pair<String,String>>> =
        list.toMutableList()

    fun setQRCodeModelList(QRCodeModelArrayList: MutableList<Pair<Pair<String,String>, Pair<String,String>>>) {
        this.QRCodeModelArrayList = QRCodeModelArrayList
        notifyDataSetChanged()
    }

    fun setLastQRCodeModeItem(QRCodeModel: Pair<Pair<String,String>, Pair<String,String>>) {
        QRCodeModelArrayList[QRCodeModelArrayList.size - 1] = QRCodeModel
        notifyItemChanged(QRCodeModelArrayList.size - 1)
    }

    fun addQRCodeModeItem(QRCodeModel: Pair<Pair<String,String>, Pair<String,String>>) {
        QRCodeModelArrayList.add(QRCodeModel)
        notifyItemInserted(QRCodeModelArrayList.size-1)
    }

    fun removeQRCodeModeItem(position: Int) {
        QRCodeModelArrayList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, QRCodeModelArrayList.size - position)
    }

    fun clear() {
        QRCodeModelArrayList.clear()
        notifyDataSetChanged()
    }

    fun getPairList(): List<Pair<Pair<String,String>, Pair<String,String>>> {
        return QRCodeModelArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_item_qrcode_calling_point, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

        val item = getItem(position)
        val first = item.first
        if (!TextUtils.isEmpty(first.first)) {
            holder.tvStartLocation.text = String.format("%s - %s", first.first, first.second)
        } else {
            holder.tvStartLocation.text = first.second
        }
        holder.tvStartLocation.tag = first
        val second = item.second
            holder.tvTargetLocation.visibility = View.VISIBLE
            if (second.first.isNotBlank()) {
                holder.tvTargetLocation.text = String.format("%s - %s", second.first, second.second)
            } else {
                holder.tvTargetLocation.text = second.second
            }
            holder.tvTargetLocation.tag = second

    }

    override fun getItemCount(): Int {
        return QRCodeModelArrayList.size
    }

    private fun getItem(position: Int): Pair<Pair<String,String>, Pair<String,String>> {
        return QRCodeModelArrayList[position]
    }


    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStartLocation: TextView
        val tvTargetLocation: TextView
        val ivQRCodePoint: AppCompatImageView

        init {
            tvStartLocation = itemView.findViewById(R.id.tv_start_calling_location)
            tvTargetLocation = itemView.findViewById(R.id.tv_target_calling_location)
            ivQRCodePoint = itemView.findViewById(R.id.iv_qrcode_point_arrow)
        }
    }


    interface OnQRCodePointBindingClickListener {
        /**
         * 删除已选的顶升点位
         *
         * @param position
         */
        fun onBindQRCode(position: Int)
    }


}