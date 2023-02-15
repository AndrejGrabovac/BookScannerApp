package com.example.isbnscanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private AppDatabase mDb;
    //private List<Book> books = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        mDb = AppDatabase.getInstance(getContext());
        retrieveBooksAndSetAdapter();
        return view;
    }

    private void retrieveBooksAndSetAdapter() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Book> books = mDb.bookDao().getAll();
                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        bookAdapter = new BookAdapter(getContext(), books);
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(bookAdapter);

                        bookAdapter.setOnItemLongClickListener(new BookAdapter.OnItemLongClickListener() {
                            @Override
                            public void onItemLongClick(final Book book) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Delete Book")
                                        .setMessage("Are you sure you want to delete this book?")
                                        .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mDb.bookDao().delete(book);
                                                    }
                                                });
                                                retrieveBooksAndSetAdapter();
                                            }
                                        })
                                        .setNegativeButton("NO", null)
                                        .show();

                            }
                        });
                    }
                });
            }
        });
    }

}
