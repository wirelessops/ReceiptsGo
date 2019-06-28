package co.smartreceipts.android.identity.apis.login;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import java.io.Serializable;

public abstract class UserCredentialsPayload implements Serializable, Parcelable {

    private final String type;
    private final String email;
    private final String password;
    private final String token;
    private final transient LoginType loginType;

    protected UserCredentialsPayload(@Nullable String type, @Nullable String email, @Nullable String password, @Nullable String token,
                                     @NonNull LoginType loginType) {
        this.type = type;
        this.email = email;
        this.password = password;
        this.token = token;
        this.loginType = Preconditions.checkNotNull(loginType);
    }

    protected UserCredentialsPayload(Parcel in) {
        this(in.readString(), in.readString(), in.readString(), in.readString(), (LoginType) in.readSerializable());
    }

    @Nullable
    public String getTypeString() {
        return type;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @NonNull
    public LoginType getLoginType() {
        return loginType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserCredentialsPayload)) return false;

        UserCredentialsPayload that = (UserCredentialsPayload) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (email != null ? !email.equals(that.email) : that.email != null) return false;
        if (password != null ? !password.equals(that.password) : that.password != null)
            return false;
        if (token != null ? !token.equals(that.token) : that.token != null) return false;
        return loginType == that.loginType;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (loginType != null ? loginType.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.email);
        dest.writeString(this.password);
        dest.writeString(this.token);
        dest.writeSerializable(this.loginType);
    }

}
