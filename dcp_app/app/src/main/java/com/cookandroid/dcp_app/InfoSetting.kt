package com.cookandroid.dcp_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.dcp_app.databinding.ActivityInfosettingBinding
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder

class InfoSetting : AppCompatActivity() {
    private lateinit var binding: ActivityInfosettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfosettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExit.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        ArrayAdapter.createFromResource(
            this, R.array.type, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.type.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.lecture_area, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.lectureArea.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.grade, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.grade.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.general_area, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.generalArea.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this, R.array.days_array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.days.adapter = adapter
        }

        binding.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()

                binding.lectureArea.visibility = if (selected == "교선균형") View.VISIBLE else View.GONE
                binding.grade.visibility = if (selected == "교양필수" || selected.startsWith("전공")) View.VISIBLE else View.GONE
                binding.dept.visibility = if (selected.startsWith("전공")) View.VISIBLE else View.GONE

                if (selected == "교양필수") {
                    val twoYearAdapter = ArrayAdapter.createFromResource(
                        this@InfoSetting, R.array.grade_two_years, android.R.layout.simple_spinner_item
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.grade.adapter = twoYearAdapter
                } else {
                    val fullAdapter = ArrayAdapter.createFromResource(
                        this@InfoSetting, R.array.grade, android.R.layout.simple_spinner_item
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.grade.adapter = fullAdapter
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnSearch.setOnClickListener {
            val type = binding.type.selectedItem.toString()
            val dept = binding.dept.text.toString()
            val grade = binding.grade.selectedItem?.toString()
            val day = binding.days.selectedItem.toString()
            val start = binding.editStartTime.text.toString()
            val end = binding.editEndTime.text.toString()
            val lectureArea = binding.lectureArea.selectedItem?.toString()
            val generalArea = binding.generalArea.selectedItem?.toString()

            CoroutineScope(Dispatchers.IO).launch {
                val filteredParams = when (type) {
                    "교양필수" -> listOf("day", "start", "end", "grade")
                    "교선균형" -> listOf("day", "start", "end", "lectureArea")
                    "전공필수", "전공선택" -> listOf("dept", "grade")
                    else -> emptyList()
                }

                fetchSubjects(
                    type = type,
                    dept = if ("dept" in filteredParams) dept else "",
                    grade = if ("grade" in filteredParams) grade else null,
                    day = if ("day" in filteredParams) day else "",
                    start = if ("start" in filteredParams) start else "",
                    end = if ("end" in filteredParams) end else "",
                    lectureArea = if ("lectureArea" in filteredParams) lectureArea else null,
                    generalArea = if ("generalArea" in filteredParams) generalArea else null
                )
            }
        }
    }

    private fun fetchSubjects(
        type: String = "",
        dept: String = "",
        grade: String? = null,
        day: String = "",
        start: String = "",
        end: String = "",
        lectureArea: String? = null,
        generalArea: String? = null
    ) {
        val baseUrl = "https://31c9-182-230-193-83.ngrok-free.app/subjects"
        val params = mutableListOf<String>()

        if (type.isNotBlank()) params.add("type=${URLEncoder.encode(type, "UTF-8")}")
        if (day.isNotBlank()) params.add("day=${URLEncoder.encode(day, "UTF-8")}")
        if (start.isNotBlank()) params.add("time_start=$start")
        if (end.isNotBlank()) params.add("time_end=$end")
        if (dept.isNotBlank()) params.add("dept=${URLEncoder.encode(dept, "UTF-8")}")
        grade?.let { params.add("grade=${URLEncoder.encode(it, "UTF-8")}") }
        lectureArea?.let { params.add("lecture_area=${URLEncoder.encode(it, "UTF-8")}") }
        generalArea?.let { params.add("general_filter=${URLEncoder.encode(it, "UTF-8")}") }

        val url = "$baseUrl?${params.joinToString("&")}"
        Log.d("API_REQUEST_URL", url)

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseData = response.body?.string()
                val moshi = Moshi.Builder().build()
                val listType = Types.newParameterizedType(List::class.java, SubjectInfo::class.java)
                val adapter = moshi.adapter<List<SubjectInfo>>(listType)
                val resultList = adapter.fromJson(responseData!!)

                runOnUiThread {
                    Toast.makeText(this, "조회 결과 ${resultList?.size ?: 0}건", Toast.LENGTH_SHORT).show()
                    resultList?.let { displaySubjects(it) }
                }
            } else {
                runOnUiThread {
                    Toast.makeText(this, "FastAPI 응답 실패", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "네트워크 오류: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displaySubjects(subjects: List<SubjectInfo>) {
        binding.resultContainer.removeAllViews()
        for ((index, subject) in subjects.withIndex()) {
            try {
                val cardView = layoutInflater.inflate(R.layout.item_class_info, binding.resultContainer, false)

                val txtClassId = cardView.findViewById<TextView>(R.id.class_id)
                val txtClassName = cardView.findViewById<TextView>(R.id.class_name)
                val txtProfessor = cardView.findViewById<TextView>(R.id.professor)
                val txtCredit = cardView.findViewById<TextView>(R.id.credit)
                val txtSchedule = cardView.findViewById<TextView>(R.id.schedule)

                txtClassId.text = subject.class_id ?: "-"
                txtClassName.text = subject.class_name ?: "-"
                txtProfessor.text = subject.professor ?: "-"
                txtCredit.text = subject.credit?.toString() ?: "-"
                txtSchedule.text = subject.schedule?.joinToString(", ") { "${it.day} ${it.time}교시" } ?: "-"

                cardView.setOnClickListener {
                    val existing = MainActivity.timetableData.any { added ->
                        added.schedule.any { addedSlot ->
                            subject.schedule.any { newSlot ->
                                addedSlot.day == newSlot.day && addedSlot.time == newSlot.time
                            }
                        }
                    }
                    if (existing) {
                        Toast.makeText(this@InfoSetting, "해당 시간에 이미 등록된 강의가 있습니다.", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    MainActivity.timetableData.add(subject)
                    Toast.makeText(this@InfoSetting, "과목이 시간표에 추가됨!", Toast.LENGTH_SHORT).show()
                }

                binding.resultContainer.addView(cardView)
            } catch (e: Exception) {
                Log.e("displaySubjects", "[$index] 렌더링 오류: ${e.localizedMessage}", e)
            }
        }
    }
}

@JsonClass(generateAdapter = true)
data class SubjectInfo(
    val id: Int,
    val dept: String?,
    val grade: String?,
    val class_id: String?,
    val class_num: Int?,
    val class_name: String?,
    val professor: String?,
    val credit: Int?,
    val type: String?,
    val general_area: String?,
    val lecture_area: String?,
    val schedule: List<Schedule>
)

@JsonClass(generateAdapter = true)
data class Schedule(
    val day: String,
    val time: Int
)