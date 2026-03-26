package es.ehfacturas.data.db.converter

import androidx.room.TypeConverter
import es.ehfacturas.data.db.entity.*
import java.util.Date

class Converters {
    // Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    // UnidadMedida
    @TypeConverter
    fun fromUnidadMedida(value: UnidadMedida): String = value.name

    @TypeConverter
    fun toUnidadMedida(value: String): UnidadMedida = UnidadMedida.valueOf(value)

    // TipoIVA
    @TypeConverter
    fun fromTipoIVA(value: TipoIVA): String = value.name

    @TypeConverter
    fun toTipoIVA(value: String): TipoIVA = TipoIVA.valueOf(value)

    // EstadoFactura
    @TypeConverter
    fun fromEstadoFactura(value: EstadoFactura): String = value.name

    @TypeConverter
    fun toEstadoFactura(value: String): EstadoFactura = EstadoFactura.valueOf(value)

    // TipoRegistro
    @TypeConverter
    fun fromTipoRegistro(value: TipoRegistro): String = value.name

    @TypeConverter
    fun toTipoRegistro(value: String): TipoRegistro = TipoRegistro.valueOf(value)

    // TipoFacturaVF
    @TypeConverter
    fun fromTipoFacturaVF(value: TipoFacturaVF): String = value.name

    @TypeConverter
    fun toTipoFacturaVF(value: String): TipoFacturaVF = TipoFacturaVF.valueOf(value)

    // EstadoEnvioVF
    @TypeConverter
    fun fromEstadoEnvioVF(value: EstadoEnvioVF): String = value.name

    @TypeConverter
    fun toEstadoEnvioVF(value: String): EstadoEnvioVF = EstadoEnvioVF.valueOf(value)

    // TipoImportacion
    @TypeConverter
    fun fromTipoImportacion(value: TipoImportacion): String = value.name

    @TypeConverter
    fun toTipoImportacion(value: String): TipoImportacion = TipoImportacion.valueOf(value)

    // List<String> como JSON
    @TypeConverter
    fun fromStringList(value: List<String>): String =
        value.joinToString(separator = "|||")

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split("|||")
}
