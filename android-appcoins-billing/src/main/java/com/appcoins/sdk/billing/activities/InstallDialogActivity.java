package com.appcoins.sdk.billing.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.billing.sdk.R;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.PaymentResponseStream;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.listeners.SDKPaymentResponse;
import com.appcoins.sdk.billing.usecases.GetAppInstalledVersion;
import com.appcoins.sdk.core.analytics.SdkAnalytics;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
import com.appcoins.sdk.core.analytics.events.SdkInstallWalletDialogLabels;
import kotlin.Pair;

import static android.graphics.Typeface.BOLD;
import static com.appcoins.sdk.billing.utils.LayoutUtils.generateRandomId;
import static com.appcoins.sdk.core.logger.Logger.logInfo;
import static com.appcoins.sdk.core.logger.Logger.logWarning;

public class InstallDialogActivity extends Activity {

    private static final int MINIMUM_APTOIDE_VERSION = 9908;
    private static final String BUY_ITEM_PROPERTIES = "buy_item_properties";

    private static final String INSTALL_BUTTON_COLOR = "#ffffbb33";
    private static final String INSTALL_BUTTON_TEXT_COLOR = "#ffffffff";

    public BuyItemProperties buyItemProperties;
    public SdkAnalytics sdkAnalytics;

    private boolean shouldSendCancelResult = true;

    public static Intent newIntent(Context context, BuyItemProperties buyItemProperties) {
        Intent intent = new Intent(context, InstallDialogActivity.class);
        intent.putExtra(BUY_ITEM_PROPERTIES, buyItemProperties);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buyItemProperties = (BuyItemProperties) getIntent().getSerializableExtra(BUY_ITEM_PROPERTIES);
        sdkAnalytics = SdkAnalyticsUtils.INSTANCE.getSdkAnalytics();
        String storeUrl = "market://details?id="
            + BuildConfig.APPCOINS_WALLET_PACKAGE_NAME
            + "&utm_source=appcoinssdk&app_source="
            + this.getPackageName();

        //This log is necessary for the automatic test that validates the wallet installation dialog
        logInfo("Starting InstallDialogActivity");

        RelativeLayout installationDialog = setupInstallationDialog(storeUrl);

        showInstallationDialog(installationDialog);

        sdkAnalytics.sendInstallWalletDialogEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (WalletUtils.INSTANCE.isAppAvailableToBind(BuildConfig.APPCOINS_WALLET_IAB_BIND_ACTION)) {
            shouldSendCancelResult = false;
            showLoadingDialog();
            sdkAnalytics.sendInstallWalletDialogSuccessEvent();
            PendingPurchaseStream.getInstance()
                .emit(new Pair<>(this, buyItemProperties));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        logInfo("Pressed back_button on InstallDialogActivity.");
        sdkAnalytics.sendInstallWalletDialogActionEvent(SdkInstallWalletDialogLabels.BACK_BUTTON);
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        logInfo("InstallDialogActivity is being destroyed.");
        if (shouldSendCancelResult) {
            logInfo("Sending cancel event.");
            PaymentResponseStream.getInstance()
                .emit(SDKPaymentResponse.Companion.createCanceledTypeResponse());
        }
        super.onDestroy();
    }

    private void showLoadingDialog() {
        boolean isLandscape = getLayoutOrientation() == Configuration.ORIENTATION_LANDSCAPE;

        RelativeLayout backgroundLayout = buildBackground();

        RelativeLayout dialogLayout = buildDialogLayout(isLandscape);
        backgroundLayout.addView(dialogLayout);
        ProgressBar progressBar = new ProgressBar(this);
        RelativeLayout.LayoutParams layoutParams =
            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.getIndeterminateDrawable()
            .setColorFilter(Color.parseColor("#fd786b"), PorterDuff.Mode.MULTIPLY);
        progressBar.setLayoutParams(layoutParams);
        dialogLayout.addView(progressBar);
        showInstallationDialog(backgroundLayout);
    }

    private RelativeLayout setupInstallationDialog(String storeUrl) {
        boolean isLandscape = getLayoutOrientation() == Configuration.ORIENTATION_LANDSCAPE;

        RelativeLayout backgroundLayout = buildBackground();

        RelativeLayout dialogLayout = buildDialogLayout(isLandscape);
        backgroundLayout.addView(dialogLayout);

        ImageView appBanner = buildAppBanner();
        dialogLayout.addView(appBanner);

        ImageView appIcon = buildAppIcon(isLandscape, dialogLayout);
        backgroundLayout.addView(appIcon);

        TextView dialogBody = buildDialogBody(isLandscape, appIcon);
        backgroundLayout.addView(dialogBody);

        Button installButton = buildInstallButton(dialogLayout, storeUrl);
        backgroundLayout.addView(installButton);

        Button skipButton = buildSkipButton(installButton);
        backgroundLayout.addView(skipButton);

        showAppRelatedImagery(appIcon, appBanner);

        return backgroundLayout;
    }

    private void showInstallationDialog(RelativeLayout dialogLayout) {
        RelativeLayout.LayoutParams layoutParams =
            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        setContentView(dialogLayout, layoutParams);
    }

    private RelativeLayout buildBackground() {
        int backgroundColor = Color.parseColor("#64000000");
        RelativeLayout backgroundLayout = new RelativeLayout(this);
        backgroundLayout.setBackgroundColor(backgroundColor);
        return backgroundLayout;
    }

    private Button buildSkipButton(Button installButton) {
        int skipButtonColor = Color.parseColor("#8f000000");
        Button skipButton = new Button(this);
        skipButton.setText(getResources().getString(R.string.iab_wallet_not_installed_popup_close_button));
        skipButton.setTextSize(12);
        skipButton.setTextColor(skipButtonColor);
        skipButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        skipButton.setBackgroundColor(Color.TRANSPARENT);
        skipButton.setIncludeFontPadding(false);
        skipButton.setClickable(true);
        skipButton.setOnClickListener(v -> {
            logInfo("Pressed cancel button on InstallDialogActivity.");
            sdkAnalytics.sendInstallWalletDialogActionEvent(SdkInstallWalletDialogLabels.CANCEL);
            finish();
        });
        RelativeLayout.LayoutParams skipButtonParams =
            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
        skipButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, installButton.getId());
        skipButtonParams.addRule(RelativeLayout.LEFT_OF, installButton.getId());
        skipButtonParams.setMargins(0, 0, dpToPx(80), 0);
        skipButton.setLayoutParams(skipButtonParams);
        return skipButton;
    }

    private Button buildInstallButton(RelativeLayout dialogLayout, final String storeUrl) {
        Button installButton = new Button(this);
        installButton.setText(getResources().getString(R.string.iab_wallet_not_installed_popup_close_install));
        installButton.setTextSize(12);
        installButton.setTextColor(Color.parseColor(INSTALL_BUTTON_TEXT_COLOR));
        installButton.setId(generateRandomId());
        installButton.setGravity(Gravity.CENTER);
        installButton.setIncludeFontPadding(false);
        installButton.setPadding(0, 0, 0, 0);

        GradientDrawable installButtonDrawable = new GradientDrawable();
        installButtonDrawable.setColor(Color.parseColor(INSTALL_BUTTON_COLOR));
        installButtonDrawable.setCornerRadius(dpToPx(16));
        installButton.setBackground(installButtonDrawable);

        RelativeLayout.LayoutParams installButtonParams = new RelativeLayout.LayoutParams(dpToPx(110), dpToPx(36));
        installButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, dialogLayout.getId());
        installButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, dialogLayout.getId());
        installButtonParams.setMargins(0, 0, dpToPx(20), dpToPx(16));
        installButton.setLayoutParams(installButtonParams);
        installButton.setOnClickListener(v -> {
            logInfo("Pressed install button on InstallDialogActivity.");
            sdkAnalytics.sendInstallWalletDialogActionEvent(SdkInstallWalletDialogLabels.INSTALL);
            redirectToRemainingStores(storeUrl);
        });
        return installButton;
    }

    private void redirectToRemainingStores(String storeUrl) {
        Pair<Intent, Boolean> storeIntentPair = buildStoreViewIntent(storeUrl);
        if (isAbleToRedirect(storeIntentPair.getFirst())) {
            logInfo("Sending to available Store storeUrl: " + storeUrl);
            sendInternalAppDownloadAnalytic(storeIntentPair.getSecond());
            startActivity(storeIntentPair.getFirst());
        } else {
            logInfo("No store available. Sending to browser.");
            sdkAnalytics.sendInstallWalletDialogDownloadWalletFallbackEvent("browser");
            startActivityForBrowser();
        }
    }

    private void sendInternalAppDownloadAnalytic(Boolean isAptoidePackage) {
        if (isAptoidePackage) {
            sdkAnalytics.sendInstallWalletDialogDownloadWalletVanillaEvent();
        } else {
            sdkAnalytics.sendInstallWalletDialogDownloadWalletFallbackEvent("native");
        }
    }

    private void startActivityForBrowser() {
        Intent browserIntent = buildBrowserIntent();
        if (isAbleToRedirect(browserIntent)) {
            startActivity(browserIntent);
        } else {
            buildAlertNoBrowserAndStores();
        }
    }

    private TextView buildDialogBody(boolean isLandscape, ImageView appIcon) {
        int dialogBodyColor = Color.parseColor("#4a4a4a");
        TextView dialogBody = new TextView(this);
        dialogBody.setMaxLines(2);
        dialogBody.setTextColor(dialogBodyColor);
        dialogBody.setTextSize(16);
        dialogBody.setGravity(Gravity.CENTER_HORIZONTAL);
        int dialogBodyWidth = RelativeLayout.LayoutParams.MATCH_PARENT;
        int textMarginTop = dpToPx(20);
        if (isLandscape) {
            dialogBodyWidth = dpToPx(384);
            textMarginTop = dpToPx(10);
        }
        RelativeLayout.LayoutParams bodyParams =
            new RelativeLayout.LayoutParams(dialogBodyWidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        bodyParams.addRule(RelativeLayout.BELOW, appIcon.getId());
        bodyParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        bodyParams.setMargins(dpToPx(32), textMarginTop, dpToPx(32), 0);
        dialogBody.setLayoutParams(bodyParams);
        dialogBody.setText(setHighlightDialogBody());
        return dialogBody;
    }

    private SpannableStringBuilder setHighlightDialogBody() {
        String highlightedString = getResources().getString(R.string.appcoins_wallet);
        String dialogBody =
            String.format(getResources().getString(R.string.iab_wallet_not_installed_popup_body), highlightedString);
        SpannableStringBuilder messageStylized = new SpannableStringBuilder(dialogBody);
        messageStylized.setSpan(new StyleSpan(BOLD), dialogBody.indexOf(highlightedString),
            dialogBody.indexOf(highlightedString) + highlightedString.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return messageStylized;
    }

    private ImageView buildAppIcon(boolean isLandscape, RelativeLayout dialogLayout) {
        ImageView appIcon = new ImageView(this);
        appIcon.setId(generateRandomId());
        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int appIconMarginTop = dpToPx(85);
        int appIconSize = dpToPx(66);
        if (isLandscape) {
            appIconMarginTop = dpToPx(80);
            appIconSize = dpToPx(80);
        }
        RelativeLayout.LayoutParams appIconParams = new RelativeLayout.LayoutParams(appIconSize, appIconSize);
        appIconParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        appIconParams.addRule(RelativeLayout.ALIGN_TOP, dialogLayout.getId());
        appIconParams.setMargins(0, appIconMarginTop, 0, 0);
        appIcon.setLayoutParams(appIconParams);
        return appIcon;
    }

    private ImageView buildAppBanner() {
        ImageView appBanner = new ImageView(this);
        appBanner.setScaleType(ImageView.ScaleType.CENTER_CROP);
        RelativeLayout.LayoutParams appBannerParams =
            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dpToPx(120));
        appBannerParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        appBanner.setLayoutParams(appBannerParams);
        return appBanner;
    }

    private RelativeLayout buildDialogLayout(boolean isLandscape) {
        RelativeLayout dialogLayout = new RelativeLayout(this);
        dialogLayout.setId(generateRandomId());
        dialogLayout.setClipToPadding(false);
        dialogLayout.setBackgroundColor(Color.WHITE);

        int dialogLayoutMargins = dpToPx(12);
        int cardWidth = RelativeLayout.LayoutParams.MATCH_PARENT;
        if (isLandscape) {
            cardWidth = dpToPx(384);
        }
        RelativeLayout.LayoutParams dialogLayoutParams = new RelativeLayout.LayoutParams(cardWidth, dpToPx(288));
        dialogLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        dialogLayoutParams.setMargins(dialogLayoutMargins, 0, dialogLayoutMargins, 0);
        dialogLayout.setLayoutParams(dialogLayoutParams);
        return dialogLayout;
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem()
            .getDisplayMetrics());
    }

    private Pair<Intent, Boolean> buildStoreViewIntent(String storeUrl) {
        final Intent appStoreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl));
        if (GetAppInstalledVersion.INSTANCE.invoke(BuildConfig.APTOIDE_PACKAGE_NAME, WalletUtils.INSTANCE.getContext())
            >= MINIMUM_APTOIDE_VERSION) {
            appStoreIntent.setPackage(BuildConfig.APTOIDE_PACKAGE_NAME);
            return new Pair<>(appStoreIntent, true);
        }
        return new Pair<>(appStoreIntent, false);
    }

    private void showAppRelatedImagery(ImageView appIcon, ImageView appBanner) {
        String packageName = getPackageName();
        Drawable icon = null;

        try {
            icon = this.getPackageManager()
                .getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            logWarning("Failed to find Application Icon: " + e);
        }

        appIcon.setVisibility(View.VISIBLE);
        appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        appIcon.setImageDrawable(icon);
        Drawable appBannerDrawable = getResources().getDrawable(R.drawable.dialog_wallet_install_empty_image);

        appBanner.setImageDrawable(appBannerDrawable);
    }

    private int getLayoutOrientation() {
        return getResources().getConfiguration().orientation;
    }

    private boolean isAbleToRedirect(Intent intent) {
        ActivityInfo activityInfo = intent.resolveActivityInfo(getPackageManager(), 0);
        return activityInfo != null;
    }

    private Intent buildBrowserIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(BuildConfig.WALLET_APP_BROWSER_URL));
    }

    private void buildAlertNoBrowserAndStores() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        String value = getResources().getString(R.string.iap_wallet_and_appstore_not_installed_popup_body);
        String dismissValue = getResources().getString(R.string.iap_wallet_and_appstore_not_installed_popup_button);
        alert.setMessage(value);
        alert.setCancelable(true);
        alert.setPositiveButton(dismissValue, (dialog, id) -> finish());
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
    }
}
