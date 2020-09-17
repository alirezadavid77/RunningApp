package com.example.runningapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningapp.R
import com.example.runningapp.db.RunEntity
import com.example.runningapp.other.TrackingUtility
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter :RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder (itemView :View) : RecyclerView.ViewHolder(itemView)

    val diffCallBack =object : DiffUtil.ItemCallback <RunEntity>() {
        override fun areItemsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: RunEntity, newItem: RunEntity): Boolean {
            return oldItem.hashCode() == newItem.hashCode()

        }
    }

    val differ =AsyncListDiffer(this,diffCallBack)

    fun submitList (list: List<RunEntity>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run,parent,false)
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(ivRunImage)

            val calender = Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yyyy",Locale.getDefault())
            tvDate.text=dateFormat.format(calender.time)

            val avgSpeed = "${run.avgSpeedInKMH} km/h"
            tvAvgSpeed.text=avgSpeed

            val distanceInKM = "${run.distanceInMeters / 1000f} km"
            tvDistance.text=distanceInKM

            tvTime.text=TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val caloriesburned = "${run.caloriesBurned} kcal"
            tvCalories.text = caloriesburned

        }
    }
}