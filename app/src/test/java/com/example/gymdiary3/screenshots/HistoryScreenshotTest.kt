package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.presentation.history.HistoryStateBuilder
import com.example.gymdiary3.presentation.history.SummaryStateBuilder
import com.example.gymdiary3.screens.HistoryActions
import com.example.gymdiary3.screens.HistoryScreen
import com.example.gymdiary3.screens.SummaryActions
import com.example.gymdiary3.screens.SummaryScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE, application = Application::class)
class HistoryScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun history() = compose.snap("S05_history") {
        HistoryScreen(HistoryStateBuilder.build(SampleData.sessions, null, "kg"), HistoryActions())
    }

    @Test
    fun historyEmpty() = compose.snap("S05_history_empty") {
        HistoryScreen(HistoryStateBuilder.build(emptyList(), null, "kg"), HistoryActions())
    }

    @Test
    @Config(qualifiers = PHONE_TALL)
    fun summary() = compose.snap("S06_summary") {
        val latest = SampleData.sessions.first()
        SummaryScreen(SummaryStateBuilder.build(latest, SampleData.sessions, "kg"), SummaryActions())
    }
}
