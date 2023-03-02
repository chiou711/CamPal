/*
 * Copyright (C) 2019 CW Chiu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cw.campal.note;

import com.cw.campal.R;
import com.cw.campal.db.DB_page;
import com.cw.campal.tabs.TabsHost;
import com.cw.campal.util.uil.UilCommon;
import com.cw.campal.util.image.TouchImageView;
import com.cw.campal.util.image.UtilImage;
import com.cw.campal.util.image.UtilImage_bitmapLoader;
import com.cw.campal.util.video.UtilVideo;
import com.cw.campal.util.video.VideoViewCustom;
import com.cw.campal.util.ColorSet;
import com.cw.campal.util.Util;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class Note_adapter extends FragmentStatePagerAdapter
{
	static int mLastPosition;
	private static LayoutInflater inflater;
	private AppCompatActivity act;
	private ViewPager pager;
	DB_page db_page;

    public Note_adapter(ViewPager viewPager, AppCompatActivity activity)
    {
    	super(activity.getSupportFragmentManager());
		pager = viewPager;
    	act = activity;
        inflater = act.getLayoutInflater();
        mLastPosition = -1;
	    db_page = new DB_page(act, TabsHost.getCurrentPageTableId());
        System.out.println("Note_adapter / constructor / mLastPosition = " + mLastPosition);
    }
    
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

    @SuppressLint("SetJavaScriptEnabled")
	@Override
	public Object instantiateItem(ViewGroup container, final int position) 
    {
    	System.out.println("Note_adapter / instantiateItem / position = " + position);
    	// Inflate the layout containing 
    	// 1. picture group: image,video, thumb nail, control buttons
    	// 2. text group: title, body, time 
    	View pagerView = inflater.inflate(R.layout.note_view_adapter, container, false);
    	int style = Note.getStyle();
        pagerView.setBackgroundColor(ColorSet.mBG_ColorArray[style]);

    	// Picture group
        ViewGroup pictureGroup = (ViewGroup) pagerView.findViewById(R.id.pictureContent);
        String tagPictureStr = "current"+ position +"pictureView";
        pictureGroup.setTag(tagPictureStr);
    	
        // image view
    	TouchImageView imageView = ((TouchImageView) pagerView.findViewById(R.id.image_view));
        String tagImageStr = "current"+ position +"imageView";
        imageView.setTag(tagImageStr);

		// video view
    	VideoViewCustom videoView = ((VideoViewCustom) pagerView.findViewById(R.id.video_view));
        String tagVideoStr = "current"+ position +"videoView";
        videoView.setTag(tagVideoStr);

		ProgressBar spinner = (ProgressBar) pagerView.findViewById(R.id.loading);

        // line view
        View line_view = pagerView.findViewById(R.id.line_view);

    	// text group
        ViewGroup textGroup = (ViewGroup) pagerView.findViewById(R.id.textGroup);

        // Set tag for text view
    	TextView textView = textGroup.findViewById(R.id.textBody);

    	// set accessibility
        textGroup.setContentDescription(act.getResources().getString(R.string.note_text));
		textView.getRootView().setContentDescription(act.getResources().getString(R.string.note_text));


        String strTitle = db_page.getNoteTitle(position,true);
        String strBody = db_page.getNoteBody(position,true);

        // View mode
    	// picture only
	  	if(Note.isPictureMode())
	  	{
			System.out.println("Note_adapter / _instantiateItem / isPictureMode ");
	  		pictureGroup.setVisibility(View.VISIBLE);
	  	    showPictureView(position,imageView,videoView,spinner);

	  	    line_view.setVisibility(View.GONE);
	  	    textGroup.setVisibility(View.GONE);
	  	}
	    // text only
	  	else if(Note.isTextMode())
	  	{
			System.out.println("Note_adapter / _instantiateItem / isTextMode ");
	  		pictureGroup.setVisibility(View.GONE);

	  		line_view.setVisibility(View.VISIBLE);
	  		textGroup.setVisibility(View.VISIBLE);

	  	    if(!Util.isEmptyString(strTitle)||
	 	  	   !Util.isEmptyString(strBody)    )
	  	    {
	  	    	showTextView(position,textView);
	  	    }
	  	}
  		// picture and text
	  	else if(Note.isViewAllMode())
	  	{
			System.out.println("Note_adapter / _instantiateItem / isViewAllMode ");

			// picture
			pictureGroup.setVisibility(View.VISIBLE);
	  	    showPictureView(position,imageView,videoView,spinner);

	  	    line_view.setVisibility(View.VISIBLE);
	  	    textGroup.setVisibility(View.VISIBLE);

			// text
	  	    if( !Util.isEmptyString(strTitle)||
	  	       	!Util.isEmptyString(strBody)  )
	  	    {
	  	    	showTextView(position,textView);
	  	    }
	  	    else
			{
				textGroup.setVisibility(View.GONE);
			}
	  	}

		// footer of note view
		TextView footerText = (TextView) pagerView.findViewById(R.id.note_view_footer);
		if(!Note.isPictureMode())
		{
			footerText.setVisibility(View.VISIBLE);
			footerText.setText(String.valueOf(position+1)+"/"+ pager.getAdapter().getCount());
            footerText.setTextColor(ColorSet.mText_ColorArray[Note.mStyle]);
            footerText.setBackgroundColor(ColorSet.mBG_ColorArray[Note.mStyle]);
		}
		else
			footerText.setVisibility(View.GONE);

    	container.addView(pagerView, 0);

		return pagerView;
    } //instantiateItem
	
    // show text view
    private void showTextView(int position, TextView textView){
    	System.out.println("Note_adapter/ _showTextView / position = " + position);
	    String strBody = db_page.getNoteBody(position,true);
	    Long createTime = db_page.getNoteCreatedTime(position,true);
	    String textStr = strBody+"\n\n"+Util.getTimeString(createTime);
	    textView.setText(textStr);
    }
    
    // show picture view
    private void showPictureView(int position,
    		             TouchImageView imageView,
    		             VideoView videoView,
    		             ProgressBar spinner          )
    {
		String pictureUri = db_page.getNotePictureUri(position,true);
		String drawingUri = db_page.getNoteDrawingUri(position,true);

    	// Check if Uri is for drawing
		if(UtilImage.hasImageExtension(drawingUri, act))
			pictureUri = drawingUri;

        // show image view
  		if( UtilImage.hasImageExtension(pictureUri, act)||
  		    (Util.isEmptyString(pictureUri)   )             ) // for wrong path icon
  		{
			System.out.println("Note_adapter / _showPictureView / show image view");
  			videoView.setVisibility(View.GONE);
  			UtilVideo.mVideoView = null;
  			imageView.setVisibility(View.VISIBLE);
  			showImageByTouchImageView(spinner, imageView, pictureUri,position);
  		}
  		// show video view
  		else if(UtilVideo.hasVideoExtension(pictureUri, act)){
			System.out.println("Note_adapter / _showPictureView / show video view");
  			imageView.setVisibility(View.GONE);
  			videoView.setVisibility(View.VISIBLE);
  		}
    }

	@Override
	public Fragment getItem(int position) {
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
		if(db_page != null)
			return db_page.getNotesCount(true);
		else
			return 0;
    }

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}
	
	static Intent mIntentView;
	static NoteUi picUI_primary;

	@Override
	public void setPrimaryItem(final ViewGroup container, int position, Object object) 
	{
		// set primary item only
	    if(mLastPosition != position)
		{
			System.out.println("Note_adapter / _setPrimaryItem / mLastPosition = " + mLastPosition);
            System.out.println("Note_adapter / _setPrimaryItem / position = " + position);

			String lastPictureStr = null;

			if(mLastPosition != -1)
				lastPictureStr = db_page.getNotePictureUri(mLastPosition,true);

			String pictureStr = db_page.getNotePictureUri(position,true);
			String drawingUri = db_page.getNoteDrawingUri(position,true);;

			// check drawing URI
			if(!Util.isEmptyString(drawingUri))
				pictureStr = drawingUri;

			// for video view
			if (!Note.isTextMode() )
			{

				// stop last video view running
				if (mLastPosition != -1)
				{
					String tagVideoStr = "current" + mLastPosition + "videoView";
					VideoViewCustom lastVideoView = (VideoViewCustom) pager.findViewWithTag(tagVideoStr);
					lastVideoView.stopPlayback();
				}

                // Show picture view UI
				if (Note.isViewAllMode() || Note.isPictureMode() )
                {
					NoteUi.cancel_UI_callbacks();
					picUI_primary = new NoteUi(act, pager, position);
					picUI_primary.tempShow_picViewUI(5002, pictureStr);
                }

				// Set video view
				if ( UtilVideo.hasVideoExtension(pictureStr, act) &&
					 !UtilImage.hasImageExtension(pictureStr, act)   )
				{
					// update current pager view
					UtilVideo.mCurrentPagerView = (View) object;

					// for view mode change
					if (Note.mIsViewModeChanged && (Note.mPlayVideoPositionOfInstance == 0) )
					{
						UtilVideo.mPlayVideoPosition = Note.mPositionOfChangeView;
						UtilVideo.setVideoViewLayout(pictureStr);

						if (UtilVideo.mPlayVideoPosition > 0)
							UtilVideo.playOrPauseVideo(pager,pictureStr);
					}
					else
					{
						// for key protect
						if (Note.mPlayVideoPositionOfInstance > 0)
						{
							UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PAUSE);
							UtilVideo.setVideoViewLayout(pictureStr);

							if (!UtilVideo.hasMediaControlWidget) {
								NoteUi.updateVideoPlayButtonState(pager, NoteUi.getFocus_notePos());
								picUI_primary.tempShow_picViewUI(5003,pictureStr);
                            }

							UtilVideo.playOrPauseVideo(pager,pictureStr);
						}
						else
						{
							if (UtilVideo.hasMediaControlWidget)
								UtilVideo.setVideoState(UtilVideo.VIDEO_AT_PLAY);
							else
								UtilVideo.setVideoState(UtilVideo.VIDEO_AT_STOP);

							UtilVideo.mPlayVideoPosition = 0; // make sure play video position is 0 after page is changed
							UtilVideo.initVideoView(pager,pictureStr, act, position);
						}
					}

					UtilVideo.currentPicturePath = pictureStr;
				}
			}

		}
	    mLastPosition = position;
	    
	} //setPrimaryItem		

    // show image by touch image view
    private void showImageByTouchImageView(final ProgressBar spinner, final TouchImageView pictureView, String strPicture,final Integer position)
    {
        if(Util.isEmptyString(strPicture))
        {
            pictureView.setImageResource(Note.mStyle%2 == 1 ?
                    R.drawable.btn_radio_off_holo_light:
                    R.drawable.btn_radio_off_holo_dark);//R.drawable.ic_empty);
        }
        else if(!Util.isUriExisted(strPicture, act))
        {
            pictureView.setImageResource(R.drawable.ic_not_found);
        }
        else
        {
			// load bitmap to image view
			try
			{
				new UtilImage_bitmapLoader(pictureView,
						strPicture,
						spinner,
						UilCommon.optionsForFadeIn,
						act);
			}
			catch(Exception e)
			{
				Log.e("Note_adapter", "UtilImage_bitmapLoader error");
			}
        }
    }
}
