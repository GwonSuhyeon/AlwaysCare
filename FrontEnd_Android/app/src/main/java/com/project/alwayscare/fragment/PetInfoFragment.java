package com.project.alwayscare.fragment;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.project.alwayscare.R;
import com.project.alwayscare.activity.CheckDiseaseActivity;
import com.project.alwayscare.activity.EditDiaryActivity;
import com.project.alwayscare.activity.PetInfoListActivity;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Path;

public class PetInfoFragment extends Fragment implements View.OnClickListener {
    
    private String currentPhotoPath;
    private Uri photoUri;
    private Dialog selectCameraAlbumDialog;
    private Dialog selectDiagnosisTypeDialog;
    private Dialog noticeDialog;
    private ImageButton listButton;
    private RelativeLayout todayDiaryLayout;
    private Button todayDiagnosisButton;
    private RelativeLayout todayDiagnosisLayout;
    private Button todayDiaryButton;
    private ImageButton editDiagnosisButton;
    private ImageButton editDiaryButton;
    private TextView diaryContentTextView;
    private TextView diagnosisEyeContentTextView;
    private TextView diagnosisSkinContentTextView;
    private String diagnosisType;
    private ArrayList<PetInfo> petInfoList;
    private PetInfo mainPetInfo = null;
    private String jwt;
    private long userId;
    private String diaryInfo;
    private int eyeDiseaseInfo;
    private int skinDiseaseInfo;
    private int eyePercentInfo;
    private int skinPercentInfo;
    private int diagnosisOption;

    private final int SAVE = 0;
    private final int EDIT = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pet_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView petImage = view.findViewById(R.id.petinfo_pet_image);
        listButton = view.findViewById(R.id.petinfo_list_button);
        todayDiagnosisLayout = view.findViewById(R.id.petinfo_today_diagnosis_layout);
        todayDiagnosisButton = view.findViewById(R.id.petinfo_today_diagnosis_button);
        todayDiaryLayout = view.findViewById(R.id.petinfo_today_diary_layout);
        todayDiaryButton = view.findViewById(R.id.petinfo_today_diary_button);
        editDiagnosisButton = view.findViewById(R.id.petinfo_edit_diagnosis_imagebutton);
        editDiaryButton = view.findViewById(R.id.petinfo_edit_diary_imagebutton);
        diaryContentTextView = view.findViewById(R.id.petinfo_diary_contenxt);
        diagnosisSkinContentTextView = view.findViewById(R.id.petinfo_skin_diagnosis_contenxt);
        diagnosisEyeContentTextView = view.findViewById(R.id.petinfo_eye_diagnosis_contenxt);
        TextView todayTextView = view.findViewById(R.id.pet_info_list_today);
        TextView name = view.findViewById(R.id.petinfo_name_text);
        TextView age = view.findViewById(R.id.petinfo_age_text);
        TextView species = view.findViewById(R.id.petinfo_species_text);
        petInfoList = new ArrayList<>();

        listButton.setOnClickListener(this);
        todayDiagnosisButton.setOnClickListener(this);
        todayDiaryButton.setOnClickListener(this);
        editDiagnosisButton.setOnClickListener(this);
        editDiaryButton.setOnClickListener(this);

        selectDiagnosisTypeDialog = new Dialog(getContext());
        selectDiagnosisTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectDiagnosisTypeDialog.setContentView(R.layout.select_diagnosis_type);

        selectCameraAlbumDialog = new Dialog(getContext());
        selectCameraAlbumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectCameraAlbumDialog.setContentView(R.layout.camera_album_select_dialog);

        noticeDialog = new Dialog(getContext());
        noticeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        noticeDialog.setContentView(R.layout.notice_dialog);

        Context context = getContext();
        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);
        userId = userInfoService.getUserIdToSharedPreferences(context);

        todayTextView.setText(EditDiaryActivity.getTodayDate());

        Bundle bundle = getArguments();
        if (bundle != null) {
            petInfoList = (ArrayList<PetInfo>) bundle.getSerializable("petInfoList");
            mainPetInfo = (PetInfo) bundle.getSerializable("mainPetInfo");

            getTodayDiaryInfo();
            getTodayDiagnosisInfo();

            Glide.with(this).load(mainPetInfo.getImageUri())
                    .into(petImage);
            name.setText("이름 : " + mainPetInfo.getName());
            age.setText("나이 : " + mainPetInfo.getAge());
            species.setText("종 : " + mainPetInfo.getSpecies());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == listButton) {
            Intent gotoPetInfoListActivity = new Intent(getActivity(), PetInfoListActivity.class);
            gotoPetInfoListActivity.putExtra("petInfoList", petInfoList);
            startActivity(gotoPetInfoListActivity);
        }
        else if (v == todayDiagnosisButton) {
            diagnosisOption = SAVE;
            showSelectDialog();
        }
        else if (v == todayDiaryButton) {
            Intent gotoEditDiaryActivity = new Intent(getActivity(), EditDiaryActivity.class);
            gotoEditDiaryActivity.putExtra("mainPetInfo", mainPetInfo);
            gotoEditDiaryActivity.putExtra("diaryInfo", diaryInfo);
            startActivity(gotoEditDiaryActivity);
        }
        else if (v == editDiagnosisButton) {
            AlertDialog doubleCheckDialog = new AlertDialog.Builder(getContext())
                    .setMessage("다시 진단 하시겠습니까?")
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            diagnosisOption = EDIT;
                            showSelectDialog();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();

        }
        else if (v == editDiaryButton) {
            Intent gotoEditDiaryActivity = new Intent(getActivity(), EditDiaryActivity.class);
            gotoEditDiaryActivity.putExtra("mainPetInfo", mainPetInfo);
            gotoEditDiaryActivity.putExtra("diaryInfo", diaryInfo);
            startActivity(gotoEditDiaryActivity);
        }
    }

    private void showSelectDialog() {
        selectDiagnosisTypeDialog.show();

        Button eyeButton = selectDiagnosisTypeDialog.findViewById(R.id.dialog_eye_button);
        Button skinButton = selectDiagnosisTypeDialog.findViewById(R.id.dialog_skin_button);

        ImageView wrongImage = noticeDialog.findViewById(R.id.notice_wrong_image);
        ImageView correctImage = noticeDialog.findViewById(R.id.notice_correct_image);
        eyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diagnosisType = "eye";
                Glide.with(getContext()).load("https://images.mypetlife.co.kr/content/uploads/2023/01/11101052/AdobeStock_108191982-1536x1025.jpeg")
                        .into(wrongImage);
                Glide.with(getContext()).load("https://www.sjpost.co.kr/news/photo/201808/33816_28613_2157.jpg")
                        .into(correctImage);
                selectDiagnosisTypeDialog.dismiss();
                noticeDialog.show();
            }
        });

        skinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diagnosisType = "skin";
                Glide.with(getContext()).load("https://alwayscare.s3.ap-northeast-2.amazonaws.com/49d827a8-0e40-469b-ac74-c7a7c2a686d5.webp")
                        .into(wrongImage);
                Glide.with(getContext()).load("https://alwayscare.s3.ap-northeast-2.amazonaws.com/82aa24b3-68a4-43c7-b52d-8383cdff78e7.jpg")
                        .into(correctImage);
                selectDiagnosisTypeDialog.dismiss();
                noticeDialog.show();
            }
        });

        Button checkNoticeButton = noticeDialog.findViewById(R.id.notice_check_button);
        checkNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noticeDialog.dismiss();
                selectCameraAlbumDialog.show();
            }
        });


        Button cameraButton = selectCameraAlbumDialog.findViewById(R.id.dialog_camera_button);
        Button albumButton = selectCameraAlbumDialog.findViewById(R.id.dialog_album_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if(takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // error
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(getActivity(),
                                    "com.project.alwayscare.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            activityResultPicture.launch(takePictureIntent);
                        }
                    }
                } else {
                    Log.i("my", "권한 설정 요청");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{android.Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
                selectCameraAlbumDialog.dismiss();
            }
        });

        albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent selectPicture = new Intent(Intent.ACTION_PICK);
                    selectPicture.setType("image/*");
                    activityResultAlbum.launch(selectPicture);
                } else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                }
                selectCameraAlbumDialog.dismiss();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    ActivityResultLauncher<Intent> activityResultPicture = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && currentPhotoPath != null) {
                        Intent gotoCheckDiseaseActivity = new Intent(getActivity(), CheckDiseaseActivity.class);
                        gotoCheckDiseaseActivity.putExtra("url_string", currentPhotoPath);
                        gotoCheckDiseaseActivity.putExtra("option", diagnosisOption);
                        gotoCheckDiseaseActivity.putExtra("mainPetInfo", mainPetInfo);
                        gotoCheckDiseaseActivity.putExtra("type", diagnosisType);
                        startActivity(gotoCheckDiseaseActivity);
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> activityResultAlbum = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        photoUri = result.getData().getData();
                        currentPhotoPath = getRealPathFromUri(photoUri);
                        Log.d("my", currentPhotoPath);
                        Intent gotoCheckDiseaseActivity = new Intent(getActivity(), CheckDiseaseActivity.class);
                        gotoCheckDiseaseActivity.putExtra("url_string", currentPhotoPath);
                        gotoCheckDiseaseActivity.putExtra("option", diagnosisOption);
                        gotoCheckDiseaseActivity.putExtra("mainPetInfo", mainPetInfo);
                        gotoCheckDiseaseActivity.putExtra("type", diagnosisType);
                        startActivity(gotoCheckDiseaseActivity);
                    }
                }
            }
    );

    public static String getTodayDate() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return today.format(formatter);
    }

    private void getTodayDiaryInfo() {
        if (mainPetInfo == null)
            return;

        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        String todayDate = getTodayDate();
        Log.d("my", todayDate);
        long petId = mainPetInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiaryRequest(jwt, petId, todayDate);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "get diary info : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            diaryInfo = jsonResponse.getString("result");
                            todayDiaryButton.setVisibility(View.GONE);
                            editDiaryButton.setVisibility(View.VISIBLE);
                            diaryContentTextView.setVisibility(View.VISIBLE);
                            diaryContentTextView.setText(diaryInfo);
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            todayDiaryButton.setVisibility(View.VISIBLE);
                            editDiaryButton.setVisibility(View.GONE);
                            diaryContentTextView.setVisibility(View.GONE);
                        } else {
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
                    Log.d("my", "failed on get diary info");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
            }
        });
    }

    private void getTodayDiagnosisInfo() {
        if (mainPetInfo == null)
            return;

        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        String todayDate = getTodayDate();
        long petId = mainPetInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiagnosisRequest(jwt, petId, todayDate);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "get diagnosis info : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            JSONObject result = jsonResponse.getJSONObject("result");
                            eyeDiseaseInfo = result.getInt("disease");
                            eyePercentInfo = result.getInt("percent");

                            todayDiagnosisButton.setVisibility(View.GONE);
                            editDiagnosisButton.setVisibility(View.VISIBLE);
                            diagnosisEyeContentTextView.setVisibility(View.VISIBLE);

                            switch (eyeDiseaseInfo) {
                                case 1000:
                                    diagnosisEyeContentTextView.setText("눈 : 건강한 상태입니다.");
                                    break;
                                case 1011:
                                    diagnosisEyeContentTextView.setText("눈 : 결막염이 " + eyePercentInfo + "%로 의심됩니다.");
                                    break;
                                case 1021:
                                    diagnosisEyeContentTextView.setText("눈 : 각막질환이 " + eyePercentInfo + "%로 의심됩니다.");
                                    break;
                                case 1031:
                                    diagnosisEyeContentTextView.setText("눈 : 백내장이 " + eyePercentInfo + "%로 의심됩니다.");
                                    break;
                                case 1061:
                                    diagnosisEyeContentTextView.setText("눈 : 안검질환이 " + eyePercentInfo + "%로 의심됩니다.");
                                    break;
                            }

                            getSkinDiseaseInfo();
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            eyeDiseaseInfo = -1;
                            diagnosisEyeContentTextView.setVisibility(View.GONE);
                            getSkinDiseaseInfo();
                        }

                        else {
                            // 잘못된 요청
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "failed get diagnosis");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
            }
        });
    }
    private void getSkinDiseaseInfo() {
        if (mainPetInfo == null)
            return;

        Retrofit retrofit = new Retrofit.Builder()
                // TODO : input base url
                .baseUrl("http://15.164.234.59:9000/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        String todayDate = getTodayDate();
        long petId = mainPetInfo.getPetId();

        Call<ResponseBody> call = apiService.getDiseaseRequest(jwt, petId, todayDate);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("my", "get diagnosis info : " + responseBody);
                        JSONObject jsonResponse = new JSONObject(responseBody);

                        int code = jsonResponse.getInt("code");
                        String message = jsonResponse.getString("message");

                        if (code == 1000) {
                            // result 객체에서 원하는 정보 추출
                            JSONObject result = jsonResponse.getJSONObject("result");
                            skinDiseaseInfo = result.getInt("disease");
                            skinPercentInfo = result.getInt("percent");

                            todayDiagnosisButton.setVisibility(View.GONE);
                            editDiagnosisButton.setVisibility(View.VISIBLE);
                            diagnosisSkinContentTextView.setVisibility(View.VISIBLE);

                            switch (skinDiseaseInfo) {
                                case 1000:
                                    diagnosisSkinContentTextView.setText("피부 : 건강한 상태입니다.");
                                    break;
                                case 2011:
                                    diagnosisSkinContentTextView.setText("피부 : 구진 플라크가 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                                case 2021:
                                    diagnosisSkinContentTextView.setText("피부 : 비듬 각질 상피성잔고리가 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                                case 2031:
                                    diagnosisSkinContentTextView.setText("피부 : 태선화 과다색소침착이 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                                case 2041:
                                    diagnosisSkinContentTextView.setText("피부 : 농포 여드름이 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                                case 2051:
                                    diagnosisSkinContentTextView.setText("피부 : 미란 궤양이 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                                case 2061:
                                    diagnosisSkinContentTextView.setText("피부 : 결정 종괴가 " + skinPercentInfo + "%로 의심됩니다.");
                                    break;
                            }

                            if (eyeDiseaseInfo == -1) {
                                todayDiagnosisButton.setVisibility(View.VISIBLE);
                                todayDiagnosisButton.setText("오늘의 눈 건상상태 진단하기");
                                editDiagnosisButton.setVisibility(View.GONE);
                            } else {
                                todayDiagnosisButton.setVisibility(View.GONE);
                                editDiagnosisButton.setVisibility(View.VISIBLE);
                            }
                        } else if (message.equals("데이터베이스 연결에 실패하였습니다.")) {
                            diagnosisSkinContentTextView.setVisibility(View.GONE);

                            if (eyeDiseaseInfo == -1) {
                                todayDiagnosisButton.setVisibility(View.VISIBLE);
                                editDiagnosisButton.setVisibility(View.GONE);
                            } else {
                                todayDiagnosisButton.setVisibility(View.VISIBLE);
                                todayDiagnosisButton.setText("오늘의 피부 건상상태 진단하기");
                                editDiagnosisButton.setVisibility(View.GONE);
                            }
                        }
                        else {
                            // 잘못된 요청
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.d("my", e.toString());
                    } catch (IOException e) {
                        Log.d("my", e.toString());
                    }
                    // API 응답 처리
                } else {
                    // API 요청 실패 처리
                    Log.d("my", "failed get diagnosis");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // API 요청 실패 처리
            }
        });
    }

    private String getRealPathFromUri(Uri uri) {
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    filePath = cursor.getString(index);
                }
                cursor.close();
            }
        } else if (uri.getScheme().equals("file")) {
            filePath = uri.getPath();
        }
        return filePath;
    }
}
