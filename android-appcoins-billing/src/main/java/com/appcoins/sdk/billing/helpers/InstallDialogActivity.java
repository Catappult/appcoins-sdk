package com.appcoins.sdk.billing.helpers;

import static android.graphics.Typeface.BOLD;
import static com.appcoins.sdk.billing.helpers.WalletUtils.context;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.appcoins_wallet;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_close_button;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_close_install;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iap_wallet_and_appstore_not_installed_popup_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iap_wallet_and_appstore_not_installed_popup_button;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;
import static com.appcoins.sdk.billing.utils.LayoutUtils.generateRandomId;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setBackground;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.analytics.AnalyticsManagerProvider;
import com.appcoins.sdk.billing.analytics.BillingAnalytics;
import com.appcoins.sdk.billing.analytics.SdkAnalytics;
import com.appcoins.sdk.billing.helpers.translations.TranslationsRepository;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.usecases.GetAppInstalledVersion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import kotlin.Pair;

public class InstallDialogActivity extends Activity {

  public final static String KEY_BUY_INTENT = "BUY_INTENT";
  public final static String LOADING_DIALOG_CARD = "loading_dialog_install";
  public final static int REQUEST_CODE = 10001;
  public final static int ERROR_RESULT_CODE = 6;
  private final static int MINIMUM_APTOIDE_VERSION = 9908;
  private final static int RESULT_USER_CANCELED = 1;
  private static final String DIALOG_WALLET_INSTALL_GRAPHIC = "dialog_wallet_install_graphic";
  private static final String DIALOG_WALLET_INSTALL_EMPTY_IMAGE =
      "dialog_wallet_install_empty_image";
  private static final String INSTALL_BUTTON_COLOR = "#ffffbb33";
  private static final String INSTALL_BUTTON_TEXT_COLOR = "#ffffffff";
  private static final String FIRST_IMPRESSION_KEY = "first_impression";
  private final static String BUY_ITEM_PROPERTIES = "buy_item_properties";
  private final static String SDK_ANALYTICS = "sdk_analytics";
  private final String appBannerResourcePath = "appcoins-wallet/resources/app-banner";

  public BuyItemProperties buyItemProperties;
  public SdkAnalytics sdkAnalytics;
  private TranslationsRepository translations;
  private boolean firstImpression = true;

  public static Intent newIntent(Context context, BuyItemProperties buyItemProperties,
      SdkAnalytics sdkAnalytics) {
    Intent intent = new Intent(context, InstallDialogActivity.class);
    intent.putExtra(BUY_ITEM_PROPERTIES, buyItemProperties);
    intent.putExtra(SDK_ANALYTICS, sdkAnalytics);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    BillingAnalytics billingAnalytics =
        new BillingAnalytics(AnalyticsManagerProvider.provideAnalyticsManager());
    buyItemProperties = (BuyItemProperties) getIntent().getSerializableExtra(BUY_ITEM_PROPERTIES);
    sdkAnalytics = (SdkAnalytics) getIntent().getSerializableExtra(SDK_ANALYTICS);
    translations = TranslationsRepository.getInstance(this);
    if (savedInstanceState != null) {
      firstImpression = savedInstanceState.getBoolean(FIRST_IMPRESSION_KEY, true);
    }
    String storeUrl = "market://details?id="
        + BuildConfig.APPCOINS_WALLET_PACKAGE_NAME
        + "&utm_source=appcoinssdk&app_source="
        + this.getPackageName();

    //This log is necessary for the automatic test that validates the wallet installation dialog
    Log.d("InstallDialogActivity",
        "com.appcoins.sdk.billing.helpers.InstallDialogActivity started");

    RelativeLayout installationDialog = setupInstallationDialog(storeUrl);

    showInstallationDialog(installationDialog);
    handlePurchaseStartEvent(billingAnalytics);

    sdkAnalytics.walletInstallImpression();
  }

  @Override
  protected void onResume() {
      super.onResume();
      if (WalletUtils.hasBillingServiceInstalled()) {
          showLoadingDialog();
          sdkAnalytics.installWalletAptoideSuccess();
          PendingPurchaseStream.getInstance().emit(new Pair<>(this, buyItemProperties));
      }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(FIRST_IMPRESSION_KEY, firstImpression);
  }

  @Override public void onBackPressed() {
    sdkAnalytics.walletInstallClick("back_button");
    Bundle response = new Bundle();
    response.putInt(RESPONSE_CODE, RESULT_USER_CANCELED);
    Intent intent = new Intent();
    intent.putExtras(response);
    setResult(Activity.RESULT_CANCELED, intent);
    finish();
    super.onBackPressed();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    Log.d("InstallDialogActivity", "onActivityResult: resultCode "+ resultCode);
    finishActivity(resultCode, data);
  }

  private void handlePurchaseStartEvent(BillingAnalytics billingAnalytics) {
    if (firstImpression) {
      billingAnalytics.sendPurchaseStartEvent(buyItemProperties.getPackageName(),
          buyItemProperties.getSku(), "0.0", buyItemProperties.getType(),
          BillingAnalytics.START_INSTALL);
      firstImpression = false;
    }
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

  private void finishActivity(int resultCode, Intent data) {
    this.setResult(resultCode, data);
    this.finish();
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

    Button installButton = buildInstallButton(dialogLayout,
        translations.getString(iab_wallet_not_installed_popup_close_install), storeUrl);
    backgroundLayout.addView(installButton);

    Button skipButton = buildSkipButton(installButton,
        translations.getString(iab_wallet_not_installed_popup_close_button));
    backgroundLayout.addView(skipButton);

    showAppRelatedImagery(appIcon, appBanner, dialogBody);

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

  private Button buildSkipButton(Button installButton, String skipButtonText) {
    int skipButtonColor = Color.parseColor("#8f000000");
    Button skipButton = new Button(this);
    skipButton.setText(skipButtonText);
    skipButton.setTextSize(12);
    skipButton.setTextColor(skipButtonColor);
    skipButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
    skipButton.setBackgroundColor(Color.TRANSPARENT);
    skipButton.setIncludeFontPadding(false);
    skipButton.setClickable(true);
    skipButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        sdkAnalytics.walletInstallClick("cancel");
        Bundle response = new Bundle();
        response.putInt(RESPONSE_CODE, RESULT_USER_CANCELED);

        Intent intent = new Intent();
        intent.putExtras(response);

        setResult(Activity.RESULT_CANCELED, intent);
        finish();
      }
    });
    RelativeLayout.LayoutParams skipButtonParams =
        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
    skipButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, installButton.getId());
    skipButtonParams.addRule(RelativeLayout.LEFT_OF, installButton.getId());
    skipButtonParams.setMargins(0, 0, dpToPx(80), 0);
    skipButton.setLayoutParams(skipButtonParams);
    return skipButton;
  }

  private Button buildInstallButton(RelativeLayout dialogLayout, String installButtonText,
      final String storeUrl) {
    Button installButton = new Button(this);
    installButton.setText(installButtonText);
    installButton.setTextSize(12);
    installButton.setTextColor(Color.parseColor(INSTALL_BUTTON_TEXT_COLOR));
    installButton.setId(generateRandomId());
    installButton.setGravity(Gravity.CENTER);
    installButton.setIncludeFontPadding(false);
    installButton.setPadding(0, 0, 0, 0);

    GradientDrawable installButtonDrawable = new GradientDrawable();
    installButtonDrawable.setColor(Color.parseColor(INSTALL_BUTTON_COLOR));
    installButtonDrawable.setCornerRadius(dpToPx(16));
    setBackground(installButton, installButtonDrawable);

    RelativeLayout.LayoutParams installButtonParams =
        new RelativeLayout.LayoutParams(dpToPx(110), dpToPx(36));
    installButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, dialogLayout.getId());
    installButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, dialogLayout.getId());
    installButtonParams.setMargins(0, 0, dpToPx(20), dpToPx(16));
    installButton.setLayoutParams(installButtonParams);
    installButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        sdkAnalytics.walletInstallClick("install_wallet");
        redirectToRemainingStores(storeUrl);
      }
    });
    return installButton;
  }

  private void redirectToRemainingStores(String storeUrl) {
    Pair<Intent, Boolean> storeIntentPair = buildStoreViewIntent(storeUrl);
    if (isAbleToRedirect(storeIntentPair.getFirst())) {
      sendInternalAppDownloadAnalytic(storeIntentPair.getSecond());
      startActivity(storeIntentPair.getFirst());
    } else {
      sdkAnalytics.downloadWalletFallbackImpression("browser");
      startActivityForBrowser(BuildConfig.WALLET_APP_BROWSER_URL);
    }
  }

  private void sendInternalAppDownloadAnalytic(Boolean isAptoidePackage) {
    if (isAptoidePackage) {
      sdkAnalytics.downloadWalletAptoideImpression();
    } else {
      sdkAnalytics.downloadWalletFallbackImpression("native");
    }
  }

  private void startActivityForBrowser(String url) {
    Intent browserIntent = buildBrowserIntent(url);
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
    String highlightedString = translations.getString(appcoins_wallet);
    String dialogBody = String.format(translations.getString(iab_wallet_not_installed_popup_body),
        highlightedString);
    SpannableStringBuilder messageStylized = new SpannableStringBuilder(dialogBody);
    messageStylized.setSpan(new StyleSpan(BOLD), dialogBody.indexOf(highlightedString),
        dialogBody.indexOf(highlightedString) + highlightedString.length(),
        Spannable.SPAN_INCLUSIVE_INCLUSIVE);
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
    RelativeLayout.LayoutParams appIconParams =
        new RelativeLayout.LayoutParams(appIconSize, appIconSize);
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
    RelativeLayout.LayoutParams dialogLayoutParams =
        new RelativeLayout.LayoutParams(cardWidth, dpToPx(288));
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
    if (GetAppInstalledVersion.Companion.invoke(BuildConfig.APTOIDE_PACKAGE_NAME, context) >= MINIMUM_APTOIDE_VERSION) {
      appStoreIntent.setPackage(BuildConfig.APTOIDE_PACKAGE_NAME);
      return new Pair<>(appStoreIntent, true);
    }
    return new Pair<>(appStoreIntent, false);
  }

  private void showAppRelatedImagery(ImageView appIcon, ImageView appBanner,
      TextView dialogLayout) {
    String packageName = getPackageName();
    Drawable icon = null;

    try {
      icon = this.getPackageManager()
          .getApplicationIcon(packageName);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    boolean hasImage = isAppBannerAvailable();
    Drawable appBannerDrawable;

    if (hasImage) {
      appIcon.setVisibility(View.INVISIBLE);
      appBannerDrawable = fetchAppGraphicDrawable(
          appBannerResourcePath + "/" + DIALOG_WALLET_INSTALL_GRAPHIC + ".png");
      RelativeLayout.LayoutParams dialogParams =
          (RelativeLayout.LayoutParams) dialogLayout.getLayoutParams();
      int textMarginTop = dpToPx(5);
      dialogParams.setMargins(dpToPx(32), textMarginTop, dpToPx(32), 0);
    } else {
      appIcon.setVisibility(View.VISIBLE);
      appIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
      appIcon.setImageDrawable(icon);
      appBannerDrawable = fetchAppGraphicDrawable(
          appBannerResourcePath + "/" + DIALOG_WALLET_INSTALL_EMPTY_IMAGE + ".png");
    }
    appBanner.setImageDrawable(appBannerDrawable);
  }

  private boolean isAppBannerAvailable() {
    boolean hasImage;
    try {
      hasImage = Arrays.asList(getAssets().list(appBannerResourcePath))
          .contains(DIALOG_WALLET_INSTALL_GRAPHIC + ".png");
    } catch (IOException e) {
      e.printStackTrace();
      hasImage = false;
    }
    return hasImage;
  }

  private Drawable fetchAppGraphicDrawable(String path) {
    InputStream inputStream = null;
    try {
      inputStream = this.getResources()
          .getAssets()
          .open(path);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return Drawable.createFromStream(inputStream, null);
  }

  private int getLayoutOrientation() {
    return getResources().getConfiguration().orientation;
  }

  private boolean isAbleToRedirect(Intent intent) {
    ActivityInfo activityInfo = intent.resolveActivityInfo(getPackageManager(), 0);
    return activityInfo != null;
  }

  private Intent buildBrowserIntent(String url) {
    return new Intent(Intent.ACTION_VIEW, Uri.parse(url));
  }

  private void buildAlertNoBrowserAndStores() {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    String value = translations.getString(iap_wallet_and_appstore_not_installed_popup_body);
    String dismissValue =
        translations.getString(iap_wallet_and_appstore_not_installed_popup_button);
    alert.setMessage(value);
    alert.setCancelable(true);
    alert.setPositiveButton(dismissValue, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        Bundle response = new Bundle();
        response.putInt(RESPONSE_CODE, RESULT_USER_CANCELED);
        Intent intent = new Intent();
        intent.putExtras(response);
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
      }
    });
    AlertDialog alertDialog = alert.create();
    alertDialog.show();
  }
}