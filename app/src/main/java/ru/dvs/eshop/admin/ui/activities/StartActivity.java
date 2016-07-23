package ru.dvs.eshop.admin.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ru.dvs.eshop.R;
import ru.dvs.eshop.admin.data.network.POSTQuery;
import ru.dvs.eshop.admin.utils.Encode;

public class StartActivity extends AppCompatActivity {
    private LinearLayout siteLayout, userLayout, responseLayout;
    private String site, email, pass_md5;
    private int curAction;
    private ImageView responseImage;
    private TextView responseText;
    private ImageButton backButton;

    private String token;

    private BroadcastReceiver pingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra("status", -1);
            String response = intent.getStringExtra("response");

            if (status == 0) {
                siteLayout.setVisibility(View.GONE);
                userLayout.setVisibility(View.VISIBLE);
                curAction = 1;
                unregisterReceiver(pingReceiver);
            } else {
                switch (Integer.parseInt(response)) {

                }
                siteLayout.setVisibility(View.GONE);
                responseLayout.setVisibility(View.VISIBLE);
                setResponseData(R.drawable.reject, R.string.start_ping_failed, false);
            }
        }
    };

    private BroadcastReceiver loginReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra("status", -1);
            String response = intent.getStringExtra("response");

            if (status == 0 && Encode.isValidMD5(response)) {
                token = response;
                userLayout.setVisibility(View.GONE);
                responseLayout.setVisibility(View.VISIBLE);
                curAction = 2;
                setResponseData(R.drawable.wait, R.string.start_wait_for_token, true);
                unregisterReceiver(loginReceiver);
            } else {
                switch (Integer.parseInt(response)) {

                }
                userLayout.setVisibility(View.GONE);
                responseLayout.setVisibility(View.VISIBLE);
                setResponseData(R.drawable.reject, R.string.start_login_failed, false);
            }
        }
    };

    private BroadcastReceiver checkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra("status", -1);
            String response = intent.getStringExtra("response");

            if (status == 0) {
                curAction = 2;

                try {
                    JSONObject node = new JSONObject(response);
                    String status_text = node.getString("status_text");
                    Log.e("checkReceiver", "status_text = " + status_text);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                setResponseData(R.drawable.wait, R.string.start_wait_for_token, true);
            } else {
                switch (Integer.parseInt(response)) {

                }
                userLayout.setVisibility(View.GONE);
                responseLayout.setVisibility(View.VISIBLE);
                setResponseData(R.drawable.reject, R.string.start_login_failed, false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        siteLayout = (LinearLayout) findViewById(R.id.site_layout);
        userLayout = (LinearLayout) findViewById(R.id.user_layout);
        responseLayout = (LinearLayout) findViewById(R.id.response_layout);
        responseImage = (ImageView) findViewById(R.id.response_image);
        responseText = (TextView) findViewById(R.id.response_text);
        backButton = (ImageButton) findViewById(R.id.back_button);
        siteLayout.setVisibility(View.VISIBLE);
        curAction = 0;
        Button next1 = (Button) findViewById(R.id.start_next1_button);
        Button next2 = (Button) findViewById(R.id.start_next2_button);
        Button next3 = (Button) findViewById(R.id.start_next3_button);
        if (next1 != null)
            next1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pingAndSwipeNext();
                }
            });
        if (next2 != null)
            next2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loginAndSwipeNext();
                }
            });
        if (next3 != null)
            next3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    responseAndSwipeNext();
                }
            });
        if (backButton != null)
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
    }

    private void pingAndSwipeNext() {
        EditText siteEditText = (EditText) findViewById(R.id.site);
        if (siteEditText != null) {
            site = siteEditText.getText().toString();
            POSTQuery task = new POSTQuery(this, site, "ping");
            task.put("controller", "api");
            task.put("method", "ping");
            task.execute();
        }
    }

    private void loginAndSwipeNext() {
        EditText emailEditText = (EditText) findViewById(R.id.email);
        EditText passEditText = (EditText) findViewById(R.id.password);
        if (emailEditText != null && passEditText != null) {
            email = emailEditText.getText().toString();
            pass_md5 = Encode.MD5(passEditText.getText().toString());

            POSTQuery task = new POSTQuery(this, site, "login");
            task.put("controller", "users");
            task.put("method", "login_api");
            task.put("email", email);
            task.put("pass", pass_md5);
            task.execute();
        }
    }

    private void setResponseData(int img_id, int str_id, boolean is_next_visible) {
        responseImage.setImageDrawable(this.getResources().getDrawable(img_id));
        responseText.setText(str_id);
        //responseNext.setVisibility(is_next_visible ? View.VISIBLE : View.GONE);
    }

    private void responseAndSwipeNext() {
        POSTQuery task = new POSTQuery(this, site, "check", token);
        task.put("controller", "api");
        task.put("method", "get_token_info");
        task.execute();
    }

    //При нажатии кнопки назад
    @Override
    public void onBackPressed() {
        if (curAction == -1)
            super.onBackPressed();
        responseLayout.setVisibility(View.GONE);
        switch (curAction) {
            case 0:
                siteLayout.setVisibility(View.VISIBLE);
                break;
            case 1:
                userLayout.setVisibility(View.VISIBLE);
                break;
        }
        curAction--;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(pingReceiver, new IntentFilter("ping"));
        registerReceiver(loginReceiver, new IntentFilter("login"));
        registerReceiver(checkReceiver, new IntentFilter("check"));

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(pingReceiver);
        unregisterReceiver(loginReceiver);
        unregisterReceiver(checkReceiver);
    }

}
