package co.smartreceipts.android.identity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Preconditions;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import co.smartreceipts.android.apis.ApiValidationException;
import co.smartreceipts.android.apis.WebServiceManager;
import co.smartreceipts.android.config.ConfigurationManager;
import co.smartreceipts.android.identity.apis.organizations.OrganizationKt;
import co.smartreceipts.android.identity.apis.organizations.OrganizationSettingsKt;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsResponseKt;
import co.smartreceipts.android.identity.apis.organizations.OrganizationsService;
import co.smartreceipts.android.identity.apis.organizations.adapters.CategoryJsonAdapter;
import co.smartreceipts.android.identity.store.IdentityStore;
import co.smartreceipts.android.settings.UserPreferenceManager;
import co.smartreceipts.android.settings.catalog.UserPreference;
import io.reactivex.Observable;

public class OrganizationManager {

    // TODO: 05.10.2018 using for testing
    public static final String JSON = "{\n" +
                    "  \"organizations\": [\n" +
                    "    {\n" +
                    "      \"id\": \"1853766902\",\n" +
                    "      \"name\": \"Test\",\n" +
                    "      \"created_at\": 1525724863,\n" +
                    "      \"app_settings\": {\n" +
                    "        \"Configurations\": {\n" +
                    "          \"IsSettingsEnable\": null\n" +
                    "        },\n" +
                    "        \"Settings\": {\n" +
                    "          \"TripDuration\": null,\n" +
                    "          \"isocurr\": \"AED\",\n" +
                    "          \"dateseparator\": \"-\",\n" +
                    "          \"trackcostcenter\": null,\n" +
                    "          \"PredictCats\": null,\n" +
                    "          \"MatchNameCats\": null,\n" +
                    "          \"MatchCommentCats\": null,\n" +
                    "          \"OnlyIncludeExpensable\": null,\n" +
                    "          \"ExpensableDefault\": null,\n" +
                    "          \"IncludeTaxField\": null,\n" +
                    "          \"TaxPercentage\": null,\n" +
                    "          \"PreTax\": null,\n" +
                    "          \"EnableAutoCompleteSuggestions\": null,\n" +
                    "          \"MinReceiptPrice\": null,\n" +
                    "          \"DefaultToFirstReportDate\": null,\n" +
                    "          \"ShowReceiptID\": null,\n" +
                    "          \"UseFullPage\": null,\n" +
                    "          \"UsePaymentMethods\": null,\n" +
                    "          \"IncludeCSVHeaders\": null,\n" +
                    "          \"PrintByIDPhotoKey\": null,\n" +
                    "          \"PrintCommentByPhoto\": null,\n" +
                    "          \"EmailTo\": \"\",\n" +
                    "          \"EmailCC\": \"\",\n" +
                    "          \"EmailBCC\": \"\",\n" +
                    "          \"EmailSubject\": \"\",\n" +
                    "          \"SaveBW\": null,\n" +
                    "          \"LayoutIncludeReceiptDate\": null,\n" +
                    "          \"LayoutIncludeReceiptCategory\": null,\n" +
                    "          \"LayoutIncludeReceiptPicture\": null,\n" +
                    "          \"MileageTotalInReport\": null,\n" +
                    "          \"MileageRate\": null,\n" +
                    "          \"MileagePrintTable\": null,\n" +
                    "          \"MileageAddToPDF\": null,\n" +
                    "          \"PdfFooterString\": null\n" +
                    "        },\n" +
                    "        \"Categories\": [\n" +
                    "          {\n" +
                    "            \"uuid\": \"b00ffd0d-ba7d-4285-a2ee-c4c8930cc486\",\n" +
                    "            \"Name\": \"Name1\",\n" +
                    "            \"Code\": \"Code1\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"uuid\": \"6192fe0d-5967-46b3-a8e2-5b5147e6c3cc\",\n" +
                    "            \"Name\": \"Name2\",\n" +
                    "            \"Code\": \"Code2\"\n" +
                    "          }\n" +
                    "        ],\n" +
                    "        \"PaymentMethods\": [\n" +
                    "          {\n" +
                    "            \"uuid\": \"bcac7cda-7177-40ee-abb5-6e01604f9952\",\n" +
                    "            \"Code\": \"Method1\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"uuid\": \"3a07357f-af6f-4cac-b26f-21259c0e8ec2\",\n" +
                    "            \"Code\": \"Method2\"\n" +
                    "          }\n" +
                    "        ],\n" +
                    "        \"CSVColumns\": [\n" +
                    "          {\n" +
                    "            \"uuid\": \"890fcb47-b54e-4917-ba41-ff2277d64eda\",\n" +
                    "            \"Code\": \"Test\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"uuid\": \"726b4128-5475-47d0-ab3b-df02358b51ce\",\n" +
                    "            \"Code\": \"Test1\"\n" +
                    "          }\n" +
                    "        ],\n" +
                    "        \"PDFColumns\": [\n" +
                    "          {\n" +
                    "            \"uuid\": \"5d94a56c-6dec-4ae6-9ad7-26bcd0d8f64d\",\n" +
                    "            \"Code\": \"Test1\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"uuid\": \"03c608d5-6309-4788-95c2-4f71daa0a02f\",\n" +
                    "            \"Code\": \"Test2\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"error\": {\n" +
                    "        \"has_error\": false,\n" +
                    "        \"errors\": []\n" +
                    "      },\n" +
                    "      \"organization_users\": [\n" +
                    "        {\n" +
                    "          \"id\": 12,\n" +
                    "          \"user_id\": 1,\n" +
                    "          \"organization_id\": 8,\n" +
                    "          \"role\": 1,\n" +
                    "          \"created_at\": \"2018-05-07T20:27:45.948Z\",\n" +
                    "          \"updated_at\": \"2018-05-07T20:27:45.948Z\"\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
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

//    @NonNull
//    public Observable<OrganizationsResponse> getOrganizations() {
//        if (configurationManager.isEnabled(ConfigurableResourceFeature.OrganizationSyncing)) {
//            return getOrganizationsApiRequest()
//                    .flatMap(organizationsResponse -> applyOrganizationsResponse(organizationsResponse)
//                            .map(o -> organizationsResponse))
//                    .doOnError(throwable -> {
//                        Logger.error(this, "Failed to complete the organizations request", throwable);
//                    });
//        } else {
//            return Observable.just(new OrganizationsResponse(null));
//        }
//    }

    // TODO: 14.08.2018 test

    /**
     * Mock
     *
     * @return
     */
    @NonNull
    public Observable<OrganizationsResponseKt> getOrganizations() {
        return Observable.fromCallable(() -> {


            final JsonAdapter<OrganizationsResponseKt> responseAdapter = new Moshi.Builder()
                    .add(new CategoryJsonAdapter())
//                    .add(categoryListAdapter)

//                .add(new  KotlinJsonAdapterFactory)
//                .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                    .build()
                    .adapter(OrganizationsResponseKt.class);

            final OrganizationsResponseKt responseModel = responseAdapter.fromJson(JSON);

            return responseModel;
        });
    }


    @NonNull
    private Observable<OrganizationsResponseKt> getOrganizationsApiRequest() {
        if (identityStore.isLoggedIn()) {
            return webServiceManager.getService(OrganizationsService.class).organizations();
        } else {
            return Observable.error(new IllegalStateException("Cannot fetch the user's organizations until we're logged in"));
        }
    }

    @NonNull
    private Observable<Object> applyOrganizationsResponse(@NonNull final OrganizationsResponseKt response) {
        return getPrimaryOrganization(response)
                .flatMap(organization -> getOrganizationSettings(organization)
                        .flatMap(settings -> userPreferenceManager.getUserPreferencesObservable()
                                .flatMapIterable(userPreferences -> userPreferences)
                                .flatMap(userPreference -> apply(settings, userPreference))));
    }

    @NonNull
    private Observable<OrganizationKt> getPrimaryOrganization(@NonNull final OrganizationsResponseKt response) {
        return Observable.create(emitter -> {
            if (!response.getOrganizations().isEmpty()) {
                final OrganizationKt organization = response.getOrganizations().get(0);
                //todo 05.10.18 organization.getError() nullable?
                if (organization.getError() != null && organization.getError().getHasError()) {
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
    private Observable<OrganizationSettingsKt> getOrganizationSettings(@NonNull final OrganizationKt organization) {
        return Observable.create(emitter -> {
            if (organization.getAppSettings() != null && organization.getAppSettings().getSettings() != null) {
                emitter.onNext(organization.getAppSettings().getSettings());
            }
            emitter.onComplete();
        });
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Observable<T> apply(@NonNull OrganizationSettingsKt settings, @NonNull UserPreference<T> toPreference) {
        final String preferenceName = userPreferenceManager.name(toPreference);
        // TODO: 17.08.2018 settings were JsonObject. fix it
//        if (settings.has(preferenceName)) {
//            final JsonElement element = settings.get(preferenceName);
//            if (!element.isJsonNull()) {
//                Logger.debug(OrganizationManager.this, "Giving preference \'{}\' a value of {}.", preferenceName, element);
//                if (Boolean.class.equals(toPreference.getType())) {
//                    return userPreferenceManager.setObservable(toPreference, (T) Boolean.valueOf(element.getAsBoolean()));
//                } else if (String.class.equals(toPreference.getType())) {
//                    return userPreferenceManager.setObservable(toPreference, (T) element.getAsString());
//                } else if (Float.class.equals(toPreference.getType())) {
//                    return userPreferenceManager.setObservable(toPreference, (T) Float.valueOf(element.getAsFloat()));
//                } else if (Integer.class.equals(toPreference.getType())) {
//                    return userPreferenceManager.setObservable(toPreference, (T) Integer.valueOf(element.getAsInt()));
//                } else {
//                    return Observable.error(new Exception("Unsupported organization setting type for " + preferenceName));
//                }
//            } else {
//                Logger.debug(OrganizationManager.this, "Skipping preference \'{}\', which is defined as null.", preferenceName, element);
//                return Observable.empty();
//            }
//        } else {
//            Logger.warn(OrganizationManager.this, "Failed to find preference: {}", preferenceName);
            return Observable.empty();
//        }
    }

}
