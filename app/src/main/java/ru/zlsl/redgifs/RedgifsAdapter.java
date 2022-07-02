package ru.zlsl.redgifs;

import static android.content.Context.DOWNLOAD_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.apmem.tools.layouts.FlowLayout;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RedgifsAdapter extends RecyclerView.Adapter<RedgifsAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<HashMap<String, Object>> GIFS;
    private final Context context;

    @Override
    public void onClick(View view) {

    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        GIFS.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView p_username;
        final TextView p_likes;
        final ImageView p_poster;
        final VideoView p_video_sd;
        final FlowLayout p_footer;
        final ImageButton p_down;

        ViewHolder(View v) {
            super(v);

            p_likes = v.findViewById(R.id.tv_likes);
            p_username = v.findViewById(R.id.tv_username);
            p_poster = v.findViewById(R.id.image_poster);
            p_video_sd = v.findViewById(R.id.video_sd);
            p_footer = v.findViewById(R.id.layout_footer);
            p_down = v.findViewById(R.id.i_down);
        }
    }

    @SuppressWarnings("unchecked")
    RedgifsAdapter(Context context, List<? extends Map<String, Object>> data) {
        this.context = context;

        try {
            GIFS = (ArrayList<HashMap<String, Object>>) data;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NotNull
    @Override
    public RedgifsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_redgif, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, final int position) {
        if (GIFS.size() == 0) {
            return;
        }

        int hh = Integer.parseInt(Objects.requireNonNull(GIFS.get(position).get("height")).toString());

        holder.p_poster.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, hh));

        holder.p_video_sd.stopPlayback();
        holder.p_video_sd.setVisibility(GONE);
        holder.p_poster.setVisibility(VISIBLE);

        holder.p_down.setOnClickListener(v -> {
            Toast.makeText(v.getContext(), "Downloading video clip", Toast.LENGTH_SHORT).show();
            DownloadImage(v.getContext(), Objects.requireNonNull(GIFS.get(position).get("url_hd")).toString());
        });

        if (Objects.requireNonNull(GIFS.get(position).get("type")).toString().equals("1")) {

            holder.p_poster.setOnClickListener(v -> {
                try {
                    String url = Objects.requireNonNull(GIFS.get(position).get("url_hd")).toString();
                    if (url.isEmpty()) {
                        url = Objects.requireNonNull(GIFS.get(position).get("url_sd")).toString();
                    }
                    holder.p_video_sd.setVideoURI(Uri.parse(url));
                    holder.p_video_sd.setLayoutParams(new LinearLayout.LayoutParams(holder.p_poster.getMeasuredWidth(), holder.p_poster.getMeasuredHeight()));
                    holder.p_video_sd.setVisibility(VISIBLE);
                    holder.p_poster.setVisibility(GONE);

                    holder.p_video_sd.setOnPreparedListener(mp -> {

                        mp.setLooping(true);
                        holder.p_video_sd.start();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            holder.p_poster.setOnClickListener(null);
        }

        holder.p_video_sd.setOnClickListener(v -> {
            try {
                holder.p_video_sd.stopPlayback();
                holder.p_video_sd.setVisibility(GONE);
                holder.p_poster.setVisibility(VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            holder.p_likes.setText(Objects.requireNonNull(GIFS.get(position).get("likes")) + " ❤️");
            holder.p_username.setText(Objects.requireNonNull(GIFS.get(position).get("userName")).toString());
            holder.p_username.setOnClickListener(view -> {
                if (onUserListener != null) {
                    onUserListener.onUserListener(Objects.requireNonNull(GIFS.get(position).get("userName")).toString());
                }
            });

            if (!Objects.requireNonNull(GIFS.get(position).get("url_poster")).toString().isEmpty()) {
                try {
                    if (Objects.requireNonNull(GIFS.get(position).get("url_poster")).toString().isEmpty()) {
                        Log.e("IMG", "empty");
                    }
                    holder.p_video_sd.setLayoutParams(new LinearLayout.LayoutParams(holder.p_poster.getMeasuredWidth(), holder.p_poster.getMeasuredHeight()));
                    Picasso.with(context)
                            .load(Objects.requireNonNull(GIFS.get(position).get("url_poster")).toString())
                            .into(holder.p_poster);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String tt = Objects.requireNonNull(GIFS.get(position).get("tags")).toString().replace("\"", "").replace("[", "").replace("]", "");

            Set<String> ttags = new LinkedHashSet<>(Arrays.asList(tt.split(",")));
            String[] tags = ttags.toArray( new String[ttags.size()] );
            holder.p_footer.removeAllViews();
            for (String tag : tags) {
                ViewBadgeWidget t = new ViewBadgeWidget(holder.itemView.getContext());
                t.setWidgetInfo(tag);
                t.setOnClickListener(view -> {
                    if (onSearchListener != null) {
                        onSearchListener.onSearchListener(tag);
                    }
                });
                holder.p_footer.addView(t);
            }

            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return GIFS.size();
    }

    @Override
    public void onViewDetachedFromWindow(@NotNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    private OnSearchListener onSearchListener;
    public interface OnSearchListener {
        void onSearchListener(String search);
    }

    void setOnSearchListener(OnSearchListener listener) {
        onSearchListener = listener;
    }

    private OnUserListener onUserListener;
    public interface OnUserListener {
        void onUserListener(String username);
    }

    void setOnUserListener(OnUserListener listener) {
        onUserListener = listener;
    }

    private static void DownloadImage(Context ctx, String url) {
        String filename = url.substring(url.lastIndexOf("/") + 1);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES ).getPath() + "/Redgifs/" + filename);
        Log.d("Environment", "Environment extraData=" + file.getPath());

        DownloadManager.Request request=new DownloadManager.Request(Uri.parse(url))
                .setTitle(filename)
                .setDescription("Downloading")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true);
        DownloadManager downloadManager = (DownloadManager) ctx.getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

}