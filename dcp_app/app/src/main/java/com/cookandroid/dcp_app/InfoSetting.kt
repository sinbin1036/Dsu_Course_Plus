package com.cookandroid.dcp_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.dcp_app.databinding.ActivityInfosettingBinding

class InfoSetting : AppCompatActivity() {
    private lateinit var binding: ActivityInfosettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfosettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExit.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
