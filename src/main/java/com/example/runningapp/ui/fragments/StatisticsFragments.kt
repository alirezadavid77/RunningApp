package com.example.runningapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.runningapp.R
import com.example.runningapp.other.CustomMarkerView
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.ui.viewmodels.StatisticsViewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragments : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticsViewModels by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObservers()
        setupBarChart()
    }

    private fun setupBarChart(){
        barChart.xAxis.apply {
            position =XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor =Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }
        barChart.axisRight.apply {
            axisLineColor =Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)

        }
        barChart.apply {
            description.text ="average speed "
            legend.isEnabled = false

        }


    }

    private fun subscribeToObservers (){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                tvTotalTime.text = totalTimeRun
            }

        })
        viewModel.distance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it/1000f
                val totalDistance = round(km  * 10f)/10f
                val totalDistanceString = "${totalDistance} km"
                tvTotalDistance.text =totalDistanceString
            }
        })
        viewModel.avgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed = round( it * 10f)/10f
                val avgSpeedString = "${avgSpeed}km/h"
                tvAverageSpeed.text=avgSpeedString
            }
        })
        viewModel.caloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val calories = "${it}"
                tvTotalCalories.text = calories
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed = it.indices.map {i-> BarEntry(i.toFloat(),it[i].avgSpeedInKMH) }
                val barDataSet = BarDataSet(allAvgSpeed,"Avg speed").apply {
                    valueTextColor = Color.WHITE
                    color =ContextCompat.getColor(requireContext(),R.color.colorAccent)

                }
                barChart.data = BarData(barDataSet)
                barChart.marker =CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }
}