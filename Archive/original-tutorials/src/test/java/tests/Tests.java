package tests;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import oracle0_DefiningAndStartingAThread.HelloRunnable;
import oracle0_DefiningAndStartingAThread.HelloThread;
import oracle1_PausingExecutionWithSleep.SleepMessages;
import oracle3_SimpleThreadsExample.SimpleThreads;
import oracle4_MemoryConsistencyErrors.ConsistencyErrors;
import oracle6_Deadlock.DeadLock;
import oracle7_GuardedBlocks.ProducerConsumerExample;
import oracle8_LockObjects.Safelock;
import oracle9_ForkJoin.Fibonacci;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import winterbe0_ThreadsAndRunnables.ThreadSleepExample;
import winterbe0_ThreadsAndRunnables.ThreadsAndRunnables;
import winterbe1_Executors.ExecutorShutdown;
import winterbe1_Executors.SingleThreadExecutor;
import winterbe2_CallablesAndFutures.CallableExample;
import winterbe2_CallablesAndFutures.FutureTimeoutExample;
import winterbe2_CallablesAndFutures.InvokeAllExample;
import winterbe2_CallablesAndFutures.InvokeAnyExample;
import winterbe3_ScheduledExecutors.PeriodicExecution;
import winterbe3_ScheduledExecutors.ScheduleExecutorExample;
import winterbe4_Synchronized.ConcurrentUtils;
import winterbe4_Synchronized.SynchronizedExample;
import winterbe5_Locks.*;
import winterbe6_Semaphores.SemaphoresExample;
import winterbe7_AtomicVariables.AtomicIntegerExample;
import winterbe8_ConcurrentMaps.ConcurrentMapExample;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

/**
 * Created: 20.10.16 creation date
 */
@RunWith(JUnitParamsRunner.class)
public class Tests
{
    private long startTimer;


    @Before
    public void startTimer()
    {
        startTimer = System.currentTimeMillis();
    }

    private void printTimer(String message)
    {
        long delta = System.currentTimeMillis() - startTimer;
        ConcurrentUtils.message(message + " " + delta + " milliseconds");
    }

    @Test
    public void testHelloRunnable()
    {
        HelloRunnable helloRunnable = new HelloRunnable();
        helloRunnable.main();
        printTimer("HelloRunnable");
    }

    @Test
    public void testHelloThread()
    {
        HelloThread helloThread = new HelloThread();
        helloThread.main();
        printTimer("HelloThread");
    }

    @Test
    public void testSleepMessages()
    {
        SleepMessages sleepMessages = new SleepMessages();
        sleepMessages.main();
        printTimer("SleepMessages");
    }

    @Test
    @Parameters({"60", "5"})
    public void testSimpleThreads(String patience) throws InterruptedException
    {
        SimpleThreads simpleThreads = new SimpleThreads();
        simpleThreads.main(patience);
        printTimer("SimpleThreads");
    }

    @Test
    public void testConsistencyErrors()
    {
        ConsistencyErrors consistencyErrors = new ConsistencyErrors();
        consistencyErrors.main();
        printTimer("ConsistencyErrors");
    }

    @Test
    @Ignore // deadlock!
    public void testDeadLock()
    {
        DeadLock deadLock = new DeadLock();
        deadLock.main();
        printTimer("DeadLock");
    }

    @Test
    public void testProducerConsumerExample()
    {
        ProducerConsumerExample producerConsumerExample = new ProducerConsumerExample();
        producerConsumerExample.main();
        printTimer("ProducerConsumerExample");
    }

    @Test @Ignore
    public void testSafelock()
    {
        Safelock safelock = new Safelock();
        safelock.main();
        printTimer("Safelock");
    }

    @Test
    @Parameters(
            {
                    "4, 3",
                    "5, 5",
                    "35, 9227465",
                    "45, 1134903170"
            }
    )
    public void testFibonacci(Long input, Long expected) throws InterruptedException
    {
        long[] result = new long[1];
        Fibonacci fibonacci = new Fibonacci(input, result);
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(fibonacci);
        assertEquals(expected, Optional.ofNullable(fibonacci.getResult()).orElse(-1L));
        String message = "Fibonacci: " + input;
        printTimer(message);
    }

    @Test
    public void testThreadsAndRunnables()
    {
        ThreadsAndRunnables threadsAndRunnables = new ThreadsAndRunnables();
        threadsAndRunnables.main();
        printTimer("ThreadsAndRunnables");
    }

    @Test
    public void testThreadSleepExample()
    {
        ThreadSleepExample threadSleepExample = new ThreadSleepExample();
        threadSleepExample.main();
        printTimer("ThreadSleepExample");
    }

    @Test
    public void testExecutorShutdown()
    {
        ExecutorShutdown executorShutdown = new ExecutorShutdown();
        executorShutdown.main();
        printTimer("ExecutorShutdown");
    }

    @Test
    public void testSingleThreadExecutor()
    {
        SingleThreadExecutor singleThreadExecutor = new SingleThreadExecutor();
        singleThreadExecutor.main();
        printTimer("SingleThreadExecutor");
    }

    @Test
    public void testCallableExample() throws ExecutionException, InterruptedException
    {
        CallableExample callableExample = new CallableExample();
        callableExample.main();
        printTimer("CallableExample");
    }

    @Test(expected = TimeoutException.class)
    public void testFutureTimeoutExample() throws InterruptedException, ExecutionException, TimeoutException
    {
        FutureTimeoutExample futureTimeoutExample = new FutureTimeoutExample();
        futureTimeoutExample.main();
        printTimer("FutureTimeoutExample");
    }

    @Test
    public void testInvokeAllExample() throws InterruptedException
    {
        InvokeAllExample invokeAllExample = new InvokeAllExample();
        invokeAllExample.main();
        printTimer("InvokeAllExample");
    }

    @Test
    public void testInvokeAnyExample() throws ExecutionException, InterruptedException
    {
        InvokeAnyExample invokeAnyExample = new InvokeAnyExample();
        invokeAnyExample.main();
        printTimer("InvokeAnyExample");
    }

    @Test
    public void testPeriodicExecution() throws InterruptedException
    {
        PeriodicExecution periodicExecution = new PeriodicExecution();
        periodicExecution.main();
        printTimer("PeriodicExecution");
    }

    @Test
    public void testScheduleExecutorExample() throws InterruptedException
    {
        ScheduleExecutorExample scheduleExecutorExample = new ScheduleExecutorExample();
        scheduleExecutorExample.main();
        printTimer("ScheduleExecutorExample");
    }

    @Test
    public void testSynchronizedExample() throws InterruptedException
    {
        SynchronizedExample synchronizedExample = new SynchronizedExample();
        synchronizedExample.main();
        printTimer("SynchronizedExample");
    }

    @Test
    public void testConvertToWriteLockExample() throws InterruptedException
    {
        ConvertToWriteLockExample convertToWriteLockExample = new ConvertToWriteLockExample();
        convertToWriteLockExample.main();
        printTimer("ConvertToWriteLockExample");
    }

    @Test
    public void testOptimisticReadExample()
    {
        OptimisticReadExample optimisticReadExample = new OptimisticReadExample();
        optimisticReadExample.main();
        printTimer("OptimisticReadExample");
    }

    @Test
    public void testReadWriteLockExample()
    {
        ReadWriteLockExample readWriteLockExample = new ReadWriteLockExample();
        readWriteLockExample.main();
        printTimer("ReadWriteLockExample");
    }

    @Test
    public void testReentrantLockExample() throws InterruptedException
    {
        ReentrantLockExample reentrantLockExample = new ReentrantLockExample();
        reentrantLockExample.main();
        printTimer("ReentrantLockExample");
    }

    @Test
    public void testStampedLockExample()
    {
        StampedLockExample stampedLockExample = new StampedLockExample();
        stampedLockExample.main();
        printTimer("StampedLockExample");
    }

    @Test
    public void testSemaphoresExample()
    {
        SemaphoresExample semaphoresExample = new SemaphoresExample();
        semaphoresExample.main();
        printTimer("SemaphoresExample");
    }

    @Test
    public void testAtomicIntegerExample()
    {
        AtomicIntegerExample atomicIntegerExample = new AtomicIntegerExample();
        atomicIntegerExample.main();
        printTimer("AtomicIntegerExample");
    }

    @Test
    public void testConcurrentMapExample()
    {
        ConcurrentMapExample concurrentMapExample = new ConcurrentMapExample();
        concurrentMapExample.main();
        printTimer("ConcurrentMapExample");
    }

}