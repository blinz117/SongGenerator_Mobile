package com.blinz117.songgenerator;

import android.app.*;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.widget.*;

public class SaveFileDialogFragment extends DialogFragment{
	static final String sChooseFile = "Choose filename";
	static final String sOk = "Ok";
	static final String sCancel = "Cancel";
	
	SaveFileDialogListener mListener;
	
	String fileName;
	
	EditText inputText;
	
	// interface for host activity to implement
    public interface SaveFileDialogListener {
        public void onSetFileName(SaveFileDialogFragment dialog);
        public void onCancelSaveDialog(SaveFileDialogFragment dialog);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SaveFileDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		 
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		View mLayout = inflater.inflate(R.layout.save_dialog_layout, null);
        builder.setView(mLayout);
        inputText = (EditText)mLayout.findViewById(R.id.saveDialogInput);
        builder.setMessage(sChooseFile)
            .setPositiveButton(sOk, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   fileName = inputText.getText().toString();
                       mListener.onSetFileName(SaveFileDialogFragment.this);
                   }
               })
               .setNegativeButton(sCancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
	}
}
