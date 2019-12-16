package com.rohan.trackmyrideversion0.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.rohan.trackmyrideversion0.R;
import com.rohan.trackmyrideversion0.pojos.Rider;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RidersAdapter extends ListAdapter<Rider, RidersAdapter.ViewHolder> {
    private RiderClickInterface mClickInterface;

    public interface RiderClickInterface {
        void onRiderClick(int position, List<Rider> riders);

        void onDirectionsClick(double lat, double lng);
    }

    private static final DiffUtil.ItemCallback<Rider> DIFF_CALLBACK = new DiffUtil.ItemCallback<Rider>() {
        @Override
        public boolean areItemsTheSame(@NonNull Rider oldItem, @NonNull Rider newItem) {
            return oldItem.id.equals(newItem.id);
        }

        @Override
        public boolean areContentsTheSame(@NonNull Rider oldItem, @NonNull Rider newItem) {
            return oldItem.name.equalsIgnoreCase(newItem.name)
                    && oldItem.status == newItem.status
                    && oldItem.orderNumber == newItem.orderNumber;
        }
    };

    public RidersAdapter(RiderClickInterface clickInterface) {
        super(DIFF_CALLBACK);
        mClickInterface = clickInterface;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_rider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_pick_drop)
        TextView tvPickDrop;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.btn_pick_drop)
        Button btnPickDrop;
        @BindView(R.id.btn_get_directions)
        Button btnGetDirections;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            btnPickDrop.setOnClickListener(view -> mClickInterface.onRiderClick(getAdapterPosition(), getCurrentList()));
            btnGetDirections.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Rider rider = getItem(getAdapterPosition());
                    if (rider.status == Rider.STATUS_TO_BE_PICKED) {
                        mClickInterface.onDirectionsClick(rider.originLat, rider.originLng);
                    } else {
                        mClickInterface.onDirectionsClick(rider.destinationLat, rider.destinationLng);
                    }
                }
            });
        }

        public void bind(int position) {
            Rider rider = getItem(position);
            if (rider.status == Rider.STATUS_TO_BE_PICKED) {
                tvPickDrop.setText(R.string.pick_up_);
                btnPickDrop.setText(R.string.pick_up);
            } else {
                tvPickDrop.setText(R.string.drop_);
                btnPickDrop.setText(R.string.drop);
            }
            tvName.setText(rider.name);
        }
    }
}
