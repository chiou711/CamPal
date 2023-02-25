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

package com.cw.campal.note_edit;

import com.cw.campal.R;
import com.cw.campal.db.DB_page;
import com.cw.campal.tabs.TabsHost;
import com.cw.campal.util.image.TouchImageView;
import com.cw.campal.util.image.UtilImage;
import com.cw.campal.util.ColorSet;
import com.cw.campal.util.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Note_edit extends Activity 
{

    private Long noteId, createdTime;
    private String title, picUriStr, drawingUri, cameraPictureUri, body;
    Note_edit_ui note_edit_ui;
    private boolean enSaveDb = true;
    boolean bUseCameraImage;
    DB_page dB;
    TouchImageView enlargedImage;
    int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
        // check note count first
	    dB = new DB_page(this, TabsHost.getCurrentPageTableId());

        if(dB.getNotesCount(true) ==  0)
        {
        	finish(); // add for last note being deleted
        	return;
        }
        
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note_title);// set title
    	
        System.out.println("Note_edit / onCreate");
        
		enlargedImage = (TouchImageView)findViewById(R.id.expanded_image);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(new ColorDrawable(ColorSet.getBarColor(this)));

    	Bundle extras = getIntent().getExtras();
    	position = extras.getInt("list_view_position");
    	noteId = extras.getLong(DB_page.KEY_NOTE_ID);
		picUriStr = extras.getString(DB_page.KEY_NOTE_PICTURE_URI);
		drawingUri = extras.getString(DB_page.KEY_NOTE_DRAWING_URI);
    	title = extras.getString(DB_page.KEY_NOTE_TITLE);
    	body = extras.getString(DB_page.KEY_NOTE_BODY);
    	createdTime = extras.getLong(DB_page.KEY_NOTE_CREATED);
        

        //initialization
        note_edit_ui = new Note_edit_ui(this, dB, noteId, title, picUriStr, drawingUri, body, createdTime);
        note_edit_ui.UI_init();
        cameraPictureUri = "";
        bUseCameraImage = false;

        if(savedInstanceState != null)
        {
	        System.out.println("Note_edit / onCreate / noteId =  " + noteId);
	        if(noteId != null)
	        {
	        	picUriStr = dB.getNotePictureUri_byId(noteId);
				note_edit_ui.currPictureUri = picUriStr;
				drawingUri = dB.getNoteDrawingUri_byId(noteId);
	        }
        }
        
    	// show view
		note_edit_ui.populateFields_all(noteId);
		
		// OK button: edit OK, save
        Button okButton = (Button) findViewById(R.id.note_edit_ok);
//        okButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_save, 0, 0, 0);
		// OK
        okButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
				if(note_edit_ui.bRemovePictureUri)
				{
					picUriStr = "";
				}
				System.out.println("Note_edit / onClick (okButton) / noteId = " + noteId);
                enSaveDb = true;
                finish();
            }

        });
        
        // delete button: delete note
        Button delButton = (Button) findViewById(R.id.note_edit_delete);
//        delButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_delete, 0, 0, 0);
        // delete
        delButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view)
			{
				Util util = new Util(Note_edit.this);
				util.vibrate();

				Builder builder1 = new Builder(Note_edit.this );
				builder1.setTitle(R.string.confirm_dialog_title)
					.setMessage(R.string.confirm_dialog_message)
					.setNegativeButton(R.string.confirm_dialog_button_no, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{/*nothing to do*/}
						})
					.setPositiveButton(R.string.confirm_dialog_button_yes, new OnClickListener()
						{   @Override
							public void onClick(DialogInterface dialog1, int which1)
							{
								note_edit_ui.deleteNote(noteId);

								finish();
							}
						})
					.show();//warning:end
            }
        });
        
        // cancel button: leave, do not save current modification
        Button cancelButton = (Button) findViewById(R.id.note_edit_cancel);
//        cancelButton.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.ic_menu_close_clear_cancel, 0, 0, 0);
        cancelButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                
                // check if note content is modified
               	if(note_edit_ui.isNoteModified())
            	{
               		// show confirmation dialog
            		confirmToUpdateDlg();
            	}
            	else
            	{
            		enSaveDb = false;
                    finish();
            	}
            }
        });
    }
    
    // confirm to update change or not
    void confirmToUpdateDlg()
    {
		AlertDialog.Builder builder = new AlertDialog.Builder(Note_edit.this);
		builder.setTitle(R.string.confirm_dialog_title)
	           .setMessage(R.string.edit_note_confirm_update)
	           // Yes, to update
			   .setPositiveButton(R.string.confirm_dialog_button_yes, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						if(note_edit_ui.bRemovePictureUri)
						{
							picUriStr = "";
						}
					    enSaveDb = true;
					    finish();
					}})
			   // cancel
			   .setNeutralButton(R.string.btn_Cancel, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{   // do nothing
					}})
			   // no, roll back to original status		
			   .setNegativeButton(R.string.confirm_dialog_button_no, new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface dialog, int which) 
					{
						Bundle extras = getIntent().getExtras();
						String originalPictureFileName = extras.getString(DB_page.KEY_NOTE_PICTURE_URI);

						if(Util.isEmptyString(originalPictureFileName))
						{   // no picture at first
							note_edit_ui.removePictureStringFromOriginalNote(noteId);
		                    enSaveDb = false;
						}
						else
						{	// roll back existing picture
                            note_edit_ui.bRollBackData = true;
							picUriStr = originalPictureFileName;
							enSaveDb = true;
						}	
						

						// roll back existing picture
                        note_edit_ui.bRollBackData = true;
						enSaveDb = true;

	                    finish();
					}})
			   .show();
    }
    

    // for finish(), for Rotate screen
    @Override
    protected void onPause() {
        super.onPause();
        
        System.out.println("Note_edit / onPause / enSaveDb = " + enSaveDb);
        System.out.println("Note_edit / onPause / picUriStr = " + picUriStr);
        noteId = note_edit_ui.saveStateInDB(noteId, enSaveDb, picUriStr, drawingUri);
    }

    // for Rotate screen
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        System.out.println("Note_edit / onSaveInstanceState / enSaveDb = " + enSaveDb);
        System.out.println("Note_edit / onSaveInstanceState / bUseCameraImage = " + bUseCameraImage);
        System.out.println("Note_edit / onSaveInstanceState / cameraPictureUri = " + cameraPictureUri);
        
        if(note_edit_ui.bRemovePictureUri)
    	    outState.putBoolean("removeOriginalPictureUri",true);

        if(bUseCameraImage)
        {
        	outState.putBoolean("UseCameraImage",true);
        	outState.putString("showCameraImageUri", picUriStr);
        }
        else
        {
        	outState.putBoolean("UseCameraImage",false);
        	outState.putString("showCameraImageUri", "");
        }
        
        noteId = note_edit_ui.saveStateInDB(noteId, enSaveDb, picUriStr, drawingUri);
        outState.putSerializable(DB_page.KEY_NOTE_ID, noteId);
        
    }
    
    // for After Rotate
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
    	super.onRestoreInstanceState(savedInstanceState);

    	bUseCameraImage = savedInstanceState.getBoolean("UseCameraImage");
    	
    	cameraPictureUri = savedInstanceState.getString("showCameraImageUri");
    	
    	System.out.println("Note_edit / onRestoreInstanceState / savedInstanceState.getBoolean removeOriginalPictureUri =" +
    							savedInstanceState.getBoolean("removeOriginalPictureUri"));
        if(savedInstanceState.getBoolean("removeOriginalPictureUri"))
        {
        	cameraPictureUri = "";
			note_edit_ui.oriPictureUri ="";
			note_edit_ui.currPictureUri ="";
        	note_edit_ui.removePictureStringFromOriginalNote(noteId);
			note_edit_ui.populateFields_all(noteId);
			note_edit_ui.bRemovePictureUri = true;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    
    @Override
    public void onBackPressed() {
	    if(note_edit_ui.bShowEnlargedImage)
	    {
            note_edit_ui.closeEnlargedImage();
	    }
	    else
	    {
	    	if(note_edit_ui.isNoteModified())
	    	{
	    		confirmToUpdateDlg();
	    	}
	    	else
	    	{
	            enSaveDb = false;
	            finish();
	    	}
	    }
    }
    
    static final int CAPTURE_IMAGE = R.id.ADD_NEW_IMAGE;
    static final int CAPTURE_VIDEO = R.id.ADD_NEW_VIDEO;
	private Uri picUri;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// inflate menu
		getMenuInflater().inflate(R.menu.edit_note_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
    
    @Override 
    public boolean onOptionsItemSelected(MenuItem item) 
    {
    	switch (item.getItemId()) 
        {
		    case android.R.id.home:
		    	if(note_edit_ui.isNoteModified())
		    	{
		    		confirmToUpdateDlg();
		    	}
		    	else
		    	{
		            enSaveDb = false;
		            finish();
		    	}
		        return true;

            case CAPTURE_IMAGE:
            	Intent intentImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            	// new picture Uri with current time stamp
            	picUri = UtilImage.getPictureUri("IMG_" + Util.getCurrentTimeString() + ".jpg",
						   						   Note_edit.this); 
            	picUriStr = picUri.toString();
			    intentImage.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
			    startActivityForResult(intentImage, Util.ACTIVITY_TAKE_PICTURE); 
			    enSaveDb = true;
				note_edit_ui.bRemovePictureUri = false; // reset
			    
			    if(UtilImage.mExpandedImageView != null)
			    	UtilImage.closeExpandedImage();
		        
			    return true;
            
            case CAPTURE_VIDEO:
            	Intent intentVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            	// new picture Uri with current time stamp
            	picUri = UtilImage.getPictureUri("VID_" + Util.getCurrentTimeString() + ".mp4",
						   						   Note_edit.this); 
            	picUriStr = picUri.toString();
			    intentVideo.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
			    startActivityForResult(intentVideo, Util.ACTIVITY_TAKE_PICTURE); 
			    enSaveDb = true;
				note_edit_ui.bRemovePictureUri = false; // reset
			    
			    return true;			    
			    
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent)
	{
		// take picture
		if (requestCode == Util.ACTIVITY_TAKE_PICTURE)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				picUri = Uri.parse(note_edit_ui.currPictureUri);
//				String str = getResources().getText(R.string.note_take_picture_OK ).toString();
//	            Toast.makeText(Note_edit.this, str + " " + imageUri.toString(), Toast.LENGTH_SHORT).show();
				note_edit_ui.populateFields_all(noteId);
	            bUseCameraImage = true;
	            cameraPictureUri = note_edit_ui.currPictureUri;
			} 
			else if (resultCode == RESULT_CANCELED)
			{
				bUseCameraImage = false;
				// to use captured picture or original picture
				if(!Util.isEmptyString(cameraPictureUri))
				{
					// update
					note_edit_ui.saveStateInDB(noteId, enSaveDb, cameraPictureUri, "");// replace with existing picture
					note_edit_ui.populateFields_all(noteId);
		            
					// set for Rotate any times
		            bUseCameraImage = true;
		            picUriStr = note_edit_ui.currPictureUri; // for pause
		            cameraPictureUri = note_edit_ui.currPictureUri; // for save instance

				}
				else
				{
					// skip new Uri, roll back to original one
					note_edit_ui.currPictureUri = note_edit_ui.oriPictureUri;
			    	picUriStr = note_edit_ui.oriPictureUri;
					Toast.makeText(Note_edit.this, R.string.note_cancel_add_new, Toast.LENGTH_LONG).show();
				}
				
				enSaveDb = true;
				note_edit_ui.saveStateInDB(noteId, enSaveDb, picUriStr, drawingUri);
				note_edit_ui.populateFields_all(noteId);
			}
		}
		
		// choose picture
        if(requestCode == Util.CHOOSER_SET_PICTURE && resultCode == Activity.RESULT_OK)
        {
			String pictureUri = Util.getPicturePathOnActivityResult(this,returnedIntent);
        	System.out.println("Note_edit / _onActivityResult / picUri = " + pictureUri);
        	
        	noteId = note_edit_ui.saveStateInDB(noteId,true,pictureUri, drawingUri);

			note_edit_ui.populateFields_all(noteId);
			
            // set for Rotate any times
            bUseCameraImage = true;
            picUriStr = note_edit_ui.currPictureUri; // for pause
            cameraPictureUri = note_edit_ui.currPictureUri; // for save instance
        }  
        
		// edit drawing
		if(requestCode == Util.DRAWING_EDIT)
		{
			this.recreate();
		}
	}
	
}
