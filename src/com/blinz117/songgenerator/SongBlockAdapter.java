package com.blinz117.songgenerator;

import com.blinz117.songbuilder.songstructure.*;
import com.blinz117.songbuilder.songstructure.MusicStructure.*;

import android.content.Context;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class SongBlockAdapter extends BaseAdapter{

	private Context mContext;
	private ChordProgression mChordProgression;
	private Pitch mKey;
	private ScaleType mScale;
	
	public SongBlockAdapter(Context context)
	{
		mContext = context;
		mChordProgression = null;
		
		mKey = null;
		mScale = null;
	}
	
	public void setProgression(ChordProgression chordProgression)
	{
		mChordProgression = chordProgression;
		notifyDataSetChanged();
	}
	
	public void setKey(Pitch key, ScaleType scale)
	{
		mKey = key;
		mScale = scale;
	}
	
	@Override
	public int getCount() {
		if (mChordProgression != null)
			return mChordProgression.getChords().size();
		
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Button songBlock;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	songBlock = new Button(mContext);
        	songBlock.setLayoutParams(new GridView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        	songBlock.setBackgroundResource(R.drawable.song_block_border);
        } else {
        	songBlock = (Button) convertView;
        }

        // Get scale degree of root of chord (in scale degrees)
        int chordDegree = mChordProgression.getChords().get(position);
        // Get absolute note of key (in semitones)
        int key = mKey.ordinal();
        // Get offset of chord degree from root of chord (in semitones)
        int offset = mScale.getAbsIntervals()[chordDegree - 1];
        // Get absolute note value of chord root (in semitones)
        int chordPitchNdx = (key + offset) % MusicStructure.NUMPITCHES;
        Pitch newPitch = Pitch.values()[chordPitchNdx];
        
        ChordType chordType = mScale.getTriadChordType(chordDegree);
        
        songBlock.setText(newPitch + "" + chordType);// + " (" + chordDegree + ")");
        return songBlock;
    }

}
