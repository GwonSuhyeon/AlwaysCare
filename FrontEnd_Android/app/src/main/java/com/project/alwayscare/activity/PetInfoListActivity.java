package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.data.PetInfoAdapter;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PetInfoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ImageButton addPetButton;
    private ArrayList<PetInfo> petInfoList = new ArrayList<>();
    private String jwt;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_info_list);
        Context context = PetInfoListActivity.this;
        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);
        userId = userInfoService.getUserIdToSharedPreferences(context);

        getPetList();

        addPetButton = findViewById(R.id.pet_info_list_add_pet_imagebutton);
        addPetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoEditPetInfoActivity = new Intent(PetInfoListActivity.this, EditPetInfoActivity.class);
                startActivity(gotoEditPetInfoActivity);
            }
        });
    }

    private void getPetList() {
        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> call = apiService.getPetListRequest(jwt, userId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "get pet list : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            JSONArray resultArray = jsonResponse.getJSONArray("result");
                            for (int i = 0; i < resultArray.length(); i++) {
                                JSONObject petObject = resultArray.getJSONObject(i);

                                String petImageURL = petObject.getString("petImageURL");
                                String petSpecies = petObject.getString("petSpecies");
                                int petId = petObject.getInt("petId");
                                String petName = petObject.getString("petName");
                                int petAge = petObject.getInt("petAge");
                                int petType = petObject.getInt("petType");

                                petInfoList.add(new PetInfo(petId, petName, petAge, petImageURL, petSpecies, petType));
                            }

                            recyclerView = findViewById(R.id.pet_info_list_recyclerview);
                            recyclerView.setLayoutManager(new LinearLayoutManager(PetInfoListActivity.this));
                            recyclerView.setAdapter(new PetInfoAdapter(PetInfoListActivity.this, petInfoList));
                        } else {
                            // 잘못된 요청
                            Toast.makeText(PetInfoListActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // API 응답 처리
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "get pet list failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
                Log.d("my", "get pet list failed from onFailure");
            }
        });
    }
}