package com.geekykel.travelnigeria;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Geeky Kelvin on 8/2/2019.
 * Email: Kelvinator4leo@gmail.com
 */
public class TravelDealAdapter extends RecyclerView.Adapter<TravelDealAdapter.TravelDealViewHolder> {

    private static final String TAG = TravelDealAdapter.class.getSimpleName();

    private ArrayList<TravelDeal> mTravelDeals;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;

    private ImageView travelImageDeal;

    public TravelDealAdapter() {

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mTravelDeals = FirebaseUtil.mDeals;

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TravelDeal travelDeal = dataSnapshot.getValue(TravelDeal.class);
                Log.d(TAG , travelDeal.getTitle());
                travelDeal.setId(dataSnapshot.getKey());
                mTravelDeals.add(travelDeal);
                notifyItemInserted(mTravelDeals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @Override
    public TravelDealAdapter.TravelDealViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new TravelDealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TravelDealAdapter.TravelDealViewHolder holder, int position) {
        TravelDeal travelDeal = mTravelDeals.get(position);
        holder.bind(travelDeal);

    }

    @Override
    public int getItemCount() {
        if (mTravelDeals == null) {
            return 0;
        }
        return mTravelDeals.size();
    }

    public class TravelDealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewTitle;
        TextView textViewDescription;
        TextView textViewPrice;

        public TravelDealViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textTitle);
            textViewDescription = itemView.findViewById(R.id.textDescription);
            textViewPrice = itemView.findViewById(R.id.textPrice);
            travelImageDeal = itemView.findViewById(R.id.image);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal travelDeal) {
            textViewTitle.setText(travelDeal.getTitle());
            textViewDescription.setText(travelDeal.getDescription());
            textViewPrice.setText(travelDeal.getPrice());
            showImage(travelDeal.getImageUrl());
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            TravelDeal selectedDeal = mTravelDeals.get(position);
            Intent intent = new Intent(view.getContext(), DealActivity.class);
            intent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(intent);
        }

        private void showImage(String url) {
            if (url != null && !url.isEmpty()) {
                Picasso.get()
                        .load(url)
                        .resize(160, 160)
                        .centerCrop()
                        .into(travelImageDeal);
            }
        }
    }
}
