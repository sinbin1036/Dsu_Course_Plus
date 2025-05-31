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
            this,
            R.array.type,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.type.adapter = adapter
        }

        binding.type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                val selected = parent?.getItemAtPosition(position).toString()
                binding.lectureArea.visibility = if (selected == "교선균형") View.VISIBLE else View.INVISIBLE
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.btnSearch.setOnClickListener {
            val type = binding.type.selectedItem.toString()
            val dept = binding.dept.text.toString()
            val day = binding.days.selectedItem.toString()
            val start = binding.editStartTime.text.toString()
            val end = binding.editEndTime.text.toString()
            val lectureArea = binding.lectureArea.selectedItem?.toString()?.takeIf { it.isNotBlank() }
            val generalArea = binding.generalArea.selectedItem?.toString()?.takeIf { it.isNotBlank() }

            CoroutineScope(Dispatchers.IO).launch {
                fetchSubjects(type, dept, day, start, end, lectureArea, generalArea)
            }
        }
    }

    private fun fetchSubjects(
        type: String = "전공선택",
        dept: String = "소프트웨어학과",
        day: String = "월",
        start: String = "2",
        end: String = "3",
        lectureArea: String? = null,
        generalArea: String? = null
    ) {
        val baseUrl = "https://48ca-182-230-193-83.ngrok-free.app/subjects"
        val params = mutableListOf<String>()

        if (type.isNotBlank()) params.add("type=${URLEncoder.encode(type, "UTF-8")}")
        if (day.isNotBlank()) params.add("day=${URLEncoder.encode(day, "UTF-8")}")
        if (start.isNotBlank()) params.add("time_start=$start")
        if (end.isNotBlank()) params.add("time_end=$end")
        if (dept.isNotBlank()) params.add("dept=${URLEncoder.encode(dept, "UTF-8")}")
        // 🔕 임시로 lecture_area, general_area 미전송 (쿼리 제외 테스트용)
        // if (!lectureArea.isNullOrBlank()) params.add("lecture_area=${URLEncoder.encode(lectureArea, "UTF-8")}")
        // if (!generalArea.isNullOrBlank()) params.add("general_area=${URLEncoder.encode(generalArea, "UTF-8")}")
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

        Log.d("displaySubjects", "받은 subject 개수: ${subjects.size}")

        for ((index, subject) in subjects.withIndex()) {
            Log.d("displaySubjects", "[$index] subject: $subject")

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

