package co.smartreceipts.android.di;

import android.content.Context;
import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class ImageLoadingModule {

    @Provides
    @ApplicationScope
    public static Picasso providePicasso(@NonNull Context context) {
        // We manually set this instance to avoid this bug
        // https://github.com/square/picasso/issues/1862
        Picasso.setSingletonInstance(new Picasso.Builder(context).build());
        return Picasso.get();
    }

}
