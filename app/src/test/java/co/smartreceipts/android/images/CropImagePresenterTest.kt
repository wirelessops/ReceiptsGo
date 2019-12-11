package co.smartreceipts.android.images

import android.graphics.Bitmap
import co.smartreceipts.android.widget.model.UiIndicator
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.File



class CropImagePresenterTest {

    private lateinit var presenter: CropImagePresenter

    private val view = mock<CropView>()
    private val interactor = mock<CropImageInteractor>()

    private val imageFile = mock<File>()
    private val bitmap = mock<Bitmap>()
    private val bitmapUpdated = mock<Bitmap>()

    @Before
    fun setUp() {
        whenever(view.getApplyCropClicks()).thenReturn(Observable.never())
        whenever(view.cropToggleClicks).thenReturn(Observable.never())
        whenever(view.rotateLeftClicks).thenReturn(Observable.never())
        whenever(view.rotateRightClicks).thenReturn(Observable.never())

        whenever(view.imageFile).thenReturn(imageFile)
        whenever(interactor.getImage(imageFile)).thenReturn(Observable.just(bitmap))
        whenever(interactor.rotateImage(eq(imageFile), any())).thenReturn(Single.just(bitmapUpdated))
        whenever(interactor.updateImage(imageFile, bitmapUpdated)).thenReturn(Completable.complete())

        presenter = CropImagePresenter(view, interactor)
    }

    @Test
    fun presentImageTest() {
        presenter.subscribe()

        verify(interactor).getImage(imageFile)
        verifyNoMoreInteractions(interactor)
        verify(view).present(UiIndicator.loading())
        verify(view).present(UiIndicator.success(bitmap))
    }

    @Test
    fun presentImageErrorTest() {
        whenever(interactor.getImage(imageFile)).thenReturn(Observable.error(Exception()))

        presenter.subscribe()

        verify(interactor).getImage(imageFile)
        verifyNoMoreInteractions(interactor)
        verify(view).present(UiIndicator.loading())
        verify(view).present(UiIndicator.error())
    }

    @Test
    fun cropToggleClicksTest() {
        whenever(view.cropToggleClicks).thenReturn(Observable.just(Unit, Unit, Unit))

        presenter.subscribe()

        verify(interactor).getImage(imageFile)
        verifyNoMoreInteractions(interactor)
        verify(view, times(3)).toggleCropMode()
    }

    @Test
    fun rotateRightClickTest() {
        val subject = PublishSubject.create<Unit>()
        whenever(view.rotateRightClicks).thenReturn(subject)

        presenter.subscribe()

        subject.onNext(Unit)

        val orderVerifier = Mockito.inOrder(view, interactor)
        orderVerifier.verify(interactor).getImage(imageFile)
        orderVerifier.verify(view).present(UiIndicator.loading())
        orderVerifier.verify(view).present(UiIndicator.success(bitmap))
        orderVerifier.verify(view).present(UiIndicator.loading())
        orderVerifier.verify(interactor).rotateImage(imageFile, true)
        orderVerifier.verify(interactor).updateImage(imageFile, bitmapUpdated)
        orderVerifier.verify(view).present(UiIndicator.success(bitmapUpdated))
    }

    @Test
    fun rotateLeftClickTest() {
        val subject = PublishSubject.create<Unit>()
        whenever(view.rotateLeftClicks).thenReturn(subject)

        presenter.subscribe()

        subject.onNext(Unit)

        val orderVerifier = Mockito.inOrder(view, interactor)
        orderVerifier.verify(interactor).getImage(imageFile)
        orderVerifier.verify(view).present(UiIndicator.loading())
        orderVerifier.verify(view).present(UiIndicator.success(bitmap))
        orderVerifier.verify(view).present(UiIndicator.loading())
        orderVerifier.verify(interactor).rotateImage(imageFile, false)
        orderVerifier.verify(interactor).updateImage(imageFile, bitmapUpdated)
        orderVerifier.verify(view).present(UiIndicator.success(bitmapUpdated))
    }

    @Test
    fun applyCropTest() {
        whenever(view.getApplyCropClicks()).thenReturn(Observable.just(bitmapUpdated))

        presenter.subscribe()

        verify(interactor).updateImage(imageFile, bitmapUpdated)
        verify(view, times(2)).present(UiIndicator.loading())
        verify(view).present(UiIndicator.success(bitmap))
        verify(view).finishCropView(true)
    }

    @Test
    fun applyCropErrorTest() {
        whenever(view.getApplyCropClicks()).thenReturn(Observable.just(bitmapUpdated))
        whenever(interactor.updateImage(any(), any())).thenReturn(Completable.error(Exception("error")))

        presenter.subscribe()

        verify(interactor).updateImage(imageFile, bitmapUpdated)
        verify(view, times(2)).present(UiIndicator.loading())
        verify(view).present(UiIndicator.success(bitmap))
        verify(view).present(UiIndicator.error())
        verify(view).finishCropView(false)
    }
}