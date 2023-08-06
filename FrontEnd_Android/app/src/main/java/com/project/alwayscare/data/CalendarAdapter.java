package com.project.alwayscare.data;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.alwayscare.R;

import java.time.LocalDate;
import java.util.ArrayList;


public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    ArrayList<String> dayList;
    ArrayList<Integer> dayToCheckList;
    private CalendarListener calendarListener;
    private CalendarViewHolder selectedHolder;

    public CalendarAdapter(ArrayList<String> dayList, ArrayList<Integer> dayToCheckList, CalendarListener calendarListener) {
        this.dayList = dayList;
        this.dayToCheckList = dayToCheckList;
        this.calendarListener = calendarListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.dayText.setText(dayList.get(position));

        if (!dayList.get(position).isEmpty()) {
            if (Integer.parseInt(dayList.get(position)) == LocalDate.now().getDayOfMonth()) {
                selectedHolder = holder;
                holder.dayText.setTextColor(Color.rgb(0x00, 0xbf, 0xff));
            }

            if (dayToCheckList.contains(Integer.parseInt(dayList.get(position)))) {
                if (Integer.parseInt(dayList.get(position)) == LocalDate.now().getDayOfMonth()) {
                    selectedHolder = holder;
                    holder.dayText.setTextColor(Color.rgb(0xFF, 0x7f, 0x00));
                }

                holder.dayLayout.setBackgroundColor(Color.rgb(0x5C, 0xC6, 0x7C));
            }
        }

        holder.dayLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (calendarListener != null) {
                    selectedHolder.dayText.setTextColor(Color.BLACK);

                    if (dayToCheckList.contains(Integer.parseInt(dayList.get(holder.getAdapterPosition())))) {
                        holder.dayText.setTextColor(Color.rgb(0xFF, 0x7f, 0x00));
                    } else {
                        holder.dayText.setTextColor(Color.rgb(0x00, 0xbf, 0xff));
                    }
                    selectedHolder = holder;
                    calendarListener.onItemClicked(Integer.parseInt(holder.dayText.getText().toString()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayList.size();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {

        LinearLayout dayLayout;
        TextView dayText;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            dayLayout = itemView.findViewById(R.id.calendar_day_layout);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}
