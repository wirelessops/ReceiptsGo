package co.smartreceipts.android.identity.apis.organizations;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface OrganizationsService {

    @GET("api/organizations")
    Observable<OrganizationsResponseKt> organizations();

}
