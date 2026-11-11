package com.example.practice10coroutines

import kotlinx.coroutines.runBlocking
import org.junit.Test

class CoroutinesLabTest {

    private val lab = CoroutinesLab()

    @Test
    fun testTask1() {
        lab.task1()
    }

    @Test
    fun testTask2() = runBlocking {
        lab.task2(3)
        lab.task2(2)
    }

    @Test
    fun testTask3() = runBlocking {
        lab.task3()
    }

    @Test
    fun testTask4() = runBlocking {
        lab.task4()
    }

    @Test
    fun testTask5() = runBlocking {
        lab.task5()
    }
}
