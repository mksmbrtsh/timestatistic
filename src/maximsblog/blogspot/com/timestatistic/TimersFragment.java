package maximsblog.blogspot.com.timestatistic;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

public final class TimersFragment extends Fragment implements LoaderCallbacks<Cursor> {
    private static final String KEY_CONTENT = "TestFragment:Content";

    public static TimersFragment newInstance(String content) {
    	TimersFragment fragment = new TimersFragment();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            builder.append(content).append(" ");
        }
        builder.deleteCharAt(builder.length() - 1);
        fragment.mContent = builder.toString();

        return fragment;
    }
    
    SimpleCursorAdapter mAdapter; 		
    LoaderManager loadermanager;		
    CursorLoader cursorLoader;

    private String mContent = "???";
	private ListView mList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getString(KEY_CONTENT);
        }
        
        loadermanager=getLoaderManager();
    	
    	String[] uiBindFrom = {  RecordsDbHelper.NAME};		
    	int[] uiBindTo = {android.R.id.text1};
    	
            /*Empty adapter that is used to display the loaded data*/
    	mAdapter = new SimpleCursorAdapter(this.getActivity(),android.R.layout.simple_list_item_1, null, uiBindFrom, uiBindTo,0);  
        loadermanager.initLoader(1, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_timers, container, false);
        mList = (ListView)layout.findViewById(R.id.listView1);
        mList.setAdapter(mAdapter);
        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CONTENT, mContent);
    }

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		CursorLoader loader = new CursorLoader(
				         this.getActivity(),
				         RecordsDbHelper.CONTENT_URI_TIMERS, 
				         null, 
				         null, 
				         null, 
				         null);
				   return loader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if(mAdapter!=null && cursor !=null)
			mAdapter.swapCursor(cursor); //swap the new cursor in.
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}
}