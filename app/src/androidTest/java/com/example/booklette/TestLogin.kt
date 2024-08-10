package com.example.booklette

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText

@RunWith(AndroidJUnit4::class)
class TestLogin {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginSuccessed() {
        onView(withId(R.id.textView2)).check(matches(withText("Welcome,")))
        onView(withId(R.id.edtEmailSignIn)).perform(typeText("ptkhang.work@gmail.com"), closeSoftKeyboard())
        onView(withId(R.id.edtPasswordSignIn)).perform(typeText("Khangthai123@@"), closeSoftKeyboard())
        onView(withId(R.id.btnLogIn)).perform(click())
    }
}
