package ru.spbstu.lyubchenkova.checkers

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import ru.spbstu.lyubchenkova.checkers.ui.MainActivity

class UITest {

    @get: Rule
    var mActivityRule: ActivityTestRule<MainActivity> = IntentsTestRule(
            MainActivity::class.java,false,true
    )

    @Test
    fun test1() {
        onView(withId(R.id.play_button)).perform(click())
        onView(withText(R.string.yes))
        .inRoot(isDialog()) // <---
                .check(matches(isDisplayed()))
                .perform(click())
        onView(withText("Белые ходят")).check(matches(isDisplayed()))
    }
}