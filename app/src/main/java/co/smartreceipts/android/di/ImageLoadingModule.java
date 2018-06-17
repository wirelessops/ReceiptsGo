package co.smartreceipts.android.di;

import com.squareup.picasso.Picasso;

import co.smartreceipts.android.di.scopes.ApplicationScope;
import dagger.Module;
import dagger.Provides;

@Module
public class ImageLoadingModule {

    @Provides
    @ApplicationScope
    public static Picasso providePicasso() {
        return Picasso.get();
    }

}
