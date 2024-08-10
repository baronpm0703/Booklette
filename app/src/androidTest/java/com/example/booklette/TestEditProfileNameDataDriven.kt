package com.example.booklette;
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.booklette.LoginActivity
import com.robotium.solo.Solo
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.BufferedReader
import java.io.InputStreamReader

@RunWith(Parameterized::class)
class TestEditProfileNameDataDriven(
    private val profileName: String,
    private val profileAddress: String,
) {

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

    @After
    @Throws(Exception::class)
    fun tearDown() {
        solo.finishOpenedActivities()
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
        assertTrue(solo.waitForView(R.id.ivHomeProfile))
        solo.clickOnView(solo.getView(R.id.ivHomeProfile))
        assertTrue(solo.waitForView(R.id.settingEditProfile))
        solo.clickOnView(solo.getView(R.id.settingEditProfile))
        solo.clickOnView(solo.getView(R.id.editProfileBtn))

        val nameEditText = solo.getView(R.id.nameETD) as EditText
        solo.clearEditText(nameEditText)
        solo.enterText(nameEditText, profileName)

        val addressEditText = solo.getView(R.id.addressETD) as EditText
        solo.clearEditText(addressEditText)
        solo.enterText(addressEditText, profileAddress)

        solo.clickOnView(solo.getView(R.id.saveChangesBtn))
        solo.sleep(2000)

        val updatedNameEditText = solo.getView(R.id.nameET) as EditText
        assertEquals(profileName, updatedNameEditText?.text.toString())
        val updatedAddress = solo.getView(R.id.addressET) as EditText
        assertEquals(profileAddress, updatedAddress?.text.toString())
    }

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            val testData = mutableListOf<Array<Any>>()
            val inputStream = InstrumentationRegistry.getInstrumentation().context.assets.open("dataDrivenEditProfile.csv")
                ?: throw IllegalArgumentException("dataDrivenEditProfile.csv.csv not found")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.use {
                it.readLine() // Skip header
                it.forEachLine { line ->
                    val values = line.split(",")
                    testData.add(values.toTypedArray())
                }
            }
            return testData
        }
    }
}