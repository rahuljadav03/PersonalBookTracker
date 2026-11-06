package com.example.booktracker.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.databinding.ItemBookBinding;
import com.example.booktracker.model.Book;
import com.example.booktracker.utils.Constants;

import java.util.List;
import java.util.Objects;

public class BookAdapter extends ListAdapter<Book, BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    public interface OnBookActionListener {
        void onBookClick(Book book);
        void onEditClick(Book book);
        void onDeleteClick(Book book);
    }

    private final OnBookActionListener actionListener;

    public static final DiffUtil.ItemCallback<Book> DIFF_CALLBACK = new DiffUtil.ItemCallback<Book>() {
        @Override
        public boolean areItemsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Book oldItem, @NonNull Book newItem) {
            return oldItem.getId() == newItem.getId() &&
                    Objects.equals(oldItem.getTitle(), newItem.getTitle()) &&
                    Objects.equals(oldItem.getAuthor(), newItem.getAuthor()) &&
                    Objects.equals(oldItem.getCategory(), newItem.getCategory()) &&
                    oldItem.getTotalPages() == newItem.getTotalPages() &&
                    oldItem.getPagesRead() == newItem.getPagesRead() &&
                    Objects.equals(oldItem.getStatus(), newItem.getStatus()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription());
        }
    };

    public BookAdapter(OnBookActionListener actionListener) {
        super(DIFF_CALLBACK);
        this.actionListener = actionListener;
    }

    public BookAdapter(OnBookClickListener clickListener) {
        this(new OnBookActionListener() {
            @Override
            public void onBookClick(Book book) {
                if (clickListener != null) clickListener.onBookClick(book);
            }

            @Override
            public void onEditClick(Book book) {}

            @Override
            public void onDeleteClick(Book book) {}
        });
    }

    public void setBooks(List<Book> newBooks) {
        submitList(newBooks);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookBinding binding = ItemBookBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new BookViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = getItem(position);
        holder.bind(book, actionListener);
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book, OnBookActionListener listener) {
            binding.tvItemTitle.setText(book.getTitle());
            binding.tvItemAuthor.setText(book.getAuthor());

            if (book.getCategory() != null && !book.getCategory().isEmpty()) {
                binding.chipItemCategory.setVisibility(View.VISIBLE);
                binding.chipItemCategory.setText(book.getCategory());
            } else {
                binding.chipItemCategory.setVisibility(View.GONE);
            }

            if (book.getStatus() != null && !book.getStatus().isEmpty()) {
                binding.chipItemStatus.setVisibility(View.VISIBLE);
                binding.chipItemStatus.setText(formatStatus(book.getStatus()));
                applyStatusBadgeColor(book.getStatus());
            } else {
                binding.chipItemStatus.setVisibility(View.GONE);
            }

            int totalPages = book.getTotalPages();
            int pagesRead = book.getPagesRead();

            binding.progressItemPages.setTrackStopIndicatorSize(0);
            if (totalPages > 0) {
                int progressPercent = (int) (((double) pagesRead / totalPages) * 100);
                binding.progressItemPages.setProgress(progressPercent);
                binding.tvItemProgressText.setText(
                        pagesRead + " / " + totalPages + " pages (" + progressPercent + "%)");
            } else {
                binding.progressItemPages.setProgress(0);
                binding.tvItemProgressText.setText("0 pages");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookClick(book);
                }
            });

            binding.btnItemEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(book);
                }
            });

            binding.btnItemDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(book);
                }
            });
        }

        private void applyStatusBadgeColor(String status) {
            int backgroundColor;
            int textColor;

            if (Constants.STATUS_WANT_TO_READ.equals(status)) {
                backgroundColor = Color.parseColor("#E3F2FD"); // Soft Blue
                textColor = Color.parseColor("#0D47A1");
            } else if (Constants.STATUS_READING.equals(status)) {
                backgroundColor = Color.parseColor("#E8F5E9"); // Soft Green
                textColor = Color.parseColor("#1B5E20");
            } else if (Constants.STATUS_COMPLETED.equals(status)) {
                backgroundColor = Color.parseColor("#F3E5F5"); // Soft Purple
                textColor = Color.parseColor("#4A148C");
            } else {
                backgroundColor = Color.parseColor("#EEEEEE");
                textColor = Color.parseColor("#212121");
            }

            binding.chipItemStatus.setChipBackgroundColor(ColorStateList.valueOf(backgroundColor));
            binding.chipItemStatus.setTextColor(textColor);
        }

        private String formatStatus(String status) {
            if (Constants.STATUS_WANT_TO_READ.equals(status)) {
                return "Want to Read";
            } else if (Constants.STATUS_READING.equals(status)) {
                return "Reading";
            } else if (Constants.STATUS_COMPLETED.equals(status)) {
                return "Completed";
            }
            return status;
        }
    }
}
