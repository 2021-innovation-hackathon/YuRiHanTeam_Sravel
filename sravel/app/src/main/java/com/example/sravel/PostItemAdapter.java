package com.example.sravel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PostItemAdapter extends RecyclerView.Adapter<PostItemAdapter.ViewHolder> {

    private ArrayList<SnapShotDTO> mData = null;
    Context context;
    OnPostItemClickListener listener;

    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }

    public void setOnItemClicklistener(OnPostItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnPostItemClickListener {
        public void onItemClick(PostItemAdapter.ViewHolder holder, View view, int position);
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);

            // 뷰 객체에 대한 참조. (hold strong reference)
            textView1 = itemView.findViewById(R.id.textView_time_post);
            imageView = itemView.findViewById(R.id.imageView_post);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(ViewHolder.this, v, position);
                    }
                }
            });
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    PostItemAdapter(ArrayList<SnapShotDTO> list) {
        mData = list;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public PostItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.post_item, parent, false);
        PostItemAdapter.ViewHolder vh = new PostItemAdapter.ViewHolder(view);

        return vh;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(PostItemAdapter.ViewHolder holder, int position) {
        SnapShotDTO snapShotDTO = mData.get(position);
        String text = snapShotDTO.time;
        holder.textView1.setText(text);
        Glide.with(context).load(snapShotDTO.imageUrl).into(holder.imageView);

        Log.d("PostTest", "text");
        //holder.imageView.setImageResource(snapShotDTO.imageUrl);
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public SnapShotDTO getItem(int position) {
        return mData.get(position);
    }
}
