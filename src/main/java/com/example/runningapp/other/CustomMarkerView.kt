package com.example.runningapp.other

import android.content.Context
import com.example.runningapp.db.RunEntity
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.marker_view.view.*
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    val runs :List<RunEntity>,
    c:Context,
    layoutid :Int
    ) :MarkerView (c,layoutid){

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if (e==null){
            return
        }
        val currentRunId = e.x.toInt()
        val runs = runs[currentRunId]


        val calender = Calendar.getInstance().apply {
            timeInMillis=runs.timestamp
        }
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        tvDate.text=dateFormat.format(calender.time)

        val avgSpeed = "${runs.avgSpeedInKMH} km/h"
        tvAvgSpeed.text=avgSpeed

        val distanceInKM = "${runs.distanceInMeters / 1000f} km"
        tvDistance.text=distanceInKM

        tvDuration.text=TrackingUtility.getFormattedStopWatchTime(runs.timeInMillis)

        val caloriesburned = "${runs.caloriesBurned} kcal"
        tvCaloriesBurned.text = caloriesburned


    }
}