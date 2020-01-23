package co.smartreceipts.core.identity.apis.login;

import android.os.Parcel;
import androidx.annotation.NonNull;

public class SmartReceiptsUserSignUp extends UserCredentialsPayload {

    public SmartReceiptsUserSignUp(@NonNull CharSequence email, @NonNull CharSequence password) {
        this(email.toString(), password.toString());
    }

    public SmartReceiptsUserSignUp(@NonNull String email, @NonNull String password) {
        super(null, email, password, null, LoginType.SignUp);
    }

    private SmartReceiptsUserSignUp(Parcel in) {
        super(in);
    }

    public static final Creator<SmartReceiptsUserSignUp> CREATOR = new Creator<SmartReceiptsUserSignUp>() {
        @Override
        public SmartReceiptsUserSignUp createFromParcel(Parcel source) {
            return new SmartReceiptsUserSignUp(source);
        }

        @Override
        public SmartReceiptsUserSignUp[] newArray(int size) {
            return new SmartReceiptsUserSignUp[size];
        }
    };
}
