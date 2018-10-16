package co.smartreceipts.android.identity.apis.organizations;

import co.smartreceipts.android.apis.SmartReceiptsRetrofitConverterFactory;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface OrganizationsService {

    @GET("api/organizations")
    @SmartReceiptsRetrofitConverterFactory.MoshiType
    Observable<OrganizationsResponse> organizations();

}
