package com.app.gearvrcontrollerapp;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private LinearLayout mLinearParent;

    private OneUICompCategoryViewListener mListener;

    //  INFLATE VIEW
    private void initView() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.comp_oneui_category, this, true);

        if(mAttrs != null){
            mTypedArray = mContext.getTheme().obtainStyledAttributes(mAttrs, R.styleable.OneUICompCategoryView, 0, 0);
            if(mTypedArray.hasValue(R.styleable.OneUICompCategoryView_compCategoryViewIconRes)){
                mResIcon = mTypedArray.getResourceId(R.styleable.OneUICompCategoryView_compCategoryViewIconRes,R.drawable.vec_icon_permissions_accessibility_error);
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

        mLinearParent = this.findViewById(R.id.comp_oneui_category_linear_parent);

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
        //this.setCardBackgroundColor(Color.TRANSPARENT);
        //this.setRadius(getResources().getDimension(R.dimen.compOneUICardRadius));
        this.setRadius(0);

        onThemeChange();

        if(mStrDescription.isEmpty()){
            mTxtDescription.setVisibility(GONE);
        }

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
        this.setCardBackgroundColor(getAppApplication().getColorSurface());

        mTxtTitle.setTextColor(getAppApplication().getColorText());
        mTxtDescription.setTextColor(getAppApplication().getColorSubText());
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

    private AppApplication getAppApplication(){
        return (AppApplication)((Activity) mContext).getApplication();
    }

    @Override
    public void setEnabled(boolean enabled) {
        //super.setEnabled(enabled);
        mLinearParent.setAlpha( (enabled) ? 1f : .5f );
        this.setClickable(enabled);
    }
}

