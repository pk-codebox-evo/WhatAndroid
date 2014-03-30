package what.whatandroid.request;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import api.cli.Utils;
import api.requests.Request;
import api.requests.Response;
import com.nostra13.universalimageloader.core.ImageLoader;
import what.whatandroid.R;
import what.whatandroid.callbacks.OnLoggedInCallback;
import what.whatandroid.callbacks.SetTitleCallback;
import what.whatandroid.imgloader.ImageLoadingListener;
import what.whatandroid.settings.SettingsActivity;

/**
 * Display the details of a request, bounty, artists etc.
 */
public class RequestFragment extends Fragment implements OnLoggedInCallback {
	/**
	 * The request being viewed
	 */
	private Request request;
	/**
	 * Request id passed through when the fragment is created so we can defer loading
	 */
	private int requestId;
	private SetTitleCallback callbacks;
	/**
	 * Various views displaying the information about the request along with associated headers/text
	 * so that we can hide any unused views
	 */
	private ImageView image;
	private ProgressBar spinner;
	private TextView title, created, recordLabel, catalogueNumber, releaseType,
		acceptBitrates, acceptFormats, acceptMedia, votes, bounty, tags;
	private View recordLabelText, catalogueNumberText, releaseTypeText,
		bitratesContainer, formatsContainer, mediaContainer;
	/**
	 * The list shows the artists & top contributors
	 */
	private ExpandableListView list;

	/**
	 * Use this factory method to create a request fragment displaying the request
	 *
	 * @param id request to load
	 * @return fragment displaying the request
	 */
	public static RequestFragment newInstance(int id){
		RequestFragment fragment = new RequestFragment();
		fragment.requestId = id;
		return fragment;
	}

	public static RequestFragment newInstance(Request r){
		RequestFragment fragment = new RequestFragment();
		fragment.request = r;
		fragment.requestId = r.getResponse().getRequestId().intValue();
		return fragment;
	}

	public RequestFragment(){
		//Required empty ctor
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		try {
			callbacks = (SetTitleCallback)activity;
		}
		catch (ClassCastException e){
			throw new ClassCastException(activity.toString() + " must implement SetTitleCallbacks");
		}
	}

	@Override
	public void onLoggedIn(){
		if (request == null){
			new LoadRequest().execute(requestId);
		}
		else {
			updateRequest();
		}
	}

	public Request getRequest(){
		return request;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.expandable_list_view, container, false);
		list = (ExpandableListView)view.findViewById(R.id.exp_list);

		View header = inflater.inflate(R.layout.header_request_info, null);
		list.addHeaderView(header);

		image = (ImageView)header.findViewById(R.id.image);
		spinner = (ProgressBar)header.findViewById(R.id.loading_indicator);
		title = (TextView)header.findViewById(R.id.title);
		created = (TextView)header.findViewById(R.id.created);
		recordLabelText = header.findViewById(R.id.record_label_text);
		recordLabel = (TextView)header.findViewById(R.id.record_label);
		catalogueNumberText = header.findViewById(R.id.catalogue_number_text);
		catalogueNumber = (TextView)header.findViewById(R.id.catalogue_number);
		releaseType = (TextView)header.findViewById(R.id.release_type);
		releaseTypeText = header.findViewById(R.id.release_type_text);
		acceptBitrates = (TextView)header.findViewById(R.id.accept_bitrates);
		bitratesContainer = header.findViewById(R.id.accept_bitrates_container);
		acceptFormats = (TextView)header.findViewById(R.id.accept_formats);
		formatsContainer = header.findViewById(R.id.accept_formats_container);
		acceptMedia = (TextView)header.findViewById(R.id.accept_media);
		mediaContainer = header.findViewById(R.id.accept_media_container);
		votes = (TextView)header.findViewById(R.id.votes);
		bounty = (TextView)header.findViewById(R.id.bounty);
		tags = (TextView)header.findViewById(R.id.tags);
		return view;
	}

	/**
	 * Update the request information being shown
	 */
	private void updateRequest(){
		Response response = request.getResponse();
		callbacks.setTitle(response.getTitle());
		title.setText(response.getTitle());
		created.setText(response.getTimeAdded());
		votes.setText(response.getVoteCount().toString());
		bounty.setText(Utils.toHumanReadableSize(response.getTotalBounty().longValue()));

		RequestAdapter adapter = new RequestAdapter(getActivity(), response.getMusicInfo(), response.getTopContributors());
		list.setAdapter(adapter);
		list.setOnChildClickListener(adapter);

		//Requests may be missing any of these fields
		String imgUrl = response.getImage();
		if (SettingsActivity.imagesEnabled(getActivity()) && imgUrl != null && !imgUrl.isEmpty()){
			ImageLoader.getInstance().displayImage(imgUrl, image, new ImageLoadingListener(spinner));
		}
		else {
			image.setVisibility(View.GONE);
			spinner.setVisibility(View.GONE);
		}
		if (response.getRecordLabel() != null && !response.getRecordLabel().isEmpty()){
			recordLabel.setText(response.getRecordLabel());
		}
		else {
			recordLabelText.setVisibility(View.GONE);
			recordLabel.setVisibility(View.GONE);
		}
		if (response.getCatalogueNumber() != null && !response.getCatalogueNumber().isEmpty()){
			catalogueNumber.setText(response.getCatalogueNumber());
		}
		else {
			catalogueNumberText.setVisibility(View.GONE);
			catalogueNumber.setVisibility(View.GONE);
		}
		if (response.getReleaseName() != null && !response.getReleaseName().isEmpty()){
			releaseType.setText(response.getReleaseName());
		}
		else {
			releaseTypeText.setVisibility(View.GONE);
			releaseType.setVisibility(View.GONE);
		}
		if (!response.getBitrateList().isEmpty()){
			String bitrates = response.getBitrateList().toString();
			bitrates = bitrates.substring(bitrates.indexOf('[') + 1, bitrates.lastIndexOf(']'));
			acceptBitrates.setText(bitrates);
		}
		else {
			bitratesContainer.setVisibility(View.GONE);
		}
		if (!response.getFormatList().isEmpty()){
			String formats = response.getFormatList().toString();
			formats = formats.substring(formats.indexOf('[') + 1, formats.lastIndexOf(']'));
			acceptFormats.setText(formats);
		}
		else {
			formatsContainer.setVisibility(View.GONE);
		}
		if (!response.getMediaList().isEmpty()){
			String media = response.getMediaList().toString();
			media = media.substring(media.indexOf('[') + 1, media.lastIndexOf(']'));
			acceptMedia.setText(media);
		}
		else {
			mediaContainer.setVisibility(View.GONE);
		}
		String tagString = response.getTags().toString();
		tagString = tagString.substring(tagString.indexOf('[') + 1, tagString.lastIndexOf(']'));
		tags.setText(tagString);
	}

	/**
	 * Async task to load the request
	 */
	private class LoadRequest extends AsyncTask<Integer, Void, Request> {

		/**
		 * Load some request from its id
		 *
		 * @param params params[0] should contain the id to load
		 * @return the loaded request, or null if something went wrong
		 */
		@Override
		protected Request doInBackground(Integer... params){
			try {
				Request r = Request.fromId(params[0]);
				if (r != null && r.getStatus()){
					return r;
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPreExecute(){
			if (getActivity() != null){
				getActivity().setProgressBarIndeterminateVisibility(true);
				getActivity().setProgressBarIndeterminate(true);
			}
		}

		@Override
		protected void onPostExecute(Request r){
			if (getActivity() != null){
				getActivity().setProgressBarIndeterminateVisibility(false);
				getActivity().setProgressBarIndeterminate(false);
			}
			if (r != null){
				request = r;
				updateRequest();
			}
			else if (getActivity() != null){
				Toast.makeText(getActivity(), "Failed to load torrent group", Toast.LENGTH_SHORT).show();
			}
		}
	}
}