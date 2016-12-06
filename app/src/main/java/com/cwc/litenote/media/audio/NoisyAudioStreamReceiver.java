package com.cwc.litenote.media.audio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.cwc.litenote.NoteFragment;
import com.cwc.litenote.R;
import com.cwc.litenote.note.Note_view_pager;
import com.cwc.litenote.note.Note_view_pagerUI;

public class NoisyAudioStreamReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
			if((AudioPlayer.mMediaPlayer != null) &&
			    AudioPlayer.mMediaPlayer.isPlaying() )
			{
				System.out.println("NoisyAudioStreamReceiver / play -> pause");
				AudioPlayer.mMediaPlayer.pause();
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunOneTimeMode); 
				AudioPlayer.mAudioHandler.removeCallbacks(AudioPlayer.mRunContinueMode); 
				AudioPlayer.mPlayerState = AudioPlayer.PLAYER_AT_PAUSE;
				//update audio control state
				UtilAudio.updateFooterAudioState(NoteFragment.footerAudio_playOrPause_button,NoteFragment.footerAudioTextView);
				
	    		// update playing state in note view pager
				if( Note_view_pager.mPager != null)
				{
					if(Note_view_pagerUI.picView_audio_button != null) 
						Note_view_pagerUI.picView_audio_button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_ringer_on, 0, 0, 0);
					
					if(Note_view_pager.mMenuItemAudio.isVisible())
						Note_view_pager.mMenuItemAudio.setIcon(R.drawable.ic_lock_ringer_on);
				}
	    		
			}        	
        }
    }
}