package co.smartreceipts.android.identity.apis.organizations;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.List;

public class Organization implements Serializable {

    private String id;
    private String name;
    private long createdAt;
    private AppSettings appSettings;
    private List<OrganizationUser> organizationUsers;
    private Error error;

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    @Nullable
    public AppSettings getAppSettings() {
        return appSettings;
    }

    @Nullable
    public List<OrganizationUser> getOrganizationUsers() {
        return organizationUsers;
    }

    @Nullable
    public Error getError() {
        return error;
    }
}
