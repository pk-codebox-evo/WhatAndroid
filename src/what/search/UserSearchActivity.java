package what.search;

import java.util.ArrayList;
import java.util.List;

import what.gui.MyActivity;
import what.gui.R;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import api.search.user.Results;
import api.search.user.UserSearch;

public class UserSearchActivity extends MyActivity implements OnClickListener {
	private ScrollView scrollView;
	private ArrayList<TextView> resultList;
	private LinearLayout scrollLayout;
	private UserSearch userSearch;
	private Intent intent;
	private ProgressDialog dialog;
	private String searchTerm = "";
	private EditText searchBar;
	private Button backButton, nextButton;
	private int page;
	private TextView title;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.usersearch, true);
	}

	@Override
	public void init() {
		resultList = new ArrayList<TextView>();
	}

	@Override
	public void load() {
		backButton = (Button) this.findViewById(R.id.previousButton);
		nextButton = (Button) this.findViewById(R.id.nextButton);
		scrollView = (ScrollView) this.findViewById(R.id.scrollView);
		scrollLayout = (LinearLayout) this.findViewById(R.id.scrollLayout);
		searchBar = (EditText) this.findViewById(R.id.searchBar);
		title = (TextView) this.findViewById(R.id.title);
	}

	@Override
	public void prepare() {
		setButtonState(backButton, false);
		setButtonState(nextButton, false);

		getBundle();
		// if the page is greater than one than get the search term and automatically search for it
		if ((page > 1) || (searchTerm.length() > 1)) {
			new LoadSearchResults().execute();
		}
	}

	private void getBundle() {
		Bundle b = this.getIntent().getExtras();
		try {
			page = b.getInt("page");
		} catch (Exception e) {
			page = 1;
		}

		try {
			searchTerm = b.getString("searchTerm");
		} catch (Exception e) {
			searchTerm = "";
		}
	}

	public void search(View v) {
		searchTerm = searchBar.getText().toString().trim();
		if (searchTerm.length() > 0) {
			clear();
			// reset the page to 1 for the search
			page = 1;
			new LoadSearchResults().execute();
		} else {
			Toast.makeText(this, "Nothing to search for", Toast.LENGTH_SHORT).show();
		}
	}

	private void populateLayout() {
		title.setText("User Search, page " + page);
		setButtonState(backButton, userSearch.hasPreviousPage());
		setButtonState(nextButton, userSearch.hasNextPage());

		List<Results> results = userSearch.getResponse().getResults();

		if (!results.isEmpty()) {
			for (int i = 0; i < results.size(); i++) {
				if ((i % 2) == 0) {
					resultList.add((TextView) getLayoutInflater().inflate(R.layout.torrent_name_even, null));
				} else {
					resultList.add((TextView) getLayoutInflater().inflate(R.layout.torrent_name_odd, null));
				}
				resultList.get(i).setText(results.get(i).getUsername());
				resultList.get(i).setTextSize(18);
				resultList.get(i).setId(i);
				resultList.get(i).setOnClickListener(this);
				scrollLayout.addView(resultList.get(i));
			}
		} else {
			Toast.makeText(this, "Nothing found", Toast.LENGTH_SHORT).show();
		}
	}

	private void clear() {
		try {
			resultList.clear();
			scrollLayout.removeAllViews();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openUser(int i) {
		Bundle b = new Bundle();
		intent = new Intent(UserSearchActivity.this, what.user.UserProfileTabActivity.class);
		b.putInt("userId", i);
		intent.putExtras(b);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < (resultList.size()); i++) {
			if (v.getId() == resultList.get(i).getId()) {
				openUser(userSearch.getResponse().getResults().get(i).getUserId().intValue());
			}
		}
	}

	public void next(View v) {
		if (userSearch.hasNextPage()) {
			Bundle b = new Bundle();
			intent = new Intent(UserSearchActivity.this, what.search.UserSearchActivity.class);
			b.putInt("page", page + 1);
			b.putString("searchTerm", searchTerm);
			intent.putExtras(b);
			startActivity(intent);
		}
	}

	public void back(View v) {
		finish();
	}

	@Override
	public void onRightGesturePerformed() {
		next(null);
	}

	@Override
	public void onDownGesturePerformed() {
		scrollView.fullScroll(ScrollView.FOCUS_DOWN);
	}

	@Override
	public void onUpGesturePerformed() {
		scrollView.fullScroll(ScrollView.FOCUS_UP);

	}

	private class LoadSearchResults extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			lockScreenRotation();
			dialog = new ProgressDialog(UserSearchActivity.this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Loading...");
			dialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			userSearch = UserSearch.userSearchFromSearchTermAndPage(searchTerm, page);
			return userSearch.getStatus();
		}

		@Override
		protected void onPostExecute(Boolean status) {
			if (status == true) {
				populateLayout();
			}
			dialog.dismiss();
			if (status == false) {
				Toast.makeText(UserSearchActivity.this, "Could not load search results", Toast.LENGTH_LONG).show();
			}
			unlockScreenRotation();
		}
	}

}
