package com.ick.kalambury.prompt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class PromptManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val promptList: ArrayList<Prompt>,
) {

    private var currentPromptIndex: Int = -1

    fun getPrompt(): Maybe<Prompt> {
        return Observable.fromIterable(promptList)
            .subscribeOn(Schedulers.computation())
            .concatMapSingle { it.isEligible() }
            .zipWith(Observable.range(0, promptList.size - 1), ::Pair)
            .filter { it.first == true }
            .take(1)
            .flatMapSingle {
                currentPromptIndex = it.second
                promptList[it.second].preparePrompt(context)
            }
            .map { promptList[currentPromptIndex] }
            .singleElement()
    }

}