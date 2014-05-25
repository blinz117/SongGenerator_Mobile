package com.blinz117.songgenerator;

import java.util.ArrayList;

import com.blinz117.songbuilder.songstructure.*;

import android.app.Activity;
import android.content.Context;
import android.os.*;
import android.support.v4.app.Fragment;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class SongViewFragment extends Fragment {

	private Song currSong;
	private LinearLayout songBlockContainer;
	
	public interface SongChangedListener{
		public void onSongChanged(Song newSong);
	}
	
	SongChangedListener mListener;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (SongChangedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement SongChangedListener");
        }
    }
	
	private final OnClickListener onSongBlockClickedListener = new OnClickListener() {
        public void onClick(final View v) {
        	int chordNdx = songBlockContainer.indexOfChild(v);
        	ChordProgression currProg = currSong.verseProgression;
        	int ndxTracker = -1;
        	for (int patternNdx = 0; patternNdx < currProg.patterns.size(); patternNdx++ )
        	{
        		Pattern currPattern = currProg.patterns.get(patternNdx);
        		// check if the chord corresponding to the button we clicked on is
        		// in this pattern or not
        		if (chordNdx > ndxTracker + currPattern.getChords().size())
        		{
        			ndxTracker += currPattern.getChords().size();
        		}
        		else
        		{
	    			int localNdx = chordNdx - ndxTracker - 1;
	    			Integer currValue = currPattern.getChords().get(localNdx);
	    			Integer newValue = (currValue % 7) + 1;
	    			currPattern.chords.set(localNdx, newValue);
	    			
	    			mListener.onSongChanged(currSong);
	    			return;
        		}
        	}
        }
    };
	
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		currSong = null;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
			
			songBlock.setOnClickListener(onSongBlockClickedListener);
			
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
