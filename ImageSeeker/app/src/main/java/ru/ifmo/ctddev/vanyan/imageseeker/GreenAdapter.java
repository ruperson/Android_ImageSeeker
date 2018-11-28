package ru.ifmo.ctddev.vanyan.imageseeker;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.squareup.picasso.Picasso;

public class GreenAdapter extends RecyclerView.Adapter<GreenAdapter.NumberViewHolder> {

    private static final String TAG = GreenAdapter.class.getSimpleName();

    final private ListItemClickListener mOnClickListener;

    private List<String> small_pics;
    private List<String> big_pics ;
    private List<String> descr;

    public interface ListItemClickListener {
        void onListItemClick(String link);
    }

    public GreenAdapter(List<String> small_pics, List<String> big_pics, List<String> descr, ListItemClickListener listener) {
        this.small_pics = small_pics;
        this.big_pics = big_pics;
        this.descr = descr;
        mOnClickListener = listener;
    }


    @Override
    public NumberViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        NumberViewHolder viewHolder = new NumberViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(NumberViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return descr.size();
    }

    class NumberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView listItemNumberView;
        ImageView imagePreview;
        public NumberViewHolder(View itemView) {
            super(itemView);

            listItemNumberView = itemView.findViewById(R.id.item_developer_name);
            imagePreview = itemView.findViewById(R.id.item_developer_image);
            itemView.setOnClickListener(this);
        }

        void bind(int listIndex) {
            listItemNumberView.setText(descr.get(listIndex));
            Picasso.get().load(small_pics.get(listIndex)).into(imagePreview);

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(big_pics.get(clickedPosition));
        }
    }
}