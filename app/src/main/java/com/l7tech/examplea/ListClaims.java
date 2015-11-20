package com.l7tech.examplea;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.l7tech.examplea.multiuser.SimpleUserStorage;
import com.l7tech.examplea.util.MssoResponseReceiver;
import com.l7tech.msso.MobileSso;
import com.l7tech.msso.MobileSsoFactory;

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
import java.util.List;

public class ListClaims extends Activity implements AsyncResponse {

    Handler handler;
    private MobileSso mobileSso;
    MyResponseReceiver receiver = new MyResponseReceiver();
    String jsonClaims;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handler = new Handler(Looper.getMainLooper());
        super.onCreate(savedInstanceState);
        receiver.delegate = this;
        mobileSso = MobileSsoFactory.getInstance(this);

        getClaimsInfo();


    }


    private List<Object> getClaimsInfo() {

        URI claimsURI = null;
        try {
            claimsURI = new URI("http://explore.apim.ca:8080/insurance/claimshistory?username=aranw@test.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        mobileSso.processRequest(new HttpGet(claimsURI), receiver);

        return null;
    }


    @Override
    public void processFinish(final String json) {

        handler.post(new Runnable() {
            public void run() {
                JSONObject jsonObject = null;
                List<Object> objects = new ArrayList<Object>();
                try {
                    jsonObject = (JSONObject) new JSONTokener(json).nextValue();
                    jsonClaims = json ;
                    JSONArray jsonArray = jsonObject.getJSONArray("claims");
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        //JSONObject item = parsed.getJSONObject(i);
                        JSONObject claim = (JSONObject) jsonArray.get(i);

                        Integer id = new Integer((String) claim.get("claimnumber"));
                        String name = (String) claim.get("details");
                        objects.add(new Pair<Integer, String>(id, name) {
                            @Override
                            public String toString() {
                                return first + "  " + second;
                            }
                        });
                    }
                    setContentView(R.layout.activity_list_claims);
                    ListView itemList = itemList = (ListView) findViewById(R.id.listClaimsView);
                    View header = getLayoutInflater().inflate(R.layout.claimlistheader, null);
                    itemList.addHeaderView(header);
                    itemList.setAdapter(new ArrayAdapter<Object>(ListClaims.this, R.layout.listitem, objects));
                    itemList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                            //Toast.makeText(ListClaims.this, "huhu", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), ClaimDetails.class);
                            intent.putExtra("claims", jsonClaims);
                            intent.putExtra("claim", position);
                            startActivity(intent);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

}
