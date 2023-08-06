package com.project.alwayscare.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.JsonObject;
import com.project.alwayscare.R;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.data.PetInfo;
import com.project.alwayscare.service.SharedPreferenceService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditPetInfoActivity extends AppCompatActivity {

    private ImageView petImageView;
    private Button editPetImageButton;
    private Dialog selectCameraAlbumDialog;
    private String currentPhotoPath;
    private String existPhotoUri;
    private Uri photoUri;
    private RadioGroup radioGroup;
    private EditText editName;
    private EditText editAge;
    private EditText editSpecies;
    private Button saveButton;
    private int type = 0;
    private String jwt;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pet_info);

        petImageView = findViewById(R.id.edit_pet_info_imageview);
        editPetImageButton = findViewById(R.id.edit_pet_image_button);
        selectCameraAlbumDialog = new Dialog(EditPetInfoActivity.this);
        selectCameraAlbumDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectCameraAlbumDialog.setContentView(R.layout.camera_album_select_dialog);
        radioGroup = findViewById(R.id.edit_pet_info_radiogroup);
        editName = findViewById(R.id.edit_pet_info_name_edittext);
        editAge = findViewById(R.id.edit_pet_info_age_edittext);
        editSpecies = findViewById(R.id.edit_pet_info_species_edittext);
        saveButton = findViewById(R.id.edit_pet_info_save_button);
        Context context = EditPetInfoActivity.this;

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        jwt = userInfoService.getJWTFromSharedPreferences(context);
        userId = userInfoService.getUserIdToSharedPreferences(context);

        PetInfo petInfo = (PetInfo) getIntent().getSerializableExtra("mainPetInfo");
        existPhotoUri = null;
        currentPhotoPath = null;
        if (petInfo != null) {
            Glide.with(this).load(petInfo.getImageUri())
                    .into(petImageView);
            existPhotoUri = petInfo.getImageUri();
            if (petInfo.getType() == 0)
                radioGroup.check(R.id.edit_pet_info_dog_button);
            else
                radioGroup.check(R.id.edit_pet_info_cat_button);

            editName.setText(petInfo.getName());
            editAge.setText(String.valueOf(petInfo.getAge()));
            editSpecies.setText(petInfo.getSpecies());
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.edit_pet_info_dog_button:
                        type = 0;
                        break;
                    case R.id.edit_pet_info_cat_button:
                        type = 1;
                        break;
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editName.getText().toString();
                String age = editAge.getText().toString();
                String petType = String.valueOf(type);
                String species = editSpecies.getText().toString();

                if (name.isEmpty() || age.isEmpty() || species.isEmpty() || (existPhotoUri == null && currentPhotoPath == null)) {
                    Toast.makeText(EditPetInfoActivity.this, "정보를 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    Integer.parseInt(age);
                } catch (NumberFormatException e) {
                    Toast.makeText(EditPetInfoActivity.this, "나이는 숫자로 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://15.164.234.59:9000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("name", name);
                jsonObject.addProperty("age", age);
                jsonObject.addProperty("type", type);
                jsonObject.addProperty("species", species);

                RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonObject.toString());

                Call<ResponseBody> call;
                // 저장
                if (petInfo == null) {
                    Log.d("my", "save pet info");
                    File file = new File(currentPhotoPath);
                    RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);
                    call = apiService.postPetRequest(jwt, requestBody, filePart);
                }
                // 수정
                else {
                    Log.d("my", "edit pet info");
                    if (currentPhotoPath != null) {
                        File file = new File(currentPhotoPath);
                        RequestBody fileRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                        MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", file.getName(), fileRequestBody);
                        Log.d("my", "photo info : " + currentPhotoPath);
                        call = apiService.editAllPetRequest(jwt, petInfo.getPetId(), requestBody, filePart);
                    } else {
                        Log.d("my", "existing : " + existPhotoUri);
                        Log.d("my", "pet info : " + petInfo.getPetId());
                        Log.d("my", "jwt : " + jwt);
                        call = apiService.editPetRequest(jwt, petInfo.getPetId(), jsonObject);
                    }
                }

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                Log.d("my", "save/save pet : " + responseBody);
                                JSONObject jsonResponse = new JSONObject(responseBody);

                                int code = jsonResponse.getInt("code");
                                String message = jsonResponse.getString("message");

                                if (code == 1000) {
                                    Toast.makeText(EditPetInfoActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                                    Intent gotoPetInfoListActivity = new Intent(EditPetInfoActivity.this, PetInfoListActivity.class);
                                    startActivity(gotoPetInfoListActivity);
                                } else {
                                    Toast.makeText(EditPetInfoActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            // API 요청 실패 처리
                            Log.d("my", "failed on save / edit pet info");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // API 요청 실패 처리
                        Log.d("my", "failed on save / edit pet info");
                    }
                });
            }
        });

        editPetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCameraAlbumDialog.show();

                Button cameraButton = selectCameraAlbumDialog.findViewById(R.id.dialog_camera_button);
                Button albumButton = selectCameraAlbumDialog.findViewById(R.id.dialog_album_button);

                cameraButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                                checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                            if(takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                    // error
                                }
                                // Continue only if the File was successfully created
                                if (photoFile != null) {
                                    photoUri = FileProvider.getUriForFile(EditPetInfoActivity.this,
                                            "com.project.alwayscare.fileprovider",
                                            photoFile);
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

                                    activityResultPicture.launch(takePictureIntent);
                                }
                            }
                        } else {
                            Log.i("my", "권한 설정 요청");
                            ActivityCompat.requestPermissions(EditPetInfoActivity.this,
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
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            Intent selectPicture = new Intent(Intent.ACTION_PICK);
                            selectPicture.setType("image/*");
                            activityResultAlbum.launch(selectPicture);

                        } else {
                            ActivityCompat.requestPermissions(EditPetInfoActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                        }
                        selectCameraAlbumDialog.dismiss();
                    }
                });
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
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
                        Glide.with(EditPetInfoActivity.this).load(currentPhotoPath)
                                .into(petImageView);
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

                        Glide.with(EditPetInfoActivity.this).load(currentPhotoPath)
                                .into(petImageView);
                    }
                }
            }
    );

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

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