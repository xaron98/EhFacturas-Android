package es.ehfacturas.domain.validation

object NifValidator {
    private val NIF_REGEX = Regex("^[0-9]{8}[A-Z]$")
    private val CIF_REGEX = Regex("^[ABCDEFGHJKLMNPQRSUVW][0-9]{7}[0-9A-J]$")
    private val NIE_REGEX = Regex("^[XYZ][0-9]{7}[A-Z]$")
    private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun esValido(nif: String): Boolean {
        val limpio = nif.trim().uppercase()
        if (limpio.isEmpty()) return true
        return NIF_REGEX.matches(limpio) || CIF_REGEX.matches(limpio) || NIE_REGEX.matches(limpio)
    }

    fun esEmailValido(email: String): Boolean {
        val limpio = email.trim()
        if (limpio.isEmpty()) return true
        return EMAIL_REGEX.matches(limpio)
    }
}
