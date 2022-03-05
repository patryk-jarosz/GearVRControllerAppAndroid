package com.app.gearvrcontrollerapp;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

public class OneUICompCategoryView extends MaterialCardView {
    private int mResIcon = R.mipmap.ic_launcher;
    private String mStrTitle = "mTxtTitle";
    private String mStrDescription = "mTxtDescription";

    private Context mContext;
    private AttributeSet mAttrs;
    private TypedArray mTypedArray;

    private ImageView mImgIcon;
    private TextView mTxtTitle,mTxtDescription;

    private OneUICompCategoryViewListener mListener;

    //  INFLATE VIEW
    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comp_oneui_category, this, true);

        if(mAttrs != null){
            mTypedArray = mContext.getTheme().obtainStyledAttributes(mAttrs, R.styleable.OneUICompCategoryView, 0, 0);
            if(mTypedArray.hasValue(R.styleable.OneUICompCategoryView_compCategoryViewIconRes)){
                mResIcon = mTypedArray.getInteger(R.styleable.OneUICompCategoryView_compCategoryViewIconRes,R.mipmap.ic_launcher);
            }
            if(mTypedArray.hasValue(R.styleable.OneUICompCategoryView_compCategoryViewTitleText)){
                mStrTitle = mTypedArray.getString(R.styleable.OneUICompCategoryView_compCategoryViewTitleText);
            }
            if(mTypedArray.hasValue(R.styleable.OneUICompCategoryView_compCategoryViewDescriptionText)){
                mStrDescription = mTypedArray.getString(R.styleable.OneUICompCategoryView_compCategoryViewDescriptionText);
            }
        }

        //
        mImgIcon = this.findViewById(R.id.comp_oneui_category_icon);
        mTxtTitle = this.findViewById(R.id.comp_oneui_category_title);
        mTxtDescription = this.findViewById(R.id.comp_oneui_category_description);

        //
        if(mResIcon != 0){
            mImgIcon.setImageResource(mResIcon);
        }
        mTxtTitle.setText(mStrTitle);
        mTxtDescription.setText(mStrDescription);
        //
        //elevation 0,radius 30dp, cardBackgroundColor @color/app_surface_color, cardElevation 0
        this.setElevation(0);
        this.setCardElevation(0);
        this.setCardBackgroundColor(getResources().getColor(R.color.app_surface_color));
        this.setRadius(getResources().getDimension(R.dimen.compOneUICardRadius));

        onThemeChange();
    }



    public OneUICompCategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //  CONTEXT
        mContext = context;
        //  ATTRIBUTES
        mAttrs = attrs;
        //  INIT
        initView();
    }
    public OneUICompCategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //  CONTEXT
        mContext = context;
        //  ATTRIBUTES
        mAttrs = attrs;
        //  INIT
        initView();
    }


    public void onThemeChange(){
        this.setCardBackgroundColor(getColorSurface());

        mTxtTitle.setTextColor(getColorText());
        mTxtDescription.setTextColor(getColorSubText());
    }


    private int getColorBackground(){
        return (isDarkMode()) ? Color.parseColor("#010101") : Color.parseColor("#F2F2F2");
    }
    private int getColorSurface(){
        return (isDarkMode()) ? Color.parseColor("#171717") : Color.parseColor("#FFFFFF");
    }
    private int getColorText(){
        return (isDarkMode()) ? Color.parseColor("#FAFAFA") : Color.parseColor("#252525");
    }
    private int getColorSubText(){
        return (isDarkMode()) ? Color.parseColor("#6A6A6A") : Color.parseColor("#919191");
    }

    private boolean isDarkMode(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return true;
            case Configuration.UI_MODE_NIGHT_NO:
                return false;
        }
        return false;
    }


    interface OneUICompCategoryViewListener{
        void onClick(OneUICompCategoryView OneUICompCategoryView);
    }

    public void setOneUICompCategoryViewListener(OneUICompCategoryViewListener mListener){
        OneUICompCategoryView self = this;
        this.mListener = mListener;
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                self.mListener.onClick(self);
                self.clearFocus();
            }
        });
    }

}

