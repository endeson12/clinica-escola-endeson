package br.edu.clinicaescola.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @Test
    void aceitaCpfValidoComESemMascara() {
        // CPFs gerados apenas para teste (ficticios, digitos verificadores corretos)
        assertTrue(CpfValidator.isValido("529.982.247-25"));
        assertTrue(CpfValidator.isValido("52998224725"));
        assertTrue(CpfValidator.isValido("111.444.777-35"));
    }

    @Test
    void rejeitaDigitoVerificadorErrado() {
        assertFalse(CpfValidator.isValido("529.982.247-24"));
        assertFalse(CpfValidator.isValido("111.444.777-36"));
    }

    @Test
    void rejeitaSequenciasRepetidasETamanhoErrado() {
        assertFalse(CpfValidator.isValido("000.000.000-00"));
        assertFalse(CpfValidator.isValido("111.111.111-11"));
        assertFalse(CpfValidator.isValido("1234567890"));
        assertFalse(CpfValidator.isValido(""));
        assertFalse(CpfValidator.isValido(null));
    }

    @Test
    void normalizaRemovendoMascara() {
        assertEquals("52998224725", CpfValidator.normalizar(" 529.982.247-25 "));
    }
}
