package org.thoughtcrime.securesms

import androidx.annotation.VisibleForTesting

import kotlinx.coroutines.launch

object ObjBeingMocked {

    private var result = 0
    private var prefix = ""


    fun add(a: Int, b: Int): Int {
        result = a + b
        return result
    }

    fun hi(): String {
        return eat("hi")
    }

    fun changePrefix(name: String) {
        this.prefix = name
    }

    private fun eat(name: String): String {
        return "eat: $name, result: $result, prefix: $prefix"
    }
}

class Engine {

    private var speed = 0
    var name = ""

    fun setSpeed(speed: Int) {
        this.speed = speed
    }

    fun getSpeed(): Int {
        return calSpeed(speed)
    }

    private fun calSpeed(base: Int): Int {
        return 30 + base
    }
}

class Bus() {
    fun drive(direction: Direction): Outcome {
        return Outcome.NO
    }
}


fun buildCar(): Car {
    val engine = Engine()
    engine.setSpeed(100)
    return Car(engine)
}

class Car(private val engine: Engine) {

    fun accelerate(fromSpeed: Int, toSpeed: Int) {

    }

    fun recordTelemetry(speed: Int, direction: Direction, lat: Double, long: Double): Outcome {
        return Outcome.NO
    }

    fun drive(direction: Direction): Outcome {
        return Outcome.NO
    }

    fun setSpeed(s: Int) {
        engine.setSpeed(s)
    }

    fun getSpeed(): Int {
        return engine.getSpeed()
    }

    fun sayHi() {
        println("sayHi>>>hello world!")
    }
}

enum class Direction {
    NORTH,
    SOUTH,
    WEST,
    EAST
}

enum class Outcome {
    OK,
    NO
}

class Adder {
    fun addOne(num: Int) = num + 1
}


enum class Enumeration(val goodInt: Int) {
    CONSTANT(35),
    OTHER_CONSTANT(45);
}

class MockCls(private val a: Int = 0) {
    constructor(x: String) : this(x.toInt())

    fun add(b: Int) = a + b
}

interface AddressBook {
    val contacts: List<Contact>
}

interface Contact {
    val name: String
    val telephone: String
    val address: Address
}

interface Address {
    val city: String
    val zip: String
}

class MockedClass {
    fun sum(a: Int, b: Int) = a + b

    fun add(a: Int, b: Int) {
        println(a + b)
    }
}

data class Obj(val value: Int)

class Ext {
    fun Obj.extensionFunc() = value + 5
}

fun Obj.extensionFunc2() = value + 7

interface ClsWithManyMany {
    fun manyMany(vararg x: Any): Int
}