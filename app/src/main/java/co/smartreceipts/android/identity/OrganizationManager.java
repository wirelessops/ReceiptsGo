package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.di.scopes.ApplicationScope;
import co.smartreceipts.android.identity.apis.organizations.AppSettings.OrganizationSettings;
import co.smartreceipts.android.identity.apis.organizations.Organization;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponse;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.store.MutableIdentityStore;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import co.smartreceipts.android.utils.ConfigurableResourceFeature;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

@ApplicationScope
public class OrganizationManager {

    // TODO: 06.11.2018 add applying Categories, PaymentMethods and Columns

    // TODO: 12.11.2018 convert to kotlin

    private final WebServiceManager webServiceManager;
    private final MutableIdentityStore identityStore;
    private final UserPreferenceManager userPreferenceManager;
    private final ConfigurationManager configurationManager;

    @Inject
    public OrganizationManager(@NonNull WebServiceManager webServiceManager,
                               @NonNull MutableIdentityStore identityStore,
                               @NonNull UserPreferenceManager userPreferenceManager,
                               @NonNull ConfigurationManager configurationManager) {
        this.webServiceManager = Preconditions.checkNotNull(webServiceManager);
        this.identityStore = Preconditions.checkNotNull(identityStore);
        this.userPreferenceManager = Preconditions.checkNotNull(userPreferenceManager);
        this.configurationManager = Preconditions.checkNotNull(configurationManager);
    }

    @NonNull
    public Maybe<Organization> getPrimaryOrganization() {
        return getOrganizations()
                .filter(organizationsResponse -> !organizationsResponse.getOrganizations().isEmpty())
                .map(organizationsResponse -> organizationsResponse.getOrganizations().get(0))
                .flatMap(organization -> {
                    if (organization.getError().getHasError()) {
                        return Maybe.error(new ApiValidationException(TextUtils.join(", ", organization.getError().getErrors())));
                    } else {
                        return Maybe.just(organization);
                    }
                });
    }

    @NonNull
    private Maybe<OrganizationsResponse> getOrganizations() {
        if (configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)) {
            return getOrganizationsApiRequest()
                    .toMaybe()
                    .doOnError(throwable -> {
                        Logger.error(this, "Failed to complete the organizations request", throwable);
                    });
        } else {
            return Maybe.empty();
        }
    }

    @NonNull
    private Single<OrganizationsResponse> getOrganizationsApiRequest() {
        if (identityStore.isLoggedIn()) {
            return webServiceManager.getService(OrganizationsService.class).organizations().lastOrError();
        } else {
            return Single.error(new IllegalStateException("Cannot fetch the user's organizations until we're logged in"));
        }
    }

    public Completable applyOrganizationSettings(Organization organization) {

        return Observable.just(organization.getAppSettings().getSettings())
                .flatMapCompletable(organizationSettings -> userPreferenceManager.getUserPreferencesSingle()
                        .flatMapObservable(Observable::fromIterable)
                        .map(userPreference -> checkPreferenceMatch(organizationSettings, userPreference, true))
                        .ignoreElements());

    }

    /**
     * @return Single that emits {true} if organization settings match app settings, else emits {false}
     */
    public Single<Boolean> checkOrganizationSettingsMatch(Organization organization) {

        return Single.just(organization.getAppSettings().getSettings())
                .flatMap(organizationSettings -> userPreferenceManager.getUserPreferencesSingle()
                        .flatMapObservable(Observable::fromIterable)
                        .map(userPreference -> checkPreferenceMatch(organizationSettings, userPreference, false))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .contains(false))
                .map(someSettingsDontMach -> !someSettingsDontMach);

    }

    /**
     * @param settings     the organization settings
     * @param toPreference preference to check
     * @param apply        boolean value that indicates if organization preference value must be applied to app settings
     * @param <T>          the type of the preference value
     * @return Observable that emits {true} if app settings already contains same preference value, else emits {false}
     */
    @SuppressWarnings("unchecked")
    @VisibleForTesting
    <T> Optional<Boolean> checkPreferenceMatch(@NonNull OrganizationSettings settings, @NonNull UserPreference<T> toPreference, boolean apply) throws Exception {
        final String preferenceName = userPreferenceManager.name(toPreference);
        final JSONObject settingsObject = settings.getJsonObject();

        if (!settingsObject.has(preferenceName)) {
            Logger.warn(OrganizationManager.this, "Failed to find preference: {}", preferenceName);
            return Optional.absent();
        }

        if (settingsObject.isNull(preferenceName)) {
            Logger.debug(OrganizationManager.this, "Skipping preference \'{}\', which is defined as null.", preferenceName);
            return Optional.absent();
        }

        try {
            T preferenceValue;
            if (Boolean.class.equals(toPreference.getType())) {
                preferenceValue = (T) Boolean.valueOf(settingsObject.getBoolean(preferenceName));
            } else if (String.class.equals(toPreference.getType())) {
                preferenceValue = (T) settingsObject.getString(preferenceName);
            } else if (Float.class.equals(toPreference.getType())) {
                preferenceValue = (T) Float.valueOf(settingsObject.getString(preferenceName));
            } else if (Integer.class.equals(toPreference.getType())) {
                preferenceValue = (T) Integer.valueOf(settingsObject.getInt(preferenceName));
            } else {
                throw new Exception("Unsupported organization setting type for " + preferenceName);
            }

            Logger.debug(OrganizationManager.this, "Checking organization settings: app: \'{}\', organization: \'{}\'", preferenceName, preferenceValue);

            final boolean equals = userPreferenceManager.get(toPreference).equals(preferenceValue);
            if (!equals && apply) {
                Logger.debug(OrganizationManager.this, "Applying organization settings: set \'{}\' to \'{}\'", preferenceName, preferenceValue);
                userPreferenceManager.set(toPreference, preferenceValue);
            }

            return Optional.of(equals);

        } catch (JSONException e) {
            e.printStackTrace();
            throw new Exception("Unsupported organization setting type for " + preferenceName);
        }
    }

}
