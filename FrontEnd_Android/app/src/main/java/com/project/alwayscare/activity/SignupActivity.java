package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText nicknameEditText;
    private EditText passwordEditText;
    private EditText passwordCheckEditText;
    private Button signupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailEditText = findViewById(R.id.signup_email_edittext);
        nicknameEditText = findViewById(R.id.signup_nickname_edittext);
        passwordEditText = findViewById(R.id.signup_password_edittext);
        passwordCheckEditText = findViewById(R.id.signup_password_check_edittext);
        signupButton = findViewById(R.id.signup_signup_button);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String nickname = nicknameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String passwordCheck = passwordCheckEditText.getText().toString();

                if(email.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (nickname.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (password.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(passwordCheck)) {
                    Toast.makeText(SignupActivity.this, "비밀번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
                }
                else {
                    // TODO : enter base url
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("http://15.164.234.59:9000/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    ApiService apiService = retrofit.create(ApiService.class);
                    JsonObject requestBody = new JsonObject();
                    requestBody.addProperty("name", nickname);
                    requestBody.addProperty("email", email);
                    requestBody.addProperty("password", password);

                    Call<ResponseBody> call = apiService.postSignupRequest(requestBody);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                try {
                                    String responseBody = response.body().string();
                                    Log.d("my", "signup : " + responseBody);
                                    JSONObject jsonResponse = new JSONObject(responseBody);

                                    int code = jsonResponse.getInt("code");
                                    String message = jsonResponse.getString("message");

                                    if (code == 1000) {
                                        Toast.makeText(SignupActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                                        Intent gotoMainActivity = new Intent(SignupActivity.this, MainActivity.class);
                                        startActivity(gotoMainActivity);
                                    } else {
                                        Toast.makeText(SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (JSONException e) {
                                    Toast.makeText(SignupActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(SignupActivity.this, "오류 발생", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // API 요청 실패 처리
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            // API 요청 실패 처리
                        }
                    });
                }
            }
        });
    }
}