package com.appcoins.sdk.billing.helpers

import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.appcoins.sdk.billing.helpers.translations.TranslationsKeys
import com.appcoins.sdk.billing.helpers.translations.TranslationsRepository
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate.invoke
import com.appcoins.sdk.billing.utils.LayoutUtils
import java.io.IOException
import java.io.InputStream

class UpdateDialogActivity : Activity() {
    private val appBannerResourcePath = "appcoins-wallet/resources/app-banner"
    private lateinit var translations: TranslationsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        translations = TranslationsRepository.getInstance(this)

        //This log is necessary for the automatic test that validates the wallet installation dialog
        Log.d(
            "InstallDialogActivity",
            "com.appcoins.sdk.billing.helpers.InstallDialogActivity started"
        )

        val updateDialog = setupUpdateDialog()

        showUpdateDialog(updateDialog)
    }

    private fun setupUpdateDialog(): RelativeLayout {
        val isLandscape = layoutOrientation == Configuration.ORIENTATION_LANDSCAPE

        val backgroundLayout = buildBackground()

        val dialogLayout = buildDialogLayout(isLandscape)
        backgroundLayout.addView(dialogLayout)

        val appBanner = buildAppBanner()
        dialogLayout.addView(appBanner)

        val appIcon = buildAppIcon(isLandscape, dialogLayout)
        backgroundLayout.addView(appIcon)

        val dialogBody = buildDialogBody(isLandscape, appIcon)
        backgroundLayout.addView(dialogBody)

        val updateAppButton = buildUpdateButton(
            dialogLayout,
            translations.getString(TranslationsKeys.igu_app_new_version_available_popup_update_button)
        )
        backgroundLayout.addView(updateAppButton)

        val closeButton = buildCloseButton(
            updateAppButton,
            translations.getString(TranslationsKeys.igu_app_new_version_available_popup_close_button)
        )
        backgroundLayout.addView(closeButton)

        showAppRelatedImagery(appIcon, appBanner, dialogBody)

        return backgroundLayout
    }

    private fun showUpdateDialog(dialogLayout: RelativeLayout) {
        val layoutParams =
            RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )

        setContentView(dialogLayout, layoutParams)
    }

    private fun buildBackground(): RelativeLayout {
        val backgroundColor = Color.parseColor("#64000000")
        val backgroundLayout = RelativeLayout(this)
        backgroundLayout.setBackgroundColor(backgroundColor)
        return backgroundLayout
    }

    private fun buildCloseButton(updateAppButton: Button, closeButtonText: String): Button {
        val skipButtonColor = Color.parseColor("#8f000000")
        val closeButton = Button(this)
        closeButton.text = closeButtonText
        closeButton.textSize = 12f
        closeButton.setTextColor(skipButtonColor)
        closeButton.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        closeButton.setBackgroundColor(Color.TRANSPARENT)
        closeButton.includeFontPadding = false
        closeButton.isClickable = true
        closeButton.setOnClickListener { finish() }
        val closeButtonParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(36))
        closeButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, updateAppButton.id)
        closeButtonParams.addRule(RelativeLayout.LEFT_OF, updateAppButton.id)
        closeButtonParams.setMargins(0, 0, dpToPx(80), 0)
        closeButton.layoutParams = closeButtonParams
        return closeButton
    }

    private fun buildUpdateButton(dialogLayout: RelativeLayout, updateButtonText: String): Button {
        val updateButton = Button(this)
        updateButton.text = updateButtonText
        updateButton.textSize = 12f
        updateButton.setTextColor(Color.parseColor(INSTALL_BUTTON_TEXT_COLOR))
        updateButton.id = LayoutUtils.generateRandomId()
        updateButton.gravity = Gravity.CENTER
        updateButton.includeFontPadding = false
        updateButton.setPadding(0, 0, 0, 0)

        val updateButtonDrawable = GradientDrawable()
        updateButtonDrawable.setColor(Color.parseColor(INSTALL_BUTTON_COLOR))
        updateButtonDrawable.cornerRadius = dpToPx(16).toFloat()
        LayoutUtils.setBackground(updateButton, updateButtonDrawable)

        val updateButtonParams = RelativeLayout.LayoutParams(dpToPx(110), dpToPx(36))
        updateButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, dialogLayout.id)
        updateButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, dialogLayout.id)
        updateButtonParams.setMargins(0, 0, dpToPx(20), dpToPx(16))
        updateButton.layoutParams = updateButtonParams
        updateButton.setOnClickListener {
            invoke(applicationContext)
            finish()
        }
        return updateButton
    }

    private fun buildDialogBody(isLandscape: Boolean, appIcon: ImageView): TextView {
        val dialogBodyColor = Color.parseColor("#4a4a4a")
        val dialogBody = TextView(this)
        dialogBody.maxLines = 2
        dialogBody.setTextColor(dialogBodyColor)
        dialogBody.textSize = 16f
        dialogBody.gravity = Gravity.CENTER_HORIZONTAL
        var dialogBodyWidth = RelativeLayout.LayoutParams.MATCH_PARENT
        var textMarginTop = dpToPx(20)
        if (isLandscape) {
            dialogBodyWidth = dpToPx(384)
            textMarginTop = dpToPx(10)
        }
        val bodyParams =
            RelativeLayout.LayoutParams(dialogBodyWidth, RelativeLayout.LayoutParams.WRAP_CONTENT)
        bodyParams.addRule(RelativeLayout.BELOW, appIcon.id)
        bodyParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        bodyParams.setMargins(dpToPx(32), textMarginTop, dpToPx(32), 0)
        dialogBody.layoutParams = bodyParams
        dialogBody.text = setHighlightDialogBody()
        return dialogBody
    }

    private fun setHighlightDialogBody(): SpannableStringBuilder {
        val dialogBody =
            translations.getString(TranslationsKeys.igu_app_new_version_available_popup_body)
        return SpannableStringBuilder(dialogBody)
    }

    private fun buildAppIcon(isLandscape: Boolean, dialogLayout: RelativeLayout): ImageView {
        val appIcon = ImageView(this)
        appIcon.id = LayoutUtils.generateRandomId()
        appIcon.scaleType = ImageView.ScaleType.CENTER_CROP
        var appIconMarginTop = dpToPx(85)
        var appIconSize = dpToPx(66)
        if (isLandscape) {
            appIconMarginTop = dpToPx(80)
            appIconSize = dpToPx(80)
        }
        val appIconParams = RelativeLayout.LayoutParams(appIconSize, appIconSize)
        appIconParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        appIconParams.addRule(RelativeLayout.ALIGN_TOP, dialogLayout.id)
        appIconParams.setMargins(0, appIconMarginTop, 0, 0)
        appIcon.layoutParams = appIconParams
        return appIcon
    }

    private fun buildAppBanner(): ImageView {
        val appBanner = ImageView(this)
        appBanner.scaleType = ImageView.ScaleType.CENTER_CROP
        val appBannerParams =
            RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(120))
        appBannerParams.addRule(RelativeLayout.CENTER_HORIZONTAL)
        appBanner.layoutParams = appBannerParams
        return appBanner
    }

    private fun buildDialogLayout(isLandscape: Boolean): RelativeLayout {
        val dialogLayout = RelativeLayout(this)
        dialogLayout.id = LayoutUtils.generateRandomId()
        dialogLayout.clipToPadding = false
        dialogLayout.setBackgroundColor(Color.WHITE)

        val dialogLayoutMargins = dpToPx(12)
        var cardWidth = RelativeLayout.LayoutParams.MATCH_PARENT
        if (isLandscape) {
            cardWidth = dpToPx(384)
        }
        val dialogLayoutParams =
            RelativeLayout.LayoutParams(cardWidth, dpToPx(288))
        dialogLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        dialogLayoutParams.setMargins(dialogLayoutMargins, 0, dialogLayoutMargins, 0)
        dialogLayout.layoutParams = dialogLayoutParams
        return dialogLayout
    }

    private fun dpToPx(dp: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), Resources.getSystem().displayMetrics
        ).toInt()

    private fun showAppRelatedImagery(
        appIcon: ImageView, appBanner: ImageView,
        dialogLayout: TextView
    ) {
        val packageName = packageName
        var icon: Drawable? = null

        try {
            icon = this.packageManager
                .getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        val hasImage = isAppBannerAvailable
        val appBannerDrawable: Drawable?

        if (hasImage) {
            appIcon.visibility = View.INVISIBLE
            appBannerDrawable =
                fetchAppGraphicDrawable("$appBannerResourcePath/$DIALOG_WALLET_INSTALL_GRAPHIC.png")
            val dialogParams =
                dialogLayout.layoutParams as RelativeLayout.LayoutParams
            val textMarginTop = dpToPx(5)
            dialogParams.setMargins(dpToPx(32), textMarginTop, dpToPx(32), 0)
        } else {
            appIcon.visibility = View.VISIBLE
            appIcon.scaleType = ImageView.ScaleType.CENTER_CROP
            appIcon.setImageDrawable(icon)
            appBannerDrawable =
                fetchAppGraphicDrawable("$appBannerResourcePath/$DIALOG_WALLET_INSTALL_EMPTY_IMAGE.png")
        }
        appBanner.setImageDrawable(appBannerDrawable)
    }

    private val isAppBannerAvailable: Boolean
        get() {
            var hasImage: Boolean
            try {
                hasImage = assets.list(appBannerResourcePath)?.let {
                    listOf(*it).contains("$DIALOG_WALLET_INSTALL_GRAPHIC.png")
                } == true
            } catch (e: IOException) {
                e.printStackTrace()
                hasImage = false
            }
            return hasImage
        }

    private fun fetchAppGraphicDrawable(path: String): Drawable? {
        var inputStream: InputStream? = null
        try {
            inputStream = this.resources.assets.open(path)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Drawable.createFromStream(inputStream, null)
    }

    private val layoutOrientation: Int
        get() = resources.configuration.orientation

    companion object {
        private const val DIALOG_WALLET_INSTALL_GRAPHIC = "dialog_wallet_install_graphic"
        private const val DIALOG_WALLET_INSTALL_EMPTY_IMAGE = "dialog_wallet_install_empty_image"
        private const val INSTALL_BUTTON_COLOR = "#ffffbb33"
        private const val INSTALL_BUTTON_TEXT_COLOR = "#ffffffff"
    }
}