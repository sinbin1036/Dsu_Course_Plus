package com.cookandroid.dcp_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.dcp_app.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignupConfirm.setOnClickListener {
            finish() // 단순히 돌아가기
        }
    }
}
