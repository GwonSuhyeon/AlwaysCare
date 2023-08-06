package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

public class AnalyzeResultActivity extends AppCompatActivity {

    private PetInfo mainPetInfo;
    private String jwt;
    private TextView analyzeTextView;
    private TextView percentView;
    private Button saveButton;
    private int disease = -1;
    private int percent = -1;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_result);
        Context context = AnalyzeResultActivity.this;

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);

        analyzeTextView = findViewById(R.id.analyze_result_textview);
        percentView = findViewById(R.id.analyze_result_percent);
        saveButton = findViewById(R.id.analyze_result_save);
        TextView descriptionView = findViewById(R.id.analyze_result_description);

        ImageView imageView = findViewById(R.id.check_disease_image);
        String photoURL = getIntent().getStringExtra("photoURL");
        mainPetInfo = (PetInfo) getIntent().getSerializableExtra("mainPetInfo");
        type = getIntent().getStringExtra("type");

        if (type.equals("eye")) {
            ArrayList<Integer> percentList = (ArrayList<Integer>) getIntent().getSerializableExtra("percentList");
            int maxIdx = 0;
            for (int i = 1; i < percentList.size(); i++) {
                if (percentList.get(maxIdx) < percentList.get(i))
                    maxIdx = i;
            }

            switch (maxIdx) {
                case 0:
                    StringBuilder sb = new StringBuilder()
                            .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                            .append("건강한 상태입니다.");
                    analyzeTextView.setText(String.valueOf(sb));
                    percentView.setVisibility(View.GONE);
                    descriptionView.setVisibility(View.GONE);
                    disease = 1000;
                    percent = percentList.get(0);
                    break;
                case 1:
                    sb = new StringBuilder()
                            .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                            .append("'결막염'이 의심됩니다.");
                    analyzeTextView.setText(String.valueOf(sb));
                    percentView.setText(percentList.get(1) + "%");
                    descriptionView.setText(R.string.결막염);
                    disease = 1011;
                    percent = percentList.get(1);
                    break;
                case 2:
                    sb = new StringBuilder()
                            .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                            .append("'각막질환'이 의심됩니다.");
                    analyzeTextView.setText(String.valueOf(sb));
                    percentView.setText(percentList.get(2) + "%");
                    descriptionView.setText(R.string.각막질환);
                    disease = 1021;
                    percent = percentList.get(2);
                    break;
                case 3:
                    sb = new StringBuilder()
                            .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                            .append("'백내장'이 의심됩니다.");
                    analyzeTextView.setText(String.valueOf(sb));
                    percentView.setText(percentList.get(3) + "%");
                    descriptionView.setText(R.string.백내장);
                    disease = 1031;
                    percent = percentList.get(3);
                    break;
                case 4:
                    sb = new StringBuilder()
                            .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                            .append("'안검질환'이 의심됩니다.");
                    analyzeTextView.setText(String.valueOf(sb));
                    percentView.setText(percentList.get(4) + "%");
                    descriptionView.setText(R.string.안검질환);
                    disease = 1061;
                    percent = percentList.get(4);
                    break;
            }
            Log.d("my", "percent : " + percent);
        }
        else {
            HashMap<String, Integer> skinResult = (HashMap<String, Integer>) getIntent().getSerializableExtra("skinResult");
            int maxCode = 1000;
            int maxPercent = 0;
            HashMap<String, String> diseaseCode = new HashMap<>();
            diseaseCode.put("2011", "구진 플라크");
            diseaseCode.put("2021", "비듬 각질 상피성잔고리");
            diseaseCode.put("2031", "태선화 과다색소침착");
            diseaseCode.put("2041", "농포 여드름");
            diseaseCode.put("2051", "미란 궤양");
            diseaseCode.put("2061", "결정 종괴");

            if (skinResult.keySet().size() == 0) {
                StringBuilder sb = new StringBuilder()
                        .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                        .append("건강한 상태입니다.");
                analyzeTextView.setText(String.valueOf(sb));
                percentView.setVisibility(View.GONE);
                descriptionView.setVisibility(View.GONE);
                disease = 1000;
                percent = 0;
            } else {
                StringBuilder sb = new StringBuilder();
                StringBuilder description_sb = new StringBuilder();

                for (String key : skinResult.keySet()) {
                    if (maxPercent < skinResult.get(key)) {
                        maxCode = Integer.parseInt(key);
                        maxPercent = skinResult.get(key);
                    }
                }

                Log.d("my", "maxCode : " + maxCode);
                switch (String.valueOf(maxCode)) {
                    case "2000":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재\n")
                                .append("건강한 상태입니다.");
                        maxCode = 1000;

                        break;
                    case "2011":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'구진플라크\'가 의심됩니다.");
                        description_sb.append("구진 플라크 : 치아와 관련된 질환으로, 치아의 치근염, 치주염, 치아우식 등이 원인이 될수있다.\n치아를 제대로 관리하지 않을 경우 발생한다.\n");
                        break;
                    case "2021":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'비듬 각질 상피성잔고리\'가 의심됩니다.");
                        description_sb.append("비듬 각질 상피성잔고리 : 피부 건조, 기름 분비 부족, 피부 염증, 알러지 반응, 비타민 및 영양소 결핍 등으로 발생한다.\n" +
                                "적절한 샴푸를 사용하여 피부를 깨끗하게 유지하고, 영양가 있는 식사를 제공하여 영양 부족을 해소하는 것이 중요하다.\n" +
                                "강아지의 피부 건강을 지속적으로 관리하고 정기적인 건강 검진을 통해 문제를 예방하고 조기에 발견하는 것이 중요하다.\n");
                        break;
                    case "2031":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'태선화 과다색소침착\'이 의심됩니다.");
                        description_sb.append("태선화 과다색소침착 : 피부에 멜라닌이라는 색소가 과도하게 생성되거나 흡수되어 발생한다.\n" +
                                "유전적인 요인, 태양 노출, 피부 염증, 호르몬 변화, 스트레스 등 다양한 원인에 의해 발생한다.\n" +
                                "올바른 영양 공급, 깨끗한 환경 유지, 정기적인 건강 검진 등을 신경 써야 한다.\n");
                        break;
                    case "2041":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'농포성 여드름\'이 의심됩니다.");
                        description_sb.append("농포 여드름 : 피지선의 과도한 분비로 인해 피지가 모낭에 쌓여 감염이 발생하여 생길 수 있다.\n" +
                                "원인에는 피부 염증, 세균 감염, 스트레스, 호르몬 변화 등이 포함될 수 있다.\n" +
                                "피부 건강을 위해 올바른 영양 공급, 깨끗한 환경 유지, 정기적인 건강 검진 등을 신경 써야 한다.\n");
                        break;
                    case "2051":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'미란 궤양\'가 의심됩니다.");
                        description_sb.append("미란 궤양 : 굵은 상처. 미란, 궤양 긁은 상처는 여러 가려움증 질환에서 가려움증을 제거하기 위해 손톱으로" +
                                "긁은 후 또는 기ㅖ적 외상, 지속적인 마찰 등에 의해 생기며 그 크기와 형태는 다양할 수 있습니다.\n" +
                                "하지만 일반적으로 점상 또는 작은 선상의 병변입니다.\n");
                        break;
                    case "2061":
                        sb = new StringBuilder()
                                .append("'").append(mainPetInfo.getName()).append("'").append("는 현재 \'결정 종괴\'가 의심됩니다.");
                        description_sb.append("결정 종괴 : 개의 피부 또는 피부 아래 조직에서 발생하는 종양으로서, 어떤 종류의 세포가 비정상적으로 증식하여 형성된다.\n" +
                                "수술적 제거가 일반적이며, 종괴가 악성인 경우 광범위한 수술 및 추가적인 치료가 필요할 수 있다.\n" +
                                "정기적인 건강 검진과 조기 발견이 중요하다.\n");
                        break;
                }
                analyzeTextView.setText(String.valueOf(sb));
                if (maxCode == 1000) {
                    percentView.setVisibility(View.GONE);
                } else {
                    percentView.setText(maxPercent + "%");
                }
                descriptionView.setText(String.valueOf(description_sb));

                disease = maxCode;
                percent = maxPercent;
            }
        }

        Glide.with(this).load(photoURL).into(imageView);

        Button searchNearHospitalButton = findViewById(R.id.analyze_result_search_nearby_hospital_button);
        searchNearHospitalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog enterLocationDialog = new Dialog(AnalyzeResultActivity.this);
                enterLocationDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                enterLocationDialog.setContentView(R.layout.enter_location_dialog);
                enterLocationDialog.show();

                EditText locationEditText = enterLocationDialog.findViewById(R.id.enter_location_location_edittext);
                Button searchButton = enterLocationDialog.findViewById(R.id.enter_location_search_button);
                searchButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enterLocationDialog.dismiss();
                        String location = locationEditText.getText().toString();

                        if (location.isEmpty()) {
                            Toast.makeText(AnalyzeResultActivity.this, "위치를 입력해주세요", Toast.LENGTH_SHORT).show();
                        } else {
                            Intent gotoNearbyHospitalActivity = new Intent(AnalyzeResultActivity.this, NearbyHospitalActivity.class);
                            gotoNearbyHospitalActivity.putExtra("location", location);
                            startActivity(gotoNearbyHospitalActivity);
                        }
                    }
                });
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDiagnosis(mainPetInfo.getPetId(), disease, percent);
            }
        });
    }

    private void saveDiagnosis(long petId, int disease, int percent) {
        Log.d("my", "percent : " + percent);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("disease", disease);
        requestBody.addProperty("percent", percent);

        Call<ResponseBody> call;
        if (type.equals("eye")) {
            call = apiService.postDiagnosisRequest(jwt, petId, requestBody);
        } else {
            call = apiService.postDiseaseRequest(jwt, petId, requestBody);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            Toast.makeText(AnalyzeResultActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                            Intent gotoHomeActivity = new Intent(AnalyzeResultActivity.this, HomeActivity.class);
                            gotoHomeActivity.putExtra("mainPetInfo", mainPetInfo);
                            startActivity(gotoHomeActivity);
                        } else {
                            Toast.makeText(AnalyzeResultActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
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

    private void editDiagnosis(long petId, int disease, int percent) {
        // TODO : enter base url
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("disease", disease);
        requestBody.addProperty("percent", percent);

        Call<ResponseBody> call = apiService.patchDiagnosisRequest(jwt, petId, requestBody);
        call.enqueue(new Callback<ResponseBody>() {
            @EverythingIsNonNull
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().toString();
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            Toast.makeText(AnalyzeResultActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                            Intent gotoHomeActivity = new Intent(AnalyzeResultActivity.this, MainActivity.class);
                            gotoHomeActivity.putExtra("mainPetInfo", mainPetInfo);
                            startActivity(gotoHomeActivity);
                        } else {
                            Toast.makeText(AnalyzeResultActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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