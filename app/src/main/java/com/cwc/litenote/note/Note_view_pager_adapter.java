package com.cwc.litenote.note;

import com.cwc.litenote.R;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.TouchImageView;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.media.video.VideoPlayer;
import com.cwc.litenote.util.Util;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class Note_view_pager_adapter extends FragmentStatePagerAdapter //PagerAdapter 
{
	static final int VIEW_LINK = 7;
	static CustomWebView customWebView;
    SharedPreferences pref_web_view;
	static int mLastPosition;
	static LayoutInflater inflater;
	static FragmentActivity mAct;
	
    public Note_view_pager_adapter(FragmentManager fm,FragmentActivity act) 
    {
    	super(fm);
    	mAct = act;
        inflater = mAct.getLayoutInflater();
        mLastPosition = -1;
        System.out.println("Note_view_pager_adapter / mLastPosition = -1;");
    }
    
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
		object = null;
	}

	static Note_view_pagerUI mController;
	
    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) 
    {
//    	System.out.println("Note_view_pager_adapter / instantiateItem / position = " + position);
    	// Inflate the layout containing 
    	// 1. text group: title, body, time 
    	// 2. picture group: picture, control buttons
    	View pagerView = (ViewGroup) inflater.inflate(R.layout.note_view_pager, container, false);
    	int style = Note_view_pager.getStyle();
        pagerView.setBackgroundColor(Util.mBG_ColorArray[style]);
        
    	// text group
        ViewGroup textGroup = (ViewGroup) pagerView.findViewById(R.id.textGroup);
        customWebView = new CustomWebView(mAct);
        customWebView = ((CustomWebView) textGroup.findViewById(R.id.textBody));

        // Set tag for custom web view
        String tagStr = "current"+position+"webView";
        customWebView.setTag(tagStr);
        
        pref_web_view = mAct.getSharedPreferences("web_view", 0);
        
        customWebView.setWebViewClient(new WebViewClient() 
        {
            @Override
            public void onScaleChanged(WebView web_view, float oldScale, float newScale) 
            {
                super.onScaleChanged(web_view, oldScale, newScale);
                System.out.println("Note_view_pager / onScaleChanged");
                System.out.println("    oldScale = " + oldScale); 
                System.out.println("    newScale = " + newScale);
                
                int newDefaultScale = (int) (newScale*100);
                pref_web_view.edit().putInt("KEY_WEB_VIEW_SCALE",newDefaultScale).commit();
                
//                // default scale: 3.0 for xxhdpi screen, 1.5 for hdpi screen
//                float defaultScale = UtilImage.getDefaultSacle(mAct);
//                System.out.println("    defaultScale = " + defaultScale);
//                float scaleChange = Math.abs(oldScale-newScale);
//                System.out.println("    Scale change = " + scaleChange);
//                // for adjusting scale downwards
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
//                {
//                    if( ( scaleChange > 0.05)&& 
//                    	( scaleChange < (defaultScale + 0.5))&&
//                    	  (oldScale != defaultScale)                         )
//                    {
//                    	System.out.println("apply view port");
//                    	web_view.loadData(Note_view_pager.getHTMLstringWithViewPort(position,Note_view_pager.VIEW_PORT_BY_SCREEN_WIDTH),
//                    					  "text/html; charset=utf-8",
//                    					  "UTF-8");
//                    }
//                }
                
                //update current position
                Note_view_pager.mCurrentPosition = Note_view_pager.mPager.getCurrentItem();
            }
        });
        
    	int scale = pref_web_view.getInt("KEY_WEB_VIEW_SCALE",0);
//        System.out.println(" Note_view_pager / instantiateItem /  scale = " + scale);
        customWebView.setInitialScale(scale); // 1 instantiateItem           	
        
    	customWebView.setBackgroundColor(Util.mBG_ColorArray[style]);
    	customWebView.getSettings().setBuiltInZoomControls(true);
    	customWebView.getSettings().setSupportZoom(true);
    	customWebView.getSettings().setUseWideViewPort(true);
//    	customWebView.getSettings().setLoadWithOverviewMode(true);
    	customWebView.getSettings().setJavaScriptEnabled(true);//Using setJavaScriptEnabled can introduce XSS vulnerabilities
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
    	{
        	customWebView.loadData(Note_view_pager.getHTMLstringWithViewPort(position,Note_view_pager.VIEW_PORT_BY_DEVICE_WIDTH),
        						   "text/html; charset=utf-8",
        						   "UTF-8");
//        	System.out.println("--- instantiateItem / loadData / position = " + position);
    	}
    	else
        	customWebView.loadData(Note_view_pager.getHTMLstringWithViewPort(position,Note_view_pager.VIEW_PORT_BY_NONE),
        						   "text/html; charset=utf-8",
        						   "UTF-8");                		
    	
    	// image group
        ViewGroup pictureGroup = (ViewGroup) pagerView.findViewById(R.id.imageContent);
        String tagImageStr = "current"+ position +"imageView";
        pictureGroup.setTag(tagImageStr);
        
		// get picture name
    	String strPicture = Note_view_pager.mDb.getNotePictureUri(position,true);
//    	System.out.println("Note_view_pager_adapter / instantiateItem / strPicture = "  + strPicture);

    	mController = new Note_view_pagerUI();
        mController.setPagerUI_listeners(strPicture, pictureGroup);
        
        // image view
    	TouchImageView imageView = new TouchImageView(container.getContext());
		imageView = ((TouchImageView) pagerView.findViewById(R.id.image_view));
		
		// video view
    	VideoView videoView = new VideoView(container.getContext());
		videoView = ((VideoView) pagerView.findViewById(R.id.video_view));

		// spinner
		final ProgressBar spinner = (ProgressBar) pagerView.findViewById(R.id.loading);

    	///
    	//TO
//		strPicture = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
		///
    	
        // view mode 
    	// picture only
	  	if(Note_view_pager.isPictureMode())
	  	{
			// picture only
	  		textGroup.setVisibility(View.GONE);
	  		pictureGroup.setVisibility(View.VISIBLE);
	  		
	  		// set image view
	  		if( UtilImage.hasImageExtension(strPicture,mAct)||
	  		    (!UtilVideo.hasVideoExtension(strPicture, mAct)) ||
	  		    Util.isEmptyString(strPicture)) // for wrong path icon
	  		{
	  			videoView.setVisibility(View.GONE);
	  			UtilVideo.mVideoView = null;
	  			imageView.setVisibility(View.VISIBLE);
	  			Note_view_pager.showImageByTouchImageView(spinner, imageView, strPicture);
	  		}
	  		
//	  		videoView.setVisibility(View.GONE);
//	  		new UtilImage_bitmapLoader(imageView, 
//	  								   strPicture, 
//	  								   spinner, 
//	  								   UilCommon.optionsForFadeIn,
//	  								   mAct);
	  	}
	    // text only
	  	else if(Note_view_pager.isTextMode())
	  	{
	  		textGroup.setVisibility(View.VISIBLE);
	  		pictureGroup.setVisibility(View.GONE);
	  	}
  		// picture and text
	  	else if(Note_view_pager.isViewAllMode())
	  	{
	  		textGroup.setVisibility(View.VISIBLE);
	  		pictureGroup.setVisibility(View.VISIBLE);
	  		
	  		// show image view
	  		if(UtilImage.hasImageExtension(strPicture,mAct) ||
	  		  (!UtilVideo.hasVideoExtension(strPicture, mAct)) ||
	  		   Util.isEmptyString(strPicture)) // for wrong path icon
	  		{
	  			videoView.setVisibility(View.GONE);
	  			UtilVideo.mVideoView = null;
	  			imageView.setVisibility(View.VISIBLE);
	  			Note_view_pager.showImageByTouchImageView(spinner, imageView, strPicture);
	  		}
	  	}
        
    	container.addView(pagerView, 0);
    	
		return pagerView;			
    } //instantiateItem
	
    // Add for FragmentStatePagerAdapter
    @Override
	public Fragment getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
    
    // Add for calling mPagerAdapter.notifyDataSetChanged() 
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
    
	@Override
    public int getCount() 
    {
    	int count = Note_view_pager.mDb.getNotesCount(true);//08-26 14:42:00.230: E/AndroidRuntime(13936): android.database.CursorWindowAllocationException: Cursor window allocation of 2048 kb failed. # Open Cursors=612 (# cursors opened by this proc=612)
    	return count;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	static Intent mIntentView;
	
	@Override
	public void setPrimaryItem(final ViewGroup container, int position, Object object) 
	{
	    // Only refresh when primary changes
//		System.out.println("Note_view_pager_adapter / _setPrimaryItem / mLastPosition = " + mLastPosition);
//		System.out.println("Note_view_pager_adapter / _setPrimaryItem / position = " + position);
		
		// for youtube support
	    if((mLastPosition != position) && (Note_view_pager.isTextMode()) ) 
	    {
			// make sure the playing state can be stopped
	    	String tag = "current"+mLastPosition+"webView";
	    	CustomWebView webView = (CustomWebView) Note_view_pager.mPager.findViewWithTag(tag);
	    	if(webView != null)
	    	{
    			webView.onPause();
    			webView.onResume();
	    	}
	    	
	    	// Show web view with pager slide for youtube
	    	tag = "current"+position+"webView";
	    	webView = (CustomWebView) Note_view_pager.mPager.findViewWithTag(tag);
	    	String strBody = Note_view_pager.mDb.getNoteBody(position,true);
	    	if(Util.isYouTubeLink(strBody))	
	    	{
	    		// open browser
//	    		webView.loadUrl(strBody);
	    		
	    		// open YouTube intent
	    		mIntentView = new Intent(Intent.ACTION_VIEW, Uri.parse(strBody));
	    		mAct.startActivityForResult(mIntentView,VIEW_LINK);	    		
	    		System.out.println("Note_view_pager_adapter / _setPrimaryItem / loadUrl strBody = " + strBody);
	    	}
	    	mLastPosition = position;
	    	Note_view_pagerUI.showPagerUI(position);
	    }

		
		// set video view
	    if((mLastPosition != position) && (!Note_view_pager.isTextMode()) ) 
	    {
	    	// fix overlay issue: remove last position video view
	        String tagImageStr = "current"+ mLastPosition +"imageView";
//	        System.out.println("Note_view_pager_adapter / _setPrimaryItem / tagImageStr = " + tagImageStr);
	        
	        ViewGroup imageGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagImageStr);
	        if(imageGroup != null)
	        {
		        VideoView videoView = (VideoView) (imageGroup.findViewById(R.id.video_view));
		        videoView.setVisibility(View.GONE);
	        }
		        
		    mLastPosition = position;
        	String strPicture = Note_view_pager.mDb.getNotePictureUri(position,true);
//    		System.out.println("Note_view_pager_adapter / _setPrimaryItem / strPicture = " + strPicture);
        	
        	///
        	// TO
//        	strPicture = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//        	strPicture = "http://techslides.com/demos/sample-videos/small.mp4";
        	///
        	
        	// update current pager view
		    UtilVideo.mCurrentPagerView = (View)object;
		    
		    if(Note_view_pager.mIsViewModeChanged )
		    {
		    	System.out.println("Note_view_pager_adapter / _setPrimaryItem / Note_view_pager.mPosistionOfChangeView = " + Note_view_pager.mPosistionOfChangeView);
		    	
		    	UtilVideo.mPlayVideoPosition = Note_view_pager.mPosistionOfChangeView;
		    	UtilVideo.setVideoViewLayout(strPicture);
		    	if(!UtilVideo.hasMediaControlWidget)
		    		UtilVideo.setVideoViewUI();
		   		
		    	if(UtilVideo.mPlayVideoPosition > 0)
		   			UtilVideo.playOrPauseVideo(Note_view_pager.getCurrentPictureString());
		    }
		    else
		    {
		    	if(Note_view_pager.mPlayVideoPositionOfInstance > 0)
			    {
		    		UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PAUSE);
			    	UtilVideo.setVideoViewLayout(strPicture);
			    	if(!UtilVideo.hasMediaControlWidget)
			    		UtilVideo.setVideoViewUI();
		   			UtilVideo.playOrPauseVideo(Note_view_pager.getCurrentPictureString());			    	
			    }
		    	else
		    	{
			    	if(UtilVideo.hasMediaControlWidget)
			    		UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PLAY);
			    	else
		    			UtilVideo.setVideoState(UtilVideo.VIDEO_AT_STOP);
			    	
				    UtilVideo.mPlayVideoPosition = 0; // make sure play video position is 0 after page is changed
			    	UtilVideo.initVideoView(strPicture,mAct);
		    	}
		    }
		    
	    	Note_view_pagerUI.showPagerUI(position);
			UtilVideo.currentPicturePath = strPicture;
	    }
	} //setPrimaryItem		
	
	
	public static void stopAV()
	{
		if(AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE)
			UtilAudio.stopAudioPlayer(); 
		
		if(UtilVideo.mVideoPlayer != null)
			VideoPlayer.stopVideo();
	}
	
}//class Note_view_pager_adapter extends PagerAdapter