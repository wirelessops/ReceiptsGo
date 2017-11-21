package co.smartreceipts.android.settings.widget.editors.columns;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Column;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.impl.columns.receipts.ReceiptColumnDefinitions;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.ColumnTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.widget.UserSelectionTrackingOnItemSelectedListener;

public abstract class ColumnsListFragment1 extends WBFragment implements TableEventsListener<Column<Receipt>> {

	public static String TAG = "ColumnsListFragment";

    private final AdapterView.OnItemSelectedListener mSpinnerSelectionListener = new ColumnTypeChangeSelectionListener();
	private BaseAdapter mAdapter;
    private Toolbar mToolbar;
	private ListView mListView;
    private ColumnTableController mColumnTableController;
	private List<Column<Receipt>> mColumns;
    private ArrayAdapter<Column<Receipt>> mSpinnerAdapter;
    private boolean mDisableInsertsAndDeletes = false;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mColumnTableController = getColumnTableController();
        mAdapter = getAdapter();
        mSpinnerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                getReceiptColumnDefinitions().getAllColumns());
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.simple_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		return rootView;
	}

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSupportActionBar(mToolbar);
    }

    @Override
	public void onResume() {
		super.onResume();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setSubtitle(null);
        }
        mColumnTableController.subscribe(this);
        mColumnTableController.get();
	}

    protected abstract ReceiptColumnDefinitions getReceiptColumnDefinitions();
    protected abstract ColumnTableController getColumnTableController();

    @Override
    public void onPause() {
        mColumnTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_columns, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }

        if (mDisableInsertsAndDeletes) {
            // Temporarily until we receive a callback below
            return false;
        }

		if (item.getItemId() == R.id.menu_settings_add) {
            mDisableInsertsAndDeletes = true;
			mColumnTableController.insertDefaultColumn();
			return true;
		}
		else if (item.getItemId() == R.id.menu_settings_delete) {
            mDisableInsertsAndDeletes = true;
			mColumnTableController.deleteLast(new DatabaseOperationMetadata());
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}

    @Override
    public void onGetSuccess(@NonNull List<Column<Receipt>> list) {
        if (isResumed()) {
            mColumns = list;
            if (mListView.getAdapter() == null) {
                mListView.setAdapter(mAdapter);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {
        // No-op
    }

    @Override
    public void onInsertSuccess(@NonNull Column<Receipt> column, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        mDisableInsertsAndDeletes = false;
        if (isResumed()) {
            mColumnTableController.get();
        }
    }

    @Override
    public void onInsertFailure(@NonNull Column<Receipt> column, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        mDisableInsertsAndDeletes = false;
    }

    @Override
    public void onUpdateSuccess(@NonNull Column<Receipt> oldT, @NonNull Column<Receipt> newT, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (isResumed()) {
            mColumnTableController.get();
        }
    }

    @Override
    public void onUpdateFailure(@NonNull Column<Receipt> oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        // No-op
    }

    @Override
    public void onDeleteSuccess(@NonNull Column<Receipt> column, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        mDisableInsertsAndDeletes = false;
        if (isResumed()) {
            mColumnTableController.get();
        }
    }

    @Override
    public void onDeleteFailure(@NonNull Column<Receipt> column, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        mDisableInsertsAndDeletes = false;
    }

	protected BaseAdapter getAdapter() {
		return new SettingsListAdapter(LayoutInflater.from(getActivity()));
	}

	protected int getListItemLayoutId() {
		return R.layout.settings_column_card_item;
	}

	private static final class SpinnerTag {
		public Column<Receipt> column;
	}

	private static final class MyViewHolder {
		public TextView column;
		public Spinner spinner;
	}

	private class SettingsListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;

		public SettingsListAdapter(LayoutInflater inflater) {
			mInflater = inflater;

		}

		@Override
		public int getCount() {
			return mColumns.size();
		}

		@Override
		public Column<Receipt> getItem(int i) {
			return mColumns.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View convertView, ViewGroup parent) {
			MyViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(getListItemLayoutId(), parent, false);
				holder = new MyViewHolder();
				holder.column = (TextView) convertView.findViewById(android.R.id.title);
				holder.spinner = (Spinner) convertView.findViewById(R.id.column_spinner);
				holder.spinner.setAdapter(mSpinnerAdapter);
				holder.spinner.setTag(new SpinnerTag());
				convertView.setTag(holder);
			}
			else {
				holder = (MyViewHolder) convertView.getTag();
                holder.spinner.setOnItemSelectedListener(null); // Null out each time to not artificially hit the setter
			}
			holder.column.setText(getString(R.string.column_item, Integer.toString(i+1))); //Add +1 to make it not 0-th index
            final int selectedPosition = getColumnPositionByName(i);
            if (selectedPosition >= 0) {
                holder.spinner.setSelection(selectedPosition);
            }
            holder.spinner.setOnItemSelectedListener(mSpinnerSelectionListener);
			SpinnerTag spinnerTag = (SpinnerTag) holder.spinner.getTag();
	        spinnerTag.column = getItem(i);
			return convertView;
		}

        /**
         * Attempts to get the position in the spinner based on the column name. Since column "equals"
         * also takes into account the actual position in the database, we do a pseudo equals here by
         * name
         * @param columnPosition the position of the column
         * @return the position in the spinner or -1 if unknown
         */
        private int getColumnPositionByName(int columnPosition) {
            final String columnName = getItem(columnPosition).getName();
            for (int i = 0; i < mSpinnerAdapter.getCount(); i++) {
                if (columnName.equals(mSpinnerAdapter.getItem(i).getName())) {
                    return i;
                }
            }
            return -1;
        }

	}

    private class ColumnTypeChangeSelectionListener extends UserSelectionTrackingOnItemSelectedListener {

        @Override
        public void onUserSelectedNewItem(AdapterView<?> parent, View view, int position, long id, int previousPosition) {
            final SpinnerTag spinnerTag = (SpinnerTag) parent.getTag();
            final Column<Receipt> oldColumn = spinnerTag.column;
            final Column<Receipt> newColumn = mSpinnerAdapter.getItem(position);
            mColumnTableController.update(oldColumn, newColumn, new DatabaseOperationMetadata());
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

}