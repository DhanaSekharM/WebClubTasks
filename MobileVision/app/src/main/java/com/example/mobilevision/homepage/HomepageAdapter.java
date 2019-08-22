package com.example.mobilevision.homepage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilevision.R;
import com.example.mobilevision.database.Bills;
import com.example.mobilevision.database.DatabaseHelper;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

/**
 * Recycler view adapter to display the list of stored information
 */
public class HomepageAdapter extends RecyclerView.Adapter<HomepageAdapter.HomepageAdapterViewHolder> {

    private ArrayList<Bills> prices;
    private Context context;
    private RecyclerView recyclerView;  // for the snackbar
    private Bills removedItem;

    public HomepageAdapter(RecyclerView recyclerView, ArrayList<Bills> prices, Context context) {
        this.recyclerView = recyclerView;
        this.prices = prices;
        this.context = context;
    }


    @NonNull
    @Override
    public HomepageAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_homepage_prices, parent, false);

        return new HomepageAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomepageAdapterViewHolder holder, final int position) {
        holder.price.setText("Rs."+prices.get(position).getPrice());
        holder.date.setText(prices.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return prices.size();
    }

    public class HomepageAdapterViewHolder extends RecyclerView.ViewHolder{
        private TextView price, date;
        private ImageView deleteCard;
        public HomepageAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            price = itemView.findViewById(R.id.homepage_tv_price);
            date = itemView.findViewById(R.id.homepage_tv_date);
            deleteCard = itemView.findViewById(R.id.homepage_iv_delete);

            //for deleting a card
            deleteCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = getAdapterPosition();  // get the index of the deleted item
                    deleteItem(i);
                    showSnackBar(i);
                }
            });
        }

    }

    /**
     * Display a snackbar with an undo option
     * @param position index of the deleted item in the original list
     */
    private void showSnackBar(final int position) {
        Snackbar undoSnackBar = Snackbar.make(recyclerView, "Deleted", Snackbar.LENGTH_LONG);

        //Add an undo option to the snackbar
        undoSnackBar.setAction("Undo", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reinsert the item if undo is pressed
                prices.add(position, removedItem);
                notifyItemInserted(position);
            }
        });

        //Callback to detect dismissal of snackbar
        undoSnackBar.addCallback(new Snackbar.Callback() {

            @Override
            public void onShown(Snackbar sb) {
                super.onShown(sb);
            }

            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);

                //delete the item from database only if the user does not press the undo option
                if(event == Snackbar.Callback.DISMISS_EVENT_MANUAL || event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT
                        || event == Snackbar.Callback.DISMISS_EVENT_SWIPE || event == Snackbar.Callback.DISMISS_EVENT_CONSECUTIVE) {
                    DatabaseHelper.getInstance(context)
                            .getDatabase()
                            .billsDao()
                            .delete(removedItem);
                }
            }
        });

        undoSnackBar.show();


    }

    private void deleteItem(int position) {

        //remove the item only from the list but not from the database
        removedItem = prices.get(position);
        prices.remove(position);
        notifyItemRemoved(position);
    }
}
