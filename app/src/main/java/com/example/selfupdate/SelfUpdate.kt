package com.example.selfupdate

import android.content.Context
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class SelfUpdate {

    companion object {
        
        val TAG="SelfUpdate"

        // Create a listener to track request state updates.
        val listener = InstallStateUpdatedListener { state ->
            try {
                // (Optional) Provide a download progress bar.
                if (state.installStatus() == InstallStatus.DOWNLOADING) {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    // Show update progress bar.
                }
                // Log state or install the update.
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    popupSnackbarForCompleteUpdate()
                }
            }catch (e:Exception){
               e.printStackTrace()
            }

        }

        fun init(type:String) {
            if (MainVC.instance != null) {
                when(type){
                    "flex" -> updateFlexible(MainVC.instance!!.applicationContext)
                    "inmediat" -> updateInmediate(MainVC.instance!!.applicationContext)

                }
            }
        }
        
        fun resume(type:String){
            if (MainVC.instance != null) {
                when(type){
                    "flex" -> resumeFlexible(MainVC.instance!!.applicationContext)
                    "inmediat" -> resumeImmediate(MainVC.instance!!.applicationContext)

                }
            }
        }

        private fun updateInmediate(context: Context){
            try{
                Log.v(TAG, "updateInmediate")

                val appUpdateManager = AppUpdateManagerFactory.create(context)
                val appUpdateInfoTask = appUpdateManager.appUpdateInfo

                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                        val statusApp = appUpdateInfo.updateAvailability()
                        when (statusApp) {
                            UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                                Log.v(TAG, "statusApp: ACTUALIZACIÓN NO DISPONIBL")
                            UpdateAvailability.UPDATE_AVAILABLE ->
                                Log.v(TAG, "statusApp: ACTUALIZACIÓN DISPONIBLE")
                            UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                                Log.v(TAG, "statusApp: ACTUALIZACIÓN ACTIVADA POR EL DESARROLLADOR EN CURSO")
                            UpdateAvailability.UNKNOWN -> { Log.v(TAG, "statusApp: DESCONOCIDO -> code: $statusApp") }
                        }

                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            MainVC.instance!!.activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())

                        //con activity //appUpdateManager.startUpdateFlowForResult(appUpdateInfo, this,AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(), 0)
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

        private fun updateFlexible(context: Context) {
            try {
                Log.v(TAG, "updateFlexible")

                val appUpdateManager = AppUpdateManagerFactory.create(context)
                appUpdateManager.registerListener(listener)
                val appUpdateInfoTask = appUpdateManager.appUpdateInfo

                appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                    val days = appUpdateInfo.clientVersionStalenessDays()
                    val statusApp = appUpdateInfo.updateAvailability()
                    val DAYS_FOR_FLEXIBLE_UPDATE = 3

                    Log.v(TAG, "días que se enteró que había una actualización disponible : $days")

                    when (statusApp) {
                        UpdateAvailability.UPDATE_NOT_AVAILABLE ->
                            Log.v(TAG, "statusApp: ACTUALIZACIÓN NO DISPONIBL")
                        UpdateAvailability.UPDATE_AVAILABLE ->
                            Log.v(TAG, "statusApp: ACTUALIZACIÓN DISPONIBLE")
                        UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
                            Log.v(TAG, "statusApp: ACTUALIZACIÓN ACTIVADA POR EL DESARROLLADOR EN CURSO")
                        UpdateAvailability.UNKNOWN -> { Log.v(TAG, "statusApp: DESCONOCIDO -> code: $statusApp") }
                    }

                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        //&& (appUpdateInfo.clientVersionStalenessDays() ?: -1) >= DAYS_FOR_FLEXIBLE_UPDATE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                    ) {

                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            MainVC.instance!!.activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        private fun resumeFlexible(context: Context) {
            try{
                Log.v(TAG, "resumeFlexible")
                val appUpdateManager = AppUpdateManagerFactory.create(context)
                appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        popupSnackbarForCompleteUpdate()
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        private fun resumeImmediate(context: Context){
            Log.v(TAG, "resumeImmediate")
            try{
                val appUpdateManager = AppUpdateManagerFactory.create(context)
                appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    if (appUpdateInfo.updateAvailability()
                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {

                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            MainVC.instance!!.activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                    }
                    //molestarlo hasta q lo actualice
                    val intrusivo=false
                    if(intrusivo) {
                        appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            MainVC.instance!!.activityResultLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                        )
                    }
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        fun popupSnackbarForCompleteUpdate() {
            try{
                if (MainVC.instance != null) {

                    val appUpdateManager =
                        AppUpdateManagerFactory.create(MainVC.instance!!.applicationContext)

                    val binding = MainVC.instance!!.binding

                    Snackbar.make(
                        binding.mainCl,
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        Log.v(TAG, "popupSnackbarForCompleteUpdate")
                        setAction("RESTART") { appUpdateManager.completeUpdate() }
                        setActionTextColor(MainVC.instance!!.resources.getColor(R.color.black))
                        show()

                        Log.v(TAG, "desactive listener");
                        appUpdateManager.unregisterListener(listener)
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

}