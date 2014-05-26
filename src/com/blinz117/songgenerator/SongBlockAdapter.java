package com.blinz117.songgenerator;

import com.blinz117.songbuilder.songstructure.ChordProgression;

import android.content.Context;
import android.view.*;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class SongBlockAdapter extends BaseAdapter{

	private Context mContext;
	private ChordProgression mChordProgression;
	
	public SongBlockAdapter(Context context)
	{
		mContext = context;
		mChordProgression = null;
	}
	
	public void setProgression(ChordProgression chordProgression)
	{
		mChordProgression = chordProgression;
		notifyDataSetChanged();
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

        songBlock.setText(mChordProgression.getChords().get(position) + "");
        return songBlock;
    }

}
