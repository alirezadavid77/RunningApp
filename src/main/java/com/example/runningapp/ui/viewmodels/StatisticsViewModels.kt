package com.example.runningapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.example.runningapp.db.repositories.MainRepository
import javax.inject.Inject


class StatisticsViewModels @ViewModelInject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
    val totalTimeRun = mainRepository.getTotalTimeInMillis()
    val distance = mainRepository.getTotalDistance()
    val caloriesBurned =mainRepository.getTotalCaloriesBurned()
    val avgSpeed = mainRepository.getTotalAvgSpeed()

    val runsSortedByDate = mainRepository.getAllRunsSortedByDate()

}