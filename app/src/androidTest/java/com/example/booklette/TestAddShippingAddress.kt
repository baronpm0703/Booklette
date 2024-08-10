package com.example.booklette

import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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


@RunWith(AndroidJUnit4::class)
class TestAddShippingAddress {

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
    fun testClickOnViewFirstBookDetailsInBestSeller() {
        testLogin()
        assertTrue(solo.waitForView(R.id.ivHomeProfile))
        solo.clickOnView(solo.getView(R.id.ivHomeProfile))
        assertTrue(solo.waitForView(R.id.settingShippingAddress))
        solo.clickOnView(solo.getView(R.id.settingShippingAddress))

        solo.sleep(2000)
        solo.clickOnView(solo.getView(R.id.addShipAddressBtn))

        solo.sleep(2000)
        solo.enterText(solo.getView(R.id.addRecieverName) as EditText, "Phan Thai Khang")
        solo.enterText(solo.getView(R.id.addRecieverPhone) as EditText, "0913796636")
        solo.enterText(solo.getView(R.id.addProvince) as EditText, "An Giang")
        solo.enterText(solo.getView(R.id.addCity) as EditText, "Long Xuyen")
        solo.enterText(solo.getView(R.id.addWard) as EditText, "Binh Duc")
        solo.enterText(solo.getView(R.id.addAddressNumber) as EditText, "123 Binh Duc 5")
        solo.clickOnView(solo.getView(R.id.btnAddNewShipAddress))

        solo.sleep(2000)
        val displayMetrics = solo.currentActivity.resources.displayMetrics
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        solo.drag(width / 2f, width / 2f, height / 4f, height / 2f, 10)

        solo.sleep(2000)
        val recyclerView = solo.getView(R.id.rvShipAddress) as RecyclerView
        val firstItemViewHolder = recyclerView.findViewHolderForAdapterPosition(0)
        val firstItemView = firstItemViewHolder?.itemView
        val recieveName = firstItemView?.findViewById<TextView>(R.id.buyerName)
        assertNotNull("Reciever Name TextView in first item is null", recieveName)
        assertEquals("Phan Thai Khang", recieveName?.text.toString())
        val addressTextView = firstItemView?.findViewById<TextView>(R.id.addressDetail)
        assertNotNull("Address TextView in first item is null", addressTextView)
        assertEquals("123 Binh Duc 5", addressTextView?.text.toString())
    }
}