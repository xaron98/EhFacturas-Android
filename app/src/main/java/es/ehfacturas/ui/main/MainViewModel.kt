package es.ehfacturas.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.ehfacturas.ai.*
import es.ehfacturas.data.repository.NegocioRepository
import es.ehfacturas.speech.SpeechService
import es.ehfacturas.speech.TTSService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EstadoProcesamiento {
    LISTO,
    ESCUCHANDO,
    PROCESANDO,
    RESPONDIDO,
    ERROR
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val aiProviderFactory: AIProviderFactory,
    private val toolExecutor: ToolExecutor,
    val speechService: SpeechService,
    val ttsService: TTSService,
    private val negocioRepository: NegocioRepository
) : ViewModel() {

    private val _mensajes = MutableStateFlow<List<MensajeChat>>(emptyList())
    val mensajes: StateFlow<List<MensajeChat>> = _mensajes.asStateFlow()

    private val _estado = MutableStateFlow(EstadoProcesamiento.LISTO)
    val estado: StateFlow<EstadoProcesamiento> = _estado.asStateFlow()

    private val _estadoDetallado = MutableStateFlow("")
    val estadoDetallado: StateFlow<String> = _estadoDetallado.asStateFlow()

    private val _textoManual = MutableStateFlow("")
    val textoManual: StateFlow<String> = _textoManual.asStateFlow()

    private val _navegarAFactura = MutableSharedFlow<Long>()
    val navegarAFactura: SharedFlow<Long> = _navegarAFactura.asSharedFlow()

    private val _mostrarConfigIA = MutableStateFlow(false)
    val mostrarConfigIA: StateFlow<Boolean> = _mostrarConfigIA.asStateFlow()

    fun ocultarConfigIA() { _mostrarConfigIA.value = false }

    val estaEscuchando: StateFlow<Boolean> = speechService.estaEscuchando
    val textoTranscrito: StateFlow<String> = speechService.textoTranscrito
    val nivelAudio: StateFlow<Float> = speechService.nivelAudio
    val errorSpeech: StateFlow<String?> = speechService.errorMensaje

    init {
        ttsService.inicializar()
    }

    fun limpiarErrorSpeech() {
        speechService.limpiarError()
    }

    fun setTextoManual(texto: String) {
        _textoManual.value = texto
    }

    fun enviarTexto(texto: String) {
        if (texto.isBlank()) return
        _textoManual.value = ""

        // Añadir mensaje del usuario
        addMensaje(MensajeChat(tipo = TipoMensaje.USUARIO, texto = texto))

        // Procesar con IA
        procesarComando(texto)
    }

    fun toggleMicrofono() {
        speechService.toggleEscucha { textoReconocido ->
            if (textoReconocido.isNotBlank()) {
                enviarTexto(textoReconocido)
            }
        }
    }

    private fun procesarComando(texto: String) {
        viewModelScope.launch {
            _estado.value = EstadoProcesamiento.PROCESANDO
            _estadoDetallado.value = detectarEstadoDetallado(texto)

            try {
                val provider = aiProviderFactory.getProvider()

                if (!provider.isAvailable) {
                    addMensaje(MensajeChat(
                        tipo = TipoMensaje.ERROR,
                        texto = provider.unavailableReason
                    ))
                    _mostrarConfigIA.value = true
                    _estado.value = EstadoProcesamiento.ERROR
                    return@launch
                }

                val systemPrompt = buildSystemPrompt()
                val tools = CloudToolSchemas.commandTools()

                val resultado = provider.processCommand(
                    texto = texto,
                    systemPrompt = systemPrompt,
                    tools = tools
                ) { toolName, arguments ->
                    _estadoDetallado.value = estadoParaTool(toolName)
                    toolExecutor.executeTool(toolName, arguments)
                }

                // Limpiar [ID:XX] del texto mostrado
                val textoLimpio = resultado.texto.replace(Regex("\\s*\\[ID:\\d+]"), "")

                // Navegar a factura si se creó una
                if (resultado.toolUsed == "crear_factura") {
                    val idMatch = Regex("\\[ID:(\\d+)]").find(resultado.texto)
                    idMatch?.groupValues?.get(1)?.toLongOrNull()?.let { facturaId ->
                        _navegarAFactura.emit(facturaId)
                    }
                }

                addMensaje(MensajeChat(
                    tipo = TipoMensaje.IA,
                    texto = textoLimpio,
                    toolUsed = resultado.toolUsed
                ))

                // Leer respuesta en voz alta
                ttsService.hablar(textoLimpio)

                _estado.value = EstadoProcesamiento.RESPONDIDO

            } catch (e: Exception) {
                addMensaje(MensajeChat(
                    tipo = TipoMensaje.ERROR,
                    texto = "Error: ${e.message}"
                ))
                _estado.value = EstadoProcesamiento.ERROR
            }
        }
    }

    private fun addMensaje(mensaje: MensajeChat) {
        _mensajes.value = _mensajes.value + mensaje
    }

    private suspend fun buildSystemPrompt(): String {
        val negocio = negocioRepository.obtenerNegocioSync()
        val tieneNegocio = negocio != null && negocio.nombre.isNotEmpty()

        return if (!tieneNegocio) {
            """
            Eres el asistente de EhFacturas!, app de facturación para autónomos en España.

            MODO ONBOARDING: El usuario no ha configurado su negocio todavía.
            Guíale paso a paso para configurar sus datos. Pregunta UN campo cada vez:
            1. Nombre del negocio
            2. NIF/CIF
            3. Teléfono
            4. Email
            5. Dirección
            6. Código postal y ciudad
            7. Provincia

            Si el usuario te da varios datos a la vez, usa configurar_negocio inmediatamente.
            Sé breve y amable. Habla en español.
            """.trimIndent()
        } else {
            """
            Eres el asistente de EhFacturas!, app de facturación para autónomos en España.
            Negocio: ${negocio!!.nombre} (NIF: ${negocio.nif})

            REGLAS CRÍTICAS:
            - EJECUTA LAS HERRAMIENTAS INMEDIATAMENTE. No preguntes, no confirmes, ACTÚA.
            - Nunca preguntes por campos opcionales (descuento, observaciones).
            - Cantidad por defecto: 1. Unidad por defecto: "ud" (productos) o "h" (servicios).
            - Si el usuario dice "presupuesto", usa esPresupuesto=true.
            - Sé breve y directo en tus respuestas.
            - Responde siempre en español.

            Prefijo facturas: ${negocio.prefijoFactura}
            Siguiente número: ${negocio.siguienteNumero}
            IVA general: ${negocio.ivaGeneral}%
            IRPF: ${if (negocio.aplicarIRPF) "${negocio.irpfPorcentaje}%" else "No aplica"}
            """.trimIndent()
        }
    }

    private fun detectarEstadoDetallado(texto: String): String {
        val lower = texto.lowercase()
        return when {
            "factura" in lower || "presupuesto" in lower -> "Generando factura..."
            "cliente" in lower -> "Gestionando cliente..."
            "artículo" in lower || "producto" in lower -> "Gestionando artículo..."
            "gasto" in lower -> "Registrando gasto..."
            "resumen" in lower || "cuánto" in lower -> "Consultando datos..."
            "paga" in lower -> "Actualizando estado..."
            "anula" in lower -> "Anulando factura..."
            "configura" in lower || "negocio" in lower -> "Configurando negocio..."
            else -> "Procesando..."
        }
    }

    private fun estadoParaTool(toolName: String): String {
        return when (toolName) {
            "crear_factura" -> "Creando factura..."
            "crear_cliente" -> "Creando cliente..."
            "crear_articulo" -> "Creando artículo..."
            "registrar_gasto" -> "Registrando gasto..."
            "marcar_pagada" -> "Marcando como pagada..."
            "anular_factura" -> "Anulando factura..."
            "consultar_resumen" -> "Consultando resumen..."
            "configurar_negocio" -> "Configurando negocio..."
            "buscar_cliente" -> "Buscando clientes..."
            "buscar_articulo" -> "Buscando artículos..."
            else -> "Ejecutando..."
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechService.destroy()
        ttsService.destroy()
    }
}
