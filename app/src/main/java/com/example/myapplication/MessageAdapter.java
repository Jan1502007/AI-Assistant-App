package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> messages;
    private OnRetryListener retryListener;

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
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).tvMessage.setText(message.getContent());
        } else if (holder instanceof ErrorViewHolder) {
            ((ErrorViewHolder) holder).tvErrorMessage.setText(message.getContent());
            ((ErrorViewHolder) holder).btnRetry.setOnClickListener(v -> {
                if (retryListener != null) retryListener.onRetry();
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
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