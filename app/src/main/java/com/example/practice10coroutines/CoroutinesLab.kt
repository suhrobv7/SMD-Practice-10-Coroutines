package com.example.practice10coroutines

import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis

@OptIn(DelicateCoroutinesApi::class)
class CoroutinesLab {

    // Task 1: First coroutine with runBlocking, launch and delay
    fun task1() = runBlocking {
        println("Task 1 Start")
        val jobA = launch {
            delay(200)
            println("A done")
        }
        val jobB = launch {
            delay(100)
            println("B done")
        }
        println("Start")
        jobA.join()
        jobB.join()
    }

    // Task 2: suspend function with n tries
    suspend fun fetchName(tryCount: Int): String? {
        delay(100)
        return if (tryCount >= 3) "Ada" else null
    }

    suspend fun task2(n: Int = 3) {
        var success = false
        for (k in 1..n) {
            val name = fetchName(k)
            if (name != null) {
                println("User: $name")
                success = true
                break
            } else {
                println("try $k failed")
            }
        }
        if (!success) {
            println("Server is down, show cached user")
        }
    }

    // Task 3: Control coroutine with Job
    suspend fun task3() = coroutineScope {
        val job = launch {
            try {
                for (i in 0..4) {
                    println("tick $i")
                    delay(100)
                }
            } finally {
                println("cleanup")
            }
        }
        delay(250)
        job.cancelAndJoin()
        println("isCancelled=${job.isCancelled}")
    }

    // Task 4: Load name and age with async
    suspend fun task4() = coroutineScope {
        // Parallel (expected ~100ms)
        val timeParallel = measureTimeMillis {
            val nameDeferred = async { delay(100); "Ada" }
            val ageDeferred = async { delay(100); 25 }
            val name = nameDeferred.await()
            val age = ageDeferred.await()
            println("Loaded $name, $age in parallel")
        }
        println("Parallel time: ${timeParallel}ms")

        // Sequential / awaiting immediately (expected ~200ms)
        val timeSequential = measureTimeMillis {
            val name = async { delay(100); "Ada" }.await()
            val age = async { delay(100); 25 }.await()
            println("Loaded $name, $age sequentially")
        }
        println("Sequential (immediate await) time: ${timeSequential}ms")
    }

    // Task 5: Find thread of each dispatcher
    suspend fun task5() = coroutineScope {
        println("Default/IO dispatchers:")
        launch(Dispatchers.Default) {
            println("Default: ${Thread.currentThread().name}")
        }
        launch(Dispatchers.IO) {
            println("IO: ${Thread.currentThread().name}")
        }

        launch(Dispatchers.Unconfined) {
            println("Unconfined before delay: ${Thread.currentThread().name}")
            delay(10)
            println("Unconfined after delay: ${Thread.currentThread().name}")
        }

        val myThreadContext = newSingleThreadContext("MyThread")
        launch(myThreadContext) {
            println("MyThread before delay: ${Thread.currentThread().name}")
            delay(10)
            println("MyThread after delay: ${Thread.currentThread().name}")
        }
        myThreadContext.close()

        launch {
            println("Inherited main/caller: ${Thread.currentThread().name}")
        }
        delay(200)
    }
}
