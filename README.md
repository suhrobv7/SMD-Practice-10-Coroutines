# Practice 10: Coroutines

## Part A: Theory Questions (Q1-Q10)

### Q1. Explain difference between `newSingleThreadContext` and `Dispatchers.Unconfined`. Show code example. State on which thread coroutine runs before and after `delay()`, and what you must do with single thread context when finished.
- **`newSingleThreadContext`**: Creates a dedicated thread for the coroutine to run on. It is a heavy resource and must be explicitly closed using `.close()` when no longer needed to prevent thread leaks.
- **`Dispatchers.Unconfined`**: Starts the coroutine in the caller's thread until the first suspension point (e.g., `delay()`). After resumption, it runs on whatever thread the suspending function resumed on.
- **Code Example**:
```kotlin
val singleThread = newSingleThreadContext("MyThread")
launch(singleThread) {
    // Runs on "MyThread"
    delay(100)
    // Still runs on "MyThread"
}
singleThread.close()

launch(Dispatchers.Unconfined) {
    // Runs on caller thread (e.g., main)
    delay(100)
    // Runs on DefaultDispatcher worker thread (resumed by delay)
}
```

---

### Q2. What is difference between `async` and `launch`? Return type, how to get result, `join()` vs `await()`.
- **`launch`**: Returns a `Job`. Used for "fire-and-forget" tasks where no result is returned. Use `job.join()` to wait for completion.
- **`async`**: Returns a `Deferred<T>` (a subclass of `Job`). Used when you need to compute a result asynchronously. Use `deferred.await()` to retrieve the result and suspend until ready.

---

### Q3. What is coroutine? What is difference between `suspend` and `block`? Compare `delay()` and `Thread.sleep()` with two coroutines on one thread.
- **Coroutine**: A lightweight thread that can be suspended and resumed without blocking an actual OS thread.
- **`suspend` vs `block`**: `suspend` pauses execution of the coroutine and frees the underlying thread for other tasks. `block` halts the underlying OS thread entirely, wasting CPU resources.
- **`delay()` vs `Thread.sleep()`**: `delay()` is non-blocking and suspends the coroutine. `Thread.sleep()` blocks the entire thread. If two coroutines run on a single thread using `delay()`, both interleave cooperatively. With `Thread.sleep()`, the first coroutine freezes the shared thread, preventing the second one from running.

---

### Q4. What does `suspend` keyword do? Does it create new thread? Who can call suspend function? What hidden parameter compiler adds?
- It marks a function as suspendable. It **does not** create a new thread.
- It can only be called from another suspend function or inside a coroutine builder.
- The Kotlin compiler adds a hidden parameter of type `Continuation<T>` to manage state machine continuation across suspension points.

---

### Q5. Explain concurrency vs parallelism. Which one coroutines give on one thread, and which one needs several cores?
- **Concurrency**: Dealing with multiple tasks at once by switching between them (interleaving). Coroutines on a single thread provide concurrency.
- **Parallelism**: Executing multiple tasks simultaneously at the exact same instant, requiring multiple CPU cores.

---

### Q6. What is structured concurrency? Describe parent and child tree. What happens when parent finishes, fails or is cancelled?
- Structured concurrency ensures that coroutines are bound to a specific scope and lifecycle.
- **Parent-Child Tree**: Every coroutine started in a scope becomes a child of that scope/parent.
- **Behavior**:
  - When a parent finishes, it waits for all its children to complete.
  - If a child fails with an exception, it cancels its siblings and propagates the failure up to the parent, cancelling the entire tree.
  - If the parent is cancelled, all its children are recursively cancelled.

---

### Q7. Compare builders: `launch`, `async`, `runBlocking`, `withContext`, `coroutineScope`. What each returns and which one blocks the thread? Why never use `runBlocking` on Android UI thread?
- **`launch`**: Returns `Job`, non-blocking.
- **`async`**: Returns `Deferred<T>`, non-blocking.
- **`runBlocking`**: Returns result of lambda, **blocks** the current thread (used mainly in main/tests).
- **`withContext`**: Returns result of lambda, suspends caller thread without blocking.
- **`coroutineScope`**: Returns result of lambda, suspends caller until all children complete.
- **Why not `runBlocking` on UI thread**: It blocks the main thread, causing ANRs (Application Not Responding).

---

### Q8. What is `CoroutineScope` and what is `CoroutineContext`? Scope is when, dispatcher is where. Which dispatcher is used if you specify nothing?
- **`CoroutineScope`**: Defines the lifecycle and scope ("when") of coroutines.
- **`CoroutineContext`**: A set of rules/elements (Dispatcher, Job, ExceptionHandler) defining how coroutines run ("where").
- **Default Dispatcher**: If unspecified, inherits from the parent scope (typically `Dispatchers.Main` in UI scopes).

---

### Q9. When to use `Dispatchers.Default`, `IO` and `Main`? One example of work for each and one thing to avoid on each.
- **`Dispatchers.Main`**: UI updates. Avoid heavy CPU/IO calculations.
- **`Dispatchers.Default`**: CPU-intensive work (sorting large lists, parsing JSON). Avoid blocking IO operations.
- **`Dispatchers.IO`**: Network calls, database queries, file reading. Avoid heavy UI manipulation or pure CPU loops.

---

### Q10. Explain line by line: `viewModelScope.launch { withContext(Dispatchers.IO) { ... } }`. On which thread each part runs? What happens to the coroutine when ViewModel is cleared?
```kotlin
viewModelScope.launch { // 1. Launches coroutine on Main thread (via viewModelScope)
    withContext(Dispatchers.IO) { // 2. Switches execution to IO dispatcher thread pool
        // 3. Executes IO work (network/database) on IO thread
    } // 4. Returns back to Main thread automatically after withContext completes
}
```
- **When ViewModel is cleared**: `viewModelScope` is automatically cancelled, cancelling all running coroutines inside it to prevent memory leaks.

---

## Structured Concurrency Explanation

```kotlin
// Example of Structured Concurrency
val parentJob = Job()
val scope = CoroutineScope(Dispatchers.Main + parentJob)

scope.launch {
    // Child 1
    launch {
        delay(1000)
        println("Child 1 finished")
    }
    // Child 2
    launch {
        delay(500)
        println("Child 2 finished")
    }
}
// Parent waits for both Child 1 and Child 2 before completing.
```

---

## Part B: Coroutines Tasks

Implemented in [CoroutinesLab.kt](file:///C:/Users/suhro/AndroidStudioProjects/Practice10Coroutines/app/src/main/java/com/example/practice10coroutines/CoroutinesLab.kt) and verified via [CoroutinesLabTest.kt](file:///C:/Users/suhro/AndroidStudioProjects/Practice10Coroutines/app/src/test/java/com/example/practice10coroutines/CoroutinesLabTest.kt).
