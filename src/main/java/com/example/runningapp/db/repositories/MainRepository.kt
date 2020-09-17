package com.example.runningapp.db.repositories

import android.location.Location
import androidx.lifecycle.LiveData
import com.example.runningapp.db.RunDAO
import com.example.runningapp.db.RunEntity
import com.google.android.gms.maps.model.CameraPosition
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO

){
    suspend fun insertRun(run : RunEntity) =runDAO.insertRun(run)

    suspend fun deleteRun(run : RunEntity) =runDAO.deleteRun(run)

    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByAvgDistance() = runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalTimeInMillis() =runDAO.getTotalTimeInMillis()

    fun getTotalCaloriesBurned() =runDAO.getTotalCaloriesBurned()

    fun getTotalDistance()=runDAO.getTotalDistance()

    fun getTotalAvgSpeed()=runDAO.getTotalAvgSpeed()




}