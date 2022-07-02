package ru.zlsl.redgifs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private boolean Loading = false;
    private static String current_user = "";
    private static String current_search = "";
    private static String order = "trending";
    private static int current_page = 1;
    private static String UrlTemplate = "";
    private static String UrlTemplateUser = "";
    private RecyclerView rv1;
    private RedgifsAdapter rv_adapter;
    private FlowLayout layout_info;

    private OkHttpClient client;

    private final ArrayList<HashMap<String, Object>> GIFS = new ArrayList<>();

    static final int DR_ID_ALL = 0;
    static final int DR_ID_TRENDING = 1;
    static final int DR_ID_BEST = 2;
    static final int DR_ID_LATEST = 3;
    static final int DR_ID_OLDEST = 4;
    static final int DR_ID_TOP7 = 5;
    static final int DR_ID_TOP28 = 6;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initDrawer();

        UrlTemplate = "https://api.redgifs.com/v2/gifs/search?type=g&search_text=#&order=$&page=@&count=30";
        UrlTemplateUser = "https://api.redgifs.com/v2/users/~/search?order=$&page=@";

        rv1 = findViewById(R.id.rv1);
        client = new OkHttpClient();
        layout_info = findViewById(R.id.layout_info);

        UpdateInfo();

        WrapContentLinearLayoutManager mLayoutManager = new WrapContentLinearLayoutManager(this);
        rv1.setLayoutManager(mLayoutManager);

        rv1.setHasFixedSize(false);

        rv_adapter = new RedgifsAdapter(this, GIFS);
        rv_adapter.setOnSearchListener(search -> {
            current_search = search;
            current_user = "";
            newFeed(order);
        });
        rv_adapter.setOnUserListener(username -> {
            current_search = "";
            current_user = username;
            order = "recent";
            newFeed(order);
        });

        rv1.setAdapter(rv_adapter);

        EndlessRecyclerViewScrollListener2 scrollListener = new EndlessRecyclerViewScrollListener2(mLayoutManager) {
            @Override
            public void onLoadMore() {
                FetchFeed();
            }
        };

        rv1.addOnScrollListener(scrollListener);

        try {
            Picasso picasso = new Picasso.Builder(this)
                    .downloader(new OkHttp3Downloader(this, 25000000))
                    .memoryCache(new LruCache(this))
                    .build();
            Picasso.setSingletonInstance(picasso);
        } catch (Exception e) {
            e.printStackTrace();
        }


        ImageButton is = findViewById(R.id.i_srch);
        is.setOnClickListener(view -> {
            final EditText input = new EditText(this);
            input.setSingleLine();
            FrameLayout container = new FrameLayout(this);
            FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
            params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
            input.setLayoutParams(params);
            container.addView(input);


            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Search")
                    .setMessage("Use \",\" as delimeter for tags")
                    .setView(container)
                    .setPositiveButton("Search", (dialogInterface, i) -> {
                        current_search = input.getText().toString().trim();
                        current_user = "";
                        newFeed(order);
                    })
                    .create();
            input.setText(current_search);
            input.requestFocus();
            dialog.show();
        });
        FetchFeed();
    }

    public static Request getSimpleRequest(String url) {
        String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.84 Safari/537.36";
        return new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build();
    }

    public void FetchFeed() {
        if (Loading) {
            return;
        }
        Loading = true;

        ExecutorService executors = Executors.newFixedThreadPool(1);
        @SuppressLint("NotifyDataSetChanged") Runnable runnable = () -> {
            try {
                String bodyx;
                JSONObject redgif;
                JSONArray gifs;

                String ur;

                if (current_user.isEmpty()) {
                    ur = UrlTemplate;
                } else {
                    ur = UrlTemplateUser;
                }
                ur = ur.replace("@", "" + current_page);
                ur = ur.replace("#", "" + current_search);
                ur = ur.replace("$", "" + order);
                ur = ur.replace("~", "" + current_user);
                Log.i("URL", ur);
                Response response =  client.newCall(getSimpleRequest(ur)).execute();
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();

                    if (body != null) {
                        bodyx = body.string();
                        response.close();
                        redgif = new JSONObject(bodyx);
                        gifs = redgif.getJSONArray("gifs");

                        for (int i = 0; i < gifs.length(); i++) {
                            JSONObject gif = gifs.getJSONObject(i);
                            JSONObject urls = gif.getJSONObject("urls");
                            JSONArray tags = gif.getJSONArray("tags");

                            HashMap<String, Object> map = new HashMap<>();
                            map.put("id", gif.getString("id"));
                            map.put("likes", gif.getInt("likes"));
//                            map.put("createDate", gif.getInt("createDate"));
                            map.put("type", gif.getInt("type"));
//                            map.put("duration", gif.getInt("duration"));
                            map.put("userName", gif.getString("userName"));
                            map.put("views", gif.getInt("views"));
//                            map.put("hasAudio", gif.getBoolean("hasAudio"));
                            map.put("width", gif.getInt("width"));
                            map.put("height", gif.getInt("height"));

                            map.put("url_poster", urls.getString("poster"));
                            map.put("url_hd", urls.getString("hd"));
                            map.put("url_sd", urls.getString("sd"));
                            map.put("url_thumbnail", urls.getString("thumbnail"));
                            map.put("url_vthumbnail", urls.getString("vthumbnail"));

                            map.put("tags", tags.toString());

                            if (!GIFS.contains(map)) {
                                GIFS.add(map);
                                Picasso.with(this).load(urls.getString("poster")).fetch();

                            } else {
                                Log.e("GIFS", "dupe");
                            }
                        }
                    } else {
                        Loading = false;
                    }
                } else {
                    Toast.makeText(this, "Network error " + response.message(), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Loading = false;
                Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            try {
                Handler uiThread = new Handler(Looper.getMainLooper());
                uiThread.post(() -> {
                    try {
                        Objects.requireNonNull(rv1.getAdapter()).notifyDataSetChanged();
                        current_page++;
                        Loading = false;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        executors.submit(runnable);
    }

    public void initDrawer() {
        new DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("All").withIdentifier(DR_ID_ALL).withSelectable(true),
                        new PrimaryDrawerItem().withName("Trending").withIdentifier(DR_ID_TRENDING).withSelectable(true),
                        new PrimaryDrawerItem().withName("Best").withIdentifier(DR_ID_BEST).withSelectable(true), //best
                        new PrimaryDrawerItem().withName("Latest").withIdentifier(DR_ID_LATEST).withSelectable(true), //latest
                        new PrimaryDrawerItem().withName("Oldest").withIdentifier(DR_ID_OLDEST).withSelectable(true), //oldest
                        new PrimaryDrawerItem().withName("Top Week").withIdentifier(DR_ID_TOP7).withSelectable(true), //top7
                        new PrimaryDrawerItem().withName("Top Month").withIdentifier(DR_ID_TOP28).withSelectable(true) //top28
                )
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    drawerItem((int) drawerItem.getIdentifier());
                    return false;
                })
                .build();
    }

    private  void drawerItem(int id) {
        switch (id) {
            case DR_ID_ALL:
                current_search = "";
                current_user = "";
                newFeed("trending");
                break;
            case DR_ID_BEST:
                newFeed("best");
                break;
            case DR_ID_TRENDING:
                current_user = "";
                newFeed("trending");
                break;
            case DR_ID_LATEST:
                if (current_user.isEmpty()) {
                    newFeed("latest");
                } else {
                    newFeed("recent");
                }
                break;
            case DR_ID_OLDEST:
                newFeed("oldest");
                break;
            case DR_ID_TOP7:
                current_user = "";
                newFeed("top7");
                break;
            case DR_ID_TOP28:
                current_user = "";
                newFeed("top28");
                break;
        }
        UpdateInfo();
    }

    private void UpdateInfo() {
        layout_info.removeAllViewsInLayout();
        ViewBadgeWidget tx = new ViewBadgeWidget(this);
        tx.setWidgetInfo(order);
        layout_info.addView(tx);

        if (!current_search.isEmpty()) {
            for (String tag : current_search.split(",")) {
                ViewBadgeWidget t = new ViewBadgeWidget(this);
                t.setWidgetInfo(tag);
                t.setOnClickListener(view -> {
                });
                layout_info.addView(t);
            }
        }

        if (!current_user.isEmpty()) {
            ViewBadgeWidget t = new ViewBadgeWidget(this);
            t.setWidgetInfo(current_user);
            t.setOnClickListener(view -> {
            });
            layout_info.addView(t);
        }
    }

    private void newFeed(String n_order) {
        order = n_order;
        current_page = 1;
        GIFS.clear();
        rv_adapter.clear();
        rv1.removeAllViewsInLayout();
        UpdateInfo();
        FetchFeed();
    }
}
