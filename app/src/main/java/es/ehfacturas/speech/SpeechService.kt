package es.ehfacturas.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

/**
 * Servicio de reconocimiento de voz continuo.
 *
 * Replica el comportamiento de iOS (SFSpeechRecognizer):
 * - Escucha continua mientras el usuario habla
 * - Timer de silencio de 2.5s que se resetea con cada palabra nueva
 * - Auto-reinicio transparente cuando Android corta la escucha
 * - Solo envía el resultado final cuando el timer de silencio expira
 */
@Singleton
class SpeechService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val _textoTranscrito = MutableStateFlow("")
    val textoTranscrito: StateFlow<String> = _textoTranscrito.asStateFlow()

    private val _estaEscuchando = MutableStateFlow(false)
    val estaEscuchando: StateFlow<Boolean> = _estaEscuchando.asStateFlow()

    private val _nivelAudio = MutableStateFlow(0f)
    val nivelAudio: StateFlow<Float> = _nivelAudio.asStateFlow()

    private val _errorMensaje = MutableStateFlow<String?>(null)
    val errorMensaje: StateFlow<String?> = _errorMensaje.asStateFlow()

    private var onResultado: ((String) -> Unit)? = null

    // Texto acumulado entre reinicios del recognizer
    private var textoAcumulado = ""
    // Controla si estamos en modo escucha continua (el usuario no ha pulsado stop)
    private var escuchaContinuaActiva = false
    // Timer de silencio (2.5s como iOS)
    private var silenceRunnable: Runnable? = null
    private val SILENCE_TIMEOUT_MS = 2500L

    val isDisponible: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(context)

    fun iniciarEscucha(onResultado: (String) -> Unit) {
        this.onResultado = onResultado
        _errorMensaje.value = null
        _textoTranscrito.value = ""
        textoAcumulado = ""
        escuchaContinuaActiva = true

        if (!isDisponible) {
            _errorMensaje.value = "Reconocimiento de voz no disponible"
            return
        }

        iniciarRecognizer()
    }

    private fun iniciarRecognizer() {
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
            // Extender timeouts de silencio para que Android no corte tan rápido
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000L)
        }
    }

    fun detenerEscucha() {
        escuchaContinuaActiva = false
        cancelarTimerSilencio()
        speechRecognizer?.stopListening()
        _estaEscuchando.value = false
        _nivelAudio.value = 0f

        // Si hay texto acumulado, enviarlo
        val textoFinal = textoAcumulado.trim()
        if (textoFinal.isNotEmpty()) {
            onResultado?.invoke(textoFinal)
            textoAcumulado = ""
        }
    }

    fun toggleEscucha(onResultado: (String) -> Unit) {
        if (_estaEscuchando.value || escuchaContinuaActiva) {
            detenerEscucha()
        } else {
            iniciarEscucha(onResultado)
        }
    }

    fun limpiarError() {
        _errorMensaje.value = null
    }

    fun destroy() {
        escuchaContinuaActiva = false
        cancelarTimerSilencio()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // --- Timer de silencio (como iOS: 2.5s) ---

    private fun reiniciarTimerSilencio() {
        cancelarTimerSilencio()
        silenceRunnable = Runnable {
            // Timer expiró: el usuario dejó de hablar
            if (escuchaContinuaActiva) {
                escuchaContinuaActiva = false
                _estaEscuchando.value = false
                _nivelAudio.value = 0f
                speechRecognizer?.stopListening()

                val textoFinal = textoAcumulado.trim()
                if (textoFinal.isNotEmpty()) {
                    onResultado?.invoke(textoFinal)
                    textoAcumulado = ""
                }
            }
        }
        handler.postDelayed(silenceRunnable!!, SILENCE_TIMEOUT_MS)
    }

    private fun cancelarTimerSilencio() {
        silenceRunnable?.let { handler.removeCallbacks(it) }
        silenceRunnable = null
    }

    // --- Reinicio transparente ---

    private fun reiniciarEscuchaTransparente() {
        if (!escuchaContinuaActiva) return
        // Reiniciar el recognizer sin que el usuario lo note
        _estaEscuchando.value = true
        handler.postDelayed({
            if (escuchaContinuaActiva) {
                iniciarRecognizer()
            }
        }, 100) // Pequeño delay para evitar errores de reinicio rápido
    }

    // --- Listener ---

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _estaEscuchando.value = true
        }

        override fun onBeginningOfSpeech() {
            // El usuario empezó a hablar, cancelar timer de silencio
            cancelarTimerSilencio()
        }

        override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            _nivelAudio.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            // Android detectó fin de habla — NO paramos, dejamos que onResults maneje
            // El timer de silencio decidirá si realmente terminó
        }

        override fun onError(error: Int) {
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    if (escuchaContinuaActiva) {
                        // Reiniciar transparentemente
                        reiniciarTimerSilencio()
                        reiniciarEscuchaTransparente()
                    }
                    // Si no está activa, silenciar — no es un error real
                    return
                }
                SpeechRecognizer.ERROR_CLIENT -> {
                    // Usuario canceló
                    return
                }
            }

            // Error real: parar
            escuchaContinuaActiva = false
            cancelarTimerSilencio()
            _estaEscuchando.value = false
            _nivelAudio.value = 0f
            _errorMensaje.value = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Error de audio"
                SpeechRecognizer.ERROR_NETWORK -> "Error de red"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout de red"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Sin permiso de micrófono"
                else -> "Error de reconocimiento ($error)"
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = matches?.firstOrNull() ?: ""

            if (texto.isNotEmpty()) {
                // Acumular texto (separado por espacio si ya hay texto previo)
                textoAcumulado = if (textoAcumulado.isEmpty()) texto
                                 else "$textoAcumulado $texto"
                _textoTranscrito.value = textoAcumulado
            }

            if (escuchaContinuaActiva) {
                // Reiniciar timer de silencio y seguir escuchando
                reiniciarTimerSilencio()
                reiniciarEscuchaTransparente()
            } else {
                // Escucha terminada manualmente
                _estaEscuchando.value = false
                _nivelAudio.value = 0f
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val texto = matches?.firstOrNull() ?: ""
            if (texto.isNotEmpty()) {
                // Mostrar texto acumulado + parcial actual
                val textoCompleto = if (textoAcumulado.isEmpty()) texto
                                    else "$textoAcumulado $texto"
                _textoTranscrito.value = textoCompleto
                // Resetear timer: el usuario sigue hablando
                reiniciarTimerSilencio()
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
