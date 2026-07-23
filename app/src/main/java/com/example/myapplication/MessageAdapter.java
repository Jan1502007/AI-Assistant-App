package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private OnRetryListener retryListener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public interface OnRetryListener {
        void onRetry();
    }

    public MessageAdapter(List<Message> messages, OnRetryListener retryListener) {
        this.messages = messages;
        this.retryListener = retryListener;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case Message.TYPE_USER:
                return new MessageViewHolder(inflater.inflate(R.layout.chat_item_user, parent, false));
            case Message.TYPE_LOADING:
                return new LoadingViewHolder(inflater.inflate(R.layout.chat_item_loading, parent, false));
            case Message.TYPE_ERROR:
                return new ErrorViewHolder(inflater.inflate(R.layout.chat_item_error, parent, false));
            case Message.TYPE_AI:
            default:
                return new MessageViewHolder(inflater.inflate(R.layout.chat_item_ai, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        // Add entry animation
        setAnimation(holder.itemView, position);

        if (holder instanceof MessageViewHolder) {
            MessageViewHolder msgHolder = (MessageViewHolder) holder;
            msgHolder.tvMessage.setText(message.getContent());
            if (msgHolder.tvTimestamp != null) {
                msgHolder.tvTimestamp.setText(timeFormat.format(new Date(message.getTimestamp())));
            }
        } else if (holder instanceof ErrorViewHolder) {
            ((ErrorViewHolder) holder).tvErrorMessage.setText(message.getContent());
            ((ErrorViewHolder) holder).btnRetry.setOnClickListener(v -> {
                if (retryListener != null) retryListener.onRetry();
            });
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // Simple fade and slide up animation for new items
        Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), android.R.anim.fade_in);
        animation.setDuration(300);
        viewToAnimate.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ErrorViewHolder extends RecyclerView.ViewHolder {
        TextView tvErrorMessage;
        Button btnRetry;
        public ErrorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvErrorMessage = itemView.findViewById(R.id.tvErrorMessage);
            btnRetry = itemView.findViewById(R.id.btnRetry);
        }
    }
}