package com.example.stt_test

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import java.util.Locale

class VoiceTriggerDetector(
    private val context: Context,
    private val onDetected: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    init {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // 필요 시 주석 해제
        }
        setupListener()
    }

    private fun setupListener() {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                // 사용자가 알 수 있게 토스트 메시지 띄움
                Toast.makeText(context, "👂 듣고 있어요! 말해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("VoiceDetector", "듣기 시작")
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onDetected(it) }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { onDetected(it) }
                // ★ 중요: 여기서 재시작(restart)을 안 함 -> 한 번 듣고 끝남!
            }

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                // 에러가 나면 사용자에게 알려주고 끝냄 (재시작 X)
                Toast.makeText(context, "에러: $errorMessage", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    fun startListening() {
        // 버튼 누를 때마다 실행됨
        speechRecognizer?.startListening(recognizerIntent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_NO_MATCH -> "말소리가 안 들렸어요"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "시간 초과"
            SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
            else -> "에러 발생 ($errorCode)"
        }
    }
}