package com.cookandroid.dcp_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.dcp_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 클릭 시 InfoSettingActivity로 이동
        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, InfoSetting::class.java)
            startActivity(intent)
        }
    }
}
