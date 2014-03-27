package com.raunaqsawhney.contakts;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class GroupDetailActivity extends Activity implements OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	// Declare Globals
	String theme;
	String font;
	String fontContent;
	String fontTitle;
	
	private SlidingMenu menu;
	private ListView navListView;
	private String group_id;
	
	SimpleCursorAdapter mAdapter;
	private ListView contactGroupListView;
	
	Cursor cursor;
    View header = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_detail);
		
        group_id = getIntent().getStringExtra("group_id");
		
		setupGlobalPrefs();
        setupActionBar();
        setupSlidingMenu();
        
        showGroupContacts();
	}
	
	private void setupGlobalPrefs() {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        theme = prefs.getString("theme", "#34AADC");
        fontContent = prefs.getString("fontContent", null);
        fontTitle = prefs.getString("fontTitle", null);	
        font = prefs.getString("font", null);
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
				getString(R.string.sMGroups),
				getString(R.string.sMFacebook),
				getString(R.string.sMSettings)
		};
		
		final Integer[] navPhoto = { R.drawable.ic_nav_star,
				R.drawable.ic_nav_recent,
				R.drawable.ic_nav_popular,
				R.drawable.ic_nav_phone,
				R.drawable.ic_allcontacts,
				R.drawable.ic_nav_group,
				R.drawable.ic_nav_fb,
				R.drawable.ic_nav_settings
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
	
	private void showGroupContacts() {
		// Set up the ListView for contacts to be displayed
        contactGroupListView = (ListView)findViewById(R.id.contactGroupList);
        contactGroupListView.setOnItemClickListener(new OnItemClickListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				cursor = (Cursor)parent.getItemAtPosition(position);
				String contact_id = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));		      
				
                Intent intent = new Intent(getApplicationContext(), ContactDetailActivity.class);
                intent.putExtra("contact_id", contact_id);
                intent.putExtra("activity","groupDetail");
                startActivity(intent);
            }
        });
	        
        // Fetch Display Name and Contact Photo URI
        String[] from = new String[] {
        		ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME,
            	ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI
        };
        
        // Put above content into XML layouts
        int[] to = new int[] {
        		R.id.c_name,
        		R.id.c_photo
        };
	        
        // Set the adapter to display the list
        mAdapter = new SimpleCursorAdapter(
        		this,
                R.layout.lv_layout, 
                null,
                from,
                to, 
                0);
        	
	    
        
        if (Integer.parseInt(group_id) == 1 ) {
        	header = getLayoutInflater().inflate(R.layout.mycontacts_header, null);
        } else if (Integer.parseInt(group_id) == 2 ) {
        	header = getLayoutInflater().inflate(R.layout.android_header, null);
        } else if (Integer.parseInt(group_id) == 3 ) {
        	header = getLayoutInflater().inflate(R.layout.friends_header, null);
        } else if (Integer.parseInt(group_id) == 4 ) {
        	header = getLayoutInflater().inflate(R.layout.family_header, null);
        } else if (Integer.parseInt(group_id) == 5 ) {
        	header = getLayoutInflater().inflate(R.layout.coworker_header, null);
        } else if (Integer.parseInt(group_id) == 6 ) {
        	header = getLayoutInflater().inflate(R.layout.youtube_header, null);
        } else if (Integer.parseInt(group_id) == 8 ) {
        	header = getLayoutInflater().inflate(R.layout.googleplus_header, null);
        }
        
	    getLoaderManager().initLoader(0, null, this);
	    contactGroupListView.setAdapter(mAdapter);	
	    
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
		
		CursorLoader cursorLoader = null;
		
		Uri baseUri = ContactsContract.Data.CONTENT_URI;
        
        String[] projection = new String[] {
        	ContactsContract.CommonDataKinds.Identity._ID,
        	ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME,
        	ContactsContract.CommonDataKinds.Photo.PHOTO_THUMBNAIL_URI,
        	ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID
        };
        
        
        cursorLoader = new CursorLoader(
        		GroupDetailActivity.this, 
        		baseUri,
                projection, 
                CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ?" + " AND "
                        + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'", 
                new String[] { String.valueOf(group_id) },
                null);	
        
        return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		View empty = findViewById(R.id.empty);
		if (contactGroupListView.getAdapter().isEmpty()) {
	        contactGroupListView.setEmptyView(empty);
		    contactGroupListView.removeHeaderView(header);
	    } else {
		    contactGroupListView.addHeaderView(header, null, false);
	    }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		long selected = (navListView.getItemIdAtPosition(position));
		
		if (selected == 0) {
		   	Intent favIntent = new Intent(GroupDetailActivity.this, FavActivity.class);
		   	GroupDetailActivity.this.startActivity(favIntent);
	   } else if (selected == 1) {
		   Intent recIntent = new Intent(GroupDetailActivity.this, RecentActivity.class);
		   GroupDetailActivity.this.startActivity(recIntent);
	   } else if (selected == 2) {
	   		Intent freqIntent = new Intent(GroupDetailActivity.this, FrequentActivity.class);
	   		GroupDetailActivity.this.startActivity(freqIntent);
	   } else if (selected == 3) {
	   		Intent phoneIntent = new Intent(GroupDetailActivity.this, MainActivity.class);
	   		GroupDetailActivity.this.startActivity(phoneIntent);
	   } else if (selected == 4) {
	   		Intent googleIntent = new Intent(GroupDetailActivity.this, GoogleActivity.class);
	   		GroupDetailActivity.this.startActivity(googleIntent);
	   } else if (selected == 5) {
		   	Intent fbIntent = new Intent(GroupDetailActivity.this, GroupActivity.class);
		   	GroupDetailActivity.this.startActivity(fbIntent);
	   }  else if (selected == 6) {
		   	Intent loIntent = new Intent(GroupDetailActivity.this, FBActivity.class);
		   	GroupDetailActivity.this.startActivity(loIntent);
	   }  else if (selected == 7) {
		   	Intent iIntent = new Intent(GroupDetailActivity.this, LoginActivity.class);
		   	GroupDetailActivity.this.startActivity(iIntent);
	   }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.menu_dial:
	        	try {
	        		Intent dialIntent = new Intent(GroupDetailActivity.this, DialerActivity.class);
	    		   	GroupDetailActivity.this.startActivity(dialIntent);
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(getApplicationContext(), getString(R.string.dialerNotFound), Toast.LENGTH_LONG).show();
	        	}
	            return true;  
	            
	        case R.id.menu_add:
	        	try {
	        		Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
	        		intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
		    		startActivity(intent);
		    		return true;
	        	} catch (ActivityNotFoundException e) {
	        		Toast.makeText(this, getString(R.string.addNotFound), Toast.LENGTH_LONG).show();
	        	}
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.group_detail, menu);
		return true;
	}
	
  @Override
  public void onResume() {
      super.onResume();  // Always call the superclass method first
      setupActionBar();
  }
  
  @Override
  public void onStart() {
    super.onStart();
    cursor = null;
    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
  }

  @Override
  public void onStop() {
    super.onStop();
    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
  }
  
  @Override
  public void onDestroy() {
	   super.onDestroy();
	   if (cursor != null) {
	      cursor.close();
	   }
	}
}
