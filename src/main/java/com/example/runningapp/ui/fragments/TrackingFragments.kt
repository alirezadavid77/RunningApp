package com.example.runningapp.ui.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.db.RunDAO
import com.example.runningapp.db.RunEntity
import com.example.runningapp.db.RunningDatabase
import com.example.runningapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runningapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningapp.other.Constants.ACTION_STOP_SERVICE
import com.example.runningapp.other.Constants.MAP_ZOOM
import com.example.runningapp.other.Constants.POLYLINE_COLOR
import com.example.runningapp.other.Constants.POLYLINE_WIDTH
import com.example.runningapp.other.TrackingUtility
import com.example.runningapp.services.Polyline
import com.example.runningapp.services.TrackingService
import com.example.runningapp.ui.viewmodels.MainViewModels
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.SnackbarContentLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_setup.view.*
import kotlinx.android.synthetic.main.fragment_tracking.*
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import java.util.jar.Manifest
import javax.inject.Inject
import kotlin.math.round
const val CANCEL_TRACKING_DIALOG ="Cancel Dialog"
@AndroidEntryPoint
class TrackingFragments : Fragment(R.layout.fragment_tracking) {



    private val viewModel: MainViewModels by viewModels()

    private var isTracking=false
    private var pathPoints= mutableListOf<Polyline>()


    private var map:GoogleMap? = null



    private var curTimeInMillis =0L

    private var menu: Menu?=null


    @set:Inject

    private var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)

    }


    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener {
           toggleRun()

        }

        if (savedInstanceState!= null){
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG) as CancelTrackingDialog?
            cancelTrackingDialog?.setYesListener {
                stopRun()

            }

        }
        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
        }

        mapView.getMapAsync {
            map =it
            it.isMyLocationEnabled = true
            addAllPolylines()
        }
        subscribeToObservers()


    }




    private fun subscribeToObservers(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer {
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()

        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer {
            curTimeInMillis=it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis,true)
            tvTimer.text=formattedTime
        })

    }

    private fun toggleRun(){
        if(isTracking){
           this.menu?.getItem(0)?.isVisible= true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu,menu)
        this.menu =menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (curTimeInMillis > 0L){
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.miCancelTracking->{
                showCancelDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelDialog(){
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG)



    }
    private fun stopRun(){
        tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragments_to_runFragments)
    }



    private fun updateTracking(isTracking :Boolean){
        this.isTracking=isTracking
        if(!isTracking && curTimeInMillis >0L){
            btnToggleRun.text="Start"
            btnFinishRun.visibility=View.VISIBLE
        }else if (isTracking){
            btnToggleRun.text="Stop"
            this.menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility=View.GONE
        }
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }




    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.builder()
        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }

        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.height,
                mapView.width,
                (mapView.height * 0.05f).toInt()
            )

        )
    }

    private fun endRunAndSaveToDb(){
        map?.snapshot {bmp->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters+=TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val avgSpeed = round((distanceInMeters /1000f) / (curTimeInMillis/1000f/60/60) *10) /10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =RunEntity(bmp,dateTimestamp,avgSpeed,distanceInMeters,curTimeInMillis,caloriesBurned)
            viewModel.insertRunEntity(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()

        }

    }


    private fun addAllPolylines() {
        for(polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)

        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size>1){
            val preLastLatLng= pathPoints.last()[pathPoints.last().size-2]
            val lastLatLng= pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action:String) =
        Intent(requireContext(),TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()

    }



    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }





}