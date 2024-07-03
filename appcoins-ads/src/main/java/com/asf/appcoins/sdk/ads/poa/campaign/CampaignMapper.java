package com.asf.appcoins.sdk.ads.poa.campaign;

import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;

import android.os.Bundle;
import android.util.Log;
import com.asf.appcoins.sdk.ads.network.responses.AppCoinsClientResponse;
import com.asf.appcoins.sdk.ads.repository.ResponseCode;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CampaignMapper {

  public static Campaign mapCampaign(AppCoinsClientResponse response) {
    String jsonResponse = response.getMsg();
    String bigIdString = GetBigIntValue("bidId", jsonResponse);
    if (!bigIdString.isEmpty()) {
      BigInteger bigId = new BigInteger(bigIdString);
      String packageName = GetStringValue("packageName", jsonResponse);
      if (!packageName.isEmpty()) {
        Campaign campaign = new Campaign(bigId, packageName);
        return campaign;
      }
    }
    return new Campaign( new BigInteger(Integer.toString(Campaign.INVALID_CAMPAIGN)) , "");
  }

  public static String GetBigIntValue(String paramName, String response) {

    String patternStr = "(?:\"" + paramName + "\"" + "[\\s]*:[\\s]*)([\\d]*)";

    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(response);
    boolean found = matcher.find();

    if (found) {
      String val = matcher.group(1);
      return val;
    }

    return "";
  }

  private static String GetStringValue(String paramName, String response) {

    String patternStr = "(?:\"" + paramName + "\"" + "[\\s]*:[\\s]*)(\".*?\")";

    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(response);
    boolean found = matcher.find();

    if (found) {
      String val = matcher.group(1);
      val = val.replaceAll("\"", "");
      return val;
    }

    return "";
  }

  public static Campaign mapCampaignFromBundle(Bundle response) {

    try {
      int responseCode = response.getInt(RESPONSE_CODE);
      if (responseCode == ResponseCode.OK.getValue()) {
        return new Campaign(new BigInteger(response.getString("CAMPAIGN_ID")), "");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    Log.d(CampaignMapper.class.getName(), "No campaign is available.");
    return new Campaign(new BigInteger(Integer.toString(Campaign.INVALID_CAMPAIGN)), "");
  }
}

