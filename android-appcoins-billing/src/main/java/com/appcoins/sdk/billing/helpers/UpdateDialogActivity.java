package com.appcoins.sdk.billing.helpers;

import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.igu_app_new_version_available_popup_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.igu_app_new_version_available_popup_close_button;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.igu_app_new_version_available_popup_update_button;
import static com.appcoins.sdk.billing.utils.LayoutUtils.generateRandomId;
import static com.appcoins.sdk.billing.utils.LayoutUtils.setBackground;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.appcoins.sdk.billing.helpers.translations.TranslationsRepository;
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class UpdateDialogActivity extends Activity {

  private static final String DIALOG_WALLET_INSTALL_GRAPHIC = "dialog_wallet_install_graphic";
  private static final String DIALOG_WALLET_INSTALL_EMPTY_IMAGE =
      "dialog_wallet_install_empty_image";
  private static final String INSTALL_BUTTON_COLOR = "#ffffbb33";
  private static final String INSTALL_BUTTON_TEXT_COLOR = "#ffffffff";
  private final String appBannerResourcePath = "appcoins-wallet/resources/app-banner";
  private TranslationsRepository translations;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    translations = TranslationsRepository.getInstance(this);

    //This log is necessary for the automatic test that validates the wallet installation dialog
    Log.d("InstallDialogActivity",
        "com.appcoins.sdk.billing.helpers.InstallDialogActivity started");

    RelativeLayout updateDialog = setupUpdateDialog();

    showUpdateDialog(updateDialog);
  }

  private RelativeLayout setupUpdateDialog() {
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

    Button updateAppButton = buildUpdateButton(dialogLayout,
        translations.getString(igu_app_new_version_available_popup_update_button));
    backgroundLayout.addView(updateAppButton);

    Button closeButton = buildCloseButton(updateAppButton,
        translations.getString(igu_app_new_version_available_popup_close_button));
    backgroundLayout.addView(closeButton);

    showAppRelatedImagery(appIcon, appBanner, dialogBody);

    return backgroundLayout;
  }

  private void showUpdateDialog(RelativeLayout dialogLayout) {
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

  private Button buildCloseButton(Button updateAppButton, String closeButtonText) {
    int skipButtonColor = Color.parseColor("#8f000000");
    Button closeButton = new Button(this);
    closeButton.setText(closeButtonText);
    closeButton.setTextSize(12);
    closeButton.setTextColor(skipButtonColor);
    closeButton.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
    closeButton.setBackgroundColor(Color.TRANSPARENT);
    closeButton.setIncludeFontPadding(false);
    closeButton.setClickable(true);
    closeButton.setOnClickListener(v -> finish());
    RelativeLayout.LayoutParams closeButtonParams =
        new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, dpToPx(36));
    closeButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, updateAppButton.getId());
    closeButtonParams.addRule(RelativeLayout.LEFT_OF, updateAppButton.getId());
    closeButtonParams.setMargins(0, 0, dpToPx(80), 0);
    closeButton.setLayoutParams(closeButtonParams);
    return closeButton;
  }

  private Button buildUpdateButton(RelativeLayout dialogLayout, String updateButtonText) {
    Button updateButton = new Button(this);
    updateButton.setText(updateButtonText);
    updateButton.setTextSize(12);
    updateButton.setTextColor(Color.parseColor(INSTALL_BUTTON_TEXT_COLOR));
    updateButton.setId(generateRandomId());
    updateButton.setGravity(Gravity.CENTER);
    updateButton.setIncludeFontPadding(false);
    updateButton.setPadding(0, 0, 0, 0);

    GradientDrawable updateButtonDrawable = new GradientDrawable();
    updateButtonDrawable.setColor(Color.parseColor(INSTALL_BUTTON_COLOR));
    updateButtonDrawable.setCornerRadius(dpToPx(16));
    setBackground(updateButton, updateButtonDrawable);

    RelativeLayout.LayoutParams updateButtonParams =
        new RelativeLayout.LayoutParams(dpToPx(110), dpToPx(36));
    updateButtonParams.addRule(RelativeLayout.ALIGN_BOTTOM, dialogLayout.getId());
    updateButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, dialogLayout.getId());
    updateButtonParams.setMargins(0, 0, dpToPx(20), dpToPx(16));
    updateButton.setLayoutParams(updateButtonParams);
    updateButton.setOnClickListener(v -> {
        LaunchAppUpdate.INSTANCE.invoke(getApplicationContext());
        finish();
    });
    return updateButton;
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
    String dialogBody = translations.getString(igu_app_new_version_available_popup_body);
    return new SpannableStringBuilder(dialogBody);
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
}