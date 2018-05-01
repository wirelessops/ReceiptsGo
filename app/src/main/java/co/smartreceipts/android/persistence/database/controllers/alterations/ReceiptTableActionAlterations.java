package co.smartreceipts.android.persistence.database.controllers.alterations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.date.DateUtils;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.Trip;
import co.smartreceipts.android.model.factory.BuilderFactory1;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactoryFactory;
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata;
import co.smartreceipts.android.persistence.database.tables.ReceiptsTable;
import co.smartreceipts.android.receipts.helper.ReceiptCustomOrderIdHelper;
import co.smartreceipts.android.utils.FileUtils;
import co.smartreceipts.android.utils.UriUtils;
import co.smartreceipts.android.utils.log.Logger;
import io.reactivex.Single;
import wb.android.storage.StorageManager;

public class ReceiptTableActionAlterations extends StubTableActionAlterations<Receipt> {

    private final Context context;
    private final ReceiptsTable receiptsTable;
    private final StorageManager storageManager;
    private final BuilderFactory1<Receipt, ReceiptBuilderFactory> receiptBuilderFactoryFactory;

    public ReceiptTableActionAlterations(@NonNull Context context, @NonNull ReceiptsTable receiptsTable,
                                         @NonNull StorageManager storageManager) {
        this.context = Preconditions.checkNotNull(context);
        this.receiptsTable = Preconditions.checkNotNull(receiptsTable);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        receiptBuilderFactoryFactory = new ReceiptBuilderFactoryFactory();
    }

    ReceiptTableActionAlterations(@NonNull Context context, @NonNull ReceiptsTable receiptsTable,
                                  @NonNull StorageManager storageManager, @Nullable BuilderFactory1<Receipt, ReceiptBuilderFactory> receiptBuilderFactoryFactory) {
        this.context = Preconditions.checkNotNull(context);
        this.receiptsTable = Preconditions.checkNotNull(receiptsTable);
        this.storageManager = Preconditions.checkNotNull(storageManager);
        this.receiptBuilderFactoryFactory = Preconditions.checkNotNull(receiptBuilderFactoryFactory);
    }

    @NonNull
    @Override
    public Single<Receipt> preInsert(@NonNull final Receipt receipt) {
        return Single.fromCallable(() ->
                updateReceiptFileNameBlocking(receiptBuilderFactoryFactory.build(receipt).setIndex(getNextReceiptIndex(receipt)).build()));
    }

    @NonNull
    @Override
    public Single<Receipt> preUpdate(@NonNull final Receipt oldReceipt, @NonNull final Receipt newReceipt) {
        return Single.fromCallable(() -> {
            if (newReceipt.getFile() != null) {
                if (!newReceipt.getFile().equals(oldReceipt.getFile())) {
                    // If we changed the receipt file, replace the old file name
                    if (oldReceipt.getFile() != null) {
                        final ReceiptBuilderFactory factory = receiptBuilderFactoryFactory.build(newReceipt);
                        final String oldExtension = "." + UriUtils.getExtension(oldReceipt.getFile(), context);
                        final String newExtension = "." + UriUtils.getExtension(newReceipt.getFile(), context);
                        if (newExtension.equals(oldExtension)) {
                            Picasso.with(context).invalidate(oldReceipt.getFile());
                            if (newReceipt.getFile().renameTo(oldReceipt.getFile())) {
                                // Note: Keep 'oldReceipt' here, since File is immutable (and renamedTo doesn't change it)
                                factory.setFile(oldReceipt.getFile());
                            }
                        } else {
                            final String renamedNewFileName = oldReceipt.getFile().getName().replace(oldExtension, newExtension);
                            final String renamedNewFilePath = newReceipt.getFile().getAbsolutePath().replace(newReceipt.getFile().getName(), renamedNewFileName);
                            final File renamedNewFile = new File(renamedNewFilePath);
                            if (newReceipt.getFile().renameTo(renamedNewFile)) {
                                factory.setFile(renamedNewFile);
                            }
                        }
                        return factory.build();
                    } else {
                        return updateReceiptFileNameBlocking(newReceipt);
                    }
                } else if (newReceipt.getIndex() != oldReceipt.getIndex()) {
                    return updateReceiptFileNameBlocking(newReceipt);
                } else {
                    return newReceipt;
                }
            } else {
                return newReceipt;
            }
        });
    }

    @NonNull
    @Override
    public Single<Receipt> postUpdate(@NonNull final Receipt oldReceipt, @Nullable final Receipt newReceipt) {
        return Single.fromCallable(() -> {
            if (newReceipt == null) {
                throw new Exception("Post update failed due to a null receipt");
            }

            if (oldReceipt.getFile() != null) {
                // Delete old file if user removed or changed it
                if (newReceipt.getFile() == null  || !newReceipt.getFile().equals(oldReceipt.getFile())) {
                    Picasso.with(context).invalidate(oldReceipt.getFile());
                    storageManager.delete(oldReceipt.getFile());
                }
            }

            return newReceipt;
        });
    }

    @NonNull
    @Override
    public Single<Receipt> postDelete(@Nullable final Receipt receipt) {
        return Single.fromCallable(() -> {
            if (receipt == null) {
                throw new Exception("Post delete failed due to a null receipt");
            }

            if (receipt.getFile() != null) {
                storageManager.delete(receipt.getFile());
            }
            return receipt;
        });
    }

    @NonNull
    public Single<Receipt> preCopy(@NonNull final Receipt receipt, @NonNull final Trip toTrip) {
        return Single.fromCallable(() -> copyReceiptFileBlocking(receipt, toTrip));
    }

    public void postCopy(@NonNull Receipt oldReceipt, @Nullable Receipt newReceipt) throws Exception {
        // Intentional no-op
    }

    @NonNull
    public Single<Receipt> preMove(@NonNull final Receipt receipt, @NonNull final Trip toTrip) {
        // Move = Copy + Delete
        return preCopy(receipt, toTrip);
    }

    public void postMove(@NonNull Receipt oldReceipt, @Nullable Receipt newReceipt) throws Exception {
        if (newReceipt != null) { // i.e. - the move succeeded (delete the old data)
            Logger.info(this, "Completed the move procedure");
            if (receiptsTable.delete(oldReceipt, new DatabaseOperationMetadata()).blockingGet() != null) {
                if (oldReceipt.hasFile()) {
                    if (!storageManager.delete(oldReceipt.getFile())) {
                        Logger.error(this, "Failed to delete the moved receipt's file");
                    }
                }
            } else {
                Logger.error(this, "Failed to delete the moved receipt from the database for it's original trip");
            }
        }
    }

    @NonNull
    private Receipt updateReceiptFileNameBlocking(@NonNull Receipt receipt) {
        final ReceiptBuilderFactory builder = receiptBuilderFactoryFactory.build(receipt);
        final StringBuilder stringBuilder = new StringBuilder(receipt.getIndex() + "_");
        stringBuilder.append(FileUtils.omitIllegalCharactersFromFileName(receipt.getName().trim()));
        final File file = receipt.getFile();
        if (file != null) {
            final String extension = UriUtils.getExtension(file, context);
            stringBuilder.append('.').append(extension);
            final String newName = stringBuilder.toString();
            final File renamedFile = storageManager.getFile(receipt.getTrip().getDirectory(), newName);
            if (!renamedFile.exists()) {
                Logger.info(this, "Changing image name from: {} to: {}", file.getName(), newName);
                builder.setFile(storageManager.rename(file, newName)); // Returns oldFile on failure
            }
        }

        return builder.build();
    }

    @NonNull
    private Receipt copyReceiptFileBlocking(@NonNull Receipt receipt, @NonNull Trip toTrip) throws IOException {
        final ReceiptBuilderFactory builder = receiptBuilderFactoryFactory.build(receipt);
        builder.setTrip(toTrip);

        if (receipt.getCustomOrderId() != 0) {
            builder.setCustomOrderId(DateUtils.getDays(receipt.getDate()) * ReceiptCustomOrderIdHelper.DAYS_TO_ORDER_FACTOR +
                    ReceiptCustomOrderIdHelper.DAYS_TO_ORDER_FACTOR - 1);
        }

        if (receipt.hasFile()) {
            final File destination = storageManager.getFile(toTrip.getDirectory(), System.currentTimeMillis() + receipt.getFileName());
            if (storageManager.copy(receipt.getFile(), destination, true)) {
                Logger.info(this, "Successfully copied the receipt file to the new trip: {}", toTrip.getName());
                builder.setFile(destination);
            } else {
                throw new IOException("Failed to copy the receipt file to the new trip: " + toTrip.getName());
            }
        }

        builder.setIndex(getNextReceiptIndex(receipt));
        return updateReceiptFileNameBlocking(builder.build());
    }

    @NonNull
    private List<? extends Map.Entry<Receipt, Receipt>> swapDates(@NonNull Receipt receipt1, @NonNull Receipt receipt2, boolean isSwappingUp) {
        final ReceiptBuilderFactory builder1 = receiptBuilderFactoryFactory.build(receipt1);
        final ReceiptBuilderFactory builder2 = receiptBuilderFactoryFactory.build(receipt2);
        long dateShift = 0;
        if (receipt1.getDate().equals(receipt2.getDate())) {
            // We shift this way to avoid possible issues wrt sorting order if these are identical
            dateShift = isSwappingUp ? 1 : -1;
        }
        builder1.setDate(new Date(receipt2.getDate().getTime() + dateShift));
        builder1.setIndex(receipt2.getIndex());
        builder2.setDate(receipt1.getDate());
        builder2.setIndex(receipt1.getIndex());
        return Arrays.asList(new AbstractMap.SimpleImmutableEntry<>(receipt2, builder2.build()), new AbstractMap.SimpleImmutableEntry<>(receipt1, builder1.build()));
    }

    private int getNextReceiptIndex(@NonNull Receipt receipt) {
        return receiptsTable.get(receipt.getTrip()).blockingGet().size() + 1;
    }
}
