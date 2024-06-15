package org.thoughtcrime.securesms

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.activityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.thoughtcrime.securesms.IdlingResource.SimpleIdlingResource


@RunWith(AndroidJUnit4::class)
class EspressoUnitTest {

    @get:Rule
    val mActivityTestRule = activityScenarioRule<LoginActivity>()

    private var idlingResource: SimpleIdlingResource? = null


    @Before
    fun registerIdlingResource() {
//        val activityScenario = ActivityScenario.launch(LoginActivity::class.java)
//        activityScenario.onActivity { activity ->
//
//        }

        mActivityTestRule.scenario.onActivity { activity ->
            idlingResource = activity.getIdlingResource()
            IdlingRegistry.getInstance().register(idlingResource)
        }
    }

    @After
    fun unRegisterIdlingResource() {
        if (idlingResource != null) {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
    }

    @Test
    fun mainActivityTest() {
        onView(withId(R.id.etName)).perform(typeText("Jack"), closeSoftKeyboard())
        onView(withId(R.id.etPwd)).perform(typeText("1234"), closeSoftKeyboard())
        onView(withText("登录")).perform(click())

//        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
        onView(withId(R.id.btnLogin)).check(matches(withText("1234")))

    }
}