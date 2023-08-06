package com.project.alwayscare.fragment;

import android.content.Context;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.CalendarAdapter;
import com.project.alwayscare.data.CalendarListener;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CalenderFragment extends Fragment implements CalendarListener {

    private PetInfo petInfo;
    private String jwt;
    private String diaryInfo;
    private int eyeDisease;
    private int eyePercent;
    private int skinDisease;
    private int skinPercent;
    private LocalDate selectedDate;
    private TextView diaryTextView;
    private TextView diagnosisTextView;
    private TextView diseaseTextView;
    private ImageButton prevMonthButton;
    private ImageButton nextMonthButton;
    private TextView dateText;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.custom_calendar_recyclerview);
        diaryTextView = view.findViewById(R.id.calendar_diary_textview);
        dateText = view.findViewById(R.id.calendar_date_text);
        prevMonthButton = view.findViewById(R.id.calendar_prev_month_button);
        nextMonthButton = view.findViewById(R.id.calendar_next_month_button);
        diagnosisTextView = view.findViewById(R.id.calendar_diagnosis_textview);
        diseaseTextView = view.findViewById(R.id.calendar_disease_textview);
        Context context = getContext();

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);

        Bundle bundle = getArguments();
        if (bundle != null) {
            petInfo = (PetInfo) bundle.getSerializable("mainPetInfo");
        }

        selectedDate = LocalDate.now();
        // 달력 표시
        getDiaryDates();

        // 다이어리, 진단 정보 표시
        setDiaryInfo(PetInfoFragment.getTodayDate());
        setDiagnosisInfo(PetInfoFragment.getTodayDate());

        prevMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.minusMonths(1);
                getDiaryDates();
            }
        });
        nextMonthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDate = selectedDate.plusMonths(1);
                getDiaryDates();
            }
        });
    }

    @Override
    public void onItemClicked(int data) {
        String clickedDate = selectedDate.getYear() + "/" + String.format("%02d", selectedDate.getMonthValue()) + "/"
                + String.format("%02d", data);

        setDiaryInfo(clickedDate);
        setDiagnosisInfo(clickedDate);
    }

    private String monthYearFromDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월");
        return date.format(formatter);
    }

    private void setMonthView(ArrayList<Integer> dateToCheckList) {
        dateText.setText(monthYearFromDate(selectedDate));
        ArrayList<String> dayList = daysInMonthArray(selectedDate);
        CalendarAdapter adapter = new CalendarAdapter(dayList, dateToCheckList, this);

        RecyclerView.LayoutManager manager = new GridLayoutManager(getContext().getApplicationContext(), 7);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> dayList = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(date);

        int lastDay = yearMonth.lengthOfMonth();
        LocalDate firstDay = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue();

        for (int i = 1; i < 42; i++) {
            if (i <= dayOfWeek || i > lastDay + dayOfWeek) {
                dayList.add("");
            } else {
                dayList.add(String.valueOf(i - dayOfWeek));
            }
        }

        return dayList;
    }

    private void getDiaryDates() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        long petId = petInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiaryDatesRequest(jwt, petId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "request diary : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            JSONArray jsonArray = jsonResponse.getJSONArray("result");
                            ArrayList<String> dateList = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++)
                                dateList.add(jsonArray.getString(i));

                            ArrayList<Integer> dateToCheckList = dateList.stream()
                                    .filter(date -> date.startsWith(selectedDate.format(DateTimeFormatter.ofPattern("yyyy/MM"))))
                                    .map(date -> Integer.parseInt(date.substring(8, 10)))
                                    .collect(Collectors.toCollection(ArrayList::new));

                            // 달력 view 구성
                            setMonthView(dateToCheckList);
                        } else {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            // 잘못된 요청
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
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

    private void setDiaryInfo(String date) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        long petId = petInfo.getPetId();
        Log.d("my", "jwt : " + jwt);
        Log.d("my", "petId : " + petId);
        Log.d("my", "selected date : " + date);
        Call<ResponseBody> call = apiService.getDiaryRequest(jwt, petId, date);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "request diary : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            diaryInfo = jsonResponse.getString("result");
                            Log.d("my", "diary info : " + diaryInfo);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    diaryTextView.setText(diaryInfo);
                                }
                            });
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    diaryTextView.setText("정보 없음");
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            // 잘못된 요청
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
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

    private void setDiagnosisInfo(String date) {
        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        long petId = petInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiagnosisRequest(jwt, petId, date);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "request diary : " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            JSONObject result = jsonResponse.getJSONObject("result");
                            eyeDisease = result.getInt("disease");
                            eyePercent = result.getInt("percent");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (eyeDisease) {
                                        case 1000:
                                            diagnosisTextView.setText("눈 : 건강한 상태입니다.");
                                            break;
                                        case 1011:
                                            diagnosisTextView.setText("눈 : 결막염이 " + eyePercent + "%로 의심됩니다.");
                                            break;
                                        case 1021:
                                            diagnosisTextView.setText("눈 : 각막질환이 " + eyePercent + "%로 의심됩니다.");
                                            break;
                                        case 1031:
                                            diagnosisTextView.setText("눈 : 백내장이 " + eyePercent + "%로 의심됩니다.");
                                            break;
                                        case 1061:
                                            diagnosisTextView.setText("눈 : 안검질환이 " + eyePercent + "%로 의심됩니다.");
                                            break;
                                    }
                                }
                            });
                            setSkinDiagnosisInfo(date);
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    diagnosisTextView.setText("눈 : 진단 정보 없음");
                                }
                            });
                            setSkinDiagnosisInfo(date);
                        } else {
                            // 잘못된 요청
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
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
    private void setSkinDiagnosisInfo(String date) {
        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        long petId = petInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiseaseRequest(jwt, petId, date);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "request diary : " + responseBody);

                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            JSONObject result = jsonResponse.getJSONObject("result");
                            skinDisease = result.getInt("disease");
                            skinPercent = result.getInt("percent");

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (skinDisease) {
                                        case 1000:
                                            diseaseTextView.setText("피부 : 건강한 상태입니다.");
                                            break;
                                        case 2011:
                                            diseaseTextView.setText("피부 : 구진 플라크가 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                        case 2021:
                                            diseaseTextView.setText("피부 : 비듬 각질 상피성잔고리가 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                        case 2031:
                                            diseaseTextView.setText("피부 : 태선화 과다색소침착이 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                        case 2041:
                                            diseaseTextView.setText("피부 : 농포 여드름이 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                        case 2051:
                                            diseaseTextView.setText("피부 : 미란 궤양이 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                        case 2061:
                                            diseaseTextView.setText("피부 : 결정 종괴가 " + skinPercent + "%로 의심됩니다.");
                                            break;
                                    }
                                }
                            });
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    diseaseTextView.setText("피부 : 진단 정보 없음");
                                }
                            });
                        } else {
                            // 잘못된 요청
                            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
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
