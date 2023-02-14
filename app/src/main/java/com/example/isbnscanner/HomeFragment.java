package com.example.isbnscanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HomeFragment extends Fragment{

    private Button scanButton;
    private TextView isbnTextView;
    private TextView authorTextView;
    private TextView titleTextView;
    private TextView publicationDateTextView;
    private RequestQueue queue;
    private Context MainActivity;


    public HomeFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_home,container,false);

        scanButton = view.findViewById(R.id.scan_button);

        isbnTextView = view.findViewById(R.id.isbn_text_view);
        authorTextView = view.findViewById(R.id.author_text_view);
        titleTextView = view.findViewById(R.id.title_text_view);
        publicationDateTextView = view.findViewById(R.id.publication_date_text_view);
        scanButton = view.findViewById(R.id.scan_button);

        queue = Volley.newRequestQueue(this.requireContext());

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = IntentIntegrator.forSupportFragment(HomeFragment.this);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
                integrator.setPrompt("Scan an ISBN code");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }
        });
        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(MainActivity, "Scan cancelled", Toast.LENGTH_LONG).show();
            } else {

                String isbn = result.getContents();
                isbnTextView.setText("ISBN: " + isbn);
                getBookDetails(isbn);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getBookDetails(String isbn) {
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray items = response.getJSONArray("items");
                            if (items.length() > 0) {
                                JSONObject volumeInfo = items.getJSONObject(0).getJSONObject("volumeInfo");

                                String title = volumeInfo.getString("title");

                                JSONArray authorsArray = volumeInfo.getJSONArray("authors");
                                String authors = "";
                                for (int i = 0; i < authorsArray.length(); i++) {
                                    authors += authorsArray.getString(i) + ", ";
                                }
                                authors = authors.substring(0, authors.length() - 2);

                                String date = volumeInfo.getString("publishedDate");

                                titleTextView.setText(String.format("Title: %s", title));
                                authorTextView.setText(String.format("Authors: %s", authors));
                                publicationDateTextView.setText(String.format("Publish date: %s", date));




                                Book book = new Book(isbn,authors,title,date);

                                String finalAuthors = authors;
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        book.setIsbn(isbn);
                                        book.setAuthors(finalAuthors);
                                        book.setName(title);
                                        book.setDate(date);
                                    }
                                });

                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppDatabase db = AppDatabase.getInstance(getContext());
                                        BookDao bookDao = db.bookDao();
                                        bookDao.insert(book);
                                    }
                                });

                            } else {
                                Toast.makeText(getContext(), "No book found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        queue.add(jsonObjectRequest);
    }
}