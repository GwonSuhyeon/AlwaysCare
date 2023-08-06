package com.project.alwayscare.data;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.project.alwayscare.R;
import com.project.alwayscare.activity.EditPetInfoActivity;
import com.project.alwayscare.activity.HomeActivity;
import com.project.alwayscare.api_interface.ApiService;
import com.project.alwayscare.service.SharedPreferenceService;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PetInfoAdapter extends RecyclerView.Adapter<PetInfoAdapter.ViewHolder> {

    private final Context context;
    private final List<PetInfo> petInfoList;

    public PetInfoAdapter(Context context, List<PetInfo> petInfoList) {
        this.context = context;
        this.petInfoList = petInfoList;
    }

    @NonNull
    @Override
    public PetInfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pet_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PetInfoAdapter.ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext())
                .load(petInfoList.get(position).getImageUri())
                .into(holder.petImageView);
        holder.petNameTextView.setText(petInfoList.get(position).getName());

        SharedPreferenceService userInfoService = new SharedPreferenceService();
        String jwt = userInfoService.getJWTFromSharedPreferences(context);

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoHomeActivity = new Intent(context, HomeActivity.class);
                gotoHomeActivity.putExtra("mainPetInfo", petInfoList.get(holder.getAdapterPosition()));
                context.startActivity(gotoHomeActivity);
            }
        });

        holder.editImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoEditPetInfoActivity = new Intent(context, EditPetInfoActivity.class);
                gotoEditPetInfoActivity.putExtra("mainPetInfo", petInfoList.get(holder.getAdapterPosition()));
                context.startActivity(gotoEditPetInfoActivity);
            }
        });

        holder.deleteImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Retrofit retrofit = new Retrofit.Builder()
                        // TODO : input base url
                        .baseUrl("http://15.164.234.59:9000/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                ApiService apiService = retrofit.create(ApiService.class);

                Call<ResponseBody> call = apiService.deletePetRequest(jwt, petInfoList.get(holder.getAdapterPosition()).getPetId());
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                JSONObject jsonResponse = new JSONObject(responseBody);
                                Log.d("my", "delete pet success");

                                int code = jsonResponse.getInt("code");
                                String message = jsonResponse.getString("message");

                                if (code == 1000) {
                                    // 성공
                                    petInfoList.remove(holder.getAdapterPosition());
                                    notifyItemRemoved(holder.getAdapterPosition());
                                } else {
                                    // 잘못된 요청
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            // API 응답 처리
                        } else {
                            // API 요청 실패 처리
                            Log.d("my", "delete pet failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        // API 요청 실패 처리
                        Log.d("my", "delete pet failed");
                    }
                });
            }
        });
    }


    @Override
    public int getItemCount() {
        return petInfoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView petImageView;
        TextView petNameTextView;
        ImageButton editImageButton;
        ImageButton deleteImageButton;
        LinearLayout layout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            layout = itemView.findViewById(R.id.pet_list_layout);
            petImageView = itemView.findViewById(R.id.pet_list_item_imageview);
            petNameTextView = itemView.findViewById(R.id.pet_list_item_name_textview);
            editImageButton = itemView.findViewById(R.id.pet_list_item_edit_imagebutton);
            deleteImageButton = itemView.findViewById(R.id.pet_list_item_delete_iamgebutton);
        }
    }
}
