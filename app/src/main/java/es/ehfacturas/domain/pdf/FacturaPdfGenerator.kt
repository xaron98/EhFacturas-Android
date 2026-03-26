// app/src/main/java/es/ehfacturas/domain/pdf/FacturaPdfGenerator.kt
package es.ehfacturas.domain.pdf

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import dagger.hilt.android.qualifiers.ApplicationContext
import es.ehfacturas.data.db.entity.*
import es.ehfacturas.data.repository.NegocioRepository
import es.ehfacturas.domain.validation.Formateadores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FacturaPdfGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val negocioRepository: NegocioRepository
) {
    companion object {
        // A4 en puntos (72 dpi)
        private const val PAGE_WIDTH = 595
        private const val PAGE_HEIGHT = 842
        private const val MARGIN = 40f
        private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN
    }

    private val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))

    private val paintTitulo = Paint().apply {
        textSize = 16f; isFakeBoldText = true; color = Color.BLACK
    }
    private val paintSubtitulo = Paint().apply {
        textSize = 11f; isFakeBoldText = true; color = Color.BLACK
    }
    private val paintNormal = Paint().apply {
        textSize = 10f; color = Color.BLACK
    }
    private val paintGris = Paint().apply {
        textSize = 9f; color = Color.GRAY
    }
    private val paintTotal = Paint().apply {
        textSize = 14f; isFakeBoldText = true; color = Color.BLACK
    }
    private val paintLinea = Paint().apply {
        color = Color.LTGRAY; strokeWidth = 0.5f
    }

    suspend fun generar(
        factura: Factura,
        lineas: List<LineaFactura>,
        registro: RegistroFacturacion?
    ): File = withContext(Dispatchers.IO) {
        val negocio = negocioRepository.obtenerNegocioSync()
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN

        // Cabecera negocio
        y = dibujarCabeceraNegocio(canvas, negocio, y)
        y += 20f

        // Datos factura
        y = dibujarDatosFactura(canvas, factura, y)
        y += 15f

        // Datos cliente
        y = dibujarDatosCliente(canvas, factura, y)
        y += 20f

        // Tabla lineas
        y = dibujarTablaLineas(canvas, lineas, y)
        y += 20f

        // Totales
        y = dibujarTotales(canvas, factura, lineas, negocio, y)
        y += 20f

        // Observaciones
        if (factura.observaciones.isNotEmpty()) {
            y = dibujarObservaciones(canvas, factura.observaciones, y)
        }

        // QR + Hash
        if (registro != null && registro.hashRegistro.isNotEmpty()) {
            y += 10f
            val qrBitmap = generarQRBitmap(registro.hashRegistro, 100)
            canvas.drawBitmap(qrBitmap, MARGIN, y, null)
            canvas.drawText("Hash: ${registro.hashRegistro}", MARGIN + 110f, y + 50f, paintGris)
            y += 110f
        }

        pdfDocument.finishPage(page)

        // Guardar archivo
        val dir = File(context.filesDir, "pdfs")
        dir.mkdirs()
        val nombreArchivo = factura.numeroFactura.replace("/", "-").ifEmpty { "factura" }
        val archivo = File(dir, "$nombreArchivo.pdf")
        FileOutputStream(archivo).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        archivo
    }

    private fun dibujarCabeceraNegocio(canvas: Canvas, negocio: Negocio?, y: Float): Float {
        var cy = y
        if (negocio == null) return cy

        canvas.drawText(negocio.nombre, MARGIN, cy, paintTitulo)
        cy += 16f
        if (negocio.nif.isNotEmpty()) {
            canvas.drawText("NIF: ${negocio.nif}", MARGIN, cy, paintNormal)
            cy += 13f
        }
        if (negocio.direccion.isNotEmpty()) {
            val direccionCompleta = buildString {
                append(negocio.direccion)
                if (negocio.codigoPostal.isNotEmpty()) append(", ${negocio.codigoPostal}")
                if (negocio.ciudad.isNotEmpty()) append(" ${negocio.ciudad}")
                if (negocio.provincia.isNotEmpty()) append(" (${negocio.provincia})")
            }
            canvas.drawText(direccionCompleta, MARGIN, cy, paintNormal)
            cy += 13f
        }
        if (negocio.telefono.isNotEmpty()) {
            canvas.drawText("Tel: ${negocio.telefono}", MARGIN, cy, paintNormal)
            cy += 13f
        }
        if (negocio.email.isNotEmpty()) {
            canvas.drawText(negocio.email, MARGIN, cy, paintNormal)
            cy += 13f
        }
        return cy
    }

    private fun dibujarDatosFactura(canvas: Canvas, factura: Factura, y: Float): Float {
        var cy = y
        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 16f

        canvas.drawText("FACTURA N\u00BA: ${factura.numeroFactura}", MARGIN, cy, paintSubtitulo)
        cy += 14f
        canvas.drawText("Fecha: ${formatoFecha.format(factura.fecha)}", MARGIN, cy, paintNormal)
        factura.fechaVencimiento?.let {
            canvas.drawText("Vencimiento: ${formatoFecha.format(it)}", MARGIN + 200f, cy, paintNormal)
        }
        cy += 14f
        return cy
    }

    private fun dibujarDatosCliente(canvas: Canvas, factura: Factura, y: Float): Float {
        var cy = y
        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 16f

        canvas.drawText("CLIENTE:", MARGIN, cy, paintSubtitulo)
        cy += 14f
        canvas.drawText(factura.clienteNombre, MARGIN, cy, paintNormal)
        cy += 13f
        if (factura.clienteNIF.isNotEmpty()) {
            canvas.drawText("NIF: ${factura.clienteNIF}", MARGIN, cy, paintNormal)
            cy += 13f
        }
        if (factura.clienteDireccion.isNotEmpty()) {
            canvas.drawText(factura.clienteDireccion, MARGIN, cy, paintNormal)
            cy += 13f
        }
        return cy
    }

    private fun dibujarTablaLineas(canvas: Canvas, lineas: List<LineaFactura>, y: Float): Float {
        var cy = y
        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 16f

        // Cabecera tabla
        val colConcepto = MARGIN
        val colCant = MARGIN + 280f
        val colPrecio = MARGIN + 340f
        val colIVA = MARGIN + 420f
        val colSubtotal = MARGIN + 470f

        canvas.drawText("Concepto", colConcepto, cy, paintSubtitulo)
        canvas.drawText("Cant.", colCant, cy, paintSubtitulo)
        canvas.drawText("Precio", colPrecio, cy, paintSubtitulo)
        canvas.drawText("IVA", colIVA, cy, paintSubtitulo)
        canvas.drawText("Subtotal", colSubtotal, cy, paintSubtitulo)
        cy += 4f
        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 14f

        lineas.forEach { linea ->
            val concepto = if (linea.concepto.length > 35) linea.concepto.take(35) + "..." else linea.concepto
            canvas.drawText(concepto, colConcepto, cy, paintNormal)
            canvas.drawText("${Formateadores.formatearDecimal(linea.cantidad)} ${linea.unidad.abreviatura}", colCant, cy, paintNormal)
            canvas.drawText(Formateadores.formatearDecimal(linea.precioUnitario), colPrecio, cy, paintNormal)
            canvas.drawText("${linea.porcentajeIVA.toInt()}%", colIVA, cy, paintNormal)
            canvas.drawText(Formateadores.formatearDecimal(linea.calcularSubtotal()), colSubtotal, cy, paintNormal)
            cy += 14f
        }

        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        return cy
    }

    private fun dibujarTotales(canvas: Canvas, factura: Factura, lineas: List<LineaFactura>, negocio: Negocio?, y: Float): Float {
        var cy = y
        val xEtiqueta = MARGIN + 330f
        val xValor = MARGIN + 470f

        canvas.drawText("Base imponible:", xEtiqueta, cy, paintNormal)
        canvas.drawText(Formateadores.formatearDecimal(factura.baseImponible), xValor, cy, paintNormal)
        cy += 14f

        if (factura.descuentoGlobalPorcentaje > 0) {
            canvas.drawText("Descuento ${Formateadores.formatearDecimal(factura.descuentoGlobalPorcentaje)}%:", xEtiqueta, cy, paintNormal)
            val descuento = factura.baseImponible * factura.descuentoGlobalPorcentaje / 100.0
            canvas.drawText("-${Formateadores.formatearDecimal(descuento)}", xValor, cy, paintNormal)
            cy += 14f
        }

        // Desglose IVA
        val desgloseIVA = lineas
            .groupBy { it.porcentajeIVA }
            .mapValues { (pct, ls) -> ls.sumOf { it.calcularSubtotal() } * pct / 100.0 }
        desgloseIVA.toSortedMap(compareByDescending { it }).forEach { (pct, importe) ->
            canvas.drawText("IVA ${pct.toInt()}%:", xEtiqueta, cy, paintNormal)
            canvas.drawText(Formateadores.formatearDecimal(importe), xValor, cy, paintNormal)
            cy += 14f
        }

        if (negocio?.aplicarIRPF == true && factura.totalIRPF > 0) {
            canvas.drawText("IRPF ${negocio.irpfPorcentaje.toInt()}%:", xEtiqueta, cy, paintNormal)
            canvas.drawText("-${Formateadores.formatearDecimal(factura.totalIRPF)}", xValor, cy, paintNormal)
            cy += 14f
        }

        canvas.drawLine(xEtiqueta, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 16f

        canvas.drawText("TOTAL:", xEtiqueta, cy, paintTotal)
        canvas.drawText("${Formateadores.formatearDecimal(factura.totalFactura)} \u20AC", xValor, cy, paintTotal)
        cy += 14f

        return cy
    }

    private fun generarQRBitmap(contenido: String, size: Int): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = MultiFormatWriter().encode(
            contenido, BarcodeFormat.QR_CODE, size, size, hints
        )
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    private fun dibujarObservaciones(canvas: Canvas, texto: String, y: Float): Float {
        var cy = y
        canvas.drawLine(MARGIN, cy, MARGIN + CONTENT_WIDTH, cy, paintLinea)
        cy += 16f
        canvas.drawText("Observaciones:", MARGIN, cy, paintSubtitulo)
        cy += 14f
        canvas.drawText(texto.take(100), MARGIN, cy, paintNormal)
        cy += 14f
        return cy
    }
}
