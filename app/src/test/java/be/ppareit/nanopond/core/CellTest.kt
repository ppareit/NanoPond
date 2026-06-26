package be.ppareit.nanopond.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CellTest {

    @Test
    fun constructorInitializesEmptyStoppedCell() {
        val cell = Cell()

        assertEquals(0, cell.ID)
        assertEquals(0, cell.parentID)
        assertEquals(0, cell.lineage)
        assertEquals(0, cell.generation)
        assertEquals(0, cell.energy)
        assertEquals(NanoPond.POND_DEPTH, cell.genome.size)
        assertTrue(cell.genome.all { it == 0xf.toByte() })
    }

    @Test
    fun setGenomeParsesLowercaseHexAndStopsRemainingGenome() {
        val cell = Cell()

        cell.setGenome("0123456789abcdef")

        assertArrayEquals(
            byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            cell.genome.copyOfRange(0, 16)
        )
        assertTrue(cell.genome.copyOfRange(16, NanoPond.POND_DEPTH).all { it == 0xf.toByte() })
    }

    @Test
    fun setGenomeParsesUppercaseHex() {
        val cell = Cell()

        cell.setGenome("ABCDEF")

        assertArrayEquals(
            byteArrayOf(10, 11, 12, 13, 14, 15),
            cell.genome.copyOfRange(0, 6)
        )
        assertTrue(cell.genome.copyOfRange(6, NanoPond.POND_DEPTH).all { it == 0xf.toByte() })
    }

    @Test
    fun setRandomGenomeKeepsGenomeSizeAndInstructionRange() {
        val cell = Cell()

        cell.setRandomGenome()

        assertEquals(NanoPond.POND_DEPTH, cell.genome.size)
        assertTrue(cell.genome.all { it in 0.toByte()..15.toByte() })
    }

    @Test
    fun hexaReturnsGenomeUntilFirstStopPair() {
        val cell = Cell()
        cell.setGenome("0123456789abcdef")

        assertEquals("0123456789abcdef", cell.hexa)
    }

    @Test
    fun hexaForEmptyStoppedCellReturnsSingleStopInstruction() {
        val cell = Cell()

        assertEquals("f", cell.hexa)
    }
}
