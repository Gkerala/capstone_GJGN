package com.example.gjgn_02v.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gjgn_02v.R
import com.example.gjgn_02v.data.model.records.WeightEntity

class WeightAdapter(private var weightList: List<WeightEntity>)
    : RecyclerView.Adapter<WeightAdapter.WeightViewHolder>() {

    class WeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textWeight: TextView = itemView.findViewById(R.id.textWeight)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeightViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weight, parent, false)
        return WeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeightViewHolder, position: Int) {
        val item = weightList[position]
        holder.textWeight.text = "${item.weight} kg"
        holder.textDate.text = item.date
    }

    override fun getItemCount(): Int = weightList.size

    fun updateList(newList: List<WeightEntity>) {
        weightList = newList
        notifyDataSetChanged()
    }
}
