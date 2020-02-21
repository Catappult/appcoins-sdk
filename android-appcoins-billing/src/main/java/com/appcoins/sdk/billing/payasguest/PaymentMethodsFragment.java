package com.appcoins.sdk.billing.payasguest;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.SharedPreferencesRepository;
import com.appcoins.sdk.billing.WalletInteract;
import com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper;
import com.appcoins.sdk.billing.helpers.WalletInstallationIntentBuilder;
import com.appcoins.sdk.billing.layouts.PaymentMethodsFragmentLayout;
import com.appcoins.sdk.billing.models.WalletGenerationModel;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.service.wallet.WalletGenerationMapper;
import com.appcoins.sdk.billing.service.wallet.WalletRepository;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class PaymentMethodsFragment extends Fragment implements PaymentMethodsView {

  public static String CREDIT_CARD_RADIO = "credit_card";
  public static String PAYPAL_RADIO = "paypal";
  public static String INSTALL_RADIO = "install";
  private static String SELECTED_RADIO_KEY = "selected_radio";
  private IabView iabView;
  private BuyItemProperties buyItemProperties;
  private PaymentMethodsPresenter paymentMethodsPresenter;
  private PaymentMethodsFragmentLayout layout;
  private String selectedRadioButton;
  private SkuDetailsModel skuDetailsModel;
  private WalletGenerationModel walletGenerationModel;

  public static PaymentMethodsFragment newInstance(BuyItemProperties buyItemProperties) {
    PaymentMethodsFragment paymentMethodsFragment = new PaymentMethodsFragment();
    Bundle bundle = new Bundle();
    bundle.putSerializable(AppcoinsBillingStubHelper.BUY_ITEM_PROPERTIES, buyItemProperties);
    paymentMethodsFragment.setArguments(bundle);
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

    BdsService bdsService = new BdsService(BuildConfig.BACKEND_BASE);

    SharedPreferencesRepository sharedPreferencesRepository =
        new SharedPreferencesRepository(getActivity());
    WalletRepository walletRepository =
        new WalletRepository(bdsService, new WalletGenerationMapper());
    WalletInteract walletInteract =
        new WalletInteract(new SharedPreferencesRepository(getActivity()), walletRepository);
    GamificationInteract gamificationInteract =
        new GamificationInteract(sharedPreferencesRepository, new GamificationMapper(), bdsService);
    PaymentMethodsRepository paymentMethodsRepository =
        new PaymentMethodsRepository(new BdsService(BuildConfig.HOST_WS));

    buyItemProperties = (BuyItemProperties) getArguments().getSerializable(
        AppcoinsBillingStubHelper.BUY_ITEM_PROPERTIES);
    paymentMethodsPresenter = new PaymentMethodsPresenter(this,
        new PaymentMethodsInteract(walletInteract, gamificationInteract, paymentMethodsRepository),
        new WalletInstallationIntentBuilder(getActivity().getPackageManager(),
            getActivity().getPackageName()));
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    layout = new PaymentMethodsFragmentLayout(getActivity(),
        getResources().getConfiguration().orientation, buyItemProperties);

    return layout.build();
  }

  @Override public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    Button cancelButton = layout.getCancelButton();
    Button positiveButton = layout.getPositiveButton();
    RadioButton creditCardButton = layout.getCreditCardRadioButton();
    RadioButton paypalButton = layout.getPaypalRadioButton();
    RadioButton installRadioButton = layout.getInstallRadioButton();
    RelativeLayout creditWrapper = layout.getCreditCardWrapperLayout();
    RelativeLayout paypalWrapper = layout.getPaypalWrapperLayout();
    RelativeLayout installWrapper = layout.getInstallWrapperLayout();
    Button errorButton = layout.getErrorPositiveButton();
    onRotation(savedInstanceState);
    onCancelButtonClicked(cancelButton);
    onPositiveButtonClicked(positiveButton);
    onErrorButtonClicked(errorButton);
    onRadioButtonClicked(creditCardButton, paypalButton, installRadioButton, creditWrapper,
        paypalWrapper, installWrapper);
    paymentMethodsPresenter.prepareUi(buyItemProperties);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(SELECTED_RADIO_KEY, selectedRadioButton);
  }

  private void onErrorButtonClicked(Button errorButton) {
    errorButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        paymentMethodsPresenter.onErrorButtonClicked();
      }
    });
  }

  private void onRadioButtonClicked(RadioButton creditCardButton, RadioButton paypalButton,
      RadioButton installRadioButton, RelativeLayout creditWrapper, RelativeLayout paypalWrapper,
      RelativeLayout installWrapper) {
    RadioButtonClickListener creditCardListener =
        new RadioButtonClickListener(PaymentMethodsFragment.CREDIT_CARD_RADIO);
    RadioButtonClickListener paypalListener =
        new RadioButtonClickListener(PaymentMethodsFragment.PAYPAL_RADIO);
    RadioButtonClickListener installListener =
        new RadioButtonClickListener(PaymentMethodsFragment.INSTALL_RADIO);

    creditCardButton.setOnClickListener(creditCardListener);
    paypalButton.setOnClickListener(paypalListener);
    installRadioButton.setOnClickListener(installListener);

    creditWrapper.setOnClickListener(creditCardListener);
    paypalWrapper.setOnClickListener(paypalListener);
    installWrapper.setOnClickListener(installListener);
  }

  private void onPositiveButtonClicked(Button positiveButton) {
    positiveButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        paymentMethodsPresenter.onPositiveButtonClicked(selectedRadioButton);
      }
    });
  }

  private void onCancelButtonClicked(Button cancelButton) {
    cancelButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        paymentMethodsPresenter.onCancelButtonClicked();
      }
    });
  }

  private void onRotation(Bundle savedInstanceState) {
    if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_RADIO_KEY)) {
      selectedRadioButton = savedInstanceState.getString(SELECTED_RADIO_KEY);
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
    ProgressBar progressBar = layout.getProgressBar();
    RelativeLayout dialogLayout = layout.getDialogLayout();
    RelativeLayout errorLayout = layout.getErrorView();
    progressBar.setVisibility(View.INVISIBLE);
    dialogLayout.setVisibility(View.GONE);
    errorLayout.setVisibility(View.VISIBLE);
  }

  @Override public void close() {
    iabView.close();
  }

  @Override public void showAlertNoBrowserAndStores() {
    iabView.showAlertNoBrowserAndStores();
  }

  @Override public void redirectToWalletInstallation(Intent intent) {
    iabView.redirectToWalletInstallation(intent);
  }

  @Override public void navigateToAdyen(String selectedRadioButton) {
    iabView.navigateToAdyen(selectedRadioButton);
  }

  @Override public void setRadioButtonSelected(String radioButtonSelected) {
    selectedRadioButton = radioButtonSelected;
    layout.selectRadioButton(radioButtonSelected);
  }

  @Override public void setPositiveButtonText(String selectedRadioButton) {
    Button positiveButton = layout.getPositiveButton();
    if (selectedRadioButton.equals(PaymentMethodsFragment.INSTALL_RADIO)) {
      positiveButton.setText("INSTALL");
    } else {
      positiveButton.setText("NEXT");
    }
  }

  @Override public void saveWalletInformation(WalletGenerationModel walletGenerationModel) {
    this.walletGenerationModel = walletGenerationModel;
  }

  @Override public void addPayment(String name) {
    if (name.equalsIgnoreCase(CREDIT_CARD_RADIO)) {
      layout.getCreditCardWrapperLayout()
          .setVisibility(View.VISIBLE);
    } else {
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
    layout.getProgressBar()
        .setVisibility(View.INVISIBLE);
    layout.getPaymentMethodsLayout()
        .setVisibility(View.VISIBLE);
    layout.getPositiveButton()
        .setEnabled(true);
  }

  @Override public void showBonus(int bonus) {
    TextView bonusText = layout.getInstallSecondaryText();
    if (bonus > 0) {
      bonusText.setText(String.format("Get up to %s%% bonus", bonus));
      bonusText.setVisibility(View.VISIBLE);
    } else if (bonus == -1) { //-1 -> Request fail code
      bonusText.setText("Earn bonus with the purchase");
      bonusText.setVisibility(View.VISIBLE);
    }
  }

  private void setInitialRadioButtonSelected() {
    RadioButton creditCardButton = layout.getCreditCardRadioButton();
    RadioButton paypalButton = layout.getPaypalRadioButton();
    RadioButton installButton = layout.getInstallRadioButton();
    if (layout.getCreditCardWrapperLayout()
        .getVisibility() == View.VISIBLE) {
      creditCardButton.setChecked(true);
      selectedRadioButton = CREDIT_CARD_RADIO;
    } else if (layout.getPaypalWrapperLayout()
        .getVisibility() == View.VISIBLE) {
      paypalButton.setChecked(true);
      selectedRadioButton = PAYPAL_RADIO;
    } else if (layout.getInstallWrapperLayout()
        .getVisibility() == View.VISIBLE) {
      installButton.setChecked(true);
      layout.getPositiveButton()
          .setText("INSTALL");
      selectedRadioButton = INSTALL_RADIO;
    }
  }

  private void attach(Context context) {
    if (!(context instanceof IabView)) {
      throw new IllegalStateException("PaymentMethodsFragment must be attached to IabActivity");
    }
    iabView = (IabView) context;
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
