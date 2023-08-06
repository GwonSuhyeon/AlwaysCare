package com.project.alwayscare.api_interface;

import com.google.gson.JsonObject;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/users/signup")
    Call<ResponseBody> postSignupRequest(@Body JsonObject requestBody);

    @POST("/users/login")
    Call<ResponseBody> postLoginRequest(@Body JsonObject requestBody);

    @GET("/pets/list/{userId}")
    Call<ResponseBody> getPetListRequest(@Header("Authorization") String jwt,
                                         @Path("userId") long userId);

    @Multipart
    @POST("/pets/save")
    Call<ResponseBody> postPetRequest(@Header("Authorization") String jwt,
                                      @Part("SaveReq") RequestBody requestBody,
                                      @Part MultipartBody.Part imageFile);

    @Multipart
    @PATCH("/pets/editAll/{petId}")
    Call<ResponseBody> editAllPetRequest(@Header("Authorization") String jwt,
                                      @Path("petId") long petId,
                                      @Part("EditPetReq") RequestBody requestBody,
                                      @Part MultipartBody.Part imageFile);

    @PATCH("/pets/edit/{petId}")
    Call<ResponseBody> editPetRequest(@Header("Authorization") String jwt,
                                      @Path("petId") long petId,
                                      @Body JsonObject requestBody);

    @PATCH("/pets/delete/{petId}")
    Call<ResponseBody> deletePetRequest(@Header("Authorization") String jwt,
                                        @Path("petId") long petId);

    @GET("/diarys/print/{petId}")
    Call<ResponseBody> getDiaryRequest(@Header("Authorization") String jwt,
                                       @Path("petId") long petId,
                                       @Query("date") String time);

    @POST("/diarys/save/{petId}")
    Call<ResponseBody> postDiaryRequest(@Header("Authorization") String jwt,
                                        @Path("petId") long petId,
                                        @Body JsonObject requestBody);

    @PATCH("/diarys/edit/{petId}")
    Call<ResponseBody> editDiaryRequest(@Header("Authorization") String jwt,
                                        @Path("petId") long petId,
                                        @Body JsonObject requestBody);

    @GET("/diarys/list/{petId}")
    Call<ResponseBody> getDiaryDatesRequest(@Header("Authorization") String jwt,
                                            @Path("petId") long petId);

    @GET("/diagnosis/print/{petId}")
    Call<ResponseBody> getDiagnosisRequest(@Header("Authorization") String jwt,
                                           @Path("petId") long petId,
                                           @Query("date") String time);

    @POST("/diagnosis/save/{petId}")
    Call<ResponseBody> postDiagnosisRequest(@Header("Authorization") String jwt,
                                            @Path("petId") long petId,
                                            @Body JsonObject requestBody);

    @PATCH("/diagnosis/edit/{petId}")
    Call<ResponseBody> patchDiagnosisRequest(@Header("Authorization") String jwt,
                                             @Path("petId") long petId,
                                             @Body JsonObject requestBody);

    @GET("/disease/print/{petId}")
    Call<ResponseBody> getDiseaseRequest(@Header("Authorization") String jwt,
                                           @Path("petId") long petId,
                                           @Query("date") String time);

    @POST("/disease/save/{petId}")
    Call<ResponseBody> postDiseaseRequest(@Header("Authorization") String jwt,
                                            @Path("petId") long petId,
                                            @Body JsonObject requestBody);

    @PATCH("/disease/edit/{petId}")
    Call<ResponseBody> patchDiseaseRequest(@Header("Authorization") String jwt,
                                             @Path("petId") long petId,
                                             @Body JsonObject requestBody);
    @GET("/user/hospital")
    Call<ResponseBody> getHospitalRequest(@Query("address") String address);

    @Multipart
    @POST("/always-care/model/eye")
    Call<ResponseBody> postDiagnosisEye(@Part MultipartBody.Part imageFile,
                                        @Part("image_type") RequestBody imageType,
                                        @Part("userId") RequestBody userId);

    @Multipart
    @POST("/always-care/model/skin")
    Call<ResponseBody> postDiagnosisSkin(@Part MultipartBody.Part imageFile,
                                        @Part("image_type") RequestBody imageType,
                                        @Part("userId") RequestBody userId);
}

