package co.smartreceipts.android.utils;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Preconditions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import co.smartreceipts.core.utils.log.Logger;

public class IntentUtils {

    private static final String AUTHORITY_FORMAT = "%s.fileprovider";

    private IntentUtils() {

    }

    /**
     * All PDF Viewers that I tested don't work with File Providers yet, so this is our fallback way
     */
    @NonNull
    @Deprecated
    public static Intent getLegacyViewIntent(@NonNull Context context, @NonNull File file, @NonNull String fallbackMimeType) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(fallbackMimeType);

        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        final Uri uri = Uri.fromFile(file);

        viewIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());
        if (!TextUtils.isEmpty(mimeType)) {
            viewIntent.setDataAndType(uri, mimeType);
        } else {
            viewIntent.setDataAndType(uri, fallbackMimeType);
        }
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return viewIntent;
    }

    @NonNull
    public static Intent getViewIntent(@NonNull Context context, @NonNull File file, @NonNull String fallbackMimeType) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(fallbackMimeType);

        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        final String authority = String.format(Locale.US, AUTHORITY_FORMAT, context.getPackageName());
        final Uri uri = getUriFromFile(context, authority, file);

        viewIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());
        if (!TextUtils.isEmpty(mimeType)) {
            viewIntent.setDataAndType(uri, mimeType);
        } else {
            viewIntent.setDataAndType(uri, fallbackMimeType);
        }

        // Grant permissions - Despite lots of testing, we seem to need to grant permissions to everything :/
        grantReadPermissionsToUri(context, viewIntent, uri);
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        return viewIntent;
    }

    @NonNull
    public static Intent getSendIntent(@NonNull Context context, @NonNull File file) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(file);

        final Intent sentIntent = new Intent(Intent.ACTION_SEND);
        final String authority = String.format(Locale.US, AUTHORITY_FORMAT, context.getPackageName());
        final Uri uri = getUriFromFile(context, authority, file);

        final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());
        if (!TextUtils.isEmpty(mimeType)) {
            sentIntent.setType(mimeType);
        } else {
            sentIntent.setType("application/octet-stream");
        }
        sentIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return sentIntent;
    }

    @NonNull
    public static Intent getSendIntent(@NonNull Context context, @NonNull List<File> files) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(files);

        // need to use Intent.ACTION_SEND_MULTIPLE even if files.size == 1 because of Google Drive bug
        // see: https://stackoverflow.com/questions/35382474/google-drive-changes-file-name-to-intent-extra-subject-when-sharing-a-file-via-i
        final Intent sentIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

        if (files.size() == 1) {
            final String authority = String.format(Locale.US, AUTHORITY_FORMAT, context.getPackageName());
            final Uri uri = getUriFromFile(context, authority, files.get(0));
            final String mimeType = UriUtils.getMimeType(uri, context.getContentResolver());
            sentIntent.setType(!TextUtils.isEmpty(mimeType) ? mimeType : "application/octet-stream");
        } else {
            sentIntent.setType("application/octet-stream");
        }

        final String authority = String.format(Locale.US, AUTHORITY_FORMAT, context.getPackageName());
        final ArrayList<Uri> uris = new ArrayList<>();
        for (final File file : files) {
            final Uri uri = getUriFromFile(context, authority, file);
            uris.add(uri);
        }

        sentIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        sentIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return sentIntent;
    }

    public static Intent getImageCaptureIntent(@NonNull Context context, @NonNull File file) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(file);

        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final String authority = String.format(Locale.US, AUTHORITY_FORMAT, context.getPackageName());
        final Uri uri = getUriFromFile(context, authority, file);

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri(null, uri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        return intent;
    }

    /**
     * KitKat doesn't handle this gracefully, so we manually give permissions to all apps :/
     */
    private static void grantReadPermissionsToUri(@NonNull Context context, @NonNull Intent intent, @NonNull Uri uri) {
        Logger.debug(IntentUtils.class, "Granting read permissions to all potential apps for {}.", uri);
        final List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (final ResolveInfo resolveInfo : resInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    /**
     * KitKat doesn't handle this gracefully, so we manually give permissions to all apps :/
     */
    private static void grantReadWritePermissionsToUri(@NonNull Context context, @NonNull Intent intent, @NonNull Uri uri) {
        Logger.debug(IntentUtils.class, "Granting read/write permissions to all potential apps for {}.", uri);
        final List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (final ResolveInfo resolveInfo : resInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
    }

    @NonNull
    private static Uri getUriFromFile(@NonNull Context context, @NonNull String authority, @NonNull File file) {
        return ContentUriProvider.getUriForFile(context, authority, file);
    }

    public static Intent getRatingIntent(Context context) {
        String GOOGLE = "com.android.vending";
        String AMAZON = "com.amazon.venezia";

        String packageName = context.getPackageName();
        String name = context.getPackageManager().getInstallerPackageName(packageName);

        if (GOOGLE.equals(name)) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        } else if (AMAZON.equals(name)) {
            return new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=" + packageName));
        } else {
            // Default to Google... May lead to a crash if user does not have this installed
            return new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
        }
    }

}
