/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */

package com.l7tech.examplea;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.Pair;
import android.view.*;
import android.widget.*;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.l7tech.examplea.enterprise.ExampleApp;
import com.l7tech.examplea.multiuser.SimpleUserStorage;
import com.l7tech.examplea.util.MssoResponseReceiver;
import com.l7tech.msso.EnterpriseApp;
import com.l7tech.msso.MobileSso;
import com.l7tech.msso.MobileSsoFactory;
import com.l7tech.msso.auth.NfcResultReceiver;
import com.l7tech.msso.auth.ble.BluetoothLe;
import com.l7tech.msso.auth.ble.BluetoothLeConsentHandler;
import com.l7tech.msso.auth.ble.BluetoothLePeripheralCallback;
import com.l7tech.msso.service.MssoIntents;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExampleActivity extends FragmentActivity {
    private static final String TAG = "insure.CA";


    private static final String STATE_PROGRESS_VISIBILITY = "exampleActivity.progressVisibility";

    private static final int MENU_GROUP_LOGOUT = 66;
    private static final int MENU_ITEM_LOG_OUT = 3;
    private static final int MENU_ITEM_LOG_OUT_CLIENT_ONLY = 1;
    private static final int MENU_ITEM_REMOVE_DEVICE_REGISTRATION = 4;
    private static final int MENU_ITEM_DESTROY_TOKEN_STORE = 2;

    //Application endpoint
    private URI productListDownloadUri = null;

    private  URI userInfoUri = null;

    private String userName ="" ;

    ListView itemList;
    ProgressBar progressBar;
    static boolean usedMobileSso = false;

    private MobileSso mobileSso() {
        //Initialize the MobileSso with the configuration defined under /assets/msso_config.json
        MobileSso mobileSso = MobileSsoFactory.getInstance(this);
        usedMobileSso = true;
        return mobileSso;
    }

    /**
     * Called when the activity is first created.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/BalsamiqSansRegular.ttf");

        setContentView(R.layout.main);
        Button button = (Button) findViewById(R.id.button);
        button.setTypeface(tf);
        button = (Button) findViewById(R.id.button2);
        button.setTypeface(tf);
        button = (Button) findViewById(R.id.button3);
        button.setTypeface(tf);

        //itemList = (ListView) findViewById(R.id.itemList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (savedInstanceState != null) {
            //progressBar.setVisibility(savedInstanceState.getInt(STATE_PROGRESS_VISIBILITY));
        }

        final Button logOutButton = (Button) findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doServerLogout();
            }
        });
        registerForContextMenu(logOutButton);

     }

    public void listClaimsHistory(View v)
    {
        Intent intent = new Intent(getApplicationContext(), ListClaims.class);
        intent.putExtra("username", userName);
        startActivity(intent);
    }

    /**
     * Access /userinfo endpoint to retrieve the user info and store username to local storage.
     */
    private void getUserInfo() {

        mobileSso().processRequest(new HttpGet(userInfoUri), new MssoResponseReceiver() {
            @Override
            public void onSuccess(HttpResponse response) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        String s = EntityUtils.toString(response.getEntity());
                        JSONObject jsonObject = new JSONObject(s);
                        String username = jsonObject.getString("preferred_username");
                        userName = username ;
                        (new SimpleUserStorage(ExampleActivity.this)).add(username);
                        showMessage("Successful Login: " + username, Toast.LENGTH_SHORT);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to parse JSON response for /userinfo endpoint", e);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage) {
                showMessage("Login Failed: " + errorMessage, Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_LOG_OUT, Menu.NONE, "Log Out");
        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_LOG_OUT_CLIENT_ONLY, Menu.NONE, "Log Out (client only)");
        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_REMOVE_DEVICE_REGISTRATION, Menu.NONE, "Unregister Device");
        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_DESTROY_TOKEN_STORE, Menu.NONE, "Destroy Token Store");
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (66 != item.getGroupId())
            return false;
        switch (item.getItemId()) {
            case 1:
                mobileSso().logout(false);
                showMessage("Logged Out (client only)", Toast.LENGTH_SHORT);
                return true;
            case 2:
                mobileSso().destroyAllPersistentTokens();
                showMessage("Device Registration Destroyed (client only)", Toast.LENGTH_SHORT);
                return true;
            case 3:
                doServerLogout();
                return true;
            case 4:
                doServerUnregisterDevice();
                return true;
        }
        return false;
    }

    // Log the user out of all client apps and notify the server to revoke tokens.
    private void doServerLogout() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mobileSso().logout(true);
                    getUserInfo();
                    showMessage("Logged Out Successfully", Toast.LENGTH_SHORT);
                } catch (Exception e) {
                    String msg = "Server Logout Failed: " + e.getMessage();
                    Log.e(TAG, msg, e);
                    showMessage(msg, Toast.LENGTH_SHORT);
                }
                return null;
            }
        }.execute((Void) null);
    }

    // Tell the token server to un-register this device, without affecting the client-side token caches in any way.
    private void doServerUnregisterDevice() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    mobileSso().removeDeviceRegistration();
                    showMessage("Server Registration Removed for This Device", Toast.LENGTH_LONG);
                } catch (Exception e) {
                    String msg = "Server Device Removal Failed: " + e.getMessage();
                    Log.e(TAG, msg, e);
                    showMessage(msg, Toast.LENGTH_LONG);
                }
                return null;
            }
        }.execute((Void) null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PROGRESS_VISIBILITY, progressBar.getVisibility());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (usedMobileSso)
            mobileSso().processPendingRequests();

        if (!mobileSso().isLogin()) {
            getUserInfo();
        }

        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mobileSso().stopBleSessionSharing();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void showMessage(final String message, final int toastLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExampleActivity.this, message, toastLength).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.scanQRCode:
                IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                intentIntegrator.initiateScan();
                break;
            case R.id.clearUsers:
                (new SimpleUserStorage(this)).clear();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * Process remote login through QRCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Got the QR Code, perform the remote login request.
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String r = scanResult.getContents();
            if (r != null) {
                mobileSso().authorize(r, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode != MssoIntents.RESULT_CODE_SUCCESS) {
                            showMessage(resultData.getString(MssoIntents.RESULT_ERROR_MESSAGE), Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Process remote login through NFC
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        // record 0 contains the MIME type, record 1 is the AAR, if present
        String authRequest = new String(msg.getRecords()[0].getPayload());

        //Authorize session request
        mobileSso().authorize(authRequest, new NfcResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode != MssoIntents.RESULT_CODE_SUCCESS) {
                    showMessage(resultData.getString(MssoIntents.RESULT_ERROR_MESSAGE), Toast.LENGTH_LONG);
                }
            }
        });
    }
}

