package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.domain.analytics.TimeRange
import com.example.gymdiary3.presentation.body.BodyStateBuilder
import com.example.gymdiary3.screens.BodyActions
import com.example.gymdiary3.screens.BodyScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE_TALL, application = Application::class)
class BodyScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun body() = compose.snap("S09_body") {
        val state = BodyStateBuilder.build(
            SampleData.bodyWeights, SampleData.allSets, SampleData.sessions.map { it.session.startTime }, "kg", SampleData.now
        )
        BodyScreen(state, "kg", SampleData.now, BodyActions(), initialRange = TimeRange.M3)
    }

    @Test
    @Config(qualifiers = PHONE)
    fun bodyEmpty() = compose.snap("S09_body_empty") {
        BodyScreen(BodyStateBuilder.build(emptyList(), emptyList(), emptyList(), "kg", SampleData.now), "kg", SampleData.now, BodyActions())
    }
}
