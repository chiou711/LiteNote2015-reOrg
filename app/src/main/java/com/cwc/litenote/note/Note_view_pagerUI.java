package com.cwc.litenote.note;

import com.cwc.litenote.R;
import com.cwc.litenote.TabsHostFragment;
import com.cwc.litenote.media.audio.AudioPlayer;
import com.cwc.litenote.media.image.UtilImage;
import com.cwc.litenote.media.video.UtilVideo;
import com.cwc.litenote.media.video.VideoPlayer;
import com.cwc.litenote.util.Util;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class Note_view_pagerUI extends BroadcastReceiver 
{
	public static boolean showSeekBarProgress;	
	public static Button picView_back_button;
	public static TextView picView_title;
	public static Button picView_audio_button;
	public static Button picView_viewMode_button;
	public static Button picView_previous_button;
	public static Button picView_next_button;
	public static TextView videoView_currPosistion;
	public static SeekBar videoView_seekBar;
	public static TextView videoView_fileLength;
    public static int videoFileLength_inMilliSeconds;
    public static int videoView_progress;
    
	public Note_view_pagerUI(){};
   
	public void setPagerUI_listeners(final String strPicture, ViewGroup viewGroup) 		
	{
        picView_back_button = (Button) (viewGroup.findViewById(R.id.image_view_back));
        picView_audio_button = (Button) (viewGroup.findViewById(R.id.image_view_audio));
        picView_viewMode_button = (Button) (viewGroup.findViewById(R.id.image_view_mode));
        picView_previous_button = (Button) (viewGroup.findViewById(R.id.image_view_previous));
        picView_next_button = (Button) (viewGroup.findViewById(R.id.image_view_next));
        videoView_currPosistion = (TextView) (viewGroup.findViewById(R.id.video_current_pos));
        videoView_seekBar = (SeekBar)(viewGroup.findViewById(R.id.video_seek_bar));
        videoView_fileLength = (TextView) (viewGroup.findViewById(R.id.video_file_length));

        // view mode 
    	// picture only
	  	if(Note_view_pager.isPictureMode())
	  	{
			// image: view back
	  		picView_back_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back /*android.R.drawable.ic_menu_revert*/, 0, 0, 0);
			// click to finish Note_view_pager
	  		picView_back_button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View view) 
	            {
        			// back to view all mode
	        		Note_view_pager.mPref_show_note_attribute.edit()
	        												 .putString("KEY_PAGER_VIEW_MODE","ALL")
	        												 .commit();
	        		Note_view_pager.showSelectedView();
	        		Note_view_pager.mAct.invalidateOptionsMenu(); 
	            }
	        });   
			
			// click to play audio 
	  		picView_audio_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
	  		picView_audio_button.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View view) {
	            	TabsHostFragment.setAudioPlayingTab_WithHighlight(false);// in case playing audio in pager
	            	Note_view_pager.playAudioInPager();
	            }
	        });       			
			
			// image: view mode
	  		picView_viewMode_button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_view, 0, 0, 0);
			// click to select view mode 
	  		picView_viewMode_button.setOnClickListener(new View.OnClickListener() {

	            public void onClick(View view) {
//	            	Note_view_pager.mAct.invalidateOptionsMenu();
            		Note_view_pager.mMenu.performIdentifierAction(R.id.VIEW_NOTE_MODE, 0);
            		//fix: update current video position will cause view mode option menu disappear
            		showSeekBarProgress = false; 
	            }
	        });       			
			
			// image: previous button
	  		picView_previous_button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_previous, 0, 0, 0);
			// click to previous 
	  		picView_previous_button.setOnClickListener(new View.OnClickListener() 
	        {
	            public void onClick(View view) {
	            	// since onPageChanged is not called fast enough, add stop functions below
	            	Note_view_pager.mCurrentPosition--;
	            	Note_view_pager.mPager.setCurrentItem(Note_view_pager.mPager.getCurrentItem() - 1);
	            }
	        });   
        
			// image: next button
	  		picView_next_button.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_media_next, 0, 0, 0);
			// click to next 
	  		picView_next_button.setOnClickListener(new View.OnClickListener()
	        {
	            public void onClick(View view) {
	            	// since onPageChanged is not called fast enough, add stop functions below
	            	Note_view_pager.mCurrentPosition++;
	            	Note_view_pager.mPager.setCurrentItem(Note_view_pager.mPager.getCurrentItem() + 1);
	            }
	        }); 
	  	}
	  	
	  	if(Note_view_pager.isPictureMode()|| Note_view_pager.isViewAllMode())
	  	{
			if(!UtilVideo.hasMediaControlWidget)
			{
				// set video seek bar listener
				videoView_seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() 
				{
					// onStartTrackingTouch
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						System.out.println("Note_view_pager_UI / _onStartTrackingTouch");
						if( (UtilVideo.mVideoPlayer == null)  && (UtilVideo.mVideoView != null))
						{
							if(Build.VERSION.SDK_INT >= 16)
								UtilVideo.mVideoView.setBackground(null);
							else
								UtilVideo.mVideoView.setBackgroundDrawable(null);
							
							UtilVideo.mVideoView.setVisibility(View.VISIBLE);
							UtilVideo.mVideoPlayer = new VideoPlayer(strPicture, UtilVideo.mAct);
							UtilVideo.mVideoView.seekTo(UtilVideo.mPlayVideoPosition);
						}
					}
					
					// onProgressChanged
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
					{
						System.out.println("Note_view_pager_UI / _onProgressChanged");
						if(fromUser)
						{	
							// show progress change
					    	int currentPos = videoFileLength_inMilliSeconds*progress/(seekBar.getMax()+1);
					    	// update current play time
					     	videoView_currPosistion.setText(Util.getTimeFormatString(currentPos));
					     	
					     	//add below to keep showing seek bar
					     	if(Note_view_pager.isPictureMode())
					     		showPicViewUI_previous_next(true,mPosition);
					        showSeekBarProgress = true;
					    	delay_pagerUI_all_off(Note_view_pager.mAct, System.currentTimeMillis() + 1000 * 3); // for 3 seconds		        
						}
					}
					
					// onStopTrackingTouch
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) 
					{
						System.out.println("Note_view_pager_UI / _onStopTrackingTouch");
						if( UtilVideo.mVideoView != null  )
						{
							int mPlayVideoPosition = (int) (((float)(videoFileLength_inMilliSeconds / 100)) * seekBar.getProgress());
							if(UtilVideo.mVideoPlayer != null)
								UtilVideo.mVideoView.seekTo(mPlayVideoPosition);
						}
					}	
					
				});
			}
	  	}
   }

	public static void delay_pagerUI_all_off(Context context, long timeMilliSec) 
	{
		 Intent intent = new Intent(context, Note_view_pagerUI.class);
		 AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		 intent = intent.putExtra("REQUESTCODE",1);
		 PendingIntent pendIntent = PendingIntent.getBroadcast(context, 1/*requestCode */, intent, 0);
		 alarmMgr.set(AlarmManager.RTC, timeMilliSec, pendIntent);
		 mOnDelay = true;
	}
	
	public static void delay_picViewUI_top_off(Context context, long timeMilliSec) 
	{
		 Intent intent = new Intent(context, Note_view_pagerUI.class);
		 AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		 intent = intent.putExtra("REQUESTCODE",2);
		 PendingIntent pendIntent = PendingIntent.getBroadcast(context, 2/*requestCode */, intent, 0);
		 alarmMgr.set(AlarmManager.RTC, timeMilliSec, pendIntent);
	}

	public static boolean mOnDelay;
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
//		System.out.println("Note_view_pager_UI / _onReceive");
		
		if(intent.getIntExtra("REQUESTCODE", 0) == 1)
		{
			System.out.println("Note_view_pager_UI / _onReceive / REQUESTCODE = 1 ");
			// add for fixing exception after App is not alive, but PendingInetent still run as plan
			if(Note_view_pager.mPager != null)  
			{
				String tagImageStr = "current"+ Note_view_pager.mPager.getCurrentItem() +"imageView";
				ViewGroup imageGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagImageStr);
		       
				if(imageGroup != null)
				{
					// to distinguish image and video, does not show video play icon 
					// only when video is playing
					setUI_all_off();
					showSeekBarProgress = false;
				   
					// set Full screen when buttons are off
					if(Note_view_pager.isPictureMode())
					{
						Window win = Note_view_pager.mAct.getWindow();
						win.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);		
						win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
									 WindowManager.LayoutParams.FLAG_FULLSCREEN);	
					}
				}
				
				mOnDelay = false;
				//??? add below will cause audio noise when WebView changing
	//		  	if(Note_view_pager.isTextMode())
	//		  	{
	//		  	    Note_view_pager.editButton.setVisibility(View.GONE);
	//		  	    Note_view_pager.sendButton.setVisibility(View.GONE);
	//		  	    Note_view_pager.backButton.setVisibility(View.GONE);
	//		  	    Note_view_pager.mAct.getActionBar().hide();
	//		  	}			
			}
		}
		else if(intent.getIntExtra("REQUESTCODE", 0) == 2)
		{
			System.out.println("Note_view_pager_UI / _onReceive / REQUESTCODE = 2 ");
			setUI_top_off();
			mOnDelay = false;
		}
	}
	
	static void setUI_top_off()
	{
		picView_title.setVisibility(View.GONE);
		
		picView_back_button.setVisibility(View.GONE);
		picView_audio_button.setVisibility(View.GONE);
		picView_viewMode_button.setVisibility(View.GONE);
		
		if(!UtilVideo.hasMediaControlWidget)
			UtilVideo.updateVideoPlayButtonState();	
		
		String str = Note_view_pager.getCurrentPictureString();
		Activity act = Note_view_pager.mAct;
		if(Note_view_pager.isPictureMode() && UtilImage.hasImageExtension(str, act))
		{
			picView_previous_button.setVisibility(View.GONE);
			picView_next_button.setVisibility(View.GONE);
		}		
	}
	
	static void setUI_all_off()
	{
		setUI_top_off();	
		
		picView_previous_button.setVisibility(View.GONE); //??? how to avoid this flash?
		picView_next_button.setVisibility(View.GONE);

		videoView_currPosistion.setVisibility(View.GONE);
		videoView_seekBar.setVisibility(View.GONE);
		videoView_fileLength.setVisibility(View.GONE);
	}	
	

   
	static int mPosition;
	static void showPagerUI(int position)
	{
        String tagImageStr = "current"+ position +"imageView";
        System.out.println("Note_view_pager_UI / _showControlButtons / tagImageStr = " + tagImageStr);
        
        mPosition = position;
        
        ViewGroup imageGroup = (ViewGroup) Note_view_pager.mPager.findViewWithTag(tagImageStr);
        if(imageGroup != null)
        {
	        picView_title = (TextView) (imageGroup.findViewById(R.id.image_title));

        	if(Note_view_pager.isPictureMode())
        		picView_back_button.setVisibility(View.VISIBLE);
        	else
        		picView_back_button.setVisibility(View.GONE);
        	
        	picView_back_button = (Button) (imageGroup.findViewById(R.id.image_view_back));
	        picView_audio_button = (Button) (imageGroup.findViewById(R.id.image_view_audio));
	        picView_viewMode_button = (Button) (imageGroup.findViewById(R.id.image_view_mode));
	        picView_previous_button = (Button) (imageGroup.findViewById(R.id.image_view_previous));
	        picView_next_button = (Button) (imageGroup.findViewById(R.id.image_view_next));

	        videoView_currPosistion = (TextView) (imageGroup.findViewById(R.id.video_current_pos));
	        videoView_seekBar = (SeekBar)(imageGroup.findViewById(R.id.video_seek_bar));
	        videoView_fileLength = (TextView) (imageGroup.findViewById(R.id.video_file_length));
        	
	        // set image title
        	String strPicture = Note_view_pager.mDb.getNotePictureUri(position,true);

			if(!Util.isEmptyString(strPicture))
				strPicture = Util.getDisplayNameByUriString(strPicture, Note_view_pager.mAct);
			else
				strPicture = "";		        
	        
			if(!Util.isEmptyString(strPicture))
			{
				picView_title.setVisibility(View.VISIBLE);
				picView_title.setText(strPicture);
			}
			else
				picView_title.setVisibility(View.INVISIBLE);
			
	        if(UtilVideo.hasVideoExtension(strPicture, Note_view_pager.mAct))
	        {
				if(!UtilVideo.hasMediaControlWidget)
			        UtilVideo.showVideoPlayButtonState();
				else
		    		showPicViewUI_previous_next(false,0);    	
	        }
	        
	        // audio playing state for one time mode
	        if((AudioPlayer.mAudioPlayMode == AudioPlayer.ONE_TIME_MODE) &&
	           (position == AudioPlayer.mAudioIndex))
	        {
        		if(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PLAY)
        			picView_audio_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_audio_selected, 0, 0, 0);
        		else if((AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_PAUSE) ||
        				(AudioPlayer.mPlayerState == AudioPlayer.PLAYER_AT_STOP)    )
        			picView_audio_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
	        }
	        
	        // set image view Audio button visibility
	        if(Note_view_pager.currentNoteHasAudioUri() && 
	           Note_view_pager.isPictureMode())
	        	picView_audio_button.setVisibility(View.VISIBLE);
	        else
	        	picView_audio_button.setVisibility(View.GONE);
	        
	        // set image view buttons (View Mode, Previous, Next) visibility
	        if(Note_view_pager.isPictureMode())
	        {
	        	picView_viewMode_button.setVisibility(View.VISIBLE);
	        	
	        	// show previous/next buttons for image, not for video
	        	if(!UtilVideo.hasMediaControlWidget )
	        		showPicViewUI_previous_next(true,position);
	        	else if(UtilVideo.mVideoView == null) // for image
        			showPicViewUI_previous_next(true,position);
	        }
	        else
	        {
	        	showPicViewUI_previous_next(false,0);
	        	picView_viewMode_button.setVisibility(View.GONE);
	        }
			
			// show seek bar for video only
	        if(!UtilVideo.hasMediaControlWidget)
	        {
				if(Note_view_pager.currentNoteHasVideoUri())
				{
					String  curPicStr = Note_view_pager.getCurrentPictureString();
					MediaPlayer mp = MediaPlayer.create(Note_view_pager_adapter.mAct,
														Uri.parse(curPicStr));
					videoFileLength_inMilliSeconds = mp.getDuration();
					mp.release();
					
					primaryVideoSeekBarProgressUpdater(UtilVideo.mPlayVideoPosition);
				}
				else
				{
					videoView_currPosistion.setVisibility(View.GONE);
					videoView_seekBar.setVisibility(View.GONE);
					videoView_fileLength.setVisibility(View.GONE);
				}
	        }
		    
	        // set Not full screen
	        if(Note_view_pager.isPictureMode())
	        {
	        	Note_view_pager.mAct.getActionBar().hide();
	        	// set full screen
		        Window win = Note_view_pager.mAct.getWindow();
		        win.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		  	    			 WindowManager.LayoutParams.FLAG_FULLSCREEN);
		    }
	        else
	        {
	        	Note_view_pager.mAct.getActionBar().show();
	        	// set NOT full screen
		        Window win = Note_view_pager.mAct.getWindow();
		        win.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		        win.setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
		  	    			 WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		    }
	        
	        showSeekBarProgress = true;
	    	delay_pagerUI_all_off(Note_view_pager.mAct, System.currentTimeMillis() + 1000 * 3); // for 3 seconds		        
        }
        
        //To show action bar buttons or not is dependent on view mode
	  	if(Note_view_pager.isViewAllMode()|| Note_view_pager.isTextMode())
	  	{
	  		Note_view_pager.editButton.setVisibility(View.VISIBLE);
	  		Note_view_pager.sendButton.setVisibility(View.VISIBLE);
	  		Note_view_pager.backButton.setVisibility(View.VISIBLE);
	  	    
	    	if(!Util.isEmptyString(Note_view_pager.mAudioTextView.getText().toString()) )
	    		Note_view_pager.mAudioTextView.setVisibility(View.VISIBLE);
	    	else
	    		Note_view_pager.mAudioTextView.setVisibility(View.GONE);
	  	}
	  	else if(Note_view_pager.isPictureMode() )
	  	{	
	  		Note_view_pager.editButton.setVisibility(View.GONE);
	  		Note_view_pager.sendButton.setVisibility(View.GONE);
	  		Note_view_pager.backButton.setVisibility(View.GONE);
	  		Note_view_pager.mAudioTextView.setVisibility(View.GONE);
	  	}	        
   	} //showImageControlButtons
   
   	public static void primaryVideoSeekBarProgressUpdater(int currentPos) 
   	{
	   	if( (UtilVideo.mVideoView == null) || (Note_view_pager_adapter.mController == null))
	   		return;
	   	
//	   	System.out.println("Note_view_pager_UI / _primaryVideoSeekBarProgressUpdater / currentPos = " + currentPos);

	    // show current play position
	   	if(videoView_currPosistion != null)
	   	{
	   		videoView_currPosistion.setText(Util.getTimeFormatString(currentPos));
	   		videoView_currPosistion.setVisibility(View.VISIBLE);
	   	}
	   	
//	   	int curHour = Math.round((float)(currentPos / 1000 / 60 / 60));
//	   	int curMin = Math.round((float)((currentPos - curHour * 60 * 60 * 1000) / 1000 / 60));
//	    int curSec = Math.round((float)((currentPos - curHour * 60 * 60 * 1000 - curMin * 60 * 1000)/ 1000));
//    	videoCurrPos.setText(String.format("%2d", curHour)+":" +
//   							 String.format("%02d", curMin)+":" +
//   							 String.format("%02d", curSec) );
	   	
		// show file length
		String curPicStr = Note_view_pager.getCurrentPictureString();
		if(!Util.isEmptyString(curPicStr))
		{
			// set file length
			if(videoView_fileLength != null)
			{
				videoView_fileLength.setText(Util.getTimeFormatString(videoFileLength_inMilliSeconds));
				videoView_fileLength.setVisibility(View.VISIBLE);
			}
		}
		
	   	// show seek bar progress
	   	if(videoView_seekBar != null)
	   	{
	   		videoView_seekBar.setVisibility(View.VISIBLE);
	   		videoView_seekBar.setMax(99);
	   		videoView_progress = (int)(((float)currentPos/videoFileLength_inMilliSeconds)*100);
	   		videoView_seekBar.setProgress(videoView_progress);
	   	}		
   }
   	
   public static void showPicViewUI_previous_next(boolean show,int position)	
   {
	   if(show)
	   {
		   picView_previous_button.setVisibility(View.VISIBLE);
		   picView_previous_button.setEnabled(position==0? false:true);
		   picView_previous_button.setAlpha(position==0? 0.1f:1f);

		   picView_next_button.setVisibility(View.VISIBLE);
		   picView_next_button.setAlpha(position == (Note_view_pager.mPagerAdapter.getCount()-1 )? 0.1f:1f);
		   picView_next_button.setEnabled(position == (Note_view_pager.mPagerAdapter.getCount()-1 )? false:true);
	   }
	   else
	   {
		   picView_previous_button.setVisibility(View.GONE);		        	
		   picView_next_button.setVisibility(View.GONE);
	   }
   }
}
