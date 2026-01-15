package com.example.stt_test

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var voiceDetector: VoiceTriggerDetector

    // 구조 신호 발송 상태
    private var isEmergencyState = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 권한 체크
        if (checkPermissions()) {
            initVoiceDetector()
        } else {
            requestPermissions()
        }

        // 2. 버튼 클릭 리스너 연결
        findViewById<Button>(R.id.btnStartListening).setOnClickListener {
            voiceDetector.startListening()
        }
    }

    private fun initVoiceDetector() {
        voiceDetector = VoiceTriggerDetector(this) { spokenText ->
            // 1. 화면에 자막 띄우기
            runOnUiThread {
                findViewById<TextView>(R.id.statusText).text = spokenText
                findViewById<TextView>(R.id.statusText).setTextColor(getColor(android.R.color.black))
            }

            // 2. 핵심 단어 검사 (띄어쓰기 무시)
            val cleanText = spokenText.replace(" ", "")
            if (cleanText.contains("살려주세요") || cleanText.contains("구조") || cleanText.contains("도와줘")) {
                triggerEmergencyMode(spokenText)
            }
        }
    }

    // ★ 이 함수가 반드시 class MainActivity 괄호 { ... } 안에 있어야 합니다!
    private fun triggerEmergencyMode(keyword: String) {
        if (isEmergencyState) return
        isEmergencyState = true

        runOnUiThread {
            Toast.makeText(this, "🚨 구조 요청 감지! ($keyword)", Toast.LENGTH_LONG).show()
            findViewById<TextView>(R.id.statusText).text = "구조 신호 송출 중..."
            findViewById<TextView>(R.id.statusText).setTextColor(getColor(android.R.color.holo_red_dark))

            // 여기에 진동이나 위치 전송 코드 추가 가능
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initVoiceDetector()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::voiceDetector.isInitialized) {
            voiceDetector.stopListening()
        }
    }
}