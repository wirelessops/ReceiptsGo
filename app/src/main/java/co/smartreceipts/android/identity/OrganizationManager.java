package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Observable;

public class OrganizationManager {

    private final WebServiceManager webServiceManager;
    private final IdentityStore identityStore;
    private final UserPreferenceManager userPreferenceManager;
    private final ConfigurationManager configurationManager;

    public OrganizationManager(@NonNull WebServiceManager webServiceManager,
                               @NonNull IdentityStore identityStore,
                               @NonNull UserPreferenceManager userPreferenceManager,
                               @NonNull ConfigurationManager configurationManager) {
        this.webServiceManager = Preconditions.checkNotNull(webServiceManager);
        this.identityStore = Preconditions.checkNotNull(identityStore);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.configurationManager = Preconditions.checkNotNull(configurationManager);
    }

    @NonNull
    public Observable<OrganizationsResponse> getOrganizations() {
        if (configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)) {
            return getOrganizationsApiRequest()
                    .flatMap(organizationsResponse -> applyOrganizationsResponse(organizationsResponse)
                            .map(o -> organizationsResponse))
                    .doOnError(throwable -> {
                        Logger.error(this, "Failed to complete the organizations request", throwable);
                    });
        } else {
            return Observable.just(new OrganizationsResponse(null));
        }
    }

    @NonNull
    private Observable<OrganizationsResponse> getOrganizationsApiRequest() {
        if (identityStore.isLoggedIn()) {
            return webServiceManager.getService(OrganizationsService.class).organizations();
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's organizations until we're logged in"));
        }
    }

    @NonNull
    private Observable<Object> applyOrganizationsResponse(@NonNull final OrganizationsResponse response) {
        return getPrimaryOrganization(response)
                .flatMap(organization -> getOrganizationSettings(organization)
                        .flatMap(settings -> userPreferenceManager.getUserPreferencesObservable()
                                .flatMapIterable(userPreferences -> userPreferences)
                                .flatMap(userPreference -> apply(settings, userPreference))));
    }

    @NonNull
    private Observable<Organization> getPrimaryOrganization(@NonNull final OrganizationsResponse response) {
        return Observable.create(emitter -> {
            if (response.getOrganizations() != null && !response.getOrganizations().isEmpty()) {
                final Organization organization = response.getOrganizations().get(0);
                if (organization.getError() != null && organization.getError().hasError()) {
                    emitter.onError(new ApiValidationException(TextUtils.join(", ", organization.getError().getErrors())));
                } else {
                    emitter.onNext(organization);
                    emitter.onComplete();
                }
            } else {
                emitter.onComplete();
            }
        });
    }

    @NonNull
    private Observable<JsonObject> getOrganizationSettings(@NonNull final Organization organization) {
        return Observable.create(emitter -> {
            if (organization.getAppSettings() != null && organization.getAppSettings().getSettings() != null) {
                emitter.onNext(organization.getAppSettings().getSettings());
            }
            emitter.onComplete();
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Observable<T> apply(@NonNull JsonObject settings, @NonNull UserPreference<T> toPreference) {
        final String preferenceName = userPreferenceManager.name(toPreference);
        if (settings.has(preferenceName)) {
            final JsonElement element = settings.get(preferenceName);
            if (!element.isJsonNull()) {
                Logger.debug(OrganizationManager.this, "Giving preference \'{}\' a value of {}.", preferenceName, element);
                if (Boolean.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Boolean.valueOf(element.getAsBoolean()));
                } else if (String.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) element.getAsString());
                } else if (Float.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Float.valueOf(element.getAsFloat()));
                } else if (Integer.class.equals(toPreference.getType())) {
                    return userPreferenceManager.setObservable(toPreference, (T) Integer.valueOf(element.getAsInt()));
                } else {
                    return Observable.error(new Exception("Unsupported organization setting type for " + preferenceName));
                }
            } else {
                Logger.debug(OrganizationManager.this, "Skipping preference \'{}\', which is defined as null.", preferenceName, element);
                return Observable.empty();
            }
        } else {
            Logger.warn(OrganizationManager.this, "Failed to find preference: {}", preferenceName);
            return Observable.empty();
        }
    }

}
