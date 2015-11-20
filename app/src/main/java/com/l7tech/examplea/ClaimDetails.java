package com.l7tech.examplea;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ClaimDetails extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.claimdetail);
        String json = getIntent().getStringExtra("claims");
        int position = getIntent().getIntExtra("claim", 0);

        JSONObject jsonObject = null;
        try {
            jsonObject = (JSONObject) new JSONTokener(json).nextValue();

            JSONArray jsonArray = (JSONArray)jsonObject.getJSONArray("claims");
            JSONObject obj = (JSONObject)jsonArray.get(position-1);
            ((EditText) findViewById(R.id.claimNumber)).setText(obj.getString("claimnumber"));
            ((EditText) findViewById(R.id.claimCode)).setText(obj.getString("claimcode"));
            ((EditText) findViewById(R.id.claimDate)).setText(obj.getString("claimdate"));
            ((EditText) findViewById(R.id.claimStatus)).setText(obj.getString("status"));
            ((EditText) findViewById(R.id.claimType)).setText(obj.getString("type"));
            ((EditText) findViewById(R.id.claimDetails)).setText(obj.getString("details"));
        } catch (Exception e) {
            e.printStackTrace();
        }

/*        ImageView imageView = (ImageView) findViewById(R.id.imageView3);
        imageView.setClickable(true);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

    }

}
