package com.appcoins.sdk.billing.payasguest;

import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_pay_with_wallet_reward_no_connection_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_pay_with_wallet_reward_title;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_purchase_support_1;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_purchase_support_2_link;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.install_button;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.next_button;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.purchase_error_item_owned;
import static com.appcoins.sdk.billing.payasguest.IabActivity.CREDIT_CARD;
import static com.appcoins.sdk.billing.payasguest.IabActivity.INSTALL_WALLET;
import static com.appcoins.sdk.billing.payasguest.IabActivity.PAYPAL;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.WalletInteract;
import com.appcoins.sdk.billing.analytics.AnalyticsManagerProvider;
import com.appcoins.sdk.billing.analytics.BillingAnalytics;
import com.appcoins.sdk.billing.analytics.WalletAddressProvider;
import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager;
import com.appcoins.sdk.billing.helpers.WalletInstallationIntentBuilder;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.helpers.translations.TranslationsRepository;
import com.appcoins.sdk.billing.layouts.PaymentMethodsFragmentLayout;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.mappers.BillingMapper;
import com.appcoins.sdk.billing.mappers.GamificationMapper;
import com.appcoins.sdk.billing.models.billing.SkuDetailsModel;
import com.appcoins.sdk.billing.models.billing.SkuPurchase;
import com.appcoins.sdk.billing.models.payasguest.WalletGenerationModel;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.service.wallet.WalletGenerationMapper;
import com.appcoins.sdk.billing.service.wallet.WalletRepository;
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;
import com.appcoins.sdk.billing.sharedpreferences.BonusSharedPreferences;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import kotlin.Pair;

public class PaymentMethodsFragment extends Fragment implements PaymentMethodsView {

  private final static String BUY_ITEM_PROPERTIES = "buy_item_properties";
  private static String SELECTED_RADIO_KEY = "selected_radio";
  private static final String SEND_WALLET_INSTALLED_KEY = "first_impression";
  private IabView iabView;
  private BuyItemProperties buyItemProperties;
  private PaymentMethodsPresenter paymentMethodsPresenter;
  private PaymentMethodsFragmentLayout layout;
  private String selectedRadioButton;
  private SkuDetailsModel skuDetailsModel;
  private WalletGenerationModel walletGenerationModel;
  private SkuPurchase itemAlreadyOwnedPurchase;
  private TranslationsRepository translations;
  private Context context;
  private BillingAnalytics billingAnalytics;
  private boolean sendWalletInstalled = true;

  public static PaymentMethodsFragment newInstance(BuyItemProperties buyItemProperties) {
    PaymentMethodsFragment paymentMethodsFragment = new PaymentMethodsFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(BUY_ITEM_PROPERTIES, buyItemProperties);
    Log.i("PaymentMethodsFragment", "putting argument: " + buyItemProperties);
    paymentMethodsFragment.setArguments(bundle);
    Log.i("PaymentMethodsFragment", "after set arguments: " + paymentMethodsFragment.getArguments().getSerializable(BUY_ITEM_PROPERTIES));
    return paymentMethodsFragment;
  }

  @Override public void onAttach(Context context) {
    super.onAttach(context);
    attach(context);
  }

  @Override public void onAttach(Activity activity) {
    super.onAttach(activity);
    attach(activity);
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    translations = TranslationsRepository.getInstance(getActivity());
    BdsService backendService =
        new BdsService(BuildConfig.BACKEND_BASE, BdsService.TIME_OUT_IN_MILLIS);
    BdsService apiService = new BdsService(BuildConfig.HOST_WS, BdsService.TIME_OUT_IN_MILLIS);

    BonusSharedPreferences bonusSharedPreferences = new BonusSharedPreferences(context);
    AttributionSharedPreferences attributionSharedPreferences = new AttributionSharedPreferences(context);
    WalletAddressProvider walletAddressProvider =
        WalletAddressProvider.provideWalletAddressProvider();
    WalletRepository walletRepository =
        new WalletRepository(backendService, new WalletGenerationMapper(), walletAddressProvider);
    PaymentMethodsRepository paymentMethodsRepository = new PaymentMethodsRepository(apiService);
    BillingRepository billingRepository = new BillingRepository(apiService);
    AnalyticsManager analyticsManager = AnalyticsManagerProvider.provideAnalyticsManager();
    billingAnalytics = new BillingAnalytics(analyticsManager);

    WalletInteract walletInteract =
        new WalletInteract(attributionSharedPreferences, walletRepository);
    GamificationInteract gamificationInteract =
        new GamificationInteract(bonusSharedPreferences, new GamificationMapper(),
            backendService);
    PaymentMethodsInteract paymentMethodsInteract =
        new PaymentMethodsInteract(walletInteract, gamificationInteract, paymentMethodsRepository,
            billingRepository);
    WalletInstallationIntentBuilder walletInstallationIntentBuilder =
        new WalletInstallationIntentBuilder(context.getPackageManager(), context.getPackageName(),
            context.getApplicationContext());
    buyItemProperties = (BuyItemProperties) getArguments().getSerializable(BUY_ITEM_PROPERTIES);
    Log.i("PaymentMethodsFragment", "after getting arguments: " + buyItemProperties);

    paymentMethodsPresenter =
        new PaymentMethodsPresenter(this, paymentMethodsInteract, walletInstallationIntentBuilder,
            billingAnalytics, buyItemProperties);
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    boolean isPortrait =
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    layout = new PaymentMethodsFragmentLayout(getActivity(), isPortrait, buyItemProperties);

    return layout.build();
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Button cancelButton = layout.getCancelButton();
    Button positiveButton = layout.getPositiveButton();
    RadioButton creditCardButton = layout.getCreditCardRadioButton();
    RadioButton paypalButton = layout.getPaypalRadioButton();
    RadioButton installRadioButton = layout.getInstallRadioButton();
    ViewGroup creditWrapper = layout.getCreditCardWrapperLayout();
    ViewGroup paypalWrapper = layout.getPaypalWrapperLayout();
    ViewGroup installWrapper = layout.getInstallWrapperLayout();
    Button errorButton = layout.getErrorPositiveButton();
    ViewGroup supportHook = layout.getSupportHookView();
    TextView helpText = layout.getHelpText();

    onRotation(savedInstanceState);
    onCancelButtonClicked(cancelButton);
    onPositiveButtonClicked(positiveButton);
    onErrorButtonClicked(errorButton);
    onRadioButtonClicked(creditCardButton, paypalButton, installRadioButton, creditWrapper,
        paypalWrapper, installWrapper);
    if (iabView.hasEmailApplication()) {
      supportHook.setVisibility(View.VISIBLE);
      createSpannableString(helpText);
    } else {
      supportHook.setVisibility(View.GONE);
    }
  }

  @Override public void onResume() {
    super.onResume();
    if (WalletUtils.hasBillingServiceInstalled()) {
      layout.getDialogLayout()
          .setVisibility(View.GONE);
      layout.getIntentLoadingView()
          .setVisibility(View.VISIBLE);
      PendingPurchaseStream.getInstance().emit(new Pair<>(getActivity(), buyItemProperties));
      if (sendWalletInstalled) {
        sendWalletInstalled = false;
        billingAnalytics.sendPaymentSuccessEvent(buyItemProperties.getPackageName(),
            buyItemProperties.getSku(), "0.0", BillingAnalytics.PAYMENT_METHOD_INSTALL_WALLET,
            buyItemProperties.getType());
      }
    } else {
      paymentMethodsPresenter.prepareUi();
    }
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (selectedRadioButton != null) {
      outState.putString(SELECTED_RADIO_KEY, selectedRadioButton);
    }
    outState.putBoolean(SEND_WALLET_INSTALLED_KEY, sendWalletInstalled);
  }

  @Override public void onDestroyView() {
    layout.onDestroyView();
    layout = null;
    super.onDestroyView();
  }

  @Override public void onDestroy() {
    skuDetailsModel = null;
    walletGenerationModel = null;
    paymentMethodsPresenter.onDestroy();
    paymentMethodsPresenter = null;
    super.onDestroy();
  }

  @Override public void onDetach() {
    super.onDetach();
    context = null;
    iabView = null;
  }

  private void createSpannableString(TextView helpText) {
    String helpString = translations.getString(iab_purchase_support_1);
    String contactString = translations.getString(iab_purchase_support_2_link);
    String concatenatedString = helpString + ' ' + contactString;
    SpannableString spannableString = new SpannableString(concatenatedString);
    ClickableSpan clickableSpan = new ClickableSpan() {
      @Override public void onClick(View widget) {
        paymentMethodsPresenter.onHelpTextClicked(buyItemProperties);
      }
    };
    spannableString.setSpan(clickableSpan, helpString.length() + 1, concatenatedString.length(),
        Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    helpText.setText(spannableString);
    helpText.setMovementMethod(LinkMovementMethod.getInstance());
    helpText.setLinkTextColor(Color.parseColor("#fe6e76"));
    helpText.setHighlightColor(Color.TRANSPARENT);
  }

  private void onErrorButtonClicked(Button errorButton) {
    errorButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        paymentMethodsPresenter.onErrorButtonClicked();
      }
    });
  }

  private void onRadioButtonClicked(RadioButton creditCardButton, RadioButton paypalButton,
      RadioButton installRadioButton, ViewGroup creditWrapper, ViewGroup paypalWrapper,
      ViewGroup installWrapper) {
    RadioButtonClickListener creditCardListener = new RadioButtonClickListener(CREDIT_CARD);
    RadioButtonClickListener paypalListener = new RadioButtonClickListener(PAYPAL);
    RadioButtonClickListener installListener = new RadioButtonClickListener(INSTALL_WALLET);

    creditCardButton.setOnClickListener(creditCardListener);
    paypalButton.setOnClickListener(paypalListener);
    installRadioButton.setOnClickListener(installListener);

    creditWrapper.setOnClickListener(creditCardListener);
    paypalWrapper.setOnClickListener(paypalListener);
    installWrapper.setOnClickListener(installListener);
  }

  private void onPositiveButtonClicked(final Button positiveButton) {
    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (selectedRadioButton != null) {
          paymentMethodsPresenter.onPositiveButtonClicked(selectedRadioButton);
        }
      }
    });
  }

  private void onCancelButtonClicked(Button cancelButton) {
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        paymentMethodsPresenter.onCancelButtonClicked(selectedRadioButton);
      }
    });
  }

  private void onRotation(Bundle savedInstanceState) {
    if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_RADIO_KEY)) {
      selectedRadioButton = savedInstanceState.getString(SELECTED_RADIO_KEY);
      sendWalletInstalled = savedInstanceState.getBoolean(SEND_WALLET_INSTALLED_KEY);
      setRadioButtonSelected(selectedRadioButton);
      setPositiveButtonText(selectedRadioButton);
    }
  }

  @Override public void setSkuInformation(SkuDetailsModel skuDetailsModel) {
    this.skuDetailsModel = skuDetailsModel;
    TextView fiatPriceView = layout.getFiatPriceView();
    TextView appcPriceView = layout.getAppcPriceView();
    DecimalFormat df = new DecimalFormat("0.00");
    String fiatText = df.format(new BigDecimal(skuDetailsModel.getFiatPrice()));
    String appcText = df.format(new BigDecimal(skuDetailsModel.getAppcPrice()));
    fiatPriceView.setText(
        String.format("%s %s", fiatText, skuDetailsModel.getFiatPriceCurrencyCode()));
    appcPriceView.setText(String.format("%s %s", appcText, "APPC"));
  }

  @Override public void showError() {
    ViewGroup intentProgressBar = layout.getIntentLoadingView();
    ViewGroup dialogLayout = layout.getDialogLayout();
    ViewGroup errorLayout = layout.getErrorView();
    intentProgressBar.setVisibility(View.INVISIBLE);
    dialogLayout.setVisibility(View.GONE);
    errorLayout.setVisibility(View.VISIBLE);
  }

  @Override public void close(boolean withError) {
    if (itemAlreadyOwnedPurchase != null) {
      BillingMapper billingMapper = new BillingMapper();
      Bundle bundle = billingMapper.mapAlreadyOwned(itemAlreadyOwnedPurchase);
      itemAlreadyOwnedPurchase = null;
      iabView.finish(bundle);
    } else {
      iabView.close(withError);
    }
  }

  @Override public void closeWithBillingUnavailable() {
    iabView.closeWithBillingUnavailable();
  }

  @Override public void showAlertNoBrowserAndStores() {
    iabView.showAlertNoBrowserAndStores();
  }

  @Override public void redirectToWalletInstallation(Intent intent) {
    iabView.redirectToWalletInstallation(intent);
  }

  @Override public void navigateToAdyen(String paymentMethod) {
    if (walletGenerationModel.getWalletAddress() != null && skuDetailsModel != null) {
      iabView.navigateToAdyen(paymentMethod, walletGenerationModel.getWalletAddress(),
          walletGenerationModel.getSignature(), skuDetailsModel.getFiatPrice(),
          skuDetailsModel.getFiatPriceCurrencyCode(), skuDetailsModel.getAppcPrice(),
          buyItemProperties.getSku());
    } else {
      showError();
    }
  }

  @Override public void resumeAdyenTransaction(String paymentMethod, String uid) {
    if (walletGenerationModel.getWalletAddress() != null && skuDetailsModel != null) {
      iabView.resumeAdyenTransaction(paymentMethod, walletGenerationModel.getWalletAddress(),
          walletGenerationModel.getSignature(), skuDetailsModel.getFiatPrice(),
          skuDetailsModel.getFiatPriceCurrencyCode(), skuDetailsModel.getAppcPrice(),
          buyItemProperties.getSku(), uid);
    } else {
      showError();
    }
  }

  @Override public void setRadioButtonSelected(String radioButtonSelected) {
    selectedRadioButton = radioButtonSelected;
    layout.selectRadioButton(radioButtonSelected);
  }

  @Override public void setPositiveButtonText(String selectedRadioButton) {
    Button positiveButton = layout.getPositiveButton();
    if (selectedRadioButton.equals(INSTALL_WALLET)) {
      positiveButton.setText(translations.getString(install_button));
    } else {
      positiveButton.setText(translations.getString(next_button));
    }
  }

  @Override public void saveWalletInformation(WalletGenerationModel walletGenerationModel) {
    this.walletGenerationModel = walletGenerationModel;
  }

  @Override public void addPayment(String name) {
    if (name.equalsIgnoreCase(CREDIT_CARD)) {
      layout.getCreditCardWrapperLayout()
          .setVisibility(View.VISIBLE);
    } else if (name.equalsIgnoreCase(PAYPAL)) {
      layout.getPaypalWrapperLayout()
          .setVisibility(View.VISIBLE);
    }
  }

  @Override public void showPaymentView() {
    if (selectedRadioButton == null) {
      setInitialRadioButtonSelected();
    } else {
      setRadioButtonSelected(selectedRadioButton);
    }
    layout.getIntentLoadingView()
        .setVisibility(View.INVISIBLE);
    layout.getPaymentMethodsLayout()
        .setVisibility(View.VISIBLE);
    layout.getDialogLayout()
        .setVisibility(View.VISIBLE);
    layout.getPositiveButton()
        .setEnabled(true);
  }

  @Override public void showBonus(int bonus) {
    TextView bonusText = layout.getInstallSecondaryText();
    if (bonus > 0) {
      bonusText.setText(
          String.format(translations.getString(iab_pay_with_wallet_reward_title), bonus));
      bonusText.setVisibility(View.VISIBLE);
    } else if (bonus == -1) { //-1 -> Request fail code
      bonusText.setText(translations.getString(iab_pay_with_wallet_reward_no_connection_body));
      bonusText.setVisibility(View.VISIBLE);
    }
  }

  @Override public void showInstallDialog() {
    iabView.navigateToInstallDialog();
  }

  @Override public void showItemAlreadyOwnedError(SkuPurchase skuPurchase) {
    itemAlreadyOwnedPurchase = skuPurchase;
    iabView.disableBack();
    layout.setErrorMessage(translations.getString(purchase_error_item_owned));
    showError();
  }

  @Override public void hideDialog() {
    layout.getDialogLayout()
        .setVisibility(View.INVISIBLE);
    layout.getIntentLoadingView()
        .setVisibility(View.VISIBLE);
  }

  @Override public void hideInstallOption() {
    layout.getInstallWrapperLayout()
        .setVisibility(View.GONE);
  }

  @Override
  public void redirectToSupportEmail(String packageName, String sku, String sdkVersionName,
      int mobileVersion) {
    String appName = layout.getAppNameView()
        .getText()
        .toString();
    if (walletGenerationModel != null) {
      EmailInfo emailInfo =
          new EmailInfo(walletGenerationModel.getWalletAddress(), packageName, sku, sdkVersionName,
              mobileVersion, appName);
      iabView.redirectToSupportEmail(emailInfo);
    }
  }

  @Override public void sendPurchaseStartEvent(String appcPrice) {
    iabView.sendPurchaseStartEvent(appcPrice);
  }

  private void setInitialRadioButtonSelected() {
    if (isVisible(layout.getCreditCardWrapperLayout())) {
      selectedRadioButton = CREDIT_CARD;
    } else if (isVisible(layout.getPaypalWrapperLayout())) {
      selectedRadioButton = PAYPAL;
    } else if (isVisible(layout.getInstallWrapperLayout())) {
      selectedRadioButton = INSTALL_WALLET;
      layout.getPositiveButton()
          .setText(translations.getString(install_button));
    }
    layout.selectRadioButton(selectedRadioButton);
  }

  private void attach(Context context) {
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("PaymentMethodsFragment must be attached to IabActivity");
    }
    this.context = context;
    iabView = (IabView) context;
  }

  /*private void makeTheStoredPurchase() {
    Bundle intent = appcoinsBillingStubHelper.getBuyIntent(buyItemProperties.getApiVersion(),
        buyItemProperties.getPackageName(), buyItemProperties.getSku(), buyItemProperties.getType(),
        buyItemProperties.getDeveloperPayload()
            .getRawPayload());

    PendingIntent pendingIntent = intent.getParcelable(KEY_BUY_INTENT);
    layout.getIntentLoadingView()
        .setVisibility(View.INVISIBLE);
    if (pendingIntent != null) {
      iabView.startIntentSenderForResult(pendingIntent.getIntentSender(),
          IabActivity.LAUNCH_INSTALL_BILLING_FLOW_REQUEST_CODE);
    } else {
      iabView.finishWithError();
    }
  }*/

  private boolean isVisible(View view) {
    return view.getVisibility() == View.VISIBLE;
  }

  public class RadioButtonClickListener implements View.OnClickListener {

    private String selectedRadioButton;

    RadioButtonClickListener(String selectedRadioButton) {
      this.selectedRadioButton = selectedRadioButton;
    }

    @Override public void onClick(View view) {
      paymentMethodsPresenter.onRadioButtonClicked(selectedRadioButton);
    }
  }
}
