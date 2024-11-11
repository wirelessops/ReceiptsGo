package com.wops.receiptsgo.imports.intents.widget.info;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import com.wops.receiptsgo.activities.SmartReceiptsActivity;
import com.wops.receiptsgo.imports.intents.IntentImportProcessor;
import com.wops.receiptsgo.imports.intents.model.IntentImportResult;
import com.wops.receiptsgo.permissions.ActivityPermissionsRequester;
import com.wops.receiptsgo.permissions.PermissionRequester;
import com.wops.receiptsgo.permissions.PermissionStatusChecker;
import com.wops.receiptsgo.permissions.exceptions.PermissionsNotGrantedException;
import com.wops.receiptsgo.widget.model.UiIndicator;
import co.smartreceipts.core.di.scopes.ActivityScope;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;

@ActivityScope
public class IntentImportInformationInteractor {

    private static final String INTENT_INFORMATION_SHOW = "com.wops.receiptsgo.INTENT_CONSUMED";
    public static final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final IntentImportProcessor intentImportProcessor;
    private final PermissionStatusChecker permissionStatusChecker;
    private final PermissionRequester permissionRequester;
    private final Scheduler subscribeOnScheduler;

    @Inject
    public IntentImportInformationInteractor(@NonNull IntentImportProcessor intentImportProcessor,
                                             @NonNull PermissionStatusChecker permissionStatusChecker,
                                             @NonNull ActivityPermissionsRequester<SmartReceiptsActivity> permissionRequester) {
        this(intentImportProcessor, permissionStatusChecker, permissionRequester, AndroidSchedulers.mainThread());
    }

    @VisibleForTesting
    IntentImportInformationInteractor(@NonNull IntentImportProcessor intentImportProcessor,
                                      @NonNull PermissionStatusChecker permissionStatusChecker,
                                      @NonNull ActivityPermissionsRequester<SmartReceiptsActivity> permissionRequester,
                                      @NonNull Scheduler subscribeOnScheduler) {
        this.intentImportProcessor = Preconditions.checkNotNull(intentImportProcessor);
        this.permissionStatusChecker = Preconditions.checkNotNull(permissionStatusChecker);
        this.permissionRequester = Preconditions.checkNotNull(permissionRequester);
        this.subscribeOnScheduler = subscribeOnScheduler;
    }

    @NonNull
    public Observable<UiIndicator<IntentImportResult>> process(@NonNull final Intent intent) {
        if (intent.hasExtra(INTENT_INFORMATION_SHOW)) {
            return Observable.just(UiIndicator.idle());
        } else {
            return intentImportProcessor.process(intent)
                    .flatMap(intentImportResult -> {
                        if (intentImportResult.getUri().toString().startsWith(ContentResolver.SCHEME_CONTENT)) {
                            return Maybe.just(intentImportResult);
                        } else {
                            final IntentImportResult intentImportResultReference = intentImportResult;
                            return permissionStatusChecker.isPermissionGranted(READ_PERMISSION)
                                    .subscribeOn(subscribeOnScheduler)
                                    .flatMapMaybe(isGranted -> {
                                        if (isGranted) {
                                            return Maybe.just(intentImportResult);
                                        } else {
                                            return permissionRequester.request(READ_PERMISSION)
                                                    .flatMapMaybe(permissionAuthorizationResponse -> {
                                                        if (permissionAuthorizationResponse.wasGranted()) {
                                                            return Maybe.just(intentImportResultReference);
                                                        } else {
                                                            permissionRequester.markRequestConsumed(READ_PERMISSION);
                                                            return Maybe.error(new PermissionsNotGrantedException("User failed to grant READ permission", READ_PERMISSION));
                                                        }
                                                    });
                                        }
                                    });
                        }
                    })
                    .doOnSuccess(ignored -> intent.putExtra(INTENT_INFORMATION_SHOW, true))
                    .toObservable()
                    .map(UiIndicator::success)
                    .startWith(UiIndicator.idle());
        }
    }

}
