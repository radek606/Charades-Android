package com.ick.kalambury.prompt

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import io.reactivex.rxjava3.core.Single

interface Prompt {
    fun isEligible(): Single<Boolean>
    fun preparePrompt(context: Context): Single<Boolean>
    fun launchPrompt(activity: FragmentActivity, navController: NavController)
}