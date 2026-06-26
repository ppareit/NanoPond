package be.ppareit.nanopond.core

import org.junit.Assert.assertEquals
import org.junit.Test

class NanoPondReportTest {

    @Test
    fun getReportAggregatesOnlyActiveCells() {
        val nanoPond = NanoPond()
        nanoPond.pond[0][0].energy = 10
        nanoPond.pond[0][0].generation = 1
        nanoPond.pond[1][0].energy = 20
        nanoPond.pond[1][0].generation = 3
        nanoPond.pond[2][0].energy = 0
        nanoPond.pond[2][0].generation = 9

        val report = nanoPond.report

        assertEquals(-1, report.year)
        assertEquals(30, report.energy)
        assertEquals(2, report.activeCells)
        assertEquals(1, report.viableReplicators)
        assertEquals(3, report.maxGeneration)
    }

    @Test
    fun getReportResetsPerReportCounters() {
        val nanoPond = NanoPond()
        nanoPond.statCounters.viableCellsKilled = 2
        nanoPond.statCounters.viableCellsReplaced = 3
        nanoPond.statCounters.viableCellShares = 4

        val firstReport = nanoPond.report
        val secondReport = nanoPond.report

        assertEquals(2, firstReport.kills)
        assertEquals(3, firstReport.replaced)
        assertEquals(4, firstReport.shares)
        assertEquals(0, secondReport.kills)
        assertEquals(0, secondReport.replaced)
        assertEquals(0, secondReport.shares)
    }
}
