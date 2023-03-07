package kr.co.widgetweather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
        holder.img_pop.setImageResource(items.get(position).imgPop); // 강수확률 이미지
        holder.tv_pop.setText(items.get(position).tvPop); // 강수확률 텍스트
        holder.img_sky.setImageResource(items.get(position).imgSky); // 하늘상태 이미지
        holder.tv_tmx_week.setText(items.get(position).tvTmpWeek); // 1시간 온도

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class VH extends RecyclerView.ViewHolder{

    TextView tv_week;
    ImageView img_pop;
    TextView tv_pop;
    TextView tv_tmx_week;
    ImageView img_sky;


    public VH(@NonNull View itemView) {
        super(itemView);
        tv_week= itemView.findViewById(R.id.week);
        img_pop= itemView.findViewById(R.id.img_pop);
        tv_pop= itemView.findViewById(R.id.tv_pop);
        tv_tmx_week= itemView.findViewById(R.id.tmp_week);
        img_sky= itemView.findViewById(R.id.img_sky);
    }
}


