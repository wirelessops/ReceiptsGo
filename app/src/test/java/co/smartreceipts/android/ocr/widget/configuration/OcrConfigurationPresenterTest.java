package co.smartreceipts.android.ocr.widget.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.Collections;

import co.smartreceipts.android.identity.store.EmailAddress;
import co.smartreceipts.android.purchases.model.AvailablePurchase;
import co.smartreceipts.android.purchases.model.InAppPurchase;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class OcrConfigurationPresenterTest {

    private static final boolean OCR_IS_ENABLED = false;
    private static final boolean SAVE_IMAGES_REMOTELY = true;
    private static final int REMAINING_SCANS = 25;
    private static final InAppPurchase PURCHASE = InAppPurchase.OcrScans10;

    @InjectMocks
    OcrConfigurationPresenter ocrConfigurationPresenter;

    @Mock
    OcrConfigurationView view;

    @Mock
    OcrConfigurationInteractor interactor;

    @Mock
    EmailAddress emailAddress;

    @Mock
    Consumer<Boolean> ocrIsEnabledConsumer;

    @Mock
    Consumer<Boolean> allowUsToSaveImagesRemotelyConsumer;

    @Mock
    AvailablePurchase availablePurchase;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(availablePurchase.getInAppPurchase()).thenReturn(PURCHASE);
        when(interactor.getEmail()).thenReturn(emailAddress);
        when(interactor.getOcrIsEnabled()).thenReturn(Observable.just(OCR_IS_ENABLED));
        when(interactor.getAllowUsToSaveImagesRemotely()).thenReturn(Observable.just(SAVE_IMAGES_REMOTELY));
        when(interactor.getRemainingScansStream()).thenReturn(Observable.just(REMAINING_SCANS));
        when(interactor.getAvailableOcrPurchases()).thenReturn(Single.just(Collections.singletonList(availablePurchase)));
        when(interactor.isUserLoggedIn()).thenReturn(true);

        when(view.getOcrIsEnabledCheckboxChanged()).thenReturn(Observable.just(OCR_IS_ENABLED));
        when(view.getAllowUsToSaveImagesRemotelyCheckboxChanged()).thenReturn(Observable.just(SAVE_IMAGES_REMOTELY));
        when(view.getAvailablePurchaseClicks()).thenReturn(Observable.just(availablePurchase));
        when(view.getDelayedPurchaseIdStream()).thenReturn(Observable.empty());
        doReturn(ocrIsEnabledConsumer).when(view).getOcrIsEnabledConsumer();
        doReturn(allowUsToSaveImagesRemotelyConsumer).when(view).getAllowUsToSaveImagesRemotelyConsumer();
    }

    @Test
    public void onResume() throws Exception {
        ocrConfigurationPresenter.subscribe();

        // Presents Email
        verify(view).present(emailAddress);

        // Consumes OCR Is Enabled State
        verify(ocrIsEnabledConsumer).accept(OCR_IS_ENABLED);

        // Consumes Save Images Remotely State
        verify(allowUsToSaveImagesRemotelyConsumer).accept(SAVE_IMAGES_REMOTELY);

        // Interacts With OCR Is Enabled on Check Changed
        verify(interactor).setOcrIsEnabled(OCR_IS_ENABLED);

        // Interacts With Save Images Remotely State on Check Changed
        verify(interactor).setAllowUsToSaveImagesRemotely(SAVE_IMAGES_REMOTELY);

        // Presents Remaining Scans
        verify(view).present(REMAINING_SCANS);

        // Presents Available purchases
        verify(view).present(Collections.singletonList(availablePurchase));

        // Interacts with purchase clicks
        verify(interactor).startOcrPurchase(availablePurchase.getInAppPurchase());
    }

}