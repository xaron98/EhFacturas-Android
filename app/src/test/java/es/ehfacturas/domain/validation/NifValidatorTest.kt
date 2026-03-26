package es.ehfacturas.domain.validation

import org.junit.Assert.*
import org.junit.Test

class NifValidatorTest {
    @Test fun `NIF valido con 8 digitos y letra`() {
        assertTrue(NifValidator.esValido("12345678Z"))
        assertTrue(NifValidator.esValido("00000000T"))
    }
    @Test fun `CIF valido con letra inicial y 7 digitos`() {
        assertTrue(NifValidator.esValido("A12345678"))
        assertTrue(NifValidator.esValido("B99999999"))
        assertTrue(NifValidator.esValido("W1234567J"))
    }
    @Test fun `NIE valido con X Y Z inicial`() {
        assertTrue(NifValidator.esValido("X1234567L"))
        assertTrue(NifValidator.esValido("Y1234567X"))
        assertTrue(NifValidator.esValido("Z1234567R"))
    }
    @Test fun `campo vacio es valido`() {
        assertTrue(NifValidator.esValido(""))
        assertTrue(NifValidator.esValido("  "))
    }
    @Test fun `formatos invalidos`() {
        assertFalse(NifValidator.esValido("1234"))
        assertFalse(NifValidator.esValido("ABC"))
        assertFalse(NifValidator.esValido("1234567Z"))
        assertFalse(NifValidator.esValido("123456789Z"))
        assertFalse(NifValidator.esValido("12345678"))
    }
    @Test fun `email valido`() {
        assertTrue(NifValidator.esEmailValido("test@mail.com"))
        assertTrue(NifValidator.esEmailValido("a@b.es"))
        assertTrue(NifValidator.esEmailValido(""))
    }
    @Test fun `email invalido`() {
        assertFalse(NifValidator.esEmailValido("noarroba"))
        assertFalse(NifValidator.esEmailValido("@sinusuario.com"))
        assertFalse(NifValidator.esEmailValido("sin.dominio@"))
    }
}
