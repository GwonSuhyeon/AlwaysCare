package com.project.alwayscare.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.ExpandableListAdapter;
import com.project.alwayscare.data.PetInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NearbyHospitalActivity extends AppCompatActivity {

    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private List<List<String>> expandableListTitle;
    private HashMap<List<String>, List<String>> expandableListDetail;
    private String location;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_hospital);

        loadingDialog = new Dialog(NearbyHospitalActivity.this);
        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.show();

        location = getIntent().getStringExtra("location");
        expandableListView = findViewById(R.id.hospital_expandablelistview);
        getHospitalData(location);
    }

    private HashMap<List<String>, List<String>> getHospitalData(String location) {
        HashMap<List<String>, List<String>> expandableListDetail = new HashMap<>();
        Log.d("my", "크롤링 시작");

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://43.202.3.43:8000//")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<ResponseBody> call = apiService.getHospitalRequest(location);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "크롤링 정보 :" + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        JSONArray nameList = jsonResponse.getJSONArray("name");
                        for (int i = 0; i < nameList.length(); i++) {
                            String hospitalName = nameList.getString(i);
                            Log.d("my", "hospital size : " + nameList.length());
                        }

                        JSONArray scoreList = jsonResponse.getJSONArray("score");
                        for (int i = 0; i < scoreList.length(); i++) {
                            String hospitalScore = scoreList.getString(i);
                            Log.d("my", hospitalScore);
                        }

                        JSONArray reviewList = jsonResponse.getJSONArray("review");
                        List<List<String>> reviews = new ArrayList<>();
                        for (int i = 0; i < reviewList.length(); i++) {
                            List<String> sentences = new ArrayList<>();
                            String hospitalReview = reviewList.getString(i);
                            String[] split = hospitalReview.split("[/]");

                            for(String sentence :split) {
                                sentence = sentence.trim().replace(",", "").replace("\"", "").replace("\\","")
                                        .replace("/","").replace("0","")
                                        .replace("[", "").replace("]", "");
                                if(!sentence.isEmpty() && sentence != "\0") {
                                    sentences.add(sentence);
                                }
                            }
                            if(sentences.size() == 0) {
                                sentences.add("리뷰가 존재하지 않습니다");
                            }
                            reviews.add(sentences);

                            Log.d("my", hospitalReview);
                        }

                        Log.d("my", "review size : " + reviews.size());
                        for(int i=0; i< nameList.length(); i++) {
                            String hospitalName = nameList.getString(i);
                            String hospitalScore = scoreList.getString(i);
                            expandableListDetail.put(Arrays.asList(hospitalName, hospitalScore), reviews.get(i));

                            expandableListTitle = new ArrayList<>(expandableListDetail.keySet());
                            expandableListAdapter = new ExpandableListAdapter(NearbyHospitalActivity.this, expandableListTitle, expandableListDetail);
                            expandableListView.setAdapter(expandableListAdapter);
                        }

                        loadingDialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        loadingDialog.dismiss();
                    } catch (IOException e) {
                        e.printStackTrace();
                        loadingDialog.dismiss();
                    }
                    // API 응답 처리
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "크롤링 실패");
                    loadingDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("my", "API요청 실패");
                loadingDialog.dismiss();
            }
        });

        return expandableListDetail;
    }
}