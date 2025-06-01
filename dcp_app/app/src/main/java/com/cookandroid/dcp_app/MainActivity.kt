package com.cookandroid.dcp_app

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.cookandroid.dcp_app.databinding.ActivityMainBinding
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInfo.setOnClickListener {
            val intent = Intent(this, InfoSetting::class.java)
            startActivity(intent)
        }

        drawerLayout = binding.root as DrawerLayout

        binding.menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        val drawerMenuView = findViewById<View>(R.id.drawerMenu)
        val btnLogout = drawerMenuView.findViewById<Button>(R.id.btnLogout)
        val btnHelp = drawerMenuView.findViewById<ImageView>(R.id.btnHelp)
        val clearButton = findViewById<Button>(R.id.btnClear)
        clearButton.setOnClickListener {
            timetableData.clear()
            val overlay = findViewById<FrameLayout>(R.id.blockOverlay)
            overlay.removeAllViews()
            Toast.makeText(this, "시간표가 초기화되었습니다.", Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnHelp.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
            drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onResume() {
        super.onResume()
        val overlay = findViewById<FrameLayout>(R.id.blockOverlay)

        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                overlay.viewTreeObserver.removeOnGlobalLayoutListener(this)
                try {
                    Log.d("MainActivity", "blockOverlay measured: width=${overlay.width}, height=${overlay.height}")
                    applyTimeTable()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error in onGlobalLayout: ${e.message}", e)
                    Toast.makeText(this@MainActivity, "오류 발생: 화면 갱신 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        overlay.viewTreeObserver.addOnGlobalLayoutListener(listener)
    }

    private fun applyTimeTable() {
        try {
            MainActivity.applyToTimeTable(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "applyTimeTable() error: ${e.message}", e)
            Toast.makeText(this, "시간표 적용 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        var timetableData: MutableList<SubjectInfo> = mutableListOf()

        private val blockColors = listOf(
            "#EF9A9A", "#F48FB1", "#CE93D8", "#90CAF9", "#81D4FA",
            "#A5D6A7", "#FFF59D", "#FFE082", "#FFAB91", "#BCAAA4", "#B0BEC5"
        ).map { Color.parseColor(it) }

        private val colorCache = mutableMapOf<String, Int>()
        private val usedColors = mutableSetOf<Int>()

        fun applyToTimeTable(activity: MainActivity) {
            try {
                val overlay = activity.findViewById<FrameLayout>(R.id.blockOverlay)
                overlay.removeAllViews()

                val density = activity.resources.displayMetrics.density
                val cellWidth = (density * 63f).toInt()
                val cellHeight = (density * 60.8f).toInt()

                Log.d("MainActivity", "blockOverlay measured: width=${overlay.width}, height=${overlay.height}")
                Log.d("applyToTimeTable", "cellWidth: $cellWidth, cellHeight: $cellHeight")

                val dayMap = mapOf("월" to 0, "화" to 1, "수" to 2, "목" to 3, "금" to 4)
                val occupiedSlots = mutableMapOf<Pair<String, Int>, String>()

                for (subject in timetableData) {
                    Log.d("applyToTimeTable", "과목: ${subject.class_name} → ${subject.schedule}")
                    val grouped = subject.schedule.groupBy { it.day.trim().take(1) }

                    val subjectName = subject.class_name ?: "-"
                    val cachedColor = colorCache.getOrPut(subjectName) {
                        val availableColors = blockColors.filterNot { usedColors.contains(it) }
                        val selected = if (availableColors.isNotEmpty()) availableColors.random() else blockColors.random()
                        usedColors.add(selected)
                        selected
                    }

                    for ((day, slots) in grouped) {
                        val times = slots.map { it.time }.sorted()
                        val groups = mutableListOf<Pair<Int, Int>>()
                        var start = times[0]
                        var last = start
                        for (i in 1 until times.size) {
                            if (times[i] == last + 1) {
                                last = times[i]
                            } else {
                                groups.add(start to last)
                                start = times[i]
                                last = start
                            }
                        }
                        groups.add(start to last)

                        for ((startT, endT) in groups) {
                            val dayIndex = dayMap[day] ?: continue
                            val conflictSlot = (startT..endT).firstOrNull { occupiedSlots.containsKey(day to it) }
                            if (conflictSlot != null) {
                                val conflictName = occupiedSlots[day to conflictSlot]
                                continue
                            }

                            for (t in startT..endT) {
                                occupiedSlots[day to t] = subjectName
                            }

                            val block = TextView(activity).apply {
                                text = subject.class_name ?: "-"
                                setTextColor(Color.WHITE)
                                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                                gravity = Gravity.CENTER
                                background = GradientDrawable().apply {
                                    setColor(cachedColor)
                                    setStroke((1 * density).toInt(), Color.BLACK)
                                }
                            }

                            val offset1 = (6 * density).toInt()
                            val offset2 = (2 * density).toInt()
                            val headerOffset = (cellHeight).toInt() // header row 높이만큼 내림
                            val top = (headerOffset + (startT - 1) * cellHeight + offset2)
                            val left = ((dayIndex + 1) * cellWidth - offset1)
                            val height = ((endT - startT + 1) * cellHeight)
                            val width = cellWidth

                            Log.d("BlockPosition", "${subject.class_name} @ day=$dayIndex, start=$startT, end=$endT → top=$top, left=$left")

                            val params = FrameLayout.LayoutParams(width, height)
                            params.topMargin = top
                            params.leftMargin = left
                            block.layoutParams = params
                            overlay.addView(block)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("applyToTimeTable", "오류 발생: ${e.message}", e)
                Toast.makeText(activity, "시간표 표시 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}






