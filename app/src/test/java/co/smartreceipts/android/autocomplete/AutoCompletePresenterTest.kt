package co.smartreceipts.android.autocomplete

import co.smartreceipts.android.editor.Editor
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.junit.Before

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AutoCompletePresenterTest {

    companion object {
        private val FIELD_1_RESULTS = listOf(AutoCompleteResult("1a", Any()), AutoCompleteResult("1b", Any()))
        private val FIELD_2_RESULTS = listOf(AutoCompleteResult("2a", Any()), AutoCompleteResult("2b", Any()))
    }

    // Class under test
    private lateinit var presenter: AutoCompletePresenter<Any>

    @Mock
    private lateinit var view: AutoCompleteView<Any>

    @Mock
    private lateinit var editor: Editor<Any>

    @Mock
    private lateinit var interactor: AutoCompleteInteractor<Any>

    @Mock
    private lateinit var field1: AutoCompleteField

    @Mock
    private lateinit var field2: AutoCompleteField

    private val field1TextChanges: PublishSubject<CharSequence> = PublishSubject.create()

    private val field2TextChanges: PublishSubject<CharSequence> = PublishSubject.create()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        whenever(interactor.supportedAutoCompleteFields).thenReturn(listOf(field1, field2))
        whenever(view.getTextChangeStream(field1)).thenReturn(field1TextChanges)
        whenever(view.getTextChangeStream(field2)).thenReturn(field2TextChanges)
        whenever(interactor.getAutoCompleteResults(eq(field1), any())).thenReturn(Maybe.just(FIELD_1_RESULTS))
        whenever(interactor.getAutoCompleteResults(eq(field2), any())).thenReturn(Maybe.just(FIELD_2_RESULTS))
        presenter = AutoCompletePresenter(view, interactor, Schedulers.trampoline())
    }

    @Test
    fun subscribeWhenEditing() {
        whenever(editor.editableItem).thenReturn(Any())
        presenter.subscribe()
        field1TextChanges.onNext("1")
        verify(view).displayAutoCompleteResults(field1, FIELD_1_RESULTS)
        field2TextChanges.onNext("2")
        verify(view).displayAutoCompleteResults(field2, FIELD_2_RESULTS)
    }

    @Test
    fun subscribeWhenNotEditing() {
        whenever(editor.editableItem).thenReturn(null)
        presenter.subscribe()
        field1TextChanges.onNext("1")
        verify(view).displayAutoCompleteResults(field1, FIELD_1_RESULTS)
        field2TextChanges.onNext("2")
        verify(view).displayAutoCompleteResults(field2, FIELD_2_RESULTS)
    }

}