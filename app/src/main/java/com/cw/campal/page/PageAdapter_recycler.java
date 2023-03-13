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

package com.cw.campal.page;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cw.campal.R;
import com.cw.campal.db.DB_folder;
import com.cw.campal.db.DB_page;
import com.cw.campal.main.MainAct;
import com.cw.campal.note.Note;
import com.cw.campal.note_edit.Note_edit;
import com.cw.campal.page.item_touch_helper.ItemTouchHelperAdapter;
import com.cw.campal.page.item_touch_helper.ItemTouchHelperViewHolder;
import com.cw.campal.page.item_touch_helper.OnStartDragListener;
import com.cw.campal.tabs.TabsHost;
import com.cw.campal.util.ColorSet;
import com.cw.campal.util.Util;
import com.cw.campal.util.image.UtilImage;
import com.cw.campal.util.image.UtilImage_bitmapLoader;
import com.cw.campal.util.preferences.Pref;
import com.cw.campal.util.uil.UilCommon;
import com.cw.campal.util.video.UtilVideo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static com.cw.campal.db.DB_page.KEY_NOTE_CREATED;
import static com.cw.campal.db.DB_page.KEY_NOTE_MARKING;
import static com.cw.campal.db.DB_page.KEY_NOTE_PICTURE_URI;
import static com.cw.campal.db.DB_page.KEY_NOTE_TITLE;
import static com.cw.campal.page.Page_recycler.swapRows;

// Pager adapter
public class PageAdapter_recycler extends RecyclerView.Adapter<PageAdapter_recycler.ViewHolder>
        implements ItemTouchHelperAdapter
{
	private AppCompatActivity mAct;
	Cursor cursor;
	private static int style;
    private DB_folder dbFolder;
	private DB_page mDb_page;
	private int page_pos;
    private final OnStartDragListener mDragStartListener;
	private int page_table_id;

    PageAdapter_recycler(int pagePos,  int pageTableId, OnStartDragListener dragStartListener) {
	    mAct = MainAct.mAct;
	    mDragStartListener = dragStartListener;

        dbFolder = new DB_folder(mAct,Pref.getPref_focusView_folder_tableId(mAct));
	    page_pos = pagePos;
	    page_table_id = pageTableId;
    }

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        ImageView btnMarking;
        ImageView btnViewNote;
        ImageView btnEditNote;
		TextView rowId;
		TextView textTitle;
		TextView textBody;
		TextView textTime;
        ImageViewCustom btnDrag;
		View thumbBlock;
		ImageView thumbPicture;
		ProgressBar progressBar;

        public ViewHolder(View v) {
            super(v);

            // Define click listener for the ViewHolder's View.
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            textTitle = (TextView) v.findViewById(R.id.row_title);
            rowId= (TextView) v.findViewById(R.id.row_id);
            btnMarking = (ImageView) v.findViewById(R.id.btn_marking);
            btnViewNote = (ImageView) v.findViewById(R.id.btn_view_note);
            btnEditNote = (ImageView) v.findViewById(R.id.btn_edit_note);
            thumbBlock = v.findViewById(R.id.row_thumb_nail);
            thumbPicture = (ImageView) v.findViewById(R.id.thumb_picture);
            btnDrag = (ImageViewCustom) v.findViewById(R.id.btn_drag);
            progressBar = (ProgressBar) v.findViewById(R.id.thumb_progress);
            textTitle = (TextView) v.findViewById(R.id.row_title);
            textBody = (TextView) v.findViewById(R.id.row_body);
            textTime = (TextView) v.findViewById(R.id.row_time);
        }

        public TextView getTextView() {
            return textTitle;
        }

        @Override
        public void onItemSelected() {
//            itemView.setBackgroundColor(Color.LTGRAY);
            ((CardView)itemView).setCardBackgroundColor(MainAct.mAct.getResources().getColor(R.color.button_color));
        }

        @Override
        public void onItemClear() {
            ((CardView)itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view.
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.page_view_card, viewGroup, false);

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @SuppressLint("Range")
    @Override
    public void onBindViewHolder(ViewHolder holder, final int _position) {

    	int position = holder.getAdapterPosition();
//        System.out.println("PageAdapter_recycler / _onBindViewHolder / position = " + position);

        // style
	    style = dbFolder.getPageStyle(page_pos, true);

        ((CardView)holder.itemView).setCardBackgroundColor(ColorSet.mBG_ColorArray[style]);

        // get DB data
        String strTitle = null;
        String strBody = null;
        String pictureUri = null;
        Long timeCreated = null;
        int marking = 0;

		SharedPreferences pref_show_note_attribute = MainAct.mAct.getSharedPreferences("show_note_attribute", 0);

	    mDb_page = new DB_page(mAct, page_table_id);
	    mDb_page.open();
	    cursor = mDb_page.mCursor_note;
        if(cursor.moveToPosition(position)) {
            strTitle = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_TITLE));
            pictureUri = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE_PICTURE_URI));
            marking = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_NOTE_MARKING));
            timeCreated = cursor.getLong(cursor.getColumnIndex(KEY_NOTE_CREATED));
        }
	    mDb_page.close();

        /**
         *  control block
         */
        // show row Id
        holder.rowId.setText(String.valueOf(position+1));
        holder.rowId.setTextColor(ColorSet.mText_ColorArray[style]);

        // show marking check box
        if(marking == 1){
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_on_holo_light :
                    R.drawable.btn_check_on_holo_dark);
        }else{
            holder.btnMarking.setBackgroundResource(style % 2 == 1 ?
                    R.drawable.btn_check_off_holo_light :
                    R.drawable.btn_check_off_holo_dark);
        }

        // show drag button
        if(pref_show_note_attribute.getString("KEY_ENABLE_DRAGGABLE", "yes").equalsIgnoreCase("yes"))
            holder.btnDrag.setVisibility(View.VISIBLE);
        else
            holder.btnDrag.setVisibility(View.GONE);

		// show text title
		if( Util.isEmptyString(strTitle) ){
			// make sure empty title is empty after scrolling
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText("");
		}else{
			holder.textTitle.setVisibility(View.VISIBLE);
			holder.textTitle.setText(strTitle);
			holder.textTitle.setTextColor(ColorSet.mText_ColorArray[style]);
		}

		// case 1: show thumb nail if picture Uri exists
		if(UtilImage.hasImageExtension(pictureUri, mAct ) ||
		   pictureUri.contains("drive.google")||
		   UtilVideo.hasVideoExtension(pictureUri, mAct )   ){
			holder.thumbBlock.setVisibility(View.VISIBLE);
			holder.thumbPicture.setVisibility(View.VISIBLE);
			// load bitmap to image view
			try{
				new UtilImage_bitmapLoader(holder.thumbPicture,
										   pictureUri,
										   holder.progressBar,
                                           UilCommon.optionsForFadeIn,
										   mAct);
			}
			catch(Exception e){
				Log.e("PageAdapter_recycler", "UtilImage_bitmapLoader error");
				holder.thumbBlock.setVisibility(View.GONE);
				holder.thumbPicture.setVisibility(View.GONE);
			}
		}else{
			holder.thumbBlock.setVisibility(View.GONE);
			holder.thumbPicture.setVisibility(View.GONE);
		}

		// Show text body
	  	if(pref_show_note_attribute.getString("KEY_SHOW_BODY", "yes").equalsIgnoreCase("yes"))
	  	{
	  		// test only: enabled for showing picture path
//            String strBody = cursor.getString(cursor.getColumnIndex(KEY_NOTE_BODY));
	  		if(!Util.isEmptyString(strBody)){
				//normal: do nothing
			}else if(!Util.isEmptyString(pictureUri)) {
//				strBody = pictureUri;//show picture Uri
			}

			holder.textBody.setText(strBody);
//			holder.textBody.setTextSize(12);

//			holder.rowDivider.setVisibility(View.VISIBLE);
			holder.textBody.setTextColor(ColorSet.mText_ColorArray[style]);
			// time stamp
            holder.textTime.setText(Util.getTimeString(timeCreated));
			holder.textTime.setTextColor(ColorSet.mText_ColorArray[style]);
	  	}else{
            holder.textBody.setVisibility(View.GONE);
            holder.textTime.setVisibility(View.GONE);
	  	}

        setBindViewHolder_listeners(holder,position);
    }


    /**
     * Set bind view holder listeners
     * @param viewHolder
     * @param position
     */
    void setBindViewHolder_listeners(ViewHolder viewHolder, final int position)
    {

//        System.out.println("PageAdapter_recycler / setBindViewHolder_listeners / position = " + position);

        /**
         *  control block
         */
        // on mark note
        viewHolder.btnMarking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println("PageAdapter / _getView / btnMarking / _onClick");
                // toggle marking
                toggleNoteMarking(mAct,position);

                //Toggle marking will resume page, so do Store v scroll
                RecyclerView listView = TabsHost.mTabsPagerAdapter.fragmentList.get(TabsHost.getFocus_tabPos()).recyclerView;
                TabsHost.store_listView_vScroll(listView);
                TabsHost.isDoingMarking = true;

                TabsHost.reloadCurrentPage();
                TabsHost.showFooter(MainAct.mAct);
            }
        });

        // on view note
        viewHolder.btnViewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TabsHost.getCurrentPage().mCurrPlayPosition = position;
                DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
                int count = db_page.getNotesCount(true);
                if(position < count){
                    // apply Note class
                    Intent intent;
                    intent = new Intent(mAct, Note.class);
                    intent.putExtra("POSITION", position);
                    mAct.startActivity(intent);
                }
            }
        });

        // on edit note
        viewHolder.btnEditNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DB_page db_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
                Long rowId = db_page.getNoteId(position,true);

                Intent i = new Intent(mAct, Note_edit.class);
                i.putExtra("list_view_position", position);
                i.putExtra(DB_page.KEY_NOTE_ID, rowId);
                i.putExtra(DB_page.KEY_NOTE_TITLE, db_page.getNoteTitle_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_PICTURE_URI , db_page.getNotePictureUri_byId(rowId));
                i.putExtra(DB_page.KEY_NOTE_CREATED, db_page.getNoteCreatedTime_byId(rowId));
                mAct.startActivity(i);
            }
        });

        // Start a drag whenever the handle view it touched
        viewHolder.btnDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                        mDragStartListener.onStartDrag(viewHolder);
                        System.out.println("PageAdapter_recycler / onTouch / ACTION_DOWN");
                        return true;
                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        return true;
                }
                return false;
            }


        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
	    mDb_page = new DB_page(mAct, page_table_id);
	    return  mDb_page.getNotesCount(true);
    }

    // toggle mark of note
    public static int toggleNoteMarking(AppCompatActivity mAct, int position)
    {
        int marking = 0;
		DB_page db_page = new DB_page(mAct,TabsHost.getCurrentPageTableId());
        db_page.open();
        int count = db_page.getNotesCount(false);
        if(position >= count) //end of list
        {
            db_page.close();
            return marking;
        }

        String strNote = db_page.getNoteTitle(position,false);
        String strPictureUri = db_page.getNotePictureUri(position,false);
        Long idNote =  db_page.getNoteId(position,false);
		Long time = db_page.getNoteCreatedTime(position,false);

        // toggle the marking
        if(db_page.getNoteMarking(position,false) == 0){
            db_page.updateNote(idNote, strNote, strPictureUri,1, time, false);
            marking = 1;
        }else{
            db_page.updateNote(idNote, strNote, strPictureUri, 0, time, false);
            marking = 0;
        }
        db_page.close();

        System.out.println("PageAdapter_recycler / _toggleNoteMarking / position = " + position + ", marking = " + db_page.getNoteMarking(position,true));
        return  marking;
    }

    @Override
    public void onItemDismiss(int position) {
        notifyItemRemoved(position);
    }

    @Override
    public boolean onItemMove(int fromPos, int toPos) {
//        System.out.println("PageAdapter_recycler / _onItemMove / fromPos = " +
//                        fromPos + ", toPos = " + toPos);

        notifyItemMoved(fromPos, toPos);

        int oriStartPos = fromPos;
        int oriEndPos = toPos;

        mDb_page = new DB_page(mAct, TabsHost.getCurrentPageTableId());
        if(fromPos >= mDb_page.getNotesCount(true)) // avoid footer error
            return false;

        //reorder data base storage
        int loop = Math.abs(fromPos-toPos);
        for(int i=0;i< loop;i++){
            swapRows(mDb_page, fromPos,toPos);
            if((fromPos-toPos) >0)
                toPos++;
            else
                toPos--;
        }

        // update footer
        TabsHost.showFooter(mAct);
        return true;
    }

    @Override
    public void onItemMoved(RecyclerView.ViewHolder sourceViewHolder, int fromPos, RecyclerView.ViewHolder targetViewHolder, int toPos) {
        System.out.println("PageAdapter_recycler / _onItemMoved");
        ((TextView)sourceViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(toPos+1));
        ((TextView)targetViewHolder.itemView.findViewById(R.id.row_id)).setText(String.valueOf(fromPos+1));

        setBindViewHolder_listeners((ViewHolder)sourceViewHolder,toPos);
        setBindViewHolder_listeners((ViewHolder)targetViewHolder,fromPos);
    }

}
