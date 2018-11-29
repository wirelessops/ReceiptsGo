package co.smartreceipts.android.settings.widget.editors.categories

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import co.smartreceipts.android.R
import co.smartreceipts.android.model.Category
import co.smartreceipts.android.persistence.database.controllers.impl.CategoriesTableController
import dagger.android.support.AndroidSupportInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

/**
 * A [DialogFragment], which allows the user to create a new [Category] or edit an existing one
 */
class CategoryEditorDialogFragment : DialogFragment(),
        CategoryEditorView,
        CategoryEditorRouter,
        DialogInterface.OnClickListener {

    lateinit var presenter: CategoryEditorPresenter

    @Inject
    lateinit var categoriesTableController: CategoriesTableController

    var category: Category? = null

    private val saveClickStream = PublishSubject.create<Any>()
    private val cancelClickStream = PublishSubject.create<Any>()

    @BindView(R.id.category_input_name)
    lateinit var nameBox: EditText

    @BindView(R.id.category_input_code)
    lateinit var codeBox: EditText

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = arguments?.getParcelable(Category.PARCEL_KEY)
        presenter = CategoryEditorPresenter(this, this, categoriesTableController, category, savedInstanceState)
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(activity)
        val dialogView = inflater.inflate(R.layout.category_editor, null)
        ButterKnife.bind(this, dialogView)

        val title: Int
        val positiveButton: Int
        if (category != null) {
            // Edit Copy
            title = R.string.dialog_category_edit
            positiveButton = R.string.update
        } else {
            // New Copy
            title = R.string.dialog_category_add
            positiveButton = R.string.add
        }

        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle(title)
        builder.setView(dialogView)
        builder.setPositiveButton(positiveButton, this)
        builder.setNegativeButton(android.R.string.cancel, this)
        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe()
    }

    override fun onStop() {
        presenter.unsubscribe()
        super.onStop()
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when(which) {
            DialogInterface.BUTTON_POSITIVE -> saveClickStream.onNext(Any())
            DialogInterface.BUTTON_NEGATIVE -> cancelClickStream.onNext(Any())
        }
    }

    override fun displayCategory(category: Category?) {
        if (category != null) {
            nameBox.setText(category.name)
            codeBox.setText(category.code)
        }
    }

    override fun getName(): String {
        return nameBox.text.toString()
    }

    override fun getCode(): String {
        return codeBox.text.toString()
    }

    override fun getSaveClickStream(): Observable<Any> {
        return saveClickStream
    }

    override fun getCancelClickStream(): Observable<Any> {
        return cancelClickStream
    }

    override fun dismissEditor() {
        dismiss()
    }

    companion object {
        const val TAG = "CategoryEditorDialogFragment"

        @JvmStatic
        fun newInstance(category: Category?) : CategoryEditorDialogFragment {
            val fragment = CategoryEditorDialogFragment()
            val args = Bundle()
            args.putParcelable(Category.PARCEL_KEY, category)
            fragment.arguments = args
            return fragment
        }
    }
}