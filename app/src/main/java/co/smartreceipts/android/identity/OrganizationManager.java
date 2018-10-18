package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import org.json.JSONException;
import org.json.JSONObject;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationSettings;
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
        Logger.debug(this, "Got OrganizationsResponse: " + response.toString());
        return getPrimaryOrganization(response)
                .flatMap(organization -> getOrganizationSettings(organization)
                        .flatMap(settings -> userPreferenceManager.getUserPreferencesObservable()
                                .flatMapIterable(userPreferences -> userPreferences)
                                .flatMap(userPreference -> apply(settings, userPreference))));
        // TODO: 19.10.2018 gets several organizations.. as much as number of prefs were applied
    }

    @NonNull
    private Observable<Organization> getPrimaryOrganization(@NonNull final OrganizationsResponse response) {
        return Observable.create(emitter -> {
            if (!response.getOrganizations().isEmpty()) {
                final Organization organization = response.getOrganizations().get(0);
                if (organization.getError().getHasError()) {
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
    private Observable<OrganizationSettings> getOrganizationSettings(@NonNull final Organization organization) {
        return Observable.create(emitter -> {
            if (organization.getAppSettings() != null && organization.getAppSettings().getSettings() != null) {
                emitter.onNext(organization.getAppSettings().getSettings());
            }
            emitter.onComplete();
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Observable<T> apply(@NonNull OrganizationSettings settings, @NonNull UserPreference<T> toPreference) {
        Logger.debug(OrganizationManager.this, "Applying organization settings");
        final String preferenceName = userPreferenceManager.name(toPreference);
        final JSONObject settingsObject = settings.getJsonObject();

        if (!settingsObject.has(preferenceName)) {
            Logger.warn(OrganizationManager.this, "Failed to find preference: {}", preferenceName);
            return Observable.empty();
        }

        if (settingsObject.isNull(preferenceName)) {
            Logger.debug(OrganizationManager.this, "Skipping preference \'{}\', which is defined as null.", preferenceName);
            return Observable.empty();
        }

        try {
            T preferenceValue;
            if (Boolean.class.equals(toPreference.getType())) {
                preferenceValue = (T) Boolean.valueOf(settingsObject.getBoolean(preferenceName));
            } else if (String.class.equals(toPreference.getType())) {
                preferenceValue = (T) settingsObject.getString(preferenceName);
            } else if (Float.class.equals(toPreference.getType())) { // TODO: 19.10.2018 check float
                preferenceValue = (T) Float.valueOf(settingsObject.getString(preferenceName));
            } else if (Integer.class.equals(toPreference.getType())) {
                preferenceValue = (T) Integer.valueOf(settingsObject.getInt(preferenceName));
            } else {
                return Observable.error(new Exception("Unsupported organization setting type for " + preferenceName));
            }

            return userPreferenceManager.setObservable(toPreference, preferenceValue);

        } catch (JSONException e) {
            e.printStackTrace();
            return Observable.error(new Exception("Unsupported organization setting type for " + preferenceName));
        }
    }

}
