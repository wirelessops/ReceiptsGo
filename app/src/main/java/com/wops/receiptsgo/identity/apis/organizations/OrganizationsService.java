package com.wops.receiptsgo.identity.apis.organizations;

import com.wops.receiptsgo.apis.SmartReceiptsRetrofitConverterFactory;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface OrganizationsService {

    @GET("api/organizations")
    @SmartReceiptsRetrofitConverterFactory.MoshiType
    Observable<OrganizationsResponse> organizations();

    @PUT("api/organizations/{organization_id}")
    @SmartReceiptsRetrofitConverterFactory.MoshiType
    Observable<OrganizationsResponse> updateOrganization(@Path("organization_id") String id, @Body AppSettingsPutWrapper appSettings);


}
