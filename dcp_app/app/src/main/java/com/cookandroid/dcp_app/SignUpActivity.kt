package com.cookandroid.dcp_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cookandroid.dcp_app.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignupConfirm.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                name.isEmpty() -> {
                    binding.etName.error = "이름을 입력하세요"
                    binding.etName.requestFocus()
                }
                phone.isEmpty() -> {
                    binding.etPhone.error = "전화번호를 입력하세요"
                    binding.etPhone.requestFocus()
                }
                email.isEmpty() -> {
                    binding.etEmail.error = "이메일을 입력하세요"
                    binding.etEmail.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.etEmail.error = "올바른 이메일 형식이 아닙니다"
                    binding.etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "비밀번호를 입력하세요"
                    binding.etPassword.requestFocus()
                }
                else -> {
                    Toast.makeText(this, "회원가입 완료!", Toast.LENGTH_SHORT).show()
                    // 로그인 화면으로 이동
                    val intent = Intent(this, LoginActivity::class.java)
                    // 이전 화면 스택을 지우고 로그인화면만 남기고 싶다면 아래 플래그 추가
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
}
