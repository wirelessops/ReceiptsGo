package com.wops.receiptsgo.sync.widget.backups;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import javax.inject.Inject;

import com.wops.analytics.log.Logger;
import com.wops.receiptsgo.BuildConfig;
import com.wops.receiptsgo.R;
import com.wops.receiptsgo.activities.NavigationHandler;
import com.wops.receiptsgo.config.ConfigurationManager;
import com.wops.receiptsgo.databinding.BackupsFragmentBinding;
import com.wops.receiptsgo.databinding.SimpleRecyclerViewBinding;
import com.wops.receiptsgo.fragments.SelectAutomaticBackupProviderDialogFragment;
import com.wops.receiptsgo.fragments.WBFragment;
import com.wops.receiptsgo.imports.intents.widget.info.IntentImportInformationPresenter;
import com.wops.receiptsgo.persistence.PersistenceManager;
import com.wops.receiptsgo.purchases.PurchaseManager;
import com.wops.receiptsgo.purchases.model.InAppPurchase;
import com.wops.receiptsgo.purchases.source.PurchaseSource;
import com.wops.receiptsgo.purchases.wallet.PurchaseWallet;
import com.wops.receiptsgo.settings.catalog.UserPreference;
import com.wops.receiptsgo.sync.BackupProviderChangeListener;
import com.wops.receiptsgo.sync.BackupProvidersManager;
import com.wops.receiptsgo.sync.network.NetworkManager;
import com.wops.receiptsgo.sync.network.SupportedNetworkType;
import com.wops.receiptsgo.utils.ConfigurableResourceFeature;
import com.wops.core.sync.provider.SyncProvider;
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
    @Inject
    IntentImportInformationPresenter intentImportInformationPresenter;
    @Inject
    ConfigurationManager configurationManager;

    private RemoteBackupsDataCache remoteBackupsDataCache;
    private CompositeDisposable compositeDisposable;

    private Toolbar toolbar;
    private ImageView backupConfigButtonImage;
    private TextView backupConfigButtonText;
    private TextView warningTextView;
    private CheckBox wifiOnlyCheckbox;
    private View existingBackupsSection;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private BackupsFragmentBinding binding;
    private SimpleRecyclerViewBinding rootBinding;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootBinding = SimpleRecyclerViewBinding.inflate(inflater, container, false);
        recyclerView = rootBinding.list;

        binding = BackupsFragmentBinding.inflate(inflater, container, false);

        warningTextView = binding.autoBackupWarning;
        backupConfigButtonImage = binding.automaticBackupConfigButtonImage;
        backupConfigButtonText = binding.automaticBackupConfigButtonText;
        wifiOnlyCheckbox = binding.autoBackupWifiOnly;
        existingBackupsSection = binding.existingBackupsSection;
        progressBar = binding.backupsProgressBar;

        View exportButton = binding.manualBackupExport;
        View importButton = binding.manualBackupImport;
        View backupConfigButton = binding.automaticBackupConfigButton;

        // hide google drive backups section for FLOSS flavor
        if (BuildConfig.FLAVOR.equals("flossFlavor")) {
            binding.autoBackupTitle.setVisibility(View.GONE);
            warningTextView.setVisibility(View.GONE);
            backupConfigButton.setVisibility(View.GONE);
            wifiOnlyCheckbox.setVisibility(View.GONE);
        }
        exportButton.setOnClickListener(view -> navigationHandler.showDialog(new ExportBackupDialogFragment()));
        importButton.setOnClickListener(view -> {
            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.import_string)), IMPORT_SMR_REQUEST_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getContext(), getString(R.string.error_no_file_intent_dialog_title), Toast.LENGTH_SHORT).show();
            }
        });
        backupConfigButton.setOnClickListener(view -> {
            if (backupProvidersManager.getSyncProvider() == SyncProvider.None
                    && !purchaseWallet.hasActivePurchase(InAppPurchase.SmartReceiptsPlus)
                    && !purchaseWallet.hasActivePurchase(InAppPurchase.PremiumSubscriptionPlan)) {

                InAppPurchase proPurchase = configurationManager.isEnabled(ConfigurableResourceFeature.SubscriptionModel) ?
                        InAppPurchase.PremiumSubscriptionPlan : InAppPurchase.SmartReceiptsPlus;

                purchaseManager.initiatePurchase(proPurchase, PurchaseSource.AutomaticBackups);
            } else {
                navigationHandler.showDialog(new SelectAutomaticBackupProviderDialogFragment());
            }
        });
        wifiOnlyCheckbox.setChecked(persistenceManager.getPreferenceManager().get(UserPreference.Misc.AutoBackupOnWifiOnly));
        wifiOnlyCheckbox.setOnCheckedChangeListener((compoundButton, checked) -> {
            persistenceManager.getPreferenceManager().set(UserPreference.Misc.AutoBackupOnWifiOnly, checked);
            backupProvidersManager.setAndInitializeNetworkProviderType(checked ? SupportedNetworkType.WifiOnly : SupportedNetworkType.AllNetworks);
        });
        return rootBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.setAdapter(new RemoteBackupsListAdapter(binding.getRoot(), navigationHandler,
                backupProvidersManager, persistenceManager.getPreferenceManager(), networkManager));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Toolbar toolbar;
        if (navigationHandler.isDualPane()) {
            toolbar = getActivity().findViewById(R.id.toolbar);
            rootBinding.toolbar.toolbar.setVisibility(View.GONE);
        } else {
            toolbar = rootBinding.toolbar.toolbar;
        }
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
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
        if (data != null) {
            data.setAction(Intent.ACTION_VIEW);
            getActivity().setIntent(data);
            intentImportInformationPresenter.subscribe();
        }
    }

    @Override
    public void onDestroy() {
        Logger.info(this, "onDestroy");
        intentImportInformationPresenter.unsubscribe();
        super.onDestroy();
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

    void updateViewsForProvider(@NonNull SyncProvider syncProvider) {
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
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(new RemoteBackupsListAdapter(binding.getRoot(), navigationHandler,
                        backupProvidersManager, persistenceManager.getPreferenceManager(), networkManager));
            } else {
                throw new IllegalArgumentException("Unsupported sync provider type was specified");
            }

            compositeDisposable.add(remoteBackupsDataCache.getBackups(syncProvider)
                    .subscribe(remoteBackupMetadatas -> {
                        if (remoteBackupMetadatas.isEmpty()) {
                            existingBackupsSection.setVisibility(View.GONE);
                        } else {
                            existingBackupsSection.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                        final RemoteBackupsListAdapter remoteBackupsListAdapter =
                                new RemoteBackupsListAdapter(binding.getRoot(), navigationHandler,
                                        backupProvidersManager, persistenceManager.getPreferenceManager(), networkManager, remoteBackupMetadatas);
                        recyclerView.setAdapter(remoteBackupsListAdapter);
                    })
            );
        }
    }

}
