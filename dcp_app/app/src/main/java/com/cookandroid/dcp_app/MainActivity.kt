package com.cookandroid.dcp_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.cookandroid.dcp_app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Info 버튼 기존 기능
        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, InfoSetting::class.java)
            startActivity(intent)
        }

        // DrawerLayout 참조
        drawerLayout = binding.root as DrawerLayout

        // 햄버거 아이콘 클릭 시 드로어 열기
        binding.menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // 드로어 메뉴 내부 뷰 접근
        val drawerMenuView = findViewById<View>(R.id.drawerMenu)
        val btnLogout = drawerMenuView.findViewById<Button>(R.id.btnLogout)
        val btnHelp = drawerMenuView.findViewById<ImageView>(R.id.btnHelp)

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        btnHelp.setOnClickListener {
            // 도움말 처리
        }
    }
}


