package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.blinz117.songbuilder.songstructure.Note;
import com.blinz117.songbuilder.songstructure.Song;

import android.content.Context;
import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class SongViewFragment extends Fragment {

	Song currSong;
	LinearLayout songBlockContainer;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		currSong = null;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.song_view_layout, container, false);
        
        songBlockContainer = (LinearLayout)view.findViewById(R.id.songBlockContainer);
        
        return view;
    }
	
	public void setSong(Song newSong)
	{
		currSong = newSong;
		
		// first, clear out container
		songBlockContainer.removeAllViews();
		
		//update view
		Context context = songBlockContainer.getContext();
		
		ArrayList<Integer> chords = currSong.verseProgression.getChords();
		ArrayList<ArrayList<Note>> notes = currSong.verseProgression.getNotes();
		for (int ndx = 0; ndx < chords.size(); ndx++)
		{
			Button songBlock = new Button(context);
			songBlock.setLayoutParams(new LayoutParams((int)getResources().getDimension(R.dimen.songBlockWidth), LayoutParams.MATCH_PARENT));//LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			songBlock.setText(chords.get(ndx) + "\n" + notes.get(ndx));
			songBlock.setTextSize(12);
			songBlockContainer.addView(songBlock);
		}
	}
	
//	To include in xml, use:
//        <fragment
//        android:id="@+id/songGridFragment"
//        android:layout_width="match_parent"
//        android:layout_height="wrap_content"
//        class="com.blinz117.songgenerator.SongViewFragment"
//        tools:layout="@layout/song_view_layout" />
}
