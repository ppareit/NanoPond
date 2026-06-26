package be.ppareit.nanopond

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import be.ppareit.nanopond.gui.NanoPondActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NanoPondActivityTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<NanoPondActivity>()

    @Test
    fun activityLaunchesWithExpectedPanels() {
        waitForAppContent()
        composeRule.onNodeWithText("NanoPond").assertIsDisplayed()
        composeRule.onNodeWithText("Report").assertIsDisplayed()
        composeRule.onNodeWithText("Detail").assertIsDisplayed()
    }

    @Test
    fun playPauseAndEditActionsAreClickable() {
        waitForAppContent()
        composeRule.onNodeWithContentDescription("Pause").performClick()
        composeRule.onNodeWithContentDescription("Run").performClick()
        composeRule.onNodeWithContentDescription("Edit").performClick()
    }

    @Test
    fun overflowMenuTogglesReportAndDetailPanels() {
        waitForAppContent()
        composeRule.onNodeWithText("...").performClick()
        composeRule.onNodeWithText("Hide Report").performClick()
        composeRule.onNodeWithText("Report").assertDoesNotExist()

        composeRule.onNodeWithText("...").performClick()
        composeRule.onNodeWithText("Hide Detail").performClick()
        composeRule.onNodeWithText("Detail").assertDoesNotExist()

        composeRule.onNodeWithText("...").performClick()
        composeRule.onNodeWithText("Show Report").performClick()
        composeRule.onNodeWithText("Report").assertIsDisplayed()

        composeRule.onNodeWithText("...").performClick()
        composeRule.onNodeWithText("Show Detail").performClick()
        composeRule.onNodeWithText("Detail").assertIsDisplayed()
    }

    private fun waitForAppContent() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("NanoPond").fetchSemanticsNodes().isNotEmpty()
            } catch (_: IllegalStateException) {
                false
            }
        }
    }
}
