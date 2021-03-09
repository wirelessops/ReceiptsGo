package co.smartreceipts.android.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import co.smartreceipts.analytics.Analytics
import co.smartreceipts.analytics.events.Events
import co.smartreceipts.analytics.log.Logger
import co.smartreceipts.android.R
import co.smartreceipts.android.activities.NavigationHandler
import co.smartreceipts.android.activities.SmartReceiptsActivity
import co.smartreceipts.android.databinding.ReceiptImageFragmentBinding
import co.smartreceipts.android.images.CropImageActivity
import co.smartreceipts.android.imports.CameraInteractionController
import co.smartreceipts.android.imports.RequestCodes
import co.smartreceipts.android.imports.importer.ActivityFileResultImporter
import co.smartreceipts.android.imports.locator.ActivityFileResultLocator
import co.smartreceipts.android.model.Receipt
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory
import co.smartreceipts.android.persistence.database.controllers.impl.ReceiptTableController
import co.smartreceipts.android.persistence.database.controllers.impl.StubTableEventsListener
import co.smartreceipts.android.persistence.database.operations.DatabaseOperationMetadata
import co.smartreceipts.android.persistence.database.operations.OperationFamilyType
import co.smartreceipts.android.tooltip.image.data.ImageCroppingPreferenceStorage
import co.smartreceipts.android.utils.IntentUtils
import com.squareup.picasso.Callback
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import dagger.Lazy
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import wb.android.flex.Flex
import javax.inject.Inject

class ReceiptImageFragment : WBFragment() {

    companion object {
        // Save state
        private const val KEY_OUT_RECEIPT = "key_out_receipt"
        private const val KEY_OUT_URI = "key_out_uri"

        fun newInstance(): ReceiptImageFragment {
            return ReceiptImageFragment()
        }
    }

    @Inject
    lateinit var flex: Flex

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var receiptTableController: ReceiptTableController

    @Inject
    lateinit var navigationHandler: NavigationHandler<SmartReceiptsActivity>

    @Inject
    lateinit var activityFileResultLocator: ActivityFileResultLocator

    @Inject
    lateinit var activityFileResultImporter: ActivityFileResultImporter

    @Inject
    lateinit var imageCroppingPreferenceStorage: ImageCroppingPreferenceStorage

    @Inject
    lateinit var picasso: Lazy<Picasso>

    private var _binding: ReceiptImageFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var imageUpdatedListener: ImageUpdatedListener
    private lateinit var compositeDisposable: CompositeDisposable

    private var imageUri: Uri? = null
    private var receipt: Receipt? = null

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            receipt = requireArguments().getParcelable(Receipt.PARCEL_KEY)
        } else {
            receipt = savedInstanceState.getParcelable(KEY_OUT_RECEIPT)
            imageUri = savedInstanceState.getParcelable(KEY_OUT_URI)
        }
        imageUpdatedListener = ImageUpdatedListener()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ReceiptImageFragmentBinding.inflate(inflater, container, false)

        binding.buttonEditPhoto.setOnClickListener { view ->
            analytics.record(Events.Receipts.ReceiptImageViewEditPhoto)
            imageCroppingPreferenceStorage.setCroppingScreenWasShown(true)
            navigationHandler.navigateToCropActivity(this, receipt!!.file!!, RequestCodes.EDIT_IMAGE_CROP)
        }

        binding.buttonRetakePhoto.setOnClickListener { view ->
            analytics.record(Events.Receipts.ReceiptImageViewRetakePhoto)
            imageUri = CameraInteractionController(this@ReceiptImageFragment).retakePhoto(receipt!!)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadImage()
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val toolbar: Toolbar
        if (navigationHandler.isDualPane) {
            toolbar = requireActivity().findViewById(R.id.toolbar)
            binding.toolbar.toolbar.visibility = View.GONE
        } else {
            toolbar = binding.toolbar.toolbar
        }
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Logger.debug(this, "Result Code: $resultCode")

        // Null out the last request
        val cachedImageSaveLocation = imageUri
        imageUri = null

        if (requestCode == RequestCodes.EDIT_IMAGE_CROP) {
            when (resultCode) {
                CropImageActivity.RESULT_CROP_ERROR -> {
                    Logger.error(this, "An error occurred while cropping the image")
                }
                else -> {
                    picasso.get().invalidate(receipt!!.file!!)
                    loadImage()
                }
            }
        } else {
            activityFileResultLocator.onActivityResult(requestCode, resultCode, data, cachedImageSaveLocation)
        }
    }

    private fun subscribe() {
        compositeDisposable = CompositeDisposable()

        compositeDisposable.add(activityFileResultLocator.uriStream
            // uri always has SCHEME_CONTENT -> we don't need to check permissions
            .subscribe { locatorResponse ->
                when {
                    locatorResponse.throwable.isPresent -> {
                        binding.receiptImageProgress.visibility = View.GONE
                        Toast.makeText(activity, getFlexString(R.string.FILE_SAVE_ERROR), Toast.LENGTH_SHORT).show()
                        activityFileResultLocator.markThatResultsWereConsumed()
                    }
                    else -> {
                        binding.receiptImageProgress.visibility = View.VISIBLE
                        activityFileResultImporter.importFile(
                            locatorResponse.requestCode,
                            locatorResponse.resultCode, locatorResponse.uri!!, receipt!!.trip
                        )
                    }
                }
            })

        compositeDisposable.add(activityFileResultImporter.resultStream
            .subscribe { (throwable, file) ->
                when {
                    throwable.isPresent -> Toast.makeText(activity, getFlexString(R.string.IMG_SAVE_ERROR), Toast.LENGTH_SHORT).show()
                    else -> {
                        val retakeReceipt = ReceiptBuilderFactory(receipt!!).setFile(file).build()
                        receiptTableController.update(receipt!!, retakeReceipt, DatabaseOperationMetadata())
                    }
                }
                binding.receiptImageProgress.visibility = View.GONE
                activityFileResultLocator.markThatResultsWereConsumed()
                activityFileResultImporter.markThatResultsWereConsumed()
            })
    }

    override fun onResume() {
        super.onResume()
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            title = receipt!!.name
        }
        receiptTableController.subscribe(imageUpdatedListener)

        subscribe()

    }

    override fun onPause() {
        receiptTableController.unsubscribe(imageUpdatedListener)

        compositeDisposable.clear()

        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Logger.debug(this, "onSaveInstanceState")
        outState.apply {
            putParcelable(KEY_OUT_RECEIPT, receipt)
            putParcelable(KEY_OUT_URI, imageUri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigationHandler.navigateBack()
                true
            }
            R.id.action_share -> {
                receipt!!.file?.let {
                    val sendIntent = IntentUtils.getSendIntent(requireActivity(), it)
                    startActivity(Intent.createChooser(sendIntent, resources.getString(R.string.send_email)))
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadImage() {

        if (receipt!!.hasImage()) {
            receipt!!.file?.let {
                binding.receiptImageProgress.visibility = View.VISIBLE

                picasso.get().load(it).memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE).fit().centerInside()
                    .into(binding.receiptImageImageview, object : Callback {

                        override fun onSuccess() {
                            binding.receiptImageProgress.visibility = View.GONE

                            binding.receiptImageImageview.visibility = View.VISIBLE
                            binding.buttonEditPhoto.visibility = View.VISIBLE
                            binding.buttonRetakePhoto.visibility = View.VISIBLE
                        }

                        override fun onError(e: Exception) {
                            binding.receiptImageProgress.visibility = View.GONE
                            Toast.makeText(requireContext(), getFlexString(R.string.IMG_OPEN_ERROR), Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }


    private inner class ImageUpdatedListener : StubTableEventsListener<Receipt>() {

        override fun onUpdateSuccess(oldReceipt: Receipt, newReceipt: Receipt, databaseOperationMetadata: DatabaseOperationMetadata) {
            if (databaseOperationMetadata.operationFamilyType != OperationFamilyType.Sync) {
                if (oldReceipt == receipt) {
                    receipt = newReceipt
                    loadImage()
                }
            }
        }

        override fun onUpdateFailure(oldReceipt: Receipt, e: Throwable?, databaseOperationMetadata: DatabaseOperationMetadata) {
            if (databaseOperationMetadata.operationFamilyType != OperationFamilyType.Sync) {
                binding.receiptImageProgress.visibility = View.GONE
                Toast.makeText(requireContext(), getFlexString(R.string.database_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFlexString(id: Int): String = getFlexString(flex, id)

}