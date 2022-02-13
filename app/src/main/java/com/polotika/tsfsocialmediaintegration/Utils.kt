package com.polotika.tsfsocialmediaintegration

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("bindImage")
fun bindImage(view: ImageView,url:String){
    view.setImageResource(R.drawable.ic_launcher_background)
}