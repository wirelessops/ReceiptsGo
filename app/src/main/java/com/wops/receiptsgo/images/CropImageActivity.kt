package com.wops.receiptsgo.images

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wops.receiptsgo.R
import com.wops.receiptsgo.databinding.ActivityCropImageBinding
import com.wops.receiptsgo.utils.StrictModeConfiguration
import com.wops.receiptsgo.widget.model.UiIndicator
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.io.File
import javax.inject.Inject

class CropImageActivity : AppCompatActivity(), CropView {

    companion object {
        const val RESULT_CROP_ERROR = 115
        const val EXTRA_IMAGE_PATH = "extra_image_path"
    }

    override val imageFile: File
        get() = File(intent.getStringExtra(EXTRA_IMAGE_PATH))

    override val rotateRightClicks: Observable<Unit>
        get() = binding.buttonRotateRight.clicks()

    override val rotateLeftClicks: Observable<Unit>
        get() = binding.buttonRotateLeft.clicks()

    override val cropToggleClicks: Observable<Unit>
        get() = binding.buttonCrop.clicks()

    private val applyCropClicksSubject = PublishSubject.create<Bitmap>()

    private var autoCrop = true

    private lateinit var binding: ActivityCropImageBinding

    @Inject
    lateinit var presenter: CropImagePresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        binding = ActivityCropImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // setting default result
        val intent = Intent()
        intent.putExtra(EXTRA_IMAGE_PATH, imageFile.absolutePath)
        setResult(Activity.RESULT_CANCELED, intent)

    }

    override fun onResume() {
        super.onResume()

        setSupportActionBar(binding.toolbar.toolbar)

        supportActionBar?.apply {
            setTitle(R.string.menu_receiptimage_edit)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onStart() {
        super.onStart()
        presenter.subscribe()
    }

    override fun onStop() {
        presenter.unsubscribe()
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_save -> {
                applyCropClicksSubject.onNext(binding.imageCrop.crop())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getApplyCropClicks(): Observable<Bitmap> = applyCropClicksSubject

    override fun present(indicator: UiIndicator<Bitmap>) {

        when (indicator.state) {
            UiIndicator.State.Loading -> {
                binding.progressCrop.visibility = View.VISIBLE
                enableControls(false)
            }
            UiIndicator.State.Success -> {
                binding.progressCrop.visibility = View.GONE
                enableControls(true)

                showImage(indicator.data.get())
            }
            UiIndicator.State.Error -> {
                binding.progressCrop.visibility = View.GONE
                Toast.makeText(this, R.string.IMG_SAVE_ERROR, Toast.LENGTH_SHORT).show()
                enableControls(true)

                setResult(RESULT_CROP_ERROR)
            }
            else -> {
                binding.progressCrop.visibility = View.GONE
                binding.imageCrop.visibility = View.GONE
                enableControls(false)
            }
        }
    }

    override fun finishCropView(success: Boolean) {
        val intent = Intent()
        intent.putExtra(EXTRA_IMAGE_PATH, imageFile.absolutePath)

        setResult(if (success) Activity.RESULT_OK else RESULT_CROP_ERROR, intent)

        this.finish()
    }

    override fun toggleCropMode() {
        autoCrop = !autoCrop

        showImage(null)

        binding.buttonCrop.setImageResource(if (autoCrop) R.drawable.ic_crop_free_24dp else R.drawable.ic_crop_24dp)
    }

    private fun showImage(bitmap: Bitmap?) {
        StrictModeConfiguration.permitDiskReads { binding.imageCrop.setImageToCrop(bitmap?: binding.imageCrop.bitmap) }

        if (!autoCrop) {
            binding.imageCrop.setFullImgCrop()
        }
    }

    private fun enableControls(enable: Boolean) {
        binding.buttonRotateLeft.isEnabled = enable
        binding.buttonRotateRight.isEnabled = enable
        binding.buttonCrop.isEnabled = enable
    }

}