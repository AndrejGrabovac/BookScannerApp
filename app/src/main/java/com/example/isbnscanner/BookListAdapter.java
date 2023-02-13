package com.example.isbnscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class BookListAdapter extends ArrayAdapter<Book> {

    private List<Book> books;
    private Context context;

    public BookListAdapter(Context context, List<Book> books) {
        super(context, 0, books);
        this.books = books;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.book_item, parent, false);
        }

        TextView titleTextView = view.findViewById(R.id.title_text_view);
        TextView authorTextView = view.findViewById(R.id.author_text_view);

        Book book = books.get(position);

        titleTextView.setText(book.getTitle());
        authorTextView.setText(book.getAuthor());

        return view;
    }
}

