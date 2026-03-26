package es.ehfacturas.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class TipoVoz(val descripcion: String) {
    FEMENINA("Femenina"),
    MASCULINA("Masculina"),
    DESACTIVADA("Desactivada")
}

@Singleton
class TTSService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _hablando = MutableStateFlow(false)
    val hablando: StateFlow<Boolean> = _hablando.asStateFlow()

    private val _vozActiva = MutableStateFlow(true)
    val vozActiva: StateFlow<Boolean> = _vozActiva.asStateFlow()

    private val _tipoVoz = MutableStateFlow(TipoVoz.FEMENINA)
    val tipoVoz: StateFlow<TipoVoz> = _tipoVoz.asStateFlow()

    fun inicializar() {
        if (tts != null) return

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                configurarVoz()
            }
        }
    }

    private fun configurarVoz() {
        tts?.let { engine ->
            engine.language = Locale("es", "ES")

            // Ajustar pitch según tipo de voz
            when (_tipoVoz.value) {
                TipoVoz.FEMENINA -> engine.setPitch(1.1f)
                TipoVoz.MASCULINA -> engine.setPitch(0.85f)
                TipoVoz.DESACTIVADA -> {}
            }

            engine.setSpeechRate(1.0f)

            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _hablando.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _hablando.value = false
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _hablando.value = false
                }
            })
        }
    }

    fun hablar(texto: String) {
        if (!_vozActiva.value || _tipoVoz.value == TipoVoz.DESACTIVADA) return
        if (!isInitialized) {
            inicializar()
            return
        }

        // Limpiar texto: quitar emojis y markdown
        val textoLimpio = texto
            .replace(Regex("[\\p{So}\\p{Cn}]"), "")  // Emojis
            .replace(Regex("[*_~`#>]"), "")            // Markdown
            .replace(Regex("\\[.*?]\\(.*?\\)"), "")    // Links
            .replace(Regex("•|–|—"), ",")               // Bullets
            .trim()

        if (textoLimpio.isEmpty()) return

        tts?.speak(
            textoLimpio,
            TextToSpeech.QUEUE_FLUSH,
            null,
            UUID.randomUUID().toString()
        )
    }

    fun detener() {
        tts?.stop()
        _hablando.value = false
    }

    fun setVozActiva(activa: Boolean) {
        _vozActiva.value = activa
        if (!activa) detener()
    }

    fun setTipoVoz(tipo: TipoVoz) {
        _tipoVoz.value = tipo
        configurarVoz()
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
