package com.cwc.litenote.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.cwc.litenote.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class UilCommon 
{
	public static ImageLoader imageLoader;
	public static DisplayImageOptions options;
	public static DisplayImageOptions optionsForRounded_light;
	public static DisplayImageOptions optionsForRounded_dark;
	public static DisplayImageOptions optionsForFadeIn;
	public static DisplayImageOptions optionsForRounded_light_playIcon;
	public static DisplayImageOptions optionsForRounded_dark_playIcon;

	UilCommon(){};

	public static void init()
	{
		imageLoader = ImageLoader.getInstance();
		
		options = new DisplayImageOptions.Builder()
			.showImageOnLoading(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_light)//R.drawable.ic_empty
			.showImageOnFail(R.drawable.ic_cab_done_holo)// R.drawable.ic_error
			.cacheInMemory(true)
	//		.cacheOnDisk(true)
			.cacheOnDisk(false)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.build();
		
		optionsForFadeIn = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_light)
			.showImageOnFail(R.drawable.ic_cab_done_holo)		
			.resetViewBeforeLoading(true)
			.cacheOnDisk(false)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new FadeInBitmapDisplayer(300))
			.build();	
		
		DisplayImageOptions.Builder optionsForRounded = new DisplayImageOptions.Builder()
			.resetViewBeforeLoading(true)
			.cacheInMemory(true)
			.cacheOnDisk(false)
			.imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.considerExifParams(true)
			.displayer(new RoundedBitmapDisplayer(4));
		
		optionsForRounded_light = optionsForRounded
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_light)
			.showImageOnFail(R.drawable.ic_cab_done_holo)
			.build();
		
		optionsForRounded_dark = optionsForRounded
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_dark)
			.showImageOnFail(R.drawable.ic_cab_done_holo) //R.drawable.ic_media_play
			.build();				
		
		optionsForRounded_light_playIcon = optionsForRounded
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_light)
			.showImageOnFail(R.drawable.ic_media_play)// for remote content video		
			.build();		
		
		optionsForRounded_dark_playIcon = optionsForRounded
			.showImageForEmptyUri(R.drawable.btn_radio_off_holo_dark)
			.showImageOnFail(R.drawable.ic_media_play)// for remote content video		
			.build();		
		
	}
	
    public static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    
	private static class AnimateFirstDisplayListener extends SimpleImageLoadingListener
	{

		static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) 
		{
			if (loadedImage != null) 
			{
				ImageView imageView = (ImageView) view;
				boolean firstDisplay = !displayedImages.contains(imageUri);
				if (firstDisplay) 
				{
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}
}
