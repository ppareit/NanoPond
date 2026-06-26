package be.ppareit.nanopond.core

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class NanoPondTest {

    @Test
    fun getNeighborWrapsAtPondEdges() {
        val nanoPond = NanoPond()

        assertSame(
            nanoPond.pond[NanoPond.POND_SIZE_X - 1][5],
            nanoPond.getNeighbor(0, 5, NanoPond.Direction.LEFT)
        )
        assertSame(
            nanoPond.pond[0][5],
            nanoPond.getNeighbor(NanoPond.POND_SIZE_X - 1, 5, NanoPond.Direction.RIGHT)
        )
        assertSame(
            nanoPond.pond[7][NanoPond.POND_SIZE_Y - 1],
            nanoPond.getNeighbor(7, 0, NanoPond.Direction.UP)
        )
        assertSame(
            nanoPond.pond[7][0],
            nanoPond.getNeighbor(7, NanoPond.POND_SIZE_Y - 1, NanoPond.Direction.DOWN)
        )
    }

    @Test
    fun getNeighborReturnsAdjacentInteriorCells() {
        val nanoPond = NanoPond()

        assertSame(nanoPond.pond[9][10], nanoPond.getNeighbor(10, 10, NanoPond.Direction.LEFT))
        assertSame(nanoPond.pond[11][10], nanoPond.getNeighbor(10, 10, NanoPond.Direction.RIGHT))
        assertSame(nanoPond.pond[10][9], nanoPond.getNeighbor(10, 10, NanoPond.Direction.UP))
        assertSame(nanoPond.pond[10][11], nanoPond.getNeighbor(10, 10, NanoPond.Direction.DOWN))
    }

    @Test
    fun seedSetsCellMetadataAndCopiesGenome() {
        val nanoPond = NanoPond()
        val genome = ByteArray(NanoPond.POND_DEPTH) { (it % 16).toByte() }

        assertTrue(nanoPond.seed(3, 4, genome))

        val cell = nanoPond.pond[3][4]
        assertEquals(5, cell.generation)
        assertEquals(10000, cell.energy)
        assertEquals(-1, cell.ID)
        assertEquals(-1, cell.parentID)
        assertEquals(-1, cell.lineage)
        assertArrayEquals(genome, cell.genome)
    }
}
