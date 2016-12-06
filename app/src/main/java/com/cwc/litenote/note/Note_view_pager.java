package com.cwc.litenote.note;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.cwc.litenote.DrawerActivity;
import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.db.DB;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.audio.UtilAudio;
import com.cwc.litenote.media.image.TouchImageView;
import com.cwc.litenote.media.video.AsyncTaskVideoBitmapPager;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.media.video.VideoPlayer;
import com.cwc.litenote.util.SendMailAct;
import com.cwc.litenote.util.UilCommon;
import com.cwc.litenote.util.Util;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import android.R.color;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Note_view_pager extends FragmentActivity //UilBaseFragment 
{
    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    public static ViewPager mPager;
    public static boolean isPagerActive;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    public static PagerAdapter mPagerAdapter;

    // DB
    public static DB mDb;
    public static Long mRowId;
    int mEntryPosition;
    public static int mCurrentPosition;
    int EDIT_CURRENT_VIEW = 5;
    int MAIL_CURRENT_VIEW = 6;
    static int mStyle;
    
    static SharedPreferences mPref_show_note_attribute;
    
    static Button editButton;
    static Button sendButton;
    static Button backButton;
    
    String mAudioUriInDB;
    public static String mVideoUriInDB;
    static TextView mAudioTextView;
    public static FragmentActivity mAct;
    public static int mPlayVideoPositionOfInstance;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        System.out.println("Note_view_pager / onCreate");
        setContentView(R.layout.note_view);
        
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(R.string.view_note_title);
		
		mAct = this;
    	mPref_show_note_attribute = getSharedPreferences("show_note_attribute", 0);

		UilCommon.init();
        
        // DB
		mDb = NoteFragment.mDb_notes;
		
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new Note_view_pager_adapter(getFragmentManager(),this);
        mPager.setAdapter(mPagerAdapter);
        
        // set current selection
        mEntryPosition = getIntent().getExtras().getInt("POSITION");
        System.out.println("Note_view_pager / onCreate / mEntryPosition = " + mEntryPosition);
        
        // init
   		if(savedInstanceState != null)
   			mCurrentPosition = savedInstanceState.getInt("currentPosition");
   		else if (savedInstanceState == null)
   		{
   	        mCurrentPosition = mEntryPosition;
   	        UtilVideo.mPlayVideoPosition = 0;   // not played yet	 
   	        AsyncTaskVideoBitmapPager.mRotationStr = null;
   			mPlayVideoPositionOfInstance = 0;
   			Note_view_pagerUI.mOnDelay = false;
   		}
        
        mPager.setCurrentItem(mCurrentPosition);

        // tab style
        mStyle = TabsHostFragment.mDbTabs.getTabStyle(TabsHostFragment.mCurrent_tabIndex, true);
        
        mRowId = mDb.getNoteId(mCurrentPosition,true);
		
        // show audio name
        mAudioUriInDB = mDb.getNoteAudioUriById(mRowId);        
        mAudioTextView = (TextView) findViewById(R.id.view_audio);
   		showAudioName();

        // Note: if mPager.getCurrentItem() is not equal to mEntryPosition, _onPageSelected will
        //       be called again after rotation
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() 
        {
            @Override
            public void onPageSelected(int nextPosition) 
            {
        		if(AudioPlayer.mAudioPlayMode  == AudioPlayer.ONE_TIME_MODE)
        			UtilAudio.stopAudioPlayer();    
        		
//            	UtilVideo.playWillPause = false; //??? TO
            	mCurrentPosition = mPager.getCurrentItem();
				System.out.println("Note_view_pager / _onPageSelected");
				System.out.println("    mCurrentPosition = " + mCurrentPosition);
				System.out.println("    nextPosition = " + nextPosition);
				
				mIsViewModeChanged = false;
				
        		// show audio name
                mRowId = mDb.getNoteId(nextPosition,true);
                System.out.println("Note_view_pager / _onPageSelected / mRowId = " + mRowId);
        		mAudioUriInDB = mDb.getNoteAudioUriById(mRowId);//???05-26 17:54:16.375: E/InputEventReceiver(9320): Exception dispatching input event.
                System.out.println("Note_view_pager / _onPageSelected / mAudioUriInDB = " + mAudioUriInDB);
       			showAudioName();				
				
	    		// update playing state of picture mode
				Note_view_pagerUI.showPagerUI(nextPosition);
				
            	if((nextPosition == mCurrentPosition+1) || (nextPosition == mCurrentPosition-1))
            	{
	            	if(AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE)
	            		AudioPlayer.mAudioIndex = mCurrentPosition;//update Audio index

            		// show web view
	            	String tagStr = "current"+nextPosition+"webView";
	            	CustomWebView customWebView = (CustomWebView) mPager.findViewWithTag(tagStr);
	            	SharedPreferences pref_web_view = getSharedPreferences("web_view", 0);;
                    int defaultScale = pref_web_view.getInt("KEY_WEB_VIEW_SCALE",0);
//                    System.out.println(" on page selected / default scale = " + defaultScale);
                    customWebView.setInitialScale(defaultScale); // 2 on page selected
                    customWebView.setBackgroundColor(Util.mBG_ColorArray[mStyle]);
                	customWebView.getSettings().setBuiltInZoomControls(true);
                	customWebView.getSettings().setSupportZoom(true);
                	
        	    	String strBody = Note_view_pager.mDb.getNoteBody(nextPosition,true);

        	    	// for Non-youTube body
        	    	if(!Util.isYouTubeLink(strBody))	
        	    	{
	                	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
	                	{
		                	customWebView.loadData(getHTMLstringWithViewPort(nextPosition,VIEW_PORT_BY_DEVICE_WIDTH),
		                						   "text/html; charset=utf-8",
		                						   "UTF-8");
	                	}
	                	else
	                	{
	                		// load empty data to fix double width issue
	                		customWebView.loadData("","text/html; charset=utf-8", "UTF-8");                		
		                	customWebView.loadData(getHTMLstringWithViewPort(nextPosition,VIEW_PORT_BY_NONE),
		                						   "text/html; charset=utf-8",
		                						   "UTF-8");   
	                	}
        	    	}
            	}
            	// When changing pages, reset the action bar actions since they are dependent
                // on which page is currently active. An alternative approach is to have each
                // fragment expose actions itself (rather than the activity exposing actions),
                // but for simplicity, the activity provides the actions in this sample.
                invalidateOptionsMenu();//The onCreateOptionsMenu(Menu) method will be called the next time it needs to be displayed.
            }
        }); //mPager.setOnPageChangeListener
        
		// edit note button
        editButton = (Button) findViewById(R.id.view_edit);
        editButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_edit, 0, 0, 0);
        editButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
		        Intent intent = new Intent(Note_view_pager.this, Note_edit.class);
		        intent.putExtra(DB.KEY_NOTE_ID, mRowId);
		        intent.putExtra(DB.KEY_NOTE_TITLE, mDb.getNoteTitleById(mRowId));
		        intent.putExtra(DB.KEY_NOTE_AUDIO_URI , mDb.getNoteAudioUriById(mRowId));
		        intent.putExtra(DB.KEY_NOTE_PICTURE_URI , mDb.getNotePictureUriById(mRowId));
		        intent.putExtra(DB.KEY_NOTE_BODY, mDb.getNoteBodyById(mRowId));
		        intent.putExtra(DB.KEY_NOTE_CREATED, mDb.getNoteCreatedTimeById(mRowId));
		        startActivityForResult(intent, EDIT_CURRENT_VIEW);
            }
        });        
        
        // send note button
        sendButton = (Button) findViewById(R.id.view_send);
        sendButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_send, 0, 0, 0);
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                // set Sent string Id
				List<Long> rowArr = new ArrayList<Long>();
				rowArr.add(0,mRowId);
                // mail
				Intent intent = new Intent(Note_view_pager.this, SendMailAct.class);
		        String extraStr = Util.getStringWithXmlTag(rowArr);
		        extraStr = Util.addXmlTag(extraStr);
		        intent.putExtra("SentString", extraStr);
		        String picFile = mDb.getNotePictureUriById(mRowId);
				System.out.println("-> picFile = " + picFile);
				if( (picFile != null) && 
				 	(picFile.length() > 0) )
				{
					String picFileArray[] = new String[]{picFile};
			        intent.putExtra("SentPictureFileNameArray", picFileArray);
				}
				startActivityForResult(intent, MAIL_CURRENT_VIEW);
            }
        });
        
        // back button
        backButton = (Button) findViewById(R.id.view_back);
        backButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
        backButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) {
        		if(AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE)
        			UtilAudio.stopAudioPlayer(); 
        		
        		VideoPlayer.stopVideo();
                finish();
            }
        });

    } //onCreate end
    
	public static int getStyle() {
		return mStyle;
	}

	public void setStyle(int style) {
		mStyle = style;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		System.out.println("Note_view_pager / onActivityResult ");
        if((requestCode==EDIT_CURRENT_VIEW) || (requestCode==MAIL_CURRENT_VIEW))
        {
    		if(AudioPlayer.mAudioPlayMode  == AudioPlayer.ONE_TIME_MODE)
    			UtilAudio.stopAudioPlayer(); 
    		
    		VideoPlayer.stopVideo();
        	finish();
        }
        
        if(requestCode==Note_view_pager_adapter.VIEW_LINK)
        {
        	finish();
        }
	}
	
    //Refer to http://stackoverflow.com/questions/4434027/android-videoview-orientation-change-with-buffered-video
	/***************************************************************
	video play spec of Pause and Rotate:
	1. Rotate: keep pause state
	 pause -> rotate -> pause -> play -> continue

	2. Rotate: keep play state
	 play -> rotate -> continue play

	3. Key guard: enable pause
	 play -> key guard on/off -> pause -> play -> continue

	4. Key guard and Rotate: keep pause
	 play -> key guard on/off -> pause -> rotate -> pause
	 ****************************************************************/	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    System.out.println("Note_view_pager / _onConfigurationChanged");
	    if(UtilVideo.mVideoView != null)
	    	UtilVideo.setVideoViewDimensions(UtilVideo.mBitmapDrawable);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		System.out.println("Note_view_pager / _onStart");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		System.out.println("Note_view_pager / _onResume");
		
		isPagerActive = true;
		
		if(UtilVideo.mVideoView != null)
		{
			if(!UtilVideo.hasMediaControlWidget )
			{
				UtilVideo.showVideoPlayButtonState();
				UtilVideo.mVideoPlayButton.setOnClickListener(UtilVideo.videoPlayBtnListener);
				
				Note_view_pagerUI.showPagerUI(mPager.getCurrentItem());
			}
			else if(UtilVideo.hasMediaControlWidget) //??? add App switch event?  
			{
					UtilVideo.setVideoViewLayout(getCurrentPictureString());
		   			UtilVideo.playOrPauseVideo(getCurrentPictureString());
			}
		}
		else if(mPlayVideoPositionOfInstance > 0)
		{
    		showSelectedView();
    		invalidateOptionsMenu();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("Note_view_pager / _onPause");
		
		isPagerActive = false;
		
		// set pause when key guard is ON
		if( UtilVideo.mVideoView != null) 
		{
			UtilVideo.mPlayVideoPosition = UtilVideo.mVideoView.getCurrentPosition();
			
			// keep play video position
			mPlayVideoPositionOfInstance = UtilVideo.mPlayVideoPosition; 
			System.out.println("Note_view_pager / _onPause / mPlayVideoPositionOfInstance = " + mPlayVideoPositionOfInstance);
			
			if(UtilVideo.mVideoPlayer != null)//??? try more to check if this is better? or still keep video view
				VideoPlayer.stopVideo();
		}
		
		// to stop youtube web view running
    	String tagStr = "current"+Note_view_pager.mPager.getCurrentItem()+"webView";
    	CustomWebView customWebView = (CustomWebView) mPager.findViewWithTag(tagStr);
    	
    	if( customWebView!= null)
    	{
			try {
		        Class.forName("android.webkit.WebView")
		             .getMethod("onPause", (Class[]) null)
		             .invoke(customWebView, (Object[]) null);
		    } catch(ClassNotFoundException cnfe) {
		        System.out.println("ClassNotFoundException");
		    } catch(NoSuchMethodException nsme) {
		        System.out.println("NoSuchMethodException");
		    } catch(InvocationTargetException ite) {
		        System.out.println("InvocationTargetException");
		    } catch (IllegalAccessException iae) {
		        System.out.println("IllegalAccessException");
		    }	
    	}
		
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mIsViewModeChanged = false;
		System.out.println("Note_view_pager / _onStop");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		System.out.println("Note_view_pager / _onDestroy");
	};

	// avoid exception: has leaked window android.widget.ZoomButtonsController
	@Override
	public void finish() {
		
		if(mPagerHandler != null)
			mPagerHandler.removeCallbacks(mOnBackPressedRun);		
	    
		ViewGroup view = (ViewGroup) getWindow().getDecorView();
	    view.setBackgroundColor(color.background_dark); // avoid white flash
	    view.removeAllViews();
	    
	    super.finish();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		System.out.println("Note_view_pager / _onSaveInstanceState");
	};
	
	// On Create Options Menu
    static Menu mMenu;
    int mSubMenu0Id;
    public static MenuItem mMenuItemAudio;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        super.onCreateOptionsMenu(menu);
    	mMenu = menu;
        // update row Id
        mRowId = mDb.getNoteId(mPager.getCurrentItem(),true);

        // menu item: for audio play
		MenuItem itemAudio = menu.add(0, R.id.AUDIO_IN_VIEW , 0, R.string.note_audio )
 								 .setIcon(R.drawable.ic_lock_ringer_on);
		itemAudio.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		mMenuItemAudio = itemAudio;
		
        if(currentNoteHasAudioUri())
        	mMenuItemAudio.setVisible(true);
        else
        	mMenuItemAudio.setVisible(false);
		
		if((AudioPlayer.mMediaPlayer != null)&&
		   (AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE) &&
		    mMenuItemAudio.isVisible())
		{
			// show playing state
			if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
				mMenuItemAudio.setIcon(R.drawable.ic_audio_selected); //highlight
			else if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
					 (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    ) 
				mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on);
		}
		
        // menu item: with sub menu for View note mode selection
        SubMenu subMenu0 = menu.addSubMenu(0, R.id.VIEW_NOTE_MODE, 1, R.string.view_note_mode);
	    MenuItem subMenuItem0 = subMenu0.getItem();
	    mSubMenu0Id = subMenuItem0.getItemId();
	    // set icon
	    subMenuItem0.setIcon(android.R.drawable.ic_menu_view);
	    // set sub menu display
		subMenuItem0.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	    subMenuItem0.setEnabled(true);

	    // sub menu item list
        // picture and text
	    MenuItem subItem = subMenu0.add(0, R.id.VIEW_ALL, 1, R.string.view_note_mode_all);
        subItem.setIcon(R.drawable.btn_check_on_holo_dark);
   		markCurrentSelected(subItem,"ALL");
        // picture only		
        subItem = subMenu0.add(0, R.id.VIEW_PICTURE, 2, R.string.view_note_mode_picture);
        markCurrentSelected(subItem,"PICTURE_ONLY");		
        // text only		
	    subItem = subMenu0.add(0, R.id.VIEW_TEXT, 3, R.string.view_note_mode_text);
	    markCurrentSelected(subItem,"TEXT_ONLY");
	    
	    // menu item: previous
		MenuItem itemPrev = menu.add(0, R.id.ACTION_PREVIOUS, 2, R.string.view_note_slide_action_previous )
				 				.setIcon(R.drawable.ic_media_previous);
	    itemPrev.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		itemPrev.setEnabled(mPager.getCurrentItem() > 0);
		itemPrev.getIcon().setAlpha(mPager.getCurrentItem() > 0?255:30);
		
		// menu item: next or finish
        // Add either a "next" or "finish" button to the action bar, depending on which page is currently selected.
		MenuItem itemNext = menu.add(0, R.id.ACTION_NEXT, 3, 
                					(mPager.getCurrentItem() == mPagerAdapter.getCount() - 1)
                							? R.string.view_note_slide_action_finish
                							: R.string.view_note_slide_action_next)
                				.setIcon(R.drawable.ic_media_next);
        itemNext.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        // set Gray for Last item
        if(mPager.getCurrentItem() == (mPagerAdapter.getCount() - 1))
        	itemNext.setEnabled( false );

        itemNext.getIcon().setAlpha(mPager.getCurrentItem() == (mPagerAdapter.getCount() - 1)?30:255);
        //??? why affect image button, workaround: one uses local, one uses system
        
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	// called after _onCreateOptionsMenu
        return true;
    }  
    
    // for menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            	if(isTextMode())
            	{
        			// back to view all mode
	        		mPref_show_note_attribute.edit()
	        								 .putString("KEY_PAGER_VIEW_MODE","ALL")
	        								 .commit();
	        		showSelectedView();
	        		mAct.invalidateOptionsMenu();             		
            	}
            	else if(isViewAllMode())
            	{
	        		if(AudioPlayer.mAudioPlayMode  == AudioPlayer.ONE_TIME_MODE)
	        			UtilAudio.stopAudioPlayer();    
	        		
	        		VideoPlayer.stopVideo();
	            	finish();
            	}
                return true;

            case R.id.AUDIO_IN_VIEW:
            	TabsHostFragment.setAudioPlayingTab_WithHighlight(false);// in case playing audio in pager
            	playAudioInPager();
        		return true;                
                
            case R.id.VIEW_ALL:
        		mPref_show_note_attribute.edit()
        								 .putString("KEY_PAGER_VIEW_MODE","ALL")
        								 .commit();
        		showSelectedView();
        		invalidateOptionsMenu();
            	return true;
            	
            case R.id.VIEW_PICTURE:
        		mPref_show_note_attribute.edit()
        								 .putString("KEY_PAGER_VIEW_MODE","PICTURE_ONLY")
        								 .commit();
        		showSelectedView();
        		invalidateOptionsMenu();
            	return true;

            case R.id.VIEW_TEXT:
        		mPref_show_note_attribute.edit()
        								 .putString("KEY_PAGER_VIEW_MODE","TEXT_ONLY")
        								 .commit();
        		showSelectedView();
        		invalidateOptionsMenu();
            	return true;
            	
            case R.id.ACTION_PREVIOUS:
                // Go to the previous step in the wizard. If there is no previous step,
                // setCurrentItem will do nothing.
            	Note_view_pager.mCurrentPosition--;
            	mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                return true;

            case R.id.ACTION_NEXT:
                // Advance to the next step in the wizard. If there is no next step, setCurrentItem
                // will do nothing.
            	Note_view_pager.mCurrentPosition++;
            	mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            	
            	//TO
//            	mMP.setVolume(0,0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    
//    public static MediaPlayer mMP;//TO
    
    // on back pressed
    @Override
    public void onBackPressed() {

    	if(isPictureMode())
    	{
	    	// dispatch touch event to show buttons
	    	long downTime = SystemClock.uptimeMillis();
	    	long eventTime = SystemClock.uptimeMillis() + 100;
	    	float x = 0.0f;
	    	float y = 0.0f;
	    	// List of meta states found here: developer.android.com/reference/android/view/KeyEvent.html#getMetaState()
	    	int metaState = 0;
	    	MotionEvent event = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, 
	                								x, y,  
	                								metaState);
	    	dispatchTouchEvent(event);
	    	event.recycle();
	    	
	    	// in order to make sure ImageViewBackButton is effective to be clicked
	    	mPagerHandler = new Handler();
	    	mPagerHandler.postDelayed(mOnBackPressedRun, 500);
    	}
    	else
    	{
    		if(AudioPlayer.mAudioPlayMode  == AudioPlayer.ONE_TIME_MODE)
    			UtilAudio.stopAudioPlayer();    
    		
    		VideoPlayer.stopVideo();
        	finish();
    	}
    }
    
    static Handler mPagerHandler;
	static Runnable mOnBackPressedRun = new Runnable()
	{   @Override
		public void run()
		{
			if(Note_view_pagerUI.picView_back_button != null)
				Note_view_pagerUI.picView_back_button.performClick();
			
			if(Note_view_pager_adapter.mIntentView != null)
				Note_view_pager_adapter.mIntentView = null;
		}
	};
    
    // check if current note has audio Uri
    static boolean currentNoteHasAudioUri()
    {
		String audioStr = getCurrentAudioString();
		return UtilAudio.hasAudioExtension(audioStr)?true:false;
    }
    
    // get current audio string
    public static String getCurrentAudioString()
    {
		return  mDb.getNoteAudioUri(mCurrentPosition,true);
    }    
    
    // check if current note has video Uri
    static boolean currentNoteHasVideoUri()
    {
		String pictureStr = getCurrentPictureString();
		return UtilVideo.hasVideoExtension(pictureStr,mAct)?true:false;
    }
    
    // get current picture string
    public static String getCurrentPictureString()
    {
		return mDb.getNotePictureUri(mCurrentPosition,true);
    }
    
    
    static void playAudioInPager()
    {
		if(currentNoteHasAudioUri())
		{
    		AudioPlayer.mAudioIndex = mCurrentPosition;
    		// new instance
    		if(AudioPlayer.mMediaPlayer == null)
    		{   
        		int lastTimeView_NotesTblId =  Integer.valueOf(Util.getPref_lastTimeView_notes_tableId(mAct));
    			DrawerActivity.mCurrentPlaying_notesTableId = lastTimeView_NotesTblId;
        		AudioPlayer.mAudioPlayMode = AudioPlayer.ONE_TIME_MODE; 
    		}
    		// If Audio player is NOT at One time mode and media exists
    		else if((AudioPlayer.mMediaPlayer != null) && 
    				(AudioPlayer.mAudioPlayMode == AudioPlayer.CONTINUE_MODE))
    		{
        		AudioPlayer.mAudioPlayMode = AudioPlayer.ONE_TIME_MODE;
        		UtilAudio.stopAudioPlayer();
    		}
    		
   			AudioPlayer.prepareAudioInfo(mAct);
    		
    		AudioPlayer.manageAudioState(mAct);
			
    		// update playing state
    		if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
    			mMenuItemAudio.setIcon(R.drawable.ic_audio_selected); //highlight
    		else if( (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
    				 (AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    ) 
    			mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on); // no highlight

    		// update playing state of picture mode
    		Note_view_pagerUI.showPagerUI(mCurrentPosition);
		}    	
    }
    
    // Mark current selected 
    void markCurrentSelected(MenuItem subItem, String str)
    {
        if(mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
        							.equalsIgnoreCase(str))
        	subItem.setIcon(R.drawable.btn_radio_on_holo_dark);
	  	else
        	subItem.setIcon(R.drawable.btn_radio_off_holo_dark);
    }    
    
    // show audio name
    void showAudioName()
    {
        String audio_name = "";
    	if(!Util.isEmptyString(mAudioUriInDB))
		{
			audio_name = getResources().getText(R.string.note_audio) +
						 ": " + 
						 Util.getDisplayNameByUriString(mAudioUriInDB,this);
		}        
   		mAudioTextView.setText(audio_name);
    }
    
    // Show selected view
    static void showSelectedView()
    {
   		mIsViewModeChanged = false;

   		if(Note_view_pager.isTextMode())
   			VideoPlayer.stopVideo();
   		else if(!Note_view_pager.isTextMode())
   		{
	   		if(UtilVideo.mVideoView != null)
	   		{
	   	   		// keep current video position for NOT text mode
	   			mPosistionOfChangeView = UtilVideo.mVideoView.getCurrentPosition();
	   			mIsViewModeChanged = true;

	   			if(VideoPlayer.mVideoHandler != null)
	   			{
	   				VideoPlayer.mVideoHandler.removeCallbacks(VideoPlayer.mRunPlayVideo); 
	   				if(UtilVideo.hasMediaControlWidget)
	   					VideoPlayer.cancelMediaController();
	   				System.out.println("Note_view_pager / _showSelectedView / just remove callbacks");
	   			}
	   		}
   			Note_view_pager_adapter.mLastPosition = -1;
   		}
   		
//    	invalidateOptionsMenu(); //update selected mode
    	if(mPagerAdapter != null)
    		mPagerAdapter.notifyDataSetChanged(); // will call Note_view_pager_adapter / _setPrimaryItem
    	Note_view_pagerUI.showPagerUI(mPager.getCurrentItem());
    }
    
    public static int mPosistionOfChangeView;
    public static boolean mIsViewModeChanged;
    
//    public void showToast (int strId){
//    	String st = getResources().getText(strId).toString();
//        try{ toast.getView().isShown();     // true if visible
//            toast.setText(st);
//        } catch (Exception e) {         // invisible if exception
//            toast = Toast.makeText(Note_view_pager.this, st, Toast.LENGTH_SHORT);
//            }
//        toast.show();  //finally display it
//    }
    
    // show picture or not
    public static void showImageByTouchImageView(final View spinner, TouchImageView pictureView, String strPicture) 
    {
    	if(Util.isEmptyString(strPicture))
    	{
    		pictureView.setImageResource(mStyle%2 == 1 ?
	    			R.drawable.btn_radio_off_holo_light:
	    			R.drawable.btn_radio_off_holo_dark);//R.drawable.ic_empty);
    	}
    	else if(!Util.isUriExisted(strPicture,mAct))	
    	{
    		pictureView.setImageResource(R.drawable.ic_cab_done_holo);
    	}
    	else
    	{
    		Uri imageUri = Uri.parse(strPicture);
    	  	
    		if(imageUri.isAbsolute())
    			UilCommon.imageLoader.displayImage(imageUri.toString(), 
    											   pictureView,
    											   UilCommon.optionsForFadeIn,
    											   new SimpleImageLoadingListener()
    		{
				@Override
				public void onLoadingStarted(String imageUri, View view) 
				{
					System.out.println("_showImageByTouchImageView / onLoadingStarted");
					// make spinner appears at center
					LinearLayout.LayoutParams paramsSpinner = (LinearLayout.LayoutParams) spinner.getLayoutParams();
//					paramsSpinner.weight = (float) 1.0;
					paramsSpinner.weight = (float) 1000.0; //??? still see garbage at left top corner
					spinner.setLayoutParams(paramsSpinner);
					spinner.setVisibility(View.VISIBLE);
					view.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingFailed(String imageUri, View view, FailReason failReason) 
				{
					System.out.println("_showImageByTouchImageView / onLoadingFailed");
					String message = null;
					switch (failReason.getType()) 
					{
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";//??? mark this line?
							break;
					}
					Toast.makeText(mAct, message, Toast.LENGTH_SHORT).show();
					spinner.setVisibility(View.GONE);
					view.setVisibility(View.GONE);
				}

				@Override
				public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
				{
					System.out.println("_showImageByTouchImageView / onLoadingComplete");
					spinner.setVisibility(View.GONE);
					view.setVisibility(View.VISIBLE);
				}
			});
    	}
	}
    
    
    static boolean isPictureMode()
    {
	  	if(mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
	  	   	  						.equalsIgnoreCase("PICTURE_ONLY"))
	  		return true;
	  	else
	  		return false;
    }
    
    static boolean isViewAllMode()
    {
	  	if(mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
	  	   	  						.equalsIgnoreCase("ALL"))
	  		return true;
	  	else
	  		return false;
    }

    static boolean isTextMode()
    {
	  	if(mPref_show_note_attribute.getString("KEY_PAGER_VIEW_MODE", "ALL")
	  	   	  						.equalsIgnoreCase("TEXT_ONLY"))
	  		return true;
	  	else
	  		return false;
    }    
    
    static int VIEW_PORT_BY_NONE = 0;
    static int VIEW_PORT_BY_DEVICE_WIDTH = 1;
    static int VIEW_PORT_BY_SCREEN_WIDTH = 2; 
    
    static String getHTMLstringWithViewPort(int position, int viewPort)
    {
    	String strTitle = mDb.getNoteTitle(position,true);
    	String strBody = mDb.getNoteBody(position,true);
    	Long createTime = mDb.getNoteCreatedTime(position,true);
    	String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+
		       	  	  "<html><head>" +
	  		       	  "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />";
    	
    	if(viewPort == VIEW_PORT_BY_NONE)
    	{
	    	head = head + "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_DEVICE_WIDTH)
    	{
	    	head = head + 
	    		   "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
	     	  	   "<head>";
    	}
    	else if(viewPort == VIEW_PORT_BY_SCREEN_WIDTH)
    	{
//        	int screen_width = UtilImage.getScreenWidth(mAct);
        	int screen_width = 640;
	    	head = head +
	    		   "<meta name=\"viewport\" content=\"width=" + String.valueOf(screen_width) + ", initial-scale=1\">"+
   	  			   "<head>";
    	}
    		
       	String seperatedLineTitle = (strTitle.length()!=0)?"<hr size=2 color=blue width=99% >":"";
       	String seperatedLineBody = (strBody.length()!=0)?"<hr size=1 color=black width=99% >":"";

       	// title
    	Spannable spanTitle = new SpannableString(strTitle);
    	Linkify.addLinks(spanTitle, Linkify.ALL);
    	spanTitle.setSpan(new AlignmentSpan.Standard(Alignment.ALIGN_CENTER), 
    					  0,
    					  spanTitle.length(), 
    					  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    	strTitle = Html.toHtml(spanTitle);
    	
    	// body
    	Spannable spanBody = new SpannableString(strBody);
    	Linkify.addLinks(spanBody, Linkify.ALL);
    	strBody = Html.toHtml(spanBody);
    	
    	// set web view text color
    	String colorStr = Integer.toHexString(Util.mText_ColorArray[mStyle]);
    	colorStr = colorStr.substring(2);
    	
    	String bgColorStr = Integer.toHexString(Util.mBG_ColorArray[mStyle]);
    	bgColorStr = bgColorStr.substring(2);
    	
    	String content = head + "<body color=\"" + bgColorStr + "\">" +
		         "<p align=\"center\"><b>" + 
				 "<font color=\"" + colorStr + "\">" + strTitle + "</font>" + 
         		 "</b></p>" + seperatedLineTitle + 
		         "<p>" + 
				 "<font color=\"" + colorStr + "\">" + strBody + "</font>" +
				 "</p>" + seperatedLineBody + 
		         "<p align=\"right\">" + 
				 "<font color=\"" + colorStr + "\">"  + Util.getTimeString(createTime) + "</font>" +
		         "</p>" + 
		         "</body></html>";
		return content;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int maskedAction = event.getActionMasked();
        switch (maskedAction) {

	        case MotionEvent.ACTION_DOWN:
	        case MotionEvent.ACTION_POINTER_DOWN: 
	        		// update playing state of picture mode
	        		System.out.println("Note_view_pager / dispatchTouchEvent / MotionEvent.ACTION_DOWN / mPager.getCurrentItem() =" + mPager.getCurrentItem());
    	    		if(Note_view_pagerUI.mOnDelay)
   	    				Note_view_pagerUI.delay_picViewUI_top_off(this,1000);
    	    		else
    	    			Note_view_pagerUI.showPagerUI(mPager.getCurrentItem());
    	  	  	 break;
	        case MotionEvent.ACTION_MOVE: 
	        case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_POINTER_UP:
	        case MotionEvent.ACTION_CANCEL: 
	        	 break;
        }

        boolean ret = super.dispatchTouchEvent(event);
        return ret;
    }    
}