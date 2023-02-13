package com.example.isbnscanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class HistoryFragment extends Fragment {

    private ListView listView;
    private BookListAdapter adapter;
    private ArrayList<Book> books;
    public static final String SHARED_PREFERENCES_NAME = "BookInfo";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        listView = view.findViewById(R.id.listView);
        books = new ArrayList<>();
        adapter = new BookListAdapter(getActivity(), books);
        listView.setAdapter(adapter);

        loadData();

        return view;
    }

    private void loadData() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("book list", null);
        Type type = new TypeToken<ArrayList<Book>>() {}.getType();
        books = gson.fromJson(json, type);

        if (books == null) {
            books = new ArrayList<>();
        }

        adapter.notifyDataSetChanged();
    }
}