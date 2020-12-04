package io.moatwel.playground.newthread

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.moatwel.playground.newthread.databinding.ActivityMainBinding
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Singles
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    //    private val workerScheduler = Schedulers.newThread()
    private val workerScheduler = Schedulers.io()

    private val compositeDisposable = CompositeDisposable()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            without.setOnClickListener {
                val now = System.currentTimeMillis()
                Singles.zip(
                    task1(),
                    task2()
                )
                    .subscribeOn(workerScheduler)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = {
                            val diff = System.currentTimeMillis() - now
                            val text = "task1: ${it.first}, task2: ${it.second}, diff: $diff ms"
                            log.text = "${log.text}\n$text"
                        }
                    )
                    .addTo(compositeDisposable)
            }

            with.setOnClickListener {
                val now = System.currentTimeMillis()
                Singles.zip(
                    task1().subscribeOn(workerScheduler),
                    task2().subscribeOn(workerScheduler)
                )
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = {
                            val diff = System.currentTimeMillis() - now
                            val text = "task1: ${it.first}, task2: ${it.second}, diff: $diff ms"
                            log.text = "${log.text}\n$text"
                        }
                    )
                    .addTo(compositeDisposable)
            }
        }
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    private fun task1(): Single<String> {
        return Single.fromCallable {
            Thread.sleep(1000)
            Thread.currentThread().name
        }
    }

    private fun task2(): Single<String> {
        return Single.fromCallable {
            Thread.sleep(3000)
            Thread.currentThread().name
        }
    }
}