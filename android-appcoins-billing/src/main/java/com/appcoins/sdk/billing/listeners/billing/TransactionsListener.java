package com.appcoins.sdk.billing.listeners.billing;

import com.appcoins.sdk.billing.models.TransactionsListModel;

public interface TransactionsListener {

    void onResponse(TransactionsListModel transactionsListModel);
}
