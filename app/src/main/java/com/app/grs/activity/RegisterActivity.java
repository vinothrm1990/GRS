package com.app.grs.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.app.grs.R;
import com.app.grs.helper.Constants;
import com.app.grs.helper.GRS;
import com.app.grs.helper.GetSet;
import com.libizo.CustomEditText;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private CustomEditText etmobile, etpassword, etconfirmpassword, etgetotp, etconfirmotp;
    private Button btnregister, btngetotp, btnconfirmotp;
    private TextView tvbacktologin;
    private ScrollView scrollLayout;
    private LinearLayout otpLayout;
    private String mobile_no = "";
    Boolean verified=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);

        btnregister = findViewById(R.id.reg_btn_register);
        etmobile = findViewById(R.id.reg_et_mobile);
        etpassword = findViewById(R.id.reg_et_password);
        etconfirmpassword = findViewById(R.id.reg_et_confirmpassword);
        tvbacktologin = findViewById(R.id.reg_tv_backtologin);
        btngetotp = findViewById(R.id.reg_btn_getotp);
        scrollLayout = findViewById(R.id.reg_scroll_layout);
        otpLayout = findViewById(R.id.otplayout);
        btnconfirmotp = findViewById(R.id.reg_btn_confirmotp);
        etgetotp = findViewById(R.id.reg_et_otp_phone);
        etconfirmotp = findViewById(R.id.reg_et_otp_confirm);

        Constants.pref = getApplicationContext().getSharedPreferences("GRS",MODE_PRIVATE);
        Constants.editor = Constants.pref.edit();

        tvbacktologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            }
        });
        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String phone = etmobile.getText().toString().trim();
                String password = etpassword.getText().toString().trim();
                String confirmpassword = etconfirmpassword.getText().toString().trim();

                boolean emptyfeilds = false;

                if(etmobile.getText().toString().trim().length() == 0){
                    emptyfeilds = true;
                    etmobile.setError("Details required");
                }if(etpassword.getText().toString().trim().length() == 0){
                    emptyfeilds = true;
                    etpassword.setError("Details required");
                }if(etconfirmpassword.getText().toString().trim().length() == 0){
                    emptyfeilds = true;
                    etconfirmpassword.setError("Details required");
                }if (emptyfeilds == false){
                    if(!confirmpassword.equals(password)){
                        Toast.makeText(RegisterActivity.this, "Password dont Match!", Toast.LENGTH_SHORT).show();
                    }else
                    new checkRegistered(RegisterActivity.this, phone).execute();
                }
            }
        });

        btngetotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!etgetotp.getText().toString().equals("") && etgetotp.getText().toString().length()==10) {

                  mobile_no = etgetotp.getText().toString().trim();
                    new GetOTP(getApplicationContext(), mobile_no).execute();
                    //Toast.makeText(getApplicationContext(),"Otp sent to your mobile successfully",Toast.LENGTH_SHORT).show();

                }else Toast.makeText(RegisterActivity.this,"Check your Number",Toast.LENGTH_SHORT).show();
            }
        });

        btnconfirmotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!etconfirmotp.getText().toString().trim().equals("") && etconfirmotp.getText().toString().length()==5){
                    String otp = etconfirmotp.getText().toString().trim();
                    //   new VerifyOTP(getApplicationContext(), GetSet.getMobileno(), otp).execute();
                    new VerifyOTP(RegisterActivity.this, mobile_no, otp).execute();
                }else Toast.makeText(RegisterActivity.this,"Check your OTP",Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(RegisterActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(RegisterActivity.this);
    }

    public class VerifyOTP extends AsyncTask<String, Integer, String> {

        private Context context;
        private String mobileno, otp;
        private String url = Constants.BASE_URL + Constants.VERIFY_OTP;
        ProgressDialog progress;

        public VerifyOTP(Context context, String mobileno, String otp) {

            this.context = context;
            this.mobileno = mobileno;
            this.otp = otp;
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
                    .add(Constants.mobileno, mobileno)
                    .add(Constants.otp, otp)
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
            Log.v("result", "" + jsonData);
            JSONObject jonj = null;
            try {
                jonj = new JSONObject(jsonData);
                if (jonj.getString("status").equalsIgnoreCase(
                        "success")) {
                    progress.dismiss();
                    scrollLayout.setVisibility(View.GONE);
                    otpLayout.setVisibility(View.GONE);
                    new Register_Asyc(context).execute();
                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }else if (jonj.getString("status").equalsIgnoreCase(
                        "failed")){
                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
                else Toast.makeText(getApplicationContext(),"Something went wrong",Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public class GetOTP extends AsyncTask<String, Integer, String> {

        private Context context;
        private String mobileno;
        private String url = Constants.BASE_URL + Constants.GET_OTP;
        ProgressDialog progress;

        public GetOTP(Context ctx, String mobileno) {

            context = ctx;
            this.mobileno = mobileno;
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

        @Nullable
        @Override
        protected String doInBackground(String... params) {
            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("mobile", mobileno)
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

            etgetotp.setVisibility(View.GONE);
            btngetotp.setVisibility(View.GONE);
            etconfirmotp.setVisibility(View.VISIBLE);
            btnconfirmotp.setVisibility(View.VISIBLE);
        }
    }

    public class Register_Asyc extends AsyncTask<String, Integer, String> {

        private Context context;
        private String url = Constants.BASE_URL + Constants.REGISTER_USER;
        ProgressDialog progress;

        public Register_Asyc(Context ctx) {
            context = ctx;
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

        @Nullable
        @Override
        protected String doInBackground(String... params) {
            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add(Constants.mobileno, mobile_no)
                    .add(Constants.password, etconfirmpassword.getText().toString().trim())
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
                jonj = new JSONObject(jsonData);
                if (jonj.getString("status").equalsIgnoreCase(
                        "success")) {

                    GetSet.setMobileNo(mobile_no);
                    Constants.editor.putString("mobileno", GetSet.getMobileNo());
                    Constants.editor.commit();

                    Toast.makeText(getApplicationContext(),"Registered Successfully!",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(context,MyAccountActivity.class);
                    startActivity(intent);
                    finish();

                }else
                {
                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private class checkRegistered extends AsyncTask<String, Integer, String>{

        private Context context;
        String phone;
        private String url = Constants.BASE_URL + Constants.CHECK_REGISTER_USER;
        ProgressDialog progress;

        public checkRegistered(Context context, String phone) {
            this.context = context;
            this.phone = phone;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(context);
            progress.setMessage("Please wait ....");
            progress.setTitle("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.show();
        }

        @Override
        protected String doInBackground(String... strings) {

            String jsonData = null;
            Response response = null;
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("mobileno", phone)
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
                jonj = new JSONObject(jsonData);
                if (jonj.getString("status").equalsIgnoreCase(
                        "Already")) {

                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();

                }else if (jonj.getString("status").equalsIgnoreCase(
                        "New User"))
                {
                    scrollLayout.setVisibility(View.GONE);
                    otpLayout.setVisibility(View.VISIBLE);
                    etgetotp.setText(etmobile.getText().toString());
                    etgetotp.setVisibility(View.VISIBLE);
                    btngetotp.setVisibility(View.VISIBLE);
                    etconfirmotp.setVisibility(View.GONE);
                    btnconfirmotp.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(),jonj.getString("message"),Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GRS.freeMemory();
    }

}
