package com.app.grs.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.grs.R;
import com.app.grs.adapter.CartAdapter;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.libizo.CustomEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CheckoutActivity extends AppCompatActivity {

    private HashMap<String, String> data;
    String proid = "", proname = "", proqty = "", totalprice;
    CustomEditText dname, dphone, demail, dstate, dcity, dpostcode, daddress, bname, bemail, bphone, bstate, bcity, bpostcode, baddress;
    Button btnOrder;
    private Dialog dialog;
    String subtotal;
    CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Shipping Details");
        setContentView(R.layout.activity_checkout);

        Constants.pref = getSharedPreferences("GRS", Context.MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

       /* data = (HashMap<String, String>) getIntent().getExtras().get("data");
        proid = data.get("id");
        proname = data.get("product");
        proqty = data.get("qty");*/

        totalprice = getIntent().getStringExtra("totalprice");

        dname = findViewById(R.id.del_name);
        dphone = findViewById(R.id.del_phone);
        demail = findViewById(R.id.del_email);
        dstate = findViewById(R.id.del_state);
        dcity = findViewById(R.id.del_city);
        dpostcode = findViewById(R.id.del_d_code);
        daddress = findViewById(R.id.del_address);
        bname = findViewById(R.id.bil_name);
        bphone = findViewById(R.id.bil_phone);
        bemail = findViewById(R.id.bil_email);
        bstate = findViewById(R.id.bil_state);
        bcity = findViewById(R.id.bil_city);
        bpostcode = findViewById(R.id.bil_b_post);
        baddress = findViewById(R.id.bil_address);
        btnOrder = findViewById(R.id.btn_del_details);

        Bundle bundle = getIntent().getExtras();

        final String id = bundle.getString("cus_id");
        String b_name = bundle.getString("name");
        String b_phone = bundle.getString("mobile_no");
        String b_email = bundle.getString("email");
        String b_state = bundle.getString("state");
        String b_city = bundle.getString("city");
        String b_post = bundle.getString("post_code");
        String b_add = bundle.getString("address1");
        final String cart_id = bundle.getString("cartid");
        subtotal = bundle.getString("subtotal");

        bname.setText(b_name);
        bphone.setText(b_phone);
        bemail.setText(b_email);
        bstate.setText(b_state);
        bcity.setText(b_city);
        bpostcode.setText(b_post);
        baddress.setText(b_add);
        dname.setText(b_name);
        dphone.setText(b_phone);
        demail.setText(b_email);
        dstate.setText(b_state);
        dcity.setText(b_city);
        dpostcode.setText(b_post);
        daddress.setText(b_add);

        final String cusid = Constants.pref.getString("mobileno", "");

        btnOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String d_name = dname.getText().toString().trim();
                String d_phone = dphone.getText().toString().trim();
                String d_email = demail.getText().toString().trim();
                String d_state = dstate.getText().toString().trim();
                String d_city = dcity.getText().toString().trim();
                String d_post = dpostcode.getText().toString().trim();
                String d_add = daddress.getText().toString().trim();
                String b_name = bname.getText().toString().trim();
                String b_phone = bphone.getText().toString().trim();
                String b_email = bemail.getText().toString().trim();
                String b_state = bstate.getText().toString().trim();
                String b_city = bcity.getText().toString().trim();
                String b_post = bpostcode.getText().toString().trim();
                String b_add = baddress.getText().toString().trim();

                new shipDetails(CheckoutActivity.this, id, cusid, cart_id, d_name, d_phone, d_email, d_state, d_city, d_post, d_add,
                        b_name, b_phone, b_email, b_state, b_city, b_post, b_add).execute();

            }
        });


    }



    private class shipDetails extends AsyncTask<String, Integer, String> {

        private Context context;
        private String id, cusid, cartid, dname, dphone, demail, dstate, dcity, dpost, dadd, bname, bphone, bemail, bstate, bcity, bpost, badd;
        private String url = Constants.BASE_URL + Constants.SHIP_DETAILS;
        ProgressDialog progress;

        public shipDetails(Context context, String id, String cusid, String cartid, String dname, String dphone, String demail, String dstate, String dcity, String dpost, String dadd, String bname, String bphone, String bemail, String bstate, String bcity, String bpost, String badd) {
            this.context = context;
            this.id = id;
            this.cusid = cusid;
            this.cartid = cartid;
            this.dname = dname;
            this.dphone = dphone;
            this.demail = demail;
            this.dstate = dstate;
            this.dcity = dcity;
            this.dpost = dpost;
            this.dadd = dadd;
            this.bname = bname;
            this.bphone = bphone;
            this.bemail = bemail;
            this.bstate = bstate;
            this.bcity = bcity;
            this.bpost = bpost;
            this.badd = badd;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("id", id)
                    .add("customer_id", cusid)
                    .add("cartid", cartid)
                    .add("dname", dname)
                    .add("dphone", dphone)
                    .add("demail", demail)
                    .add("dstate", dstate)
                    .add("dcity", dcity)
                    .add("dpost", dpost)
                    .add("dadd", dadd)
                    .add("bname", bname)
                    .add("bphone", bphone)
                    .add("bemail", bemail)
                    .add("bstate",bstate)
                    .add("bcity", bcity)
                    .add("bpost", bpost)
                    .add("badd", badd)
                    .add("payment_mode", "Cash")
                    .add("sum_price", subtotal)
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = client.newCall(request);

            try {
                response = call.execute();

                if (response.isSuccessful()) {
                    jsonData = response.body().string();
                } else {
                    jsonData = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return jsonData;
        }

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);

            progress.dismiss();
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                if (jsonData != null) {
                    jonj = new JSONObject(jsonData);
                    if (jonj.getString("status").equalsIgnoreCase(
                            "success")) {

                        dialog = new Dialog(CheckoutActivity.this);
                        dialog.setContentView(R.layout.payment_dialog);
                        dialog.setCancelable(false);

                        TextView textView = dialog.findViewById(R.id.cash_tv);
                        TextView close = dialog.findViewById(R.id.close_tv);
                        Button confirm = dialog.findViewById(R.id.btn_confirm_order);
                        final RadioButton radioButton = dialog.findViewById(R.id.cash_rad);
                        if (radioButton.isChecked()){
                            textView.setTextColor(Color.parseColor("#69b418"));
                        }else {
                            textView.setTextColor(Color.parseColor("#808080"));
                        }
                        close.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (radioButton.isChecked()){
                                    startActivity(new Intent(CheckoutActivity.this, PaymentActivity.class));
                                    finish();
                                }
                            }
                        });

                    }else if (jonj.getString("status").equalsIgnoreCase(
                            "failed ")){
                        Toast.makeText(getApplicationContext(), jonj.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Something went Wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GRS.freeMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(CheckoutActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(CheckoutActivity.this);
    }
}
