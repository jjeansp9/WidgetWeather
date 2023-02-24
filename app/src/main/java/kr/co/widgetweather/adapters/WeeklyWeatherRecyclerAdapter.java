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

import kr.co.widgetweather.R;
import kr.co.widgetweather.model.WeeklyWeatherItem;

public class WeeklyWeatherRecyclerAdapter extends RecyclerView.Adapter<VH> {

    Context context;
    ArrayList<WeeklyWeatherItem> items;

    public WeeklyWeatherRecyclerAdapter(Context context, ArrayList<WeeklyWeatherItem> items){
        this.context = context;
        this.items= items;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context= parent.getContext();
        LayoutInflater inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_recycler_weather_weekly, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tv_week.setText(items.get(position).tvWeek); // 요일
        holder.tv_tmx_week.setText(items.get(position).tvTmxWeek); // 주간 최고온도
        holder.tv_tmn_week.setText(items.get(position).tvTmnWeek); // 주간 최저온도
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

class VH extends RecyclerView.ViewHolder{

    TextView tv_week;
    TextView tv_tmx_week;
    TextView tv_tmn_week;

    public VH(@NonNull View itemView) {
        super(itemView);
        tv_week= itemView.findViewById(R.id.week);
        tv_tmx_week= itemView.findViewById(R.id.tmx_week);
        tv_tmn_week= itemView.findViewById(R.id.tmn_week);
    }
}


