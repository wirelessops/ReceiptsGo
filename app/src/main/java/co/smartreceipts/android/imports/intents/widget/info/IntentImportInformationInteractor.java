package co.smartreceipts.android.imports.intents.widget.info;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.common.base.Preconditions;

import javax.inject.Inject;

import co.smartreceipts.android.activities.SmartReceiptsActivity;
import co.smartreceipts.android.di.scopes.ActivityScope;
import co.smartreceipts.android.imports.intents.IntentImportProcessor;
import co.smartreceipts.android.imports.intents.model.IntentImportResult;
import co.smartreceipts.android.permissions.ActivityPermissionsRequester;
import co.smartreceipts.android.permissions.PermissionRequester;
import co.smartreceipts.android.permissions.PermissionStatusChecker;
import co.smartreceipts.android.permissions.exceptions.PermissionsNotGrantedException;
import co.smartreceipts.android.widget.model.UiIndicator;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

@ActivityScope
public class IntentImportInformationInteractor {

    private static final String INTENT_INFORMATION_SHOW = "co.smartreceipts.android.INTENT_CONSUMED";
    public static final String READ_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    private final IntentImportProcessor intentImportProcessor;
    private final PermissionStatusChecker permissionStatusChecker;
    private final PermissionRequester permissionRequester;

    @Inject
    public IntentImportInformationInteractor(@NonNull IntentImportProcessor intentImportProcessor,
                                             @NonNull PermissionStatusChecker permissionStatusChecker,
                                             @NonNull ActivityPermissionsRequester<SmartReceiptsActivity> permissionRequester) {
        this.intentImportProcessor = Preconditions.checkNotNull(intentImportProcessor);
        this.permissionStatusChecker = Preconditions.checkNotNull(permissionStatusChecker);
        this.permissionRequester = Preconditions.checkNotNull(permissionRequester);
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
                                    .subscribeOn(AndroidSchedulers.mainThread())
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
                    .doOnSuccess(ignored -> {
                        intent.putExtra(INTENT_INFORMATION_SHOW, true);
                    })
                    .toObservable()
                    .map(UiIndicator::success)
                    .startWith(UiIndicator.idle());
        }
    }

}
