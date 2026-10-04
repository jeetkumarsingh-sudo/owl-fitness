package com.example.gymdiary3.screenshots

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gymdiary3.presentation.home.HomeStateBuilder
import com.example.gymdiary3.screens.HomeActions
import com.example.gymdiary3.screens.HomeScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = PHONE, application = Application::class)
class HomeScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun home() = compose.snap("S01_home") {
        val state = HomeStateBuilder.build(SampleData.sessions, null, emptyList(), "kg", SampleData.now)
        HomeScreen(state, elapsedSeconds = 0, actions = HomeActions())
    }

    @Test
    fun homeActiveWorkout() = compose.snap("S01_home_active") {
        val state = HomeStateBuilder.build(
            SampleData.sessionsWithActive, SampleData.ACTIVE_SESSION_ID, emptyList(), "kg", SampleData.now
        )
        HomeScreen(state, elapsedSeconds = 32 * 60 + 8, actions = HomeActions())
    }

    @Test
    fun homeNewUser() = compose.snap("S01_home_empty") {
        val state = HomeStateBuilder.build(emptyList(), null, emptyList(), "kg", SampleData.now)
        HomeScreen(state, elapsedSeconds = 0, actions = HomeActions())
    }
}
