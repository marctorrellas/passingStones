package marc.passingStones;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class Menu extends Activity implements OnClickListener{
	
	private Button but;
	private Spinner spinner;
	private SharedPreferences mPrefs;
	private int gamesPlayed;
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.first_page);
//        

        
        mPrefs = getPreferences(MODE_PRIVATE);
        int initialPos = mPrefs.getInt("initialStones", 3);
        gamesPlayed = mPrefs.getInt("gamesPlayed", 0);

        
        but =(Button) findViewById(R.id.button1);
        but.setOnClickListener(this);
        //but.setText(getResources().getString(R.string.botoMenu)+gamesPlayed);
        but.setText(getResources().getString(R.string.MENU_BUTTON));
        
        spinner=(Spinner) findViewById(R.id.spinner1);
        String []options={"2","3","4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, options);
        spinner.setAdapter(adapter);
        spinner.setSelection(initialPos-2);
    }


	public void onClick(View arg0) {
		String selec=spinner.getSelectedItem().toString();
		Intent i = new Intent(this,Main.class);
		i.putExtra("initialStones", selec);
		
		// save number of stones for the next session
		
		Editor editor = mPrefs.edit();
		editor.putInt("initialStones",Integer.parseInt(selec));
		gamesPlayed = gamesPlayed +1 ;
		editor.putInt("gamesPlayed", gamesPlayed);
		editor.commit(); 
		Log.d("Menu",selec);
		Log.d("Menu",String.valueOf(gamesPlayed));
		startActivity(i);
	}
}
