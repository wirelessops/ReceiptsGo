package com.wops.core.identity.apis.me;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class User implements Serializable {

    private String id;
    private String email;

    private String name;
    private String display_name;
    private List<String> registration_ids;
    private String cognito_token;
    private String identity_id;
    private Date cognito_token_expires_at;
    private Date cognito_token_expires_at_iso8601;
    private int recognitions_available;

    public User(@NonNull List<String> registrationIds) {
        this.registration_ids = Preconditions.checkNotNull(registrationIds);
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getDisplayName() {
        return display_name;
    }

    @Nullable
    public List<String> getRegistrationIds() {
        return registration_ids;
    }

    @Nullable
    public String getCognitoToken() {
        return cognito_token;
    }

    @Nullable
    public String getIdentityId() {
        return identity_id;
    }

    @Nullable
    public Date getCognitoTokenExpiresAt() {
        // Supports a fallback until we upgrade our service to avoid this entirely
        if (cognito_token_expires_at != null) {
            return cognito_token_expires_at;
        } else {
            return cognito_token_expires_at_iso8601;
        }
    }

    public int getRecognitionsAvailable() {
        return recognitions_available;
    }
}
