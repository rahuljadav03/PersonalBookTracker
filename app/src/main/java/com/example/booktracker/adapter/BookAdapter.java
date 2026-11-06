package com.example.booktracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.booktracker.databinding.ItemBookBinding;
import com.example.booktracker.model.Book;

import java.util.ArrayList;
import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    public interface OnBookClickListener {
        void onBookClick(Book book);
    }

    private List<Book> books = new ArrayList<>();
    private OnBookClickListener listener;

    public BookAdapter() {
    }

    public BookAdapter(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setOnBookClickListener(OnBookClickListener listener) {
        this.listener = listener;
    }

    public void setBooks(List<Book> newBooks) {
        this.books = newBooks != null ? newBooks : new ArrayList<>();
        notifyDataSetChanged();
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
        Book book = books.get(position);
        holder.bind(book, listener);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookBinding binding;

        BookViewHolder(ItemBookBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Book book, OnBookClickListener listener) {
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
            } else {
                binding.chipItemStatus.setVisibility(View.GONE);
            }

            int totalPages = book.getTotalPages();
            int pagesRead = book.getPagesRead();

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
        }

        private String formatStatus(String status) {
            if ("WANT_TO_READ".equals(status)) {
                return "Want to Read";
            } else if ("READING".equals(status)) {
                return "Reading";
            } else if ("COMPLETED".equals(status)) {
                return "Completed";
            }
            return status;
        }
    }
}
