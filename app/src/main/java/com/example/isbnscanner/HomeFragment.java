package com.example.isbnscanner;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;


public class HomeFragment extends Fragment{

    private Button scanButton;
    private TextView isbnTextView;
    private TextView authorTextView;
    private TextView titleTextView;
    private TextView publicationDateTextView;
    private RequestQueue queue;
    private SharedPreferences SharedPreferences;
    public static final String SHARED_PREFERENCES_NAME = "BookInfo";
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
        SharedPreferences = requireActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);

        queue = Volley.newRequestQueue(this.requireContext());

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanISBN();
            }
        });
        return view;
    }

    private void scanISBN() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(HomeFragment.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("Scan an ISBN code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
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
                retrieveBookInformation(isbn);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void retrieveBookInformation(String isbn) {
        String apiUrl = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, apiUrl, null,
                new Response.Listener<JSONObject>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject volumeInfo = response.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo");
                            String author = volumeInfo.getJSONArray("authors").getString(0);
                            String title = volumeInfo.getString("title");
                            String publicationDate = volumeInfo.getString("publishedDate");

                            authorTextView.setText("Author: " + author);
                            titleTextView.setText("Title: " + title);
                            publicationDateTextView.setText("Publication Date: " + publicationDate);

                            saveBookToPreferences(isbn, author, title, publicationDate);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("MainActivity", "Error with API request");
            }
        });

        queue.add(request);
    }

    private void saveBookToPreferences(String isbn, String author, String title, String publicationDate) {
        SharedPreferences preferences = requireContext().getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("isbn", isbn);
        editor.putString("author", author);
        editor.putString("title", title);
        editor.putString("publication_date", publicationDate);
        editor.apply();
    }
}