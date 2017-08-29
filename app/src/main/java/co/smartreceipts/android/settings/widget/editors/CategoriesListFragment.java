package co.smartreceipts.android.settings.widget.editors;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.model.Category;
import co.smartreceipts.android.model.factory.CategoryBuilderFactory;
import co.smartreceipts.android.persistence.database.controllers.TableEventsListener;
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import dagger.android.support.AndroidSupportInjection;

public class CategoriesListFragment extends WBFragment implements View.OnClickListener, TableEventsListener<Category> {

	public static String TAG = "CategoriesListFragment";

	@Inject
	CategoriesTableController categoriesTableController;

	private BaseAdapter mAdapter;
    private Toolbar mToolbar;
	private ListView mListView;
	private List<Category> mCategories;
    private Category mScrollToCategory;

	public static CategoriesListFragment newInstance() {
		return new CategoriesListFragment();
	}

	@Override
	public void onAttach(Context context) {
		AndroidSupportInjection.inject(this);
		super.onAttach(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mAdapter = getAdapter();
		mCategories = new ArrayList<>();
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.simple_list, container, false);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
		mListView = (ListView) rootView.findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
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
            actionBar.setTitle(R.string.menu_main_categories);
            actionBar.setSubtitle(null);
        }
        categoriesTableController.subscribe(this);
        categoriesTableController.get();
	}

    @Override
    public void onPause() {
        categoriesTableController.unsubscribe(this);
        super.onPause();
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_settings_categories, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings_add) {
			addCategory();
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

	protected BaseAdapter getAdapter() {
		return new CategoriesListAdapter(LayoutInflater.from(getActivity()));
	}

	protected int getListItemLayoutId() {
		return R.layout.settings_category_card_item;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.edit) {
			Integer index = (Integer) view.getTag();
			editCategory(index);
		}
		else if (view.getId() == R.id.delete) {
			Integer index = (Integer) view.getTag();
			deleteCategory(index);
		}
	}

	private void addCategory() {
		final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
		final LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final TextView nameLabel = new TextView(getActivity()); nameLabel.setText(R.string.category_name);
		final EditText nameBox = new EditText(getActivity());
		final TextView codeLabel = new TextView(getActivity()); codeLabel.setText(R.string.category_code);
		final EditText codeBox = new EditText(getActivity());
		layout.addView(nameLabel);
		layout.addView(nameBox);
		layout.addView(codeLabel);
		layout.addView(codeBox);
		innerBuilder.setTitle(getString(R.string.dialog_category_add))
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String name = nameBox.getText().toString();
							final String code = codeBox.getText().toString();
                            categoriesTableController.insert(new CategoryBuilderFactory().setName(name).setCode(code).build(), new DatabaseOperationMetadata());

						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

	private void editCategory(int position) {
		final Category editCategory = mCategories.get(position);
		final String oldName = editCategory.getName();
		final String oldCode = editCategory.getCode();

		final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
		final LinearLayout layout = new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.BOTTOM);
		layout.setPadding(6, 6, 6, 6);
		final TextView nameLabel = new TextView(getActivity());
		nameLabel.setText(R.string.item_name);
		final EditText nameBox = new EditText(getActivity());
		nameBox.setText(oldName);
		final TextView codeLabel = new TextView(getActivity());
		codeLabel.setText(R.string.item_code);
		final EditText codeBox = new EditText(getActivity());
		codeBox.setText(oldCode);
		layout.addView(nameLabel);
		layout.addView(nameBox);
		layout.addView(codeLabel);
		layout.addView(codeBox);
		innerBuilder.setTitle(R.string.dialog_category_edit)
					.setView(layout)
					.setCancelable(true)
					.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							final String newName = nameBox.getText().toString();
							final String newCode = codeBox.getText().toString();
							final Category newCategory = new CategoryBuilderFactory().setName(newName).setCode(newCode).build();
							categoriesTableController.update(editCategory, newCategory, new DatabaseOperationMetadata());

						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

	private void deleteCategory(int position) {
		final Category category = mCategories.get(position);

		final AlertDialog.Builder innerBuilder = new AlertDialog.Builder(getActivity());
		innerBuilder.setTitle(getString(R.string.delete_item, category.getName()))
					.setCancelable(true)
					.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							categoriesTableController.delete(category, new DatabaseOperationMetadata());
						}
					})
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					})
					.show();
	}

    @Override
    public void onGetSuccess(@NonNull List<Category> list) {
        mCategories = list;
        mAdapter.notifyDataSetChanged();
        if (mScrollToCategory != null) {
            mListView.smoothScrollToPosition(mCategories.indexOf(mScrollToCategory));
            mScrollToCategory = null;
        }
    }

    @Override
    public void onGetFailure(@Nullable Throwable e) {

    }

    @Override
    public void onInsertSuccess(@NonNull Category category, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        categoriesTableController.get();
        mScrollToCategory = category;
    }

    @Override
    public void onInsertFailure(@NonNull Category category, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (e instanceof SQLException) {
            Toast.makeText(getActivity(), getString(R.string.toast_error_category_exists), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onUpdateSuccess(@NonNull Category oldCategory, @NonNull Category newCategory, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        categoriesTableController.get();
        mScrollToCategory = newCategory;
    }

    @Override
    public void onUpdateFailure(@NonNull Category oldT, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        if (e instanceof SQLException) {
            Toast.makeText(getActivity(), getString(R.string.toast_error_category_exists), Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getActivity(), getString(R.string.database_error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteSuccess(@NonNull Category category, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        categoriesTableController.get();
    }

    @Override
    public void onDeleteFailure(@NonNull Category category, @Nullable Throwable e, @NonNull DatabaseOperationMetadata databaseOperationMetadata) {
        Toast.makeText(getActivity(), getString(R.string.database_error), Toast.LENGTH_SHORT).show();
    }

    private static final class MyViewHolder {
		public TextView category;
		public TextView code;
		public View edit;
		public View delete;
	}

	private class CategoriesListAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;

		public CategoriesListAdapter(LayoutInflater inflater) {
			mInflater = inflater;
		}

		@Override
		public int getCount() {
			return mCategories.size();
		}

		@Override
		public Category getItem(int i) {
			return mCategories.get(i);
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
				holder.category = (TextView) convertView.findViewById(android.R.id.title);
				holder.code = (TextView) convertView.findViewById(android.R.id.summary);
				holder.edit = convertView.findViewById(R.id.edit);
				holder.delete = convertView.findViewById(R.id.delete);
				holder.edit.setOnClickListener(CategoriesListFragment.this);
				holder.delete.setOnClickListener(CategoriesListFragment.this);
				convertView.setTag(holder);
			}
			else {
				holder = (MyViewHolder) convertView.getTag();
			}
			holder.category.setText(getItem(i).getName());
			holder.code.setText(getItem(i).getCode());
			holder.edit.setTag(i);
			holder.delete.setTag(i);
			return convertView;
		}

	}

}