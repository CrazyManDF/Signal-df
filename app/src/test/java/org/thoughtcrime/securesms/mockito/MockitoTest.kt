package org.thoughtcrime.securesms.mockito

import android.provider.Telephony.Mms.Addr
import android.util.Log
import io.mockk.coJustAwait
import io.mockk.Called
import io.mockk.EqMatcher
import io.mockk.InternalPlatformDsl
import io.mockk.MockKAnnotations
import io.mockk.OfTypeMatcher
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.mockkConstructor
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import io.mockk.verifySequence
import junit.framework.TestCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.thoughtcrime.securesms.Adder
import org.thoughtcrime.securesms.AddressBook
import org.thoughtcrime.securesms.Bus
import org.thoughtcrime.securesms.Car
import org.thoughtcrime.securesms.Direction
import org.thoughtcrime.securesms.Engine
import org.thoughtcrime.securesms.Enumeration
import org.thoughtcrime.securesms.MockCls
import org.thoughtcrime.securesms.MockedClass
import org.thoughtcrime.securesms.ObjBeingMocked
import org.thoughtcrime.securesms.Outcome
import kotlin.concurrent.thread
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.thoughtcrime.securesms.ClsWithManyMany
import org.thoughtcrime.securesms.Ext
import org.thoughtcrime.securesms.Obj
import org.thoughtcrime.securesms.buildCar
import org.thoughtcrime.securesms.extensionFunc2
import java.io.PrintStream

class MockStaticDemo {

    @get:Rule
    val mockkRule = MockKRule(this)


    @MockK // 通过注解mock
    lateinit var car1: Car

    @RelaxedMockK // 通过注解mock，并设置relaxed
    lateinit var car2: Car

    @MockK(relaxUnitFun = true) // 通过注解mock，并设置relaxed
    lateinit var car3: Car

    @Before
    fun setup() {
//        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    @Test
    fun testDriver1() {
        every { car1.drive(Direction.NORTH) } returns Outcome.OK
        every { car2.getSpeed() } answers {
            println("+++++")
            200
        }
        every { car1.sayHi() } just Runs

        assertEquals(Outcome.OK, car1.drive(Direction.NORTH))

        car1.sayHi()

        assertEquals(200, car2.getSpeed())
    }

    @Test
    fun testDriver21() {
        val bus = mockk<Bus>(relaxed = true)
        val result = bus.drive(Direction.NORTH)
        verify { bus.drive(Direction.NORTH) }

        val func = mockk<() -> Bus>(relaxed = true)
        every { func() } returns Bus()
        println(func())
    }

    @Test
    fun testDriver2() {
        val engine = spyk<Engine>(recordPrivateCalls = true)
        val car = Car(engine)

//        every { car.getSpeed() } returns 200
        every { engine["calSpeed"](0) } returns 40

        val speed = car.getSpeed()
        assertEquals(40, speed)

        verifySequence {
            car.getSpeed()
            engine["calSpeed"](0) // 要设置recordPrivateCalls = true
        }
    }

    @Test
    fun testDriver3() {
//        val engine = mockk<Engine>(relaxed = true)
        val engine = spyk<Engine>()
        val car = Car(engine)

        val mySlot = slot<Int>()

        every { engine.setSpeed(capture(mySlot)) } just Runs

        car.setSpeed(8)
        assertEquals(9, mySlot.captured)
    }

    @Test
    fun testDriver4() {
        mockkObject(ObjBeingMocked, recordPrivateCalls = false)

        assertEquals(3, ObjBeingMocked.add(1, 2))

        every { ObjBeingMocked.add(3, 2) } returns 55

        assertEquals(55, ObjBeingMocked.add(3, 2))

        // 私有方法
        every { ObjBeingMocked["eat"]("hi") } returns "hi jack"

        assertEquals("hi jack", ObjBeingMocked.hi())
    }

    @Test
    fun testDriver5() {
        mockkObject(ObjBeingMocked, recordPrivateCalls = false)
        // 给mock对象设置私有属性
        InternalPlatformDsl.dynamicSetField(ObjBeingMocked, "result", 70)

        InternalPlatformDsl.dynamicCall(ObjBeingMocked, "changePrefix", arrayOf("xx"), mockk())

        println(ObjBeingMocked.hi())
    }

    @Test
    fun testDriver6() {
        val adder = mockk<Adder>()

        every { adder.addOne(any()) } returns -1
        every { adder.addOne(3) } answers { callOriginal() }

        assertEquals(-1, adder.addOne(1))
        assertEquals(4, adder.addOne(3))
    }

    @Test
    fun testDriver7() {
        val car = mockkClass(Car::class)
        every { car.drive(Direction.NORTH) } returns Outcome.OK
        car.drive(Direction.NORTH) // returns OK
        println(car.drive(Direction.NORTH))
        verify { car.drive(Direction.NORTH) }
    }

    @Test
    fun testDriver8() {
        mockkObject(Enumeration.CONSTANT)
        every { Enumeration.CONSTANT.goodInt } returns 42
        assertEquals(42, Enumeration.CONSTANT.goodInt)
    }

    @Test
    fun testDriver9() {

        mockkConstructor(MockCls::class)

//        every { anyConstructed<MockCls>().add(1) } returns 2

        every { constructedWith<MockCls>(OfTypeMatcher<String>(String::class)).add(2) } returns 3

        every { constructedWith<MockCls>(EqMatcher(4)).add(any()) } returns 4

//        assertEquals(2, MockCls().add(1))
        assertEquals(3, MockCls("2").add(2))
        assertEquals(4, MockCls(4).add(7))
//
//        assertEquals(4, MockCls().add(1, 2))
//
//        verify { anyConstructed<MockCls>().add(1, 2) }
    }

    @Test
    fun testDriver10() {
        val car = mockk<Car>()

        every {
            car.recordTelemetry(
                speed = less(50), direction = Direction.NORTH, lat = any(), long = any()
            )
        } returns Outcome.NO

        every {
            car.recordTelemetry(
                speed = more(50), direction = Direction.NORTH, lat = any(), long = any()
            )
        } returns Outcome.OK

        val result = car.recordTelemetry(20, Direction.NORTH, 51.1377382, 17.0257142)

        assertEquals(Outcome.OK, result)

    }

    @Test
    fun testDriver11() {
        val addressBook = mockk<AddressBook> {
            every { contacts } returns listOf(mockk {
                every { name } returns "John"
                every { telephone } returns "123-456-789"
                every { address.city } returns "New-York"
                every { address.zip } returns "123-45"
            }, mockk {
                every { name } returns "Alex"
                every { telephone } returns "789-456-123"
                every { address } returns mockk {
                    every { city } returns "Wroclaw"
                    every { zip } returns "543-21"
                }
            })
        }

        println(addressBook.contacts[1].name)
        println(addressBook.contacts[1].telephone)
        println(addressBook.contacts[1].address.city)
    }

    @Test
    fun testDriver12() {
        val car = mockk<Car>(relaxed = true)

        car.accelerate(fromSpeed = 10, toSpeed = 20)
        car.accelerate(fromSpeed = 10, toSpeed = 30)
        car.accelerate(fromSpeed = 20, toSpeed = 30)

        verify(atLeast = 3) { car.accelerate(allAny(), allAny()) }
        verify(atMost = 2) { car.accelerate(fromSpeed = 10, toSpeed = or(20, 30)) }
        verify(exactly = 1) { car.accelerate(fromSpeed = 10, toSpeed = 20) }
        verify(exactly = 0) { car.accelerate(fromSpeed = 30, toSpeed = 10) }

//        verifyCount {
//            (3..5) * { car.accelerate(allAny(), allAny()) } // same as verify(atLeast = 3, atMost = 5) { car.accelerate(allAny(), allAny()) }
//            1 * { car.accelerate(fromSpeed = 10, toSpeed = 20) } // same as verify(exactly = 1) { car.accelerate(fromSpeed = 10, toSpeed = 20) }
//            0 * { car.accelerate(fromSpeed = 30, toSpeed = 10) } // same as verify(exactly = 0) { car.accelerate(fromSpeed = 30, toSpeed = 10) }
//        }

    }

    @Test
    fun testDriver13() {
        val obj = mockk<MockedClass>()
        val slot = slot<Int>()

        every { obj.sum(any(), capture(slot)) } answers { 1 + firstArg<Int>() + slot.captured }
        obj.sum(1, 2) // returns 4
        obj.sum(1, 3) // returns 5
        obj.sum(2, 2) // returns 5

        verifyAll {
            obj.sum(1, 3)
            obj.sum(1, 2)
            obj.sum(2, 2)
        }

        verifySequence {
            obj.sum(1, 2)
            obj.sum(1, 3)
            obj.sum(2, 2)
        }

        verifyOrder {
            obj.sum(1, 2)
//            obj.sum(1, 3)
            obj.sum(2, 2)
        }

        val obj2 = mockk<MockedClass>()
        val obj3 = mockk<MockedClass>()
        verify {
            listOf(obj2, obj3) wasNot Called
        }
    }

    @Test
    fun testDriver14() {
        mockk<Adder>() {
            every { addOne(2) } returns 4

            thread {
                Thread.sleep(2000L)
                addOne(2)
            }.start()

            verify(timeout = 3000L) { addOne(2) }
        }
    }

    @Test
    fun testDriver15() {
        val obj = mockk<MockedClass>()
        justRun { obj.add(any(), 3) }
        every { obj.add(any(), 3) } returns Unit
        every { obj.add(any(), 2) } just Runs
        every { obj.add(any(), 1) } answers { Unit }
        obj.add(1, 1)
        obj.add(1, 2)
        obj.add(1, 3)

        verify {
            obj.add(1, 1)
            obj.add(1, 2)
            obj.add(1, 3)
        }
    }


    @Test
    fun testDriver16() {
        val car = mockk<Car>()
        coEvery { car.drive(Direction.NORTH) } returns Outcome.OK

        car.drive(Direction.NORTH)
        coVerify { car.drive(Direction.NORTH) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testDriver17() = runTest {

        val car = mockk<Car>()

        coJustAwait { car.drive(any()) }

        val job = launch(UnconfinedTestDispatcher()) {
            car.drive(Direction.NORTH)
        }

        coVerify { car.drive(Direction.NORTH) }

        job.cancelAndJoin()
    }

    @Test
    fun testDriver18() {
        val testCar = Car(engine = Engine())

        mockkStatic(::buildCar)
        every { buildCar() } returns testCar

        assertEquals(testCar, buildCar())
        verify { buildCar() }
    }

    @Test
    fun testDriver19() {
        mockk<Ext>() {
            every { Obj(5).extensionFunc() } returns 11

            assertEquals(11, Obj(5).extensionFunc())

            verify {
                Obj(5).extensionFunc()
            }
        }
    }

    @Test
    fun testDriver20() {
        mockkStatic("org.thoughtcrime.securesms.StaticDemoKt")
        every { Obj(5).extensionFunc2() } returns 11

        assertEquals(11, Obj(5).extensionFunc2())

        verify {
            Obj(5).extensionFunc2()
        }
    }

    @Test
    fun testDriver22() {
        val obj = mockk<ClsWithManyMany>()

        every { obj.manyMany(5, 6, *varargAll { it == 7 }) } returns 3
        println(obj.manyMany(5, 6, 7)) // 3
        println(obj.manyMany(5, 6, 7, 7)) // 3
        println(obj.manyMany(5, 6, 7, 7, 7)) // 3

        every { obj.manyMany(5, 6, *anyVararg(), 7) } returns 4
        println(obj.manyMany(5, 6, 1, 7)) // 4
        println(obj.manyMany(5, 6, 2, 3, 7)) // 4
        println(obj.manyMany(5, 6, 4, 5, 6, 7)) // 4

        every { obj.manyMany(5, 6, *varargAny { nArgs > 5 }, 7) } returns 5
        println(obj.manyMany(5, 6, 4, 5, 6, 7)) // 5
        println(obj.manyMany(5, 6, 4, 5, 6, 7, 7)) // 5

        every {
            obj.manyMany(5, 6, *varargAny {
                if (position < 3) it == 3 else it == 4
            }, 7)
        } returns 6

        println(obj.manyMany(5, 6, 3, 4, 7)) // 6
        println(obj.manyMany(5, 6, 3, 4, 4, 7)) // 6
    }

    @Test
    fun testDriver23() {
        val mock = spyk(Engine(), recordPrivateCalls = true)

        every { mock getProperty "name" } returns "hhhh"
//        every { mock setProperty "acceleration" value less(5) } just runs
//        justRun { mock invokeNoArgs "privateMethod" }
//        every { mock invoke "openDoor" withArguments listOf("left", "rear") } returns "OK"

        println(mock.name)

        verify { mock getProperty "name" }
//        verify { mock setProperty "acceleration" value less(5) }
//        verify { mock invoke "openDoor" withArguments listOf("left", "rear") }
    }

    @Test
    fun testDriver24() {
        val spy = spyk(System.out, moreInterfaces = arrayOf(Runnable::class))
        spy.println(555)

        every { (spy as Runnable).run() } answers { (self as PrintStream).println("Run! Run! Run!") }
        val thread = Thread(spy as Runnable)
        thread.start()
        thread.join()
    }
}
