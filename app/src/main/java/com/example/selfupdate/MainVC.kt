package com.example.selfupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.selfupdate.databinding.ActivityMainBinding

class MainVC : AppCompatActivity() {

    companion object{
        var instance: MainVC? = null
    }

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        instance=this

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setText()
        SelfUpdate.init("x")

    }

    fun setText(){
        binding.txt.text = "selfUpdate m1"
    }

    val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
                result: ActivityResult ->
            if (result.resultCode != RESULT_OK) {
                Log.v(SelfUpdate.TAG, "activityResultLauncher result.resultCode : ${result.resultCode}")
            }
        }
}