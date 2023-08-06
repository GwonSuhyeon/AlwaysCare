package com.project.alwayscare.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationBarView;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.fragment.CalenderFragment;
import com.project.alwayscare.fragment.PetInfoFragment;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeActivity extends AppCompatActivity {

    private NavigationBarView bottomNavigationView;
    private PetInfoFragment petInfoFragment;
    private Bundle bundleToPetInfoFragment;
    private PetInfo mainPetInfo;
    private Dialog selectDiagnosisTypeDialog;
    private Dialog selectCameraAlbumDialog;
    private String currentPhotoPath;
    private Uri photoUri;
    private String diagnosisType;
    private Context context;
    private String jwt;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.home_bottom_navigation);
        bundleToPetInfoFragment = new Bundle();
        petInfoFragment = new PetInfoFragment();

        context = HomeActivity.this;

        selectDiagnosisTypeDialog = new Dialog(context);
        selectDiagnosisTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectDiagnosisTypeDialog.setContentView(R.layout.camera_album_select_dialog);

        selectCameraAlbumDialog = new Dialog(context);
        selectCameraAlbumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectCameraAlbumDialog.setContentView(R.layout.select_diagnosis_type);

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);
        userId = userInfoService.getUserIdToSharedPreferences(context);

        // PetInfoListActivity 에서 넘겨준 새로운 mainPetInfo가 있다면 해당 pet으로 화면 구성
        mainPetInfo = (PetInfo) getIntent().getSerializableExtra("mainPetInfo");
        if (mainPetInfo != null) {
            bundleToPetInfoFragment.putSerializable("mainPetInfo", mainPetInfo);
            petInfoFragment.setArguments(bundleToPetInfoFragment);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.home_framelayout, petInfoFragment).commit();
        }
        else {
            getPetList();
        }

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.page_1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_framelayout, petInfoFragment).commit();
                        return true;
                    case R.id.page_2:
                        Intent intent = new Intent(HomeActivity.this, NearbyHospitalActivity.class);
                        startActivity(intent);
                        return true;
                    case R.id.page_3:
                        Bundle bundleToCalendarFragment = new Bundle();
                        bundleToCalendarFragment.putSerializable("mainPetInfo", mainPetInfo);
                        CalenderFragment calenderFragment = new CalenderFragment();
                        calenderFragment.setArguments(bundleToCalendarFragment);
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_framelayout, calenderFragment).commit();
                        return true;
                }
                return true;
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

                            JSONObject petObject = resultArray.getJSONObject(0);

                            String petImageURL = petObject.getString("petImageURL");
                            String petSpecies = petObject.getString("petSpecies");
                            int petId = petObject.getInt("petId");
                            String petName = petObject.getString("petName");
                            int petAge = petObject.getInt("petAge");
                            int petType = petObject.getInt("petType");

                            mainPetInfo = new PetInfo(petId, petName, petAge, petImageURL, petSpecies, petType);
                            bundleToPetInfoFragment.putSerializable("mainPetInfo", mainPetInfo);
                            petInfoFragment.setArguments(bundleToPetInfoFragment);

                            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.add(R.id.home_framelayout, petInfoFragment).commit();
                        }
                        // 반려동물 정보가 하나도 없다면 EditPetInfoActivity로 이동
                        else if (code == 2070) {
                            Intent gotoEditPetInfoActivity = new Intent(HomeActivity.this, EditPetInfoActivity.class);
                            startActivity(gotoEditPetInfoActivity);
                        } else {
                            // 잘못된 요청
                            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
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

    private void showSelectDialog() {
        selectDiagnosisTypeDialog.show();

        Button eyeButton = selectDiagnosisTypeDialog.findViewById(R.id.dialog_eye_button);
        Button skinButton = selectDiagnosisTypeDialog.findViewById(R.id.dialog_skin_button);

        eyeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diagnosisType = "eye";
                selectDiagnosisTypeDialog.dismiss();
                selectCameraAlbumDialog.show();
            }
        });

        skinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diagnosisType = "skin";
                selectDiagnosisTypeDialog.dismiss();
                selectCameraAlbumDialog.show();
            }
        });


        Button cameraButton = selectCameraAlbumDialog.findViewById(R.id.dialog_camera_button);
        Button albumButton = selectCameraAlbumDialog.findViewById(R.id.dialog_album_button);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context.checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    if(takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // error
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            photoUri = FileProvider.getUriForFile(context,
                                    "com.project.alwayscare.fileprovider",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            activityResultPicture.launch(takePictureIntent);
                        }
                    }
                } else {
                    Log.i("my", "권한 설정 요청");
                    ActivityCompat.requestPermissions(HomeActivity.this,
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
                if (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent selectPicture = new Intent(Intent.ACTION_PICK);
                    selectPicture.setType("image/*");
                    activityResultAlbum.launch(selectPicture);
                } else {
                    ActivityCompat.requestPermissions(HomeActivity.this,
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
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                        Intent gotoCheckDiseaseActivity = new Intent(context, CheckDiseaseActivity.class);
                        gotoCheckDiseaseActivity.putExtra("url_string", currentPhotoPath);
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
                        Intent gotoCheckDiseaseActivity = new Intent(context, CheckDiseaseActivity.class);
                        gotoCheckDiseaseActivity.putExtra("url_string", currentPhotoPath);
                        gotoCheckDiseaseActivity.putExtra("mainPetInfo", mainPetInfo);
                        gotoCheckDiseaseActivity.putExtra("type", diagnosisType);
                        startActivity(gotoCheckDiseaseActivity);
                    }
                }
            }
    );
    private String getRealPathFromUri(Uri uri) {
        String filePath = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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