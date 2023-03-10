package kr.co.widgetweather.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        holder.tv_pop.setText(items.get(position).tvPop); // 강수확률 텍스트
//        holder.img_sky.setText(items.get(position).imgSky); // 하늘상태
        holder.img_sky_max.setImageResource(items.get(position).imgSkyMax);
        holder.img_sky_min.setImageResource(items.get(position).imgSkyMin);
        holder.tv_tmp_week.setText(items.get(position).tvTmpWeek); // 최고온도
        holder.tv_tmn_week.setText(items.get(position).tvTmnWeek); // 최저온도

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class VH extends RecyclerView.ViewHolder{

    TextView tv_week;
    TextView tv_pop;
    TextView tv_tmp_week;
    TextView tv_tmn_week;
//    TextView img_sky;
    ImageView img_sky_max;
    ImageView img_sky_min;


    public VH(@NonNull View itemView) {
        super(itemView);
        tv_week= itemView.findViewById(R.id.week);
        tv_pop= itemView.findViewById(R.id.tv_pop);
        tv_tmp_week= itemView.findViewById(R.id.tmp_week);
        tv_tmn_week= itemView.findViewById(R.id.tmn_week);
        //img_sky= itemView.findViewById(R.id.img_sky);
        img_sky_max= itemView.findViewById(R.id.img_sky_max);
        img_sky_min= itemView.findViewById(R.id.img_sky_min);

    }
}


