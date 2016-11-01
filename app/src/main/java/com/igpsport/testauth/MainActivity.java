package com.igpsport.testauth;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private String mUserAuthResult;
    private UserBean mUser;

    @BindView(R.id.tvAge)
    TextView tvAge;

    @BindView(R.id.tvExpire)
    TextView tvExpire;

    @BindView(R.id.tvHeight)
    TextView tvHeight;

    @BindView(R.id.tvID)
    TextView tvID;

    @BindView(R.id.tvNickName)
    TextView tvNickname;

    @BindView(R.id.tvSex)
    TextView tvSex;

    @BindView(R.id.tvToken)
    TextView tvToken;

    @BindView(R.id.tvWeight)
    TextView tvWeight;

    @BindView(R.id.btnGoAuth)
    Button btnGoAuth;

    @BindView(R.id.btnGoUpload)
    Button btnUpload;

    @OnClick(R.id.btnGoUpload)
    public void onButtonUploadClick() {
        if (null == mUser) {
            Toast.makeText(getApplicationContext(), "未授权", Toast.LENGTH_SHORT).show();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "请开启存储读取权限", Toast.LENGTH_SHORT).show();
            } else {
                new MaterialFilePicker()
                        .withActivity(this)
                        .withRequestCode(1)
                        .withFilterDirectories(true) // Set directories filterable (false by default)
                        .withHiddenFiles(true) // Show hidden files and folders
                        .start();
            }
        }
    }

    @OnClick(R.id.btnGoAuth)
    public void onButtonAuthClick() {
        Intent intent = new Intent(MainActivity.this, AuthActivity.class);
        startActivityForResult(intent, 10);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        loadUser();
        if (null != mUser) {
            showResult();
            btnGoAuth.setEnabled(false);
            btnUpload.setEnabled(true);
        } else {
            btnGoAuth.setEnabled(true);
            btnUpload.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1000 && requestCode == 10) {
            mUserAuthResult = data.getStringExtra("data");
            try {
                Gson gson = new Gson();
                mUser = gson.fromJson(mUserAuthResult, UserBean.class);
                saveUser();
                showResult();
                btnGoAuth.setEnabled(false);
                btnUpload.setEnabled(true);
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            String filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            Toast.makeText(getApplicationContext(), filePath, Toast.LENGTH_SHORT).show();
            uploadFile(filePath);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void uploadFile(String filename) {


        File myFile = new File(filename);

        RequestParams params = new RequestParams();
        try {
            params.put("file", myFile);
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), "无法上传该文件", Toast.LENGTH_SHORT).show();
            return;
        }
        params.add("memberid", mUser.MemberID + "");
        params.add("appid", Constants.APPID + "");
        params.add("token", mUser.Token + "");

        final ProgressDialog progressDialog = ProgressDialog.show(getApplicationContext(), "", "上传中...");
        AsyncHttpClient myClient = new AsyncHttpClient();
        myClient.post(getApplicationContext(), Constants.API_UPLOAD_FIT, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                progressDialog.dismiss();
                String result = new String(responseBody);
                Toast.makeText(getApplicationContext(), "上传成功，结果：" + result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void showResult() {
        tvAge.setText(mUser.Age + "");
        tvID.setText(mUser.MemberID + "");
        tvExpire.setText(mUser.Expires);
        tvWeight.setText(mUser.Weight + "");
        tvToken.setText(mUser.Token);
        tvHeight.setText(mUser.Height + "");
        tvNickname.setText(mUser.NickName);
        tvSex.setText(mUser.Sex);
    }

    void saveUser() {
        if (mUserAuthResult != null && mUserAuthResult.length() > 0) {
            SharedPreferences sp = getSharedPreferences("testAuth", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("auth", mUserAuthResult);
            editor.commit();
        }
    }

    void loadUser() {
        SharedPreferences sp = getSharedPreferences("testAuth", MODE_PRIVATE);
        mUserAuthResult = sp.getString("auth", "");
        if (mUserAuthResult != null && mUserAuthResult.length() > 0) {
            Gson gson = new Gson();
            mUser = gson.fromJson(mUserAuthResult, UserBean.class);
        }
    }
}
