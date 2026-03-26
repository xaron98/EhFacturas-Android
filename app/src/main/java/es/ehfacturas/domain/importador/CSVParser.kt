package es.ehfacturas.domain.importador

import java.io.InputStream
import java.nio.charset.Charset

data class ResultadoCSV(
    val cabeceras: List<String>,
    val filas: List<List<String>>,
    val separador: Char,
    val encoding: String
)

object CSVParser {
    fun parsear(inputStream: InputStream): ResultadoCSV {
        val bytes = inputStream.readBytes()

        // Detectar encoding
        val encoding = detectarEncoding(bytes)
        val texto = String(bytes, Charset.forName(encoding))

        // Detectar separador
        val primeraLinea = texto.lines().firstOrNull() ?: ""
        val separador = detectarSeparador(primeraLinea)

        // Parsear
        val lineas = parsearLineas(texto, separador)
        val cabeceras = lineas.firstOrNull()?.map { it.trim().lowercase() } ?: emptyList()
        val filas = if (lineas.size > 1) lineas.drop(1) else emptyList()

        return ResultadoCSV(cabeceras, filas, separador, encoding)
    }

    private fun detectarEncoding(bytes: ByteArray): String {
        return try {
            val texto = String(bytes, Charsets.UTF_8)
            if (texto.contains('\uFFFD')) "ISO-8859-1" else "UTF-8"
        } catch (_: Exception) {
            "ISO-8859-1"
        }
    }

    private fun detectarSeparador(linea: String): Char {
        val counts = mapOf(
            ';' to linea.count { it == ';' },
            ',' to linea.count { it == ',' },
            '\t' to linea.count { it == '\t' }
        )
        return counts.maxByOrNull { it.value }?.key ?: ';'
    }

    private fun parsearLineas(texto: String, separador: Char): List<List<String>> {
        val resultado = mutableListOf<List<String>>()
        val lineas = texto.lines().filter { it.isNotBlank() }

        for (linea in lineas) {
            val campos = mutableListOf<String>()
            val campo = StringBuilder()
            var dentroComillas = false
            var i = 0

            while (i < linea.length) {
                val c = linea[i]
                when {
                    c == '"' && !dentroComillas -> dentroComillas = true
                    c == '"' && dentroComillas -> {
                        if (i + 1 < linea.length && linea[i + 1] == '"') {
                            campo.append('"')
                            i++
                        } else {
                            dentroComillas = false
                        }
                    }
                    c == separador && !dentroComillas -> {
                        campos.add(campo.toString().trim())
                        campo.clear()
                    }
                    else -> campo.append(c)
                }
                i++
            }
            campos.add(campo.toString().trim())
            resultado.add(campos)
        }

        return resultado
    }
}
