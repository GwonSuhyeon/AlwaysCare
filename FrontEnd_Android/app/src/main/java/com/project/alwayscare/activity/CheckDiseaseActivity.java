package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;


public class CheckDiseaseActivity extends AppCompatActivity {

    private ImageView imageView;
    private long userId;
    private String photoURL;
    private int option;
    private PetInfo mainPetInfo;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_disease);
        Context context = CheckDiseaseActivity.this;

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        userId = userInfoService.getUserIdToSharedPreferences(context);

        photoURL = getIntent().getStringExtra("url_string");
        mainPetInfo = (PetInfo) getIntent().getSerializableExtra("mainPetInfo");
        type = getIntent().getStringExtra("type");

        imageView = findViewById(R.id.check_disease_image);
        Glide.with(this).load(photoURL).into(imageView);

        if (type.equals("eye")) {
            postEyeDiagnosis();
        }
        else if (type.equals("skin")) {
            postSkinDiagnosis();
        }
    }

    private void postEyeDiagnosis() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://59.29.138.9:9000/") // API 엔드포인트의 기본 URL 설정
                .addConverterFactory(GsonConverterFactory.create()) // JSON 데이터를 객체로 변환하기 위해 Gson 컨버터 팩토리 추가
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        File imageFile = new File(photoURL);

        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image_file", imageFile.getName(), imageRequestBody);

        RequestBody typeRequestBody = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody userIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));

        Call<ResponseBody> call = apiService.postDiagnosisEye(filePart, typeRequestBody, userIdRequestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String result1000 = jsonResponse.getString("1000");
                        String result1011 = jsonResponse.getString("1011");
                        String result1021 = jsonResponse.getString("1021");
                        String result1031 = jsonResponse.getString("1031");
                        String result1061 = jsonResponse.getString("1061");

                        int noDiseasePercent = (int) (Float.valueOf(result1000) * 100);
                        int conjunctivitis = (int) (Float.valueOf(result1011) * 100); // 결막염
                        int cornea = (int) (Float.valueOf(result1021) * 100);; // 각막
                        int cataract = (int) (Float.valueOf(result1031) * 100); // 백내장
                        int eyelid = (int) (Float.valueOf(result1061) * 100); // 안검

                        ArrayList<Integer> percentArray = new ArrayList<>();
                        percentArray.add(noDiseasePercent);
                        percentArray.add(conjunctivitis);
                        percentArray.add(cornea);
                        percentArray.add(cataract);
                        percentArray.add(eyelid);

                        Intent intent = new Intent(CheckDiseaseActivity.this, AnalyzeResultActivity.class);
                        intent.putExtra("photoURL", photoURL);
                        intent.putExtra("mainPetInfo", mainPetInfo);
                        intent.putExtra("type", "eye");
                        intent.putExtra("percentList", percentArray);
                        startActivity(intent);

                    } catch (JSONException e) {
                        Log.d("my", "save / edit diary failed");
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.d("my", "실패1");
                    }
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "실패2");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
                Log.d("my", "실패3");
            }
        });
    }

    private void postSkinDiagnosis() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://59.29.138.9:9000/") // API 엔드포인트의 기본 URL 설정
                .addConverterFactory(GsonConverterFactory.create()) // JSON 데이터를 객체로 변환하기 위해 Gson 컨버터 팩토리 추가
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        File imageFile = new File(photoURL);

        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("image_file", imageFile.getName(), imageRequestBody);

        RequestBody typeRequestBody = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody userIdRequestBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(userId));

        Call<ResponseBody> call = apiService.postDiagnosisSkin(filePart, typeRequestBody, userIdRequestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @EverythingIsNonNull
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", responseBody);
                        HashMap<String, Integer> skinResult = new HashMap<>();
                        Gson gson = new Gson();

                        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                        for (String key : jsonObject.keySet()) {
                            String value = jsonObject.get(key).getAsString();
                            int percent = (int) (Float.parseFloat(value) * 100);
                            skinResult.put(key, percent);
                        }

                        Intent intent = new Intent(CheckDiseaseActivity.this, AnalyzeResultActivity.class);
                        intent.putExtra("photoURL", photoURL);
                        intent.putExtra("mainPetInfo", mainPetInfo);
                        intent.putExtra("type", "skin");
                        intent.putExtra("skinResult", skinResult);
                        startActivity(intent);
                        Log.d("my", responseBody);

                    } catch (IOException e) {
                        Log.d("my", "실패1");
                    }
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "실패2");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
                Log.d("my", "실패3");
            }
        });
    }
}