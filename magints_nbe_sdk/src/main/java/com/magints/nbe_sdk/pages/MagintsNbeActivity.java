package com.magints.nbe_sdk.pages;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.magints.nbe_sdk.R;
import com.magints.nbe_sdk.databinding.ActivityMagintsNbeBinding;
import com.magints.nbe_sdk.network.ApiController;
import com.magints.nbe_sdk.utils.Config;
import com.mastercard.gateway.android.sdk.Gateway;

public class MagintsNbeActivity extends BaseActivity {

    ActivityMagintsNbeBinding binding;
    TextChangeListener textChangeListener = new TextChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMagintsNbeBinding.inflate(LayoutInflater.from(this)); //initializing the binding class
        setContentView(binding.getRoot());

        binding.merchantId.setText(Config.MERCHANT_ID.getValue(this));
        binding.merchantId.addTextChangedListener(textChangeListener);

        binding.region.setText(Config.REGION.getValue(this));
        binding.region.addTextChangedListener(textChangeListener);
        binding.region.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.region.clearFocus();
                showRegionPicker();
            }
        });

        binding.merchantUrl.setText(Config.MERCHANT_URL.getValue(this));
        binding.merchantUrl.addTextChangedListener(textChangeListener);

        binding.processPaymentButton.setOnClickListener(v -> goTo(ProcessPaymentActivity.class));

        enableButtons();
    }

    void goTo(Class klass) {
        Intent i = new Intent(this, klass);
        startActivity(i);
    }

    void persistConfig() {
        Config.MERCHANT_ID.setValue(this, binding.merchantId.getText().toString());
        Config.REGION.setValue(this, binding.region.getText().toString());
        Config.MERCHANT_URL.setValue(this, binding.merchantUrl.getText().toString());

        // update api controller url
        ApiController.getInstance().setMerchantServerUrl(Config.MERCHANT_URL.getValue(this));
    }

    void enableButtons() {
        boolean enabled = !TextUtils.isEmpty(binding.merchantId.getText())
                && !TextUtils.isEmpty(binding.region.getText())
                && !TextUtils.isEmpty(binding.merchantUrl.getText());

        binding.processPaymentButton.setEnabled(enabled);
    }

    void showRegionPicker() {
        Gateway.Region[] regions = Gateway.Region.values();
        final String[] items = new String[regions.length + 1];
        items[0] = getString(R.string.none);
        for (int i = 0; i < regions.length; i++) {
            items[i + 1] = regions[i].name();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.main_select_region)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        binding.region.setText("");
                    } else {
                        binding.region.setText(items[which]);
                    }
                    dialog.cancel();
                })
                .show();
    }

    class TextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            enableButtons();
            persistConfig();
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    }
}
