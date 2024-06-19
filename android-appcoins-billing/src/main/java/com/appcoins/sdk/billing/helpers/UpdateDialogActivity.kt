package com.appcoins.sdk.billing.helpers

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.appcoins.billing.sdk.R
import com.appcoins.sdk.billing.helpers.translations.TranslationsRepository
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate

class UpdateDialogActivity : Activity() {
    private lateinit var translations: TranslationsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        translations = TranslationsRepository.getInstance(this)

        setContentView(R.layout.update_dialog_activity)

        //This log is necessary for the automatic test that validates the wallet installation dialog
        Log.d(
            "InstallDialogActivity",
            "com.appcoins.sdk.billing.helpers.InstallDialogActivity started"
        )

        setActionsForButtons()
    }

    private fun setActionsForButtons() {
        findViewById<Button>(R.id.button_update)?.let {
            it.setOnClickListener {
                LaunchAppUpdate.invoke(applicationContext)
                finish()
            }
        }
        findViewById<Button>(R.id.button_close)?.let {
            it.setOnClickListener {
                finish()
            }
        }
    }
}
