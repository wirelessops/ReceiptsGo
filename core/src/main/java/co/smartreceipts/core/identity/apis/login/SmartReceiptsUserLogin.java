package co.smartreceipts.core.identity.apis.login;

import android.os.Parcel;
import androidx.annotation.NonNull;

public class SmartReceiptsUserLogin extends UserCredentialsPayload {

    public SmartReceiptsUserLogin(@NonNull CharSequence email, @NonNull CharSequence password) {
        this(email.toString(), password.toString());
    }

    public SmartReceiptsUserLogin(@NonNull String email, @NonNull String password) {
        super("login", email, password, null, LoginType.LogIn);
    }

    private SmartReceiptsUserLogin(Parcel in) {
        super(in);
    }

    public static final Creator<SmartReceiptsUserLogin> CREATOR = new Creator<SmartReceiptsUserLogin>() {
        @Override
        public SmartReceiptsUserLogin createFromParcel(Parcel source) {
            return new SmartReceiptsUserLogin(source);
        }

        @Override
        public SmartReceiptsUserLogin[] newArray(int size) {
            return new SmartReceiptsUserLogin[size];
        }
    };
}
