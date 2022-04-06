package com.ick.kalambury

import androidx.fragment.app.strictmode.FragmentStrictMode
import android.os.Bundle
import com.ick.kalambury.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    init {
        if (BuildConfig.DEBUG) {
            supportFragmentManager.strictModePolicy = FragmentStrictMode.Policy.Builder()
                .penaltyLog()
                .detectFragmentReuse()
                .detectFragmentTagUsage()
                .detectRetainInstanceUsage()
                .detectSetUserVisibleHint()
                .detectTargetFragmentUsage()
                .detectWrongFragmentContainer()
                .build()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(ActivityMainBinding.inflate(layoutInflater).root)
    }

}