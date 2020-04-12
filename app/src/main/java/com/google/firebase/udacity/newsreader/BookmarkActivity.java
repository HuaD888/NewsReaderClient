package com.google.firebase.udacity.newsreader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.google.firebase.udacity.newsreader.BrowserActivity.PREFERENCES;
import static com.google.firebase.udacity.newsreader.BrowserActivity.WEB_LINKS;
import static com.google.firebase.udacity.newsreader.BrowserActivity.WEB_TITLE;

public class BookmarkActivity extends AppCompatActivity {
    ArrayList<HashMap<String, String>> listRowData;

    public static String TAG_TITLE = "title";
    public static String TAG_LINK = "link";

    Button addUrlBtn;
    EditText editText;
    ListView listView;
    SimpleAdapter adapter;
    LinearLayout linearLayout;
    SwipeRefreshLayout mSwipeRefreshLayout;

    private String url;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        editText = findViewById(R.id.editText);
        Intent intent = getIntent();
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        if(title == null)
            title = "";

        if(url != "")
        {
            editText.setText(url);
        }


        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                title = "";
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    addUrlBtn.callOnClick();
                    return true;
                }

                return false;
            }
        });

        addUrlBtn = findViewById(R.id.add_url_btn);
        addUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            //On click function
            public void onClick(View view) {
                url = editText.getText().toString();
                if(url == "")
                    return;

                SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
                String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);
                ArrayList<String> linkList, titleList;
                String current_page_url = url;

                if (jsonLink != null && jsonTitle != null) {

                    Gson gson = new Gson();
                    linkList = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                    }.getType());

                    titleList = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {
                    }.getType());

                    if (linkList.contains(current_page_url)) {
                        return;
                    }
                }
                else {
                    linkList = new ArrayList<>();
                    titleList = new ArrayList<>();
                }

                    linkList.add(current_page_url);
                    titleList.add(title);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(WEB_LINKS, new Gson().toJson(linkList));
                    editor.putString(WEB_TITLE, new Gson().toJson(titleList));
                    editor.apply();

                    listRowData.clear();
                    for (int i = 0; i < linkList.size(); i++) {
                        HashMap<String, String> map = new HashMap<>();

                        if (titleList.get(i).length() == 0)
                            map.put(TAG_TITLE, "Bookmark " + (i + 1));
                        else
                            map.put(TAG_TITLE, titleList.get(i));

                        map.put(TAG_LINK, linkList.get(i));
                        listRowData.add(map);
                    }

                    adapter = new SimpleAdapter(BookmarkActivity.this,
                            listRowData, R.layout.bookmark_list_row,
                            new String[]{TAG_TITLE, TAG_LINK},
                            new int[]{R.id.title, R.id.link});

                    listView.setAdapter(adapter);

                    Snackbar snackbar1 = Snackbar.make(linearLayout, "Bookmark was saved successfully!", Snackbar.LENGTH_SHORT);
                    snackbar1.show();
            }
        });


        listView = findViewById(R.id.listView);
        linearLayout = findViewById(R.id.emptyList);

        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new LoadBookmarks().execute();

            }
        });

        new LoadBookmarks().execute();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                Object o = listView.getAdapter().getItem(position);
                if (o instanceof Map) {
                    Map map = (Map) o;
                    Intent in = new Intent(BookmarkActivity.this, BrowserActivity.class);
                    in.putExtra("url", String.valueOf(map.get(TAG_LINK)));
                    startActivity(in);
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Object o = listView.getAdapter().getItem(i);
                if (o instanceof Map) {
                    Map map = (Map) o;
                    deleteBookmark(String.valueOf(map.get(TAG_TITLE)), String.valueOf(map.get(TAG_LINK)));
                }

                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        new LoadBookmarks().execute();
    }
    private class LoadBookmarks extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {

                    SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                    String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
                    String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);
                    listRowData = new ArrayList<>();

                    if (jsonLink != null && jsonTitle != null) {

                        Gson gson = new Gson();
                        ArrayList<String> linkArray = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                        }.getType());

                        ArrayList<String> titleArray = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {
                        }.getType());


                        for (int i = 0; i < linkArray.size(); i++) {
                            HashMap<String, String> map = new HashMap<>();

                            if (titleArray.get(i).length() == 0)
                                map.put(TAG_TITLE, "Bookmark " + (i + 1));
                            else
                                map.put(TAG_TITLE, titleArray.get(i));

                            map.put(TAG_LINK, linkArray.get(i));
                            listRowData.add(map);
                        }

                        adapter = new SimpleAdapter(BookmarkActivity.this,
                                listRowData, R.layout.bookmark_list_row,
                                new String[]{TAG_TITLE, TAG_LINK},
                                new int[]{R.id.title, R.id.link});

                        listView.setAdapter(adapter);
                    }

                    linearLayout.setVisibility(View.VISIBLE);
                    listView.setEmptyView(linearLayout);


                }
            });
            return null;
        }

        protected void onPostExecute(String args) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    private void deleteBookmark(final String title, final String link) {

        new AlertDialog.Builder(this)
                .setTitle("DELETE")
                .setMessage("Confirm that you want to delete this bookmark?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
                        String jsonLink = sharedPreferences.getString(WEB_LINKS, null);
                        String jsonTitle = sharedPreferences.getString(WEB_TITLE, null);

                        if (jsonLink != null && jsonTitle != null) {
                            Gson gson = new Gson();
                            ArrayList<String> linkArray = gson.fromJson(jsonLink, new TypeToken<ArrayList<String>>() {
                            }.getType());

                            ArrayList<String> titleArray = gson.fromJson(jsonTitle, new TypeToken<ArrayList<String>>() {
                            }.getType());


                            linkArray.remove(link);
                            titleArray.remove(title);

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(WEB_LINKS, new Gson().toJson(linkArray));
                            editor.putString(WEB_TITLE, new Gson().toJson(titleArray));
                            editor.apply();

                            new LoadBookmarks().execute();
                        }
                        dialogInterface.dismiss();
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

}
