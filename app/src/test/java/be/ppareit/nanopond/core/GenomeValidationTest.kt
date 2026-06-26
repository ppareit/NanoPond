package be.ppareit.nanopond.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenomeValidationTest {

    @Test
    fun acceptsExactlyPondDepthHexCharacters() {
        assertTrue(isValidGenomeHex("a".repeat(NanoPond.POND_DEPTH)))
    }

    @Test
    fun rejectsNonHexCharacters() {
        assertFalse(isValidGenomeHex("a".repeat(NanoPond.POND_DEPTH - 1) + "x"))
    }

    @Test
    fun rejectsShortGenome() {
        assertFalse(isValidGenomeHex("a".repeat(NanoPond.POND_DEPTH - 1)))
    }

    @Test
    fun rejectsLongGenome() {
        assertFalse(isValidGenomeHex("a".repeat(NanoPond.POND_DEPTH + 1)))
    }
}
