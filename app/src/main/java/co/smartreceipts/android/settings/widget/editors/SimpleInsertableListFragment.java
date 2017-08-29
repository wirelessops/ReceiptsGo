package co.smartreceipts.android.settings.widget.editors;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.persistence.database.controllers.TableController;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;

public abstract class SimpleInsertableListFragment<T> extends WBFragment implements View.OnClickListener, TableEventsListener<T> {

    private Toolbar mToolbar;
	private ListView mListView;
	private Adapter mAdapter;
    private List<T> mData;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
        mData = new ArrayList<>();
		mAdapter = new Adapter(this, getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.simple_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView.setAdapter(mAdapter);
        setSupportActionBar(mToolbar);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_categories, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings_add) {
			addItem();
			return true;
		}
		else if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onResume() {
        super.onResume();
        getTableController().subscribe(this);
        getTableController().get();
    }

    @Override
    public void onPause() {
        getTableController().unsubscribe(this);
        super.onPause();
    }

    /**
	 * @return the {@link Adapter} that is being used by this fragment
	 */
	protected final Adapter getAdapter() {
		return mAdapter;
	}
	
	/**
	 * @return - the data set used to populate this list fragment
	 */
	protected abstract TableController<T> getTableController();
	
	/**
	 * Shows the proper message in order to assist the user with inserting an item
	 */
	protected abstract void addItem();
	
	/**
	 * Gets a view for a particular item. Subclasses should implement the traditional view-holder pattern
	 *  
	 * @param item - the data item to use
	 * @param convertView - the recycled view or {@code null} if it does not yet exist
	 * @param parent - the parent view with which to add this to
	 * @return the properly constructed view object
	 */
	public abstract View getView(LayoutInflater inflater, T item, View convertView, ViewGroup parent);

    @Override
    public void onGetSuccess(@NonNull List<T> list) {
        mData = list;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onInsertFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onUpdateSuccess(@NonNull T oldT, @NonNull T newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onUpdateFailure(@NonNull T oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }

    @Override
    public void onDeleteSuccess(@NonNull T t, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        getTableController().get();
    }

    @Override
    public void onDeleteFailure(@NonNull T t, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {

    }
	
	private final class Adapter extends BaseAdapter {
		
		private final SimpleInsertableListFragment<T> mParentFragment;
		protected final LayoutInflater mInflater;
		
		public Adapter(final SimpleInsertableListFragment<T> parentFragment, final Context context) {
			mParentFragment = parentFragment;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public T getItem(int i) {
			return mData.get(i);
		}

		@Override
		public long getItemId(int i) {
			return 0;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent) {
			final T item = getItem(i);
			return mParentFragment.getView(mInflater, item, convertView, parent);
		}
		
	}

}
