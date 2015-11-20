/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */

package com.l7tech.examplea.logon;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.l7tech.examplea.R;
import com.l7tech.examplea.multiuser.SimpleUserStorage;
import com.l7tech.msso.auth.NFCRenderer;
import com.l7tech.msso.auth.QRCode;
import com.l7tech.msso.auth.QRCodeRenderer;
import com.l7tech.msso.auth.ble.BluetoothLeCentralCallback;
import com.l7tech.msso.auth.ble.BluetoothLeCentralRenderer;
import com.l7tech.msso.gui.AbstractLogonActivity;

import java.util.List;

/**
 * Example to show a Custom Logon Dialog.
 * Please make sure this activity is defined in the AndroidManifest.xml with
 * with intent filter com.l7tech.msso.service.action.OBTAIN_CREDENTIALS
 */
public class CustomLogonActivity extends AbstractLogonActivity {

    private static final String TAG = CustomLogonActivity.class.getCanonicalName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.customlogon);
        final Button logonButton = (Button) findViewById(R.id.btnlogin);
        if (isEnterpriseLoginEnabled()) {
            logonButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String username = ((EditText) findViewById(R.id.etxtname)).getText().toString();
                    final String password = ((EditText) findViewById(R.id.etxtpass)).getText().toString();

                    sendCredentialsIntent(username, password);

                    setResult(RESULT_OK);
                    finish();
                }
            });
        } else {
            logonButton.setEnabled(false);
        }


        final LinearLayout qr = (LinearLayout) findViewById(R.id.qrcode);

        //Enable touch less logon for QRCode, NFC and BlueToothLe
        enableQRCode();
        enableNFC();
        enableBle();

        //Retrieve the Login providers View (Social Login and QRCode)
        //There is no View for NFC and BLE
        List<View> providers = getProviders();

        GridLayout gridLayout = (GridLayout) findViewById(R.id.socialLoginGridLayout);

        //Display the View in Grid
        if (!providers.isEmpty()) {
            for (final View provider : providers) {
                if (provider instanceof ImageButton) {
                    gridLayout.addView(provider);
                } else if (provider instanceof QRCode) {
                    qr.addView(provider);
                }
            }
        }

        //Show previous logon user in Spinner for quick login
        Spinner usersSpinner = (Spinner) findViewById(R.id.usersSpinner);
        final String[] users = new SimpleUserStorage(this).getAll();
        if (users.length > 1) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_dropdown_item, users);
            usersSpinner.setAdapter(adapter);

            usersSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((EditText) findViewById(R.id.etxtname)).setText(users[position]);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    ((EditText) findViewById(R.id.etxtname)).setText("");
                }
            });
        } else {
            usersSpinner.setVisibility(View.INVISIBLE);
            if (users.length == 1) {
                ((EditText) findViewById(R.id.etxtname)).setText(users[0]);
            }
        }

    }

    private void enableQRCode() {
        final LinearLayout qr = (LinearLayout) findViewById(R.id.qrcode);
        addAuthRenderer((new QRCodeRenderer() {
            @Override
            public void onError(int code, final String m, Exception e) {
                super.onError(code, m, e);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, m, Toast.LENGTH_SHORT).show();
                        qr.removeAllViews();
                    }
                });
            }

            @Override
            protected long getDelay() {
                return 10;
            }

            @Override
            protected long getPollInterval() {
                //Not recommended to poll the server too often. Recommend to poll the server 5+s
                return 2;
            }

        }));
    }

    /**
     * Enable NFC Touch less login
     */
    private void enableNFC() {
        addAuthRenderer(new NFCRenderer() {

            @Override
            protected long getPollInterval() {
                return 2;
            }

            @Override
            protected long getMaxPollCount() {
                return 10;
            }

            @Override
            protected long getDelay() {
                return 3;
            }
            @Override
            public void onError(int code, String message, Exception e) {
                super.onError(code, message, e);
            }
        });
    }

    /**
     * Enable Bluetooth LE Touch less login
     */
    private void enableBle() {

        //prepare callback to receive status update.
        BluetoothLeCentralCallback callback = new BluetoothLeCentralCallback() {

            @Override
            public void onStatusUpdate(int state) {
                switch (state) {
                    case BluetoothLeCentralCallback.BLE_STATE_SCAN_STARTED:
                        Log.i(TAG, "Scan Started");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_SCAN_STOPPED:
                        Log.i(TAG, "Scan Stopped");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_DEVICE_DETECTED:
                        Log.i(TAG, "Device detected");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_CONNECTED:
                        Log.i(TAG, "Connected to Gatt Server");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_DISCONNECTED:
                        Log.i(TAG, "Disconnected from Gatt Server");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_SERVICE_DISCOVERED:
                        Log.i(TAG, "Service Discovered");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_CHARACTERISTIC_FOUND:
                        Log.i(TAG, "Characteristic Found");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_CHARACTERISTIC_WRITTEN:
                        Log.i(TAG, "Writing data to Characteristic... ");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_AUTH_SUCCEEDED:
                        Log.i(TAG, "Auth Succeeded");
                        break;
                    case BluetoothLeCentralCallback.BLE_STATE_AUTH_FAILED:
                        Log.i(TAG, "Auth Failed");
                        break;
                }

            }
        };

        addAuthRenderer(new BluetoothLeCentralRenderer(callback) {
            @Override
            public void onError(int code, final String message, Exception e) {
                super.onError(code, message, e);
                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            protected long getPollInterval() {
                return 2;
            }

            @Override
            protected long getMaxPollCount() {
                return 10;
            }

            @Override
            protected long getDelay() {
                return 3;
            }
        });
    }
}