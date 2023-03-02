package kr.co.widgetweather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.co.widgetweather.R;
import kr.co.widgetweather.model.Item;
import kr.co.widgetweather.model.WeeklyWeatherItem;

public class WeeklyWeatherRecyclerAdapter extends RecyclerView.Adapter<VH> {

    Context context;
    List<WeeklyWeatherItem> items;

    public WeeklyWeatherRecyclerAdapter(Context context, List<WeeklyWeatherItem> items) {
        this.context = context;
        this.items = items;
    }
    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater= LayoutInflater.from(context);
        View itemView= layoutInflater.inflate(R.layout.item_recycler_weather_weekly, parent, false);

        VH holder= new VH(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv_week.setText(items.get(position).tvWeek); // 요일
        holder.tv_tmx_week.setText(items.get(position).tvTmpWeek); // 주간 온도

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class VH extends RecyclerView.ViewHolder{

    TextView tv_week;
    TextView tv_tmx_week;

    public VH(@NonNull View itemView) {
        super(itemView);
        tv_week= itemView.findViewById(R.id.week);
        tv_tmx_week= itemView.findViewById(R.id.tmp_week);
    }
}


