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

package com.cw.campal.note_add;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cw.campal.R;
import com.cw.campal.note_add.image.Note_addCameraImage;
import com.cw.campal.note_add.image.Note_addReadyImage;
import com.cw.campal.note_add.pictureUri.Note_addPictureUri;
import com.cw.campal.note_add.video.Note_addCameraVideo;
import com.cw.campal.note_add.video.Note_addReadyVideo;
import com.cw.campal.util.Util;
import com.cw.campal.note_add.drawing.Note_drawingAct;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by cw on 2017/10/7.
 */
public class Add_note_option {
    int option_id;
    int option_drawable_id;
    int option_string_id;

    Add_note_option(int id, int draw_id, int string_id)
    {
        this.option_id = id;
        this.option_drawable_id = draw_id;
        this.option_string_id = string_id;
    }

    /**
     *
     * 	Add new note
     *
     */
    static List<Add_note_option> addNoteList;

    private final static int ID_NEW_CAMERA_IMAGE = 1;
    private final static int ID_NEW_READY_IMAGE = 2;
    private final static int ID_NEW_CAMERA_VIDEO = 3;
    private final static int ID_NEW_READY_VIDEO = 4;
    private final static int ID_NEW_DRAWING = 5;
    private final static int ID_NEW_PICTURE_URI = 6;
    private final static int ID_NEW_SETTING = 7;
    private final static int ID_NEW_BACK = 8;

    public static void createSelection(AppCompatActivity act, boolean permitted)
    {

        System.out.println("Add_note_option / _createSelection");
        AbsListView gridView;

        // get layout inflater
        View rootView = act.getLayoutInflater().inflate(R.layout.option_grid, null);

        // check camera feature
        PackageManager packageManager = act.getPackageManager();

        addNoteList = new ArrayList<>();

        if(permitted) {
            // camera image
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                addNoteList.add(new Add_note_option(ID_NEW_CAMERA_IMAGE,
                        android.R.drawable.ic_menu_camera,
                        R.string.note_camera_image));
            }

            // ready image
            addNoteList.add(new Add_note_option(ID_NEW_READY_IMAGE,
                    android.R.drawable.ic_menu_gallery,
                    R.string.note_ready_image));

            // camera video
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                addNoteList.add(new Add_note_option(ID_NEW_CAMERA_VIDEO,
                        android.R.drawable.presence_video_online,
                        R.string.note_camera_video));
            }

            // ready video
            addNoteList.add(new Add_note_option(ID_NEW_READY_VIDEO,
                    R.drawable.ic_ready_video,
                    R.string.note_ready_video));

            // drawing
            addNoteList.add(new Add_note_option(ID_NEW_DRAWING,
                    R.drawable.ic_menu_draw,
                    R.string.note_drawing));

            // picture Uri
            addNoteList.add(new Add_note_option(ID_NEW_PICTURE_URI,
                    android.R.drawable.ic_menu_edit,
                    R.string.edit_note_dlg_picture_uri));
        }

        // Setting
        addNoteList.add(new Add_note_option(ID_NEW_SETTING,
                android.R.drawable.ic_menu_preferences,
                R.string.settings));

        // Back
        addNoteList.add(new Add_note_option(ID_NEW_BACK,
                R.drawable.ic_menu_back,
                R.string.btn_Cancel));


        gridView = (GridView) rootView.findViewById(R.id.option_grid_view);

        // check if directory is created AND not empty
        if( (addNoteList != null  ) && (addNoteList.size() > 0))
        {
            GridIconAdapter mGridIconAdapter = new GridIconAdapter(act);
            gridView.setAdapter(mGridIconAdapter);
        }
        else
        {
            Toast.makeText(act,R.string.gallery_toast_no_file, Toast.LENGTH_SHORT).show();
            act.finish();
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("MainUi / _addNewNote / _OnItemClickListener / position = " + position +" id = " + id);
                startAddNoteOption(act, addNoteList.get(position).option_id);
            }
        });

        // set view to dialog
        AlertDialog.Builder builder1 = new AlertDialog.Builder(act);
        builder1.setView(rootView);
        dlgAddNew = builder1.create();
        dlgAddNew.show();
    }

    private static AlertDialog dlgAddNew;

    private static void startAddNoteOption(AppCompatActivity act, int option)
    {
        System.out.println("MainUi / _startAddNoteOption / option = " + option);

        SharedPreferences mPref_add_new_note_location = act.getSharedPreferences("add_new_note_option", 0);
        boolean bTop = mPref_add_new_note_location.getString("KEY_ADD_NEW_NOTE_TO","bottom").equalsIgnoreCase("top");
        boolean bDirectory = mPref_add_new_note_location.getString("KEY_ADD_DIRECTORY","no").equalsIgnoreCase("yes");

        switch (option) {
            case ID_NEW_CAMERA_IMAGE:
            {
                Intent intent = new Intent(act, Note_addCameraImage.class);
                if(bTop)
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "true");
                else
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "false");

                act.startActivity(intent);
            }
            break;

            case ID_NEW_READY_IMAGE:
            {
                Intent intent = new Intent(act, Note_addReadyImage.class);
                if( bTop && !bDirectory )
                    intent.putExtra("EXTRA_ADD_EXIST", "single_to_top");
                else if(!bTop && !bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "single_to_bottom");
                else if(bTop && bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "directory_to_top");
                else if(!bTop && bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "directory_to_bottom");

                act.startActivity(intent);
            }
            break;

            case ID_NEW_CAMERA_VIDEO:
            {
                Intent intent = new Intent(act, Note_addCameraVideo.class);
                if(bTop)
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "true");
                else
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "false");

                act.startActivity(intent);
            }
            break;

            case ID_NEW_READY_VIDEO:
            {
                Intent intent = new Intent(act, Note_addReadyVideo.class);
                if( bTop && !bDirectory )
                    intent.putExtra("EXTRA_ADD_EXIST", "single_to_top");
                else if(!bTop && !bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "single_to_bottom");
                else if(bTop && bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "directory_to_top");
                else if(!bTop && bDirectory)
                    intent.putExtra("EXTRA_ADD_EXIST", "directory_to_bottom");

                act.startActivity(intent);
            }
            break;

            case ID_NEW_DRAWING:
            {
                Intent intent = new Intent(act, Note_drawingAct.class);
                intent.putExtra("drawing_mode",Util.DRAWING_ADD);

                if(bTop)
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "true");
                else
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "false");

                act.startActivity(intent);
            }
            break;

            // picture Uri
            case ID_NEW_PICTURE_URI:
            {
                Intent intent = new Intent(act, Note_addPictureUri.class);
                if(bTop)
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "true");
                else
                    intent.putExtra("extra_ADD_NEW_TO_TOP", "false");

                act.startActivity(intent);
            }
            break;

            case ID_NEW_BACK:
            {
                dlgAddNew.dismiss();
            }
            break;

            case ID_NEW_SETTING:
            {
                new Note_addNew_option(act);
            }
            break;

            // default
            default:
                break;
        }

    }


    /**
     * Grid Icon Adapter for showing optional items
     */
    static class GridIconAdapter extends BaseAdapter {
        private AppCompatActivity act;
        GridIconAdapter(AppCompatActivity fragAct){act = fragAct;}

        @Override
        public int getCount() {
            return addNoteList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View view = convertView;
            if (view == null) {
                view = act.getLayoutInflater().inflate(R.layout.add_note_grid_item, parent, false);
                holder = new ViewHolder();
                assert view != null;
                holder.imageView = (ImageView) view.findViewById(R.id.grid_item_image);
                holder.text = (TextView) view.findViewById(R.id.grid_item_text);
                view.setTag(holder);

                // set grid item background color
                // text , drawing
                if( (position == 0) || (position == 1) )
                    view.setBackgroundColor( act.getResources().getColor(R.color.textGrid));
                // picture, ready picture
                else if ( (position == 2) || (position == 3) )
                    view.setBackgroundColor( act.getResources().getColor(R.color.pictureGrid));
                // video , ready video
                else if((position == 4) || (position == 5))
                    view.setBackgroundColor( act.getResources().getColor(R.color.videoGrid));
                // others
                else if((position == 6) || (position == 7))
                    view.setBackgroundColor( act.getResources().getColor(R.color.otherGrid));

            } else {
                holder = (ViewHolder) view.getTag();
            }

            Drawable drawable = act.getResources().getDrawable(addNoteList.get(position).option_drawable_id);
            holder.imageView.setImageDrawable(drawable);
            holder.text.setText(addNoteList.get(position).option_string_id);
            return view;
        }

        private class ViewHolder {
            ImageView imageView;
            TextView text;
        }
    }
}
