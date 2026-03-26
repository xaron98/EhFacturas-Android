package es.ehfacturas.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _textoTranscrito = MutableStateFlow("")
    val textoTranscrito: StateFlow<String> = _textoTranscrito.asStateFlow()

    private val _estaEscuchando = MutableStateFlow(false)
    val estaEscuchando: StateFlow<Boolean> = _estaEscuchando.asStateFlow()

    private val _nivelAudio = MutableStateFlow(0f)
    val nivelAudio: StateFlow<Float> = _nivelAudio.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    private var onResultado: ((String) -> Unit)? = null

    val isDisponible: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun iniciarEscucha(onResultado: (String) -> Unit) {
        this.onResultado = onResultado
        _errorMensaje.value = null
        _textoTranscrito.value = ""

        if (!isDisponible) {
            _errorMensaje.value = "Reconocimiento de voz no disponible"
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createListener())
        }

        speechRecognizer?.startListening(createRecognizerIntent())
        _estaEscuchando.value = true
    }

    private fun createRecognizerIntent(): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("es", "ES").toLanguageTag())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
    }

    fun detenerEscucha() {
        speechRecognizer?.stopListening()
        _estaEscuchando.value = false
        _nivelAudio.value = 0f
    }

    fun toggleEscucha(onResultado: (String) -> Unit) {
        if (_estaEscuchando.value) {
            detenerEscucha()
        } else {
            iniciarEscucha(onResultado)
        }
    }

    fun limpiarError() {
        _errorMensaje.value = null
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _estaEscuchando.value = true
        }

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {
            // Normalizar RMS a 0.0-1.0
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _nivelAudio.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _estaEscuchando.value = false
            _nivelAudio.value = 0f
        }

        override fun onError(error: Int) {
            _estaEscuchando.value = false
            _nivelAudio.value = 0f

            // Auto-retry: silencio detectado, reintentar escucha sin mostrar error
            if (error == SpeechRecognizer.ERROR_NO_MATCH && onResultado != null) {
                _estaEscuchando.value = true
                speechRecognizer?.startListening(createRecognizerIntent())
                return
            }

            _errorMensaje.value = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_CLIENT -> null // Usuario canceló
                SpeechRecognizer.ERROR_NO_MATCH -> "No se reconoció el habla"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tiempo de espera agotado"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Sin permiso de micrófono"
                else -> "Error de reconocimiento ($error)"
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = matches?.firstOrNull() ?: ""
            _textoTranscrito.value = texto
            _estaEscuchando.value = false
            _nivelAudio.value = 0f
            if (texto.isNotEmpty()) {
                onResultado?.invoke(texto)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = matches?.firstOrNull() ?: ""
            if (texto.isNotEmpty()) {
                _textoTranscrito.value = texto
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
