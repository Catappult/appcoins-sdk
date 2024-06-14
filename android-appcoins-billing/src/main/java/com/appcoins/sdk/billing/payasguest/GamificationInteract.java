package com.appcoins.sdk.billing.payasguest;

import com.appcoins.sdk.billing.mappers.GamificationMapper;
import com.appcoins.sdk.billing.models.GamificationModel;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.service.RequestResponse;
import com.appcoins.sdk.billing.service.ServiceResponseListener;
import com.appcoins.sdk.billing.sharedpreferences.BonusSharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;

class GamificationInteract {
  private final BonusSharedPreferences bonusSharedPreferences;
  private final GamificationMapper gamificationMapper;
  private final BdsService bdsService;

  GamificationInteract(BonusSharedPreferences bonusSharedPreferences,
                       GamificationMapper gamificationMapper, BdsService bdsService) {
    this.bonusSharedPreferences = bonusSharedPreferences;
    this.gamificationMapper = gamificationMapper;
    this.bdsService = bdsService;
  }

  void loadMaxBonus(final MaxBonusListener maxBonusListener) {
    if (bonusSharedPreferences.hasSavedBonus(System.currentTimeMillis())) {
      maxBonusListener.onBonusReceived(
          new GamificationModel(bonusSharedPreferences.getMaxBonus()));
    } else {
      ServiceResponseListener serviceResponseListener = new ServiceResponseListener() {
        @Override public void onResponseReceived(RequestResponse requestResponse) {
          GamificationModel gamificationModel = gamificationMapper.mapToMaxBonus(requestResponse);
          maxBonusListener.onBonusReceived(gamificationModel);
        }
      };
      bdsService.makeRequest("/gamification/levels", "GET", new ArrayList<String>(),
          new HashMap<String, String>(), new HashMap<String, String>(),
          new HashMap<String, Object>(), serviceResponseListener);
    }
  }

  public void cancelRequests() {
    bdsService.cancelRequests();
  }

  void setMaxBonus(int maxBonus) {
    bonusSharedPreferences.setMaxBonus(maxBonus);
  }
}
