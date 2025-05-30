package com.cookandroid.dcp_app   // ← 반드시 실제 패키지 경로와 일치시켜주세요!

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help) // 레이아웃 파일명과 일치해야 함

        val btnExit = findViewById<Button>(R.id.btn_exit)
        btnExit.setOnClickListener {
            finish() // 현재 액티비티 종료, 메인화면으로 복귀
        }
    }
}
