package co.smartreceipts.android.sync.widget.backups;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import co.smartreceipts.android.R;
import co.smartreceipts.android.activities.NavigationHandler;
import co.smartreceipts.android.fragments.SelectAutomaticBackupProviderDialogFragment;
import co.smartreceipts.android.fragments.WBFragment;
import co.smartreceipts.android.persistence.PersistenceManager;
import co.smartreceipts.android.purchases.PurchaseManager;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import co.smartreceipts.android.purchases.source.PurchaseSource;
import co.smartreceipts.android.purchases.wallet.PurchaseWallet;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.sync.BackupProviderChangeListener;
import co.smartreceipts.android.sync.BackupProvidersManager;
import co.smartreceipts.android.sync.network.NetworkManager;
import co.smartreceipts.android.sync.network.SupportedNetworkType;
import co.smartreceipts.android.sync.provider.SyncProvider;
import co.smartreceipts.android.utils.log.Logger;
import dagger.android.support.AndroidSupportInjection;
import io.reactivex.disposables.CompositeDisposable;


public class BackupsFragment extends WBFragment implements BackupProviderChangeListener {

    private static final int IMPORT_SMR_REQUEST_CODE = 50;

    @Inject
    PersistenceManager persistenceManager;
    @Inject
    PurchaseWallet purchaseWallet;
    @Inject
    NetworkManager networkManager;
    @Inject
    BackupProvidersManager backupProvidersManager;
    @Inject
    PurchaseManager purchaseManager;
    @Inject
    NavigationHandler navigationHandler;

    private RemoteBackupsDataCache remoteBackupsDataCache;
    private CompositeDisposable compositeDisposable;

    private Toolbar toolbar;
    private View headerView;
    private View exportButton;
    private View importButton;
    private View backupConfigButton;
    private ImageView backupConfigButtonImage;
    private TextView backupConfigButtonText;
    private TextView warningTextView;
    private CheckBox wifiOnlyCheckbox;
    private View existingBackupsSection;
    private RecyclerView recyclerView;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.debug(this, "onCreate");
        setHasOptionsMenu(true);
        remoteBackupsDataCache = new RemoteBackupsDataCache(getFragmentManager(), getContext(),
                backupProvidersManager, networkManager, persistenceManager.getDatabase());
    }

    @Nullable
    @Override
    @SuppressLint("InflateParams")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.simple_recycler_view, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(android.R.id.list);

        headerView = inflater.inflate(R.layout.backups_header, null);
        exportButton = headerView.findViewById(R.id.manual_backup_export);
        importButton = headerView.findViewById(R.id.manual_backup_import);
        warningTextView = (TextView) headerView.findViewById(R.id.auto_backup_warning);
        backupConfigButton = headerView.findViewById(R.id.automatic_backup_config_button);
        backupConfigButtonImage = (ImageView) headerView.findViewById(R.id.automatic_backup_config_button_image);
        backupConfigButtonText = (TextView) headerView.findViewById(R.id.automatic_backup_config_button_text);
        wifiOnlyCheckbox = (CheckBox) headerView.findViewById(R.id.auto_backup_wifi_only);
        existingBackupsSection = headerView.findViewById(R.id.existing_backups_section);

        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigationHandler.showDialog(new ExportBackupDialogFragment());
            }
        });
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(intent, getString(R.string.import_string)), IMPORT_SMR_REQUEST_CODE);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
                }
            }
        });
        backupConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (backupProvidersManager.getSyncProvider() == SyncProvider.None
                        && !purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)) {
                    purchaseManager.initiatePurchase(InAppPurchase.SmartReceiptsPlus, PurchaseSource.AutomaticBackups);
                } else {
                    navigationHandler.showDialog(new SelectAutomaticBackupProviderDialogFragment());
                }
            }
        });
        wifiOnlyCheckbox.setChecked(persistenceManager.getPreferenceManager().get(UserPreference.Misc.AutoBackupOnWifiOnly));
        wifiOnlyCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                persistenceManager.getPreferenceManager().set(UserPreference.Misc.AutoBackupOnWifiOnly, checked);
                backupProvidersManager.setAndInitializeNetworkProviderType(checked ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(new RemoteBackupsListAdapter(headerView, navigationHandler,
                backupProvidersManager, persistenceManager.getPreferenceManager(), networkManager));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.debug(this, "onResume");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.backups);
        }
        compositeDisposable = new CompositeDisposable();
        updateViewsForProvider(backupProvidersManager.getSyncProvider());
        backupProvidersManager.registerChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == IMPORT_SMR_REQUEST_CODE) {
                if (data != null) {
                    navigationHandler.showDialog(ImportLocalBackupDialogFragment.newInstance(data.getData()));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            return navigationHandler.navigateBack();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        Logger.debug(this, "onPause");
        backupProvidersManager.unregisterChangeListener(this);
        compositeDisposable.dispose();
        super.onPause();
    }

    @Override
    public void onProviderChanged(@NonNull SyncProvider newProvider) {
        // Clear out any existing subscriptions when we change providers
        compositeDisposable.dispose();
        compositeDisposable = new CompositeDisposable();
        remoteBackupsDataCache.clearGetBackupsResults();

        updateViewsForProvider(newProvider);
    }

    public void updateViewsForProvider(@NonNull SyncProvider syncProvider) {
        if (isResumed()) {
            if (syncProvider == SyncProvider.None) {
                warningTextView.setText(R.string.auto_backup_warning_none);
                backupConfigButtonText.setText(R.string.auto_backup_configure);
                backupConfigButtonImage.setImageResource(R.drawable.ic_cloud_off_24dp);
                wifiOnlyCheckbox.setVisibility(View.GONE);
            } else if (syncProvider == SyncProvider.GoogleDrive) {
                warningTextView.setText(R.string.auto_backup_warning_drive);
                backupConfigButtonText.setText(R.string.auto_backup_source_google_drive);
                backupConfigButtonImage.setImageResource(R.drawable.ic_cloud_done_24dp);
                wifiOnlyCheckbox.setVisibility(View.VISIBLE);
            } else {
                throw new IllegalArgumentException("Unsupported sync provider type was specified");
            }

            compositeDisposable.add(remoteBackupsDataCache.getBackups(syncProvider)
                    .subscribe(remoteBackupMetadatas -> {
                        if (remoteBackupMetadatas.isEmpty()) {
                            existingBackupsSection.setVisibility(View.GONE);
                        } else {
                            existingBackupsSection.setVisibility(View.VISIBLE);
                        }
                        final RemoteBackupsListAdapter remoteBackupsListAdapter =
                                new RemoteBackupsListAdapter(headerView, navigationHandler,
                                        backupProvidersManager, persistenceManager.getPreferenceManager(), networkManager, remoteBackupMetadatas);
                        recyclerView.setAdapter(remoteBackupsListAdapter);
                    })
            );
        }
    }

}
