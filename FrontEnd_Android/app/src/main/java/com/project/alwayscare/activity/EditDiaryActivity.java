package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.fragment.PetInfoFragment;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.HTTP;

public class EditDiaryActivity extends AppCompatActivity {

    private EditText diaryEditText;
    private TextView todayDateTextView;
    private Button saveButton;
    private PetInfo petInfo;
    private String diaryInfo;
    private String jwt;
    private long petId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_diary);

        Context context = EditDiaryActivity.this;

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);

        diaryEditText = findViewById(R.id.edit_diary_edittext);
        todayDateTextView = findViewById(R.id.edit_diary_date_textview);
        saveButton = findViewById(R.id.edit_diary_save_button);

        todayDateTextView.setText(getTodayDate());
        petInfo = (PetInfo) getIntent().getSerializableExtra("mainPetInfo");
        if (petInfo != null) {
            Log.d("my", "petInfo not null");
            petId = petInfo.getPetId();
        }

        diaryInfo = getIntent().getStringExtra("diaryInfo");
        if (diaryInfo != null) {
            Log.d("my", "diary not null");
            diaryEditText.setText(diaryInfo);
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newSentence = diaryEditText.getText().toString();
                if (newSentence.isEmpty()) {
                    Toast.makeText(EditDiaryActivity.this, "일지를 작성해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://15.164.234.59:9000/") // API 엔드포인트의 기본 URL 설정
                        .addConverterFactory(GsonConverterFactory.create()) // JSON 데이터를 객체로 변환하기 위해 Gson 컨버터 팩토리 추가
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("sentence", newSentence);

                Call<ResponseBody> call;
                if (diaryInfo == null) {
                    call = apiService.postDiaryRequest(jwt, petId, requestBody);
                    Log.d("my", "save diary request");
                } else {
                    call = apiService.editDiaryRequest(jwt, petId, requestBody);
                    Log.d("my", "save diary request");
                }
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                Log.d("my", "save / edit diary : " + responseBody);
                                JSONObject jsonResponse = new JSONObject(responseBody);

                                int code = jsonResponse.getInt("code");
                                String message = jsonResponse.getString("message");

                                // 요청 성공 시
                                if (code == 1000) {
                                    Toast.makeText(EditDiaryActivity.this, "변경 사항이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                                    Intent gotoHomeActivity = new Intent(EditDiaryActivity.this, HomeActivity.class);
                                    gotoHomeActivity.putExtra("mainPetInfo", petInfo);
                                    startActivity(gotoHomeActivity);
                                } else {
                                    Toast.makeText(EditDiaryActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                Log.d("my", "save / edit diary failed");
                                e.printStackTrace();
                            } catch (IOException e) {

                            }
                        } else {
                            // API 요청 실패 처리
                            Toast.makeText(EditDiaryActivity.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // API 요청 실패 처리
                        Toast.makeText(EditDiaryActivity.this, "저장에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    public static String getTodayDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return today.format(formatter);
    }
}