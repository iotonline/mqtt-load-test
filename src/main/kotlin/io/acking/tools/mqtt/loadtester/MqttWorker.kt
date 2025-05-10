package io.acking.tools.mqtt.loadtester

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.system.exitProcess
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttMessage

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Usage: MqttWorker <broker> <threadCount> <messagesPerThread>")
        exitProcess(1)
    }

    val broker = args[0]
    val threadCount = args[1].toInt()
    val messagesPerThread = args[2].toInt()

    val topic = "test/topic"
    val qos = 0
    val startLatch = CountDownLatch(1)
    val executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().factory())


    val success = AtomicInteger()
    val failure = AtomicInteger()

    val totalTime = AtomicLong(0)
    repeat(threadCount) { threadId ->
        executor.submit {
            val clientId = "client-${Thread.currentThread().id}-${System.nanoTime()}"
            val client = MqttClient(broker, clientId, null)

            startLatch.await()

            val now = System.currentTimeMillis()
            try {
                client.connect(MqttConnectOptions().apply {
                    isCleanSession = true
                    connectionTimeout = 10
                    userName = "yoram"
                    password = "password".toCharArray()
                })

                repeat(messagesPerThread) { i ->
                    val message = MqttMessage("msg #$i from $clientId".toByteArray())
                    message.qos = qos
                    client.publish(topic, message)
                    success.incrementAndGet()
                    totalTime.addAndGet(System.currentTimeMillis() - now)
                }

                client.disconnect()
            } catch (e: Exception) {
                println("❌ $clientId failed: ${e.message}")
                failure.incrementAndGet()
            }
        }
    }

    println("⏳ All clients ready, starting in 3 seconds...")
    Thread.sleep(3000)
    startLatch.countDown() //

    executor.shutdown()
    executor.awaitTermination(10, TimeUnit.MINUTES)

    println("✅ Success: ${success.get()} / avg: ${totalTime.get() / success.get()} ms | ❌ Failed clients: ${failure.get()}")

}