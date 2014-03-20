package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class RecentActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	String font;
	String fontContent;
	String fontTitle;
	String theme;
	
	String contact_id;
	
	private SlidingMenu menu;
	private ListView navListView;
	
	private RecentCursorAdapter mAdapter;
	private boolean firstRunDoneRec;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent);

		setupGlobalPrefs();
		setupActionBar();
		setupSlidingMenu();
		
		getRecents();
		
	}


	private void setupGlobalPrefs() {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Editor edit = preferences.edit();
		
		theme = prefs.getString("theme", "#34AADC");
        font = prefs.getString("font", null);
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        
        firstRunDoneRec = prefs.getBoolean("firstRunDoneRec", false);
        if (!firstRunDoneRec) {
        	edit.putBoolean("firstRunDoneRec", true);
        	edit.apply();
        	
        	new AlertDialog.Builder(this)
		    .setTitle(getString(R.string.recDialogHeader))
		    .setMessage(getString(R.string.recMsg1) + getString(R.string.recMsg5))
		    .setNeutralButton(getString(R.string.okay), null)
		    .show();
        }
        
	}

	private void setupActionBar() {
		
		// Set up Action Bar
        TextView actionBarTitleText = (TextView) findViewById(getResources()
        		.getIdentifier("action_bar_title", "id","android"));
        actionBarTitleText.setTypeface(Typeface.createFromAsset(this.getAssets(), fontTitle));
        actionBarTitleText.setTextColor(Color.WHITE);
        actionBarTitleText.setTextSize(22);
        
        ActionBar bar = getActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(theme)));
        bar.setDisplayShowHomeEnabled(false);
        bar.setHomeButtonEnabled(true);
       
        // Do Tint if KitKat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        SystemBarTintManager tintManager = new SystemBarTintManager(this);
	        tintManager.setStatusBarTintEnabled(true);
	        
	        SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
	        getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, -150, 0,0);
	        config.getPixelInsetBottom();
	        
	        int actionBarColor = Color.parseColor(theme);
	        tintManager.setStatusBarTintColor(actionBarColor);
        }	
        
	}

	private void setupSlidingMenu() {
		
		// Set up Sliding Menu
        menu = new SlidingMenu(this);
        menu.setMode(SlidingMenu.LEFT);
        menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        menu.setShadowWidth(8);
        menu.setFadeDegree(0.8f);
        menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        menu.setBehindWidth(800);
        menu.setShadowDrawable(R.drawable.shadow);
        menu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        menu.setFadeDegree(0.35f);
        menu.setMenu(R.layout.menu_frame);
        navListView = (ListView) findViewById(R.id.nav_menu);
      
        final String[] nav = { getString(R.string.sMfavourites),
        		getString(R.string.sMRecent),
				getString(R.string.sMMostContacted),
				getString(R.string.sMPhoneContacts),
				getString(R.string.sMGoogleContacts),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings),
				getString(R.string.sMAbout)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_nav_google,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_settings,
				R.drawable.ic_nav_about
		};

		List<RowItem> rowItems;
		
		rowItems = new ArrayList<RowItem>();
        for (int i = 0; i < nav.length; i++) {
            RowItem item = new RowItem(navPhoto[i], nav[i]);
            rowItems.add(item);
        }
		
		CustomListViewAdapter listAdapter = new CustomListViewAdapter(this,
                R.layout.nav_item_layout, rowItems);
		
		navListView.setAdapter(listAdapter);
		navListView.setOnItemClickListener(this);			
	}
	
	private void getRecents() {
		ListView recentList = (ListView) findViewById(R.id.recentList);
		
		recentList.setOnItemClickListener(new OnItemClickListener() {
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            	String name = ((TextView)view.findViewById(R.id.r_name)).getText().toString();
                String number = ((TextView)view.findViewById(R.id.r_number)).getText().toString();
                String contact_id = getContactIDFromNumber(number, getApplicationContext());
                
                if (number.isEmpty()) {
                	Toast.makeText(getBaseContext(), getString(R.string.noPhoneNumber), Toast.LENGTH_LONG).show();
                } else if (name.isEmpty()) {
                	Toast.makeText(getBaseContext(), getString(R.string.noPhoneContact), Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                    intent.putExtra("contact_id", contact_id);
                    intent.putExtra("activity", "recent");
                    startActivity(intent);
                }
            }
        });
		
		
		recentList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(final AdapterView<?> parent, View view,
					final int position, long id) {
				
                String number = ((TextView)view.findViewById(R.id.r_number)).getText().toString();
				
                if (number.isEmpty()) {
                	Toast.makeText(getBaseContext(), getString(R.string.noPhoneNumber), Toast.LENGTH_LONG).show();
                } else {
    				Intent callIntent = new Intent(Intent.ACTION_CALL);          
    	            callIntent.setData(Uri.parse("tel:"+number.toString()));          
    	            startActivity(callIntent);
                }
				return true;
			}
        });
        
	    String[] from = {CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE};
	    int to[] = new int[] {
	    		R.id.r_name,
	    		R.id.r_number,
	    		R.id.r_date,
	    };
	    
	    mAdapter = new RecentCursorAdapter(
	            this,
	            R.layout.recent_layout,
	            null,
	            from,
	            to);		
	    
        recentList.setAdapter(mAdapter);
	    getLoaderManager().initLoader(0, null, this);

	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		        
		Uri queryUri = android.provider.CallLog.Calls.CONTENT_URI;

	    String[] projection = new String[] {
	            ContactsContract.Contacts._ID,
	            CallLog.Calls._ID,
	            CallLog.Calls.NUMBER,
	            CallLog.Calls.CACHED_NAME,
	            CallLog.Calls.DATE,
	            CallLog.Calls.DURATION,
	            CallLog.Calls.TYPE};
        
	    String sortOrder = String.format("%s limit 100 ", CallLog.Calls.DATE + " DESC");

        cursorLoader = new CursorLoader(
        		RecentActivity.this, 
        		queryUri,
                projection, 
                null, 
                null,
                sortOrder);	
        
        return cursorLoader;
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.recent, menu);
		
		
		return true;
	}
	
	public static String getContactIDFromNumber(String contactNumber,Context context)
    {
        contactNumber = Uri.encode(contactNumber);
        int phoneContactID = new Random().nextInt();
        try {
        	Cursor contactLookupCursor = context.getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,Uri.encode(contactNumber)),new String[] {PhoneLookup.DISPLAY_NAME, PhoneLookup._ID}, null, null, null);
            while(contactLookupCursor.moveToNext()){
                phoneContactID = contactLookupCursor.getInt(contactLookupCursor.getColumnIndexOrThrow(PhoneLookup._ID));
            }
            contactLookupCursor.close();

        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        }
        return String.valueOf(phoneContactID).toString();
    }
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(RecentActivity.this, FavActivity.class);
		   	RecentActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(RecentActivity.this, RecentActivity.class);
		   RecentActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(RecentActivity.this, FrequentActivity.class);
	   		RecentActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(RecentActivity.this, MainActivity.class);
	   		RecentActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(RecentActivity.this, GoogleActivity.class);
	   		RecentActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(RecentActivity.this, FBActivity.class);
		   	RecentActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(RecentActivity.this, LoginActivity.class);
		   	RecentActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(RecentActivity.this, InfoActivity.class);
		   	RecentActivity.this.startActivity(iIntent);
	   } 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
			case R.id.menu_clearRecents:
				AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(this);
				deleteBuilder.setMessage(getString(R.string.clearLogs));
				deleteBuilder.setCancelable(true);
				deleteBuilder.setPositiveButton(getString(R.string.yes),
	                    new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                	getApplicationContext().getContentResolver().delete(CallLog.Calls.CONTENT_URI,null,null);
	    				Intent recIntent = new Intent(RecentActivity.this, FavActivity.class);
	    				RecentActivity.this.startActivity(recIntent);
	                    dialog.cancel();
	                }
	            });
				deleteBuilder.setNegativeButton(getString(R.string.no),
	                    new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    dialog.cancel();
	                }
	            });

	            AlertDialog deleteAlert = deleteBuilder.create();
	            deleteAlert.show();
	            
				return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onContentChanged() {
	    super.onContentChanged();

	    View empty = findViewById(R.id.empty);
	    ListView list = (ListView) findViewById(R.id.recentList);
	    list.setEmptyView(empty);
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}
	
	@Override
	public void onStop() {
		super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	  
	@Override
	public void onResume() {
		super.onResume();  // Always call the superclass method first
	    setupActionBar();
	}
}