package com.example.booklette

import android.widget.EditText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.robotium.solo.Solo
import junit.framework.Assert
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TestEditProfile {

    private lateinit var solo: Solo

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        activityScenarioRule.scenario.onActivity { activity ->
            solo = Solo(InstrumentationRegistry.getInstrumentation(), activity)
        }
    }

    fun testLogin() {
        solo.assertCurrentActivity("Expected LoginActivity", LoginActivity::class.java)
        assertTrue(solo.searchText("Welcome,"))
        solo.enterText(solo.getView(R.id.edtEmailSignIn) as EditText, "ptkhang.work@gmail.com")
        solo.enterText(solo.getView(R.id.edtPasswordSignIn) as EditText, "Khangthai123@@")
        solo.clickOnView(solo.getView(R.id.btnLogIn))
    }

    @Test
    fun testEditProfile() {
        testLogin()
        solo.sleep(2000)
        assertTrue(solo.waitForView(R.id.ivHomeProfile))
        solo.clickOnView(solo.getView(R.id.ivHomeProfile))
        assertTrue(solo.waitForView(R.id.settingEditProfile))
        solo.clickOnView(solo.getView(R.id.settingEditProfile))
        solo.clickOnView(solo.getView(R.id.editProfileBtn))
        val nameEditText = solo.getView(R.id.nameETD) as EditText
        solo.clearEditText(nameEditText)
        solo.enterText(nameEditText, "Phan Thai Khang Edit")
        solo.clickOnView(solo.getView(R.id.saveChangesBtn))
        solo.sleep(2000)
        val updatedNameEditText = solo.getView(R.id.nameET) as EditText
        Assert.assertEquals("Phan Thai Khang Edit", updatedNameEditText.text.toString())
    }


    @After
    @Throws(Exception::class)
    fun tearDown() {
        solo.finishOpenedActivities()
    }
}
