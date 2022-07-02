package ru.zlsl.redgifs;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;


public class ViewBadgeWidget extends LinearLayout implements View.OnClickListener {

    TextView tv_text;
    private Context ctx;

    private String text;
    private GradientDrawable gradientDrawable;

    public ViewBadgeWidget(Context context) {
        super(context);
        init(context);
    }

    public ViewBadgeWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setWidgetInfo(String text) {
        this.text = text;
        tv_text.setBackgroundColor(ContextCompat.getColor(getContext(), com.mikepenz.materialdrawer.R.color.md_red_500));
        tv_text.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
        tv_text.setText(text);
    }

    private void init(Context context) {
        View rootView;
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setCornerRadius(5);
        rootView = inflate(context, R.layout.widget_badge, this);
        tv_text = rootView.findViewById(R.id.tv_text);
        setOnClickListener(this);

        ctx = context;
    }

    @Override
    public void onClick(View view) {
    }
}