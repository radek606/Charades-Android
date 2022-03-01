package com.ick.kalambury.util

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.TestScheduler

interface SchedulerProvider {

    fun io(): Scheduler
    fun computation(): Scheduler
    fun single(): Scheduler
    fun main(): Scheduler

}

object MainSchedulerProvider : SchedulerProvider {

    override fun io(): Scheduler = Schedulers.io()
    override fun computation(): Scheduler = Schedulers.computation()
    override fun single(): Scheduler = Schedulers.single()
    override fun main(): Scheduler = AndroidSchedulers.mainThread()

}

object TrampolineSchedulerProvider : SchedulerProvider {

    override fun io(): Scheduler = Schedulers.trampoline()
    override fun computation(): Scheduler = Schedulers.trampoline()
    override fun single(): Scheduler = Schedulers.trampoline()
    override fun main(): Scheduler = Schedulers.trampoline()

}

class TestSchedulerProvider(private val scheduler: TestScheduler) : SchedulerProvider {

    override fun io(): Scheduler = scheduler
    override fun computation(): Scheduler = scheduler
    override fun single(): Scheduler = scheduler
    override fun main(): Scheduler = scheduler

}