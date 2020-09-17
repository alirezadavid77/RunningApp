package com.example.runningapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningapp.db.RunEntity
import com.example.runningapp.db.repositories.MainRepository
import com.example.runningapp.other.SortType
import com.google.android.gms.maps.model.CameraPosition
import kotlinx.coroutines.launch


class MainViewModels @ViewModelInject constructor(
    val mainRepository: MainRepository

) : ViewModel() {

    private val runsSortedByDate = mainRepository.getAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getAllRunsSortedByDistance()
    private val runsSortedByCaloriesBurned = mainRepository.getAllRunsSortedByCaloriesBurned()
    private val runsSortedByTimeInMillis = mainRepository.getAllRunsSortedTimeInMillis()
    private val runsSortedByDAvgSpeed = mainRepository.getAllRunsSortedByAvgDistance()

    val runs = MediatorLiveData<List<RunEntity>>()

    var sortType = SortType.DATE

    init {
        runs.addSource(runsSortedByDate) { result ->
            if (sortType == SortType.DATE) {
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) { result ->
            if (sortType == SortType.DISTANCE) {
                result?.let { runs.value = it }

            }

        }
        runs.addSource(runsSortedByDAvgSpeed) { result ->
            if (sortType == SortType.AVG_SPEED) {
                result?.let { runs.value = it }

            }

        }
        runs.addSource(runsSortedByCaloriesBurned) { result ->
            if (sortType == SortType.CALORIES_BURNED) {
                result?.let { runs.value = it }

            }

        }
        runs.addSource(runsSortedByTimeInMillis) { result ->
            if (sortType == SortType.RUNNING_TIME) {
                result?.let { runs.value = it }

            }

        }


    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runsSortedByCaloriesBurned.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByDAvgSpeed.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }

    private var cameraPosition: CameraPosition? = null
    fun insertRunEntity(run: RunEntity) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }




}
