package co.smartreceipts.android.images

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import co.smartreceipts.android.R
import co.smartreceipts.android.databinding.ActivityCropImageBinding
import co.smartreceipts.android.utils.StrictModeConfiguration
import co.smartreceipts.android.widget.model.UiIndicator
import com.jakewharton.rxbinding3.view.clicks
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_crop_image.*
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
        get() = button_rotate_right.clicks()

    override val rotateLeftClicks: Observable<Unit>
        get() = button_rotate_left.clicks()

    override val cropToggleClicks: Observable<Unit>
        get() = button_crop.clicks()

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
                applyCropClicksSubject.onNext(image_crop.crop())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getApplyCropClicks(): Observable<Bitmap> = applyCropClicksSubject

    override fun present(indicator: UiIndicator<Bitmap>) {

        when (indicator.state) {
            UiIndicator.State.Loading -> {
                progress_crop.visibility = View.VISIBLE
                enableControls(false)
            }
            UiIndicator.State.Success -> {
                progress_crop.visibility = View.GONE
                enableControls(true)

                showImage(indicator.data.get())
            }
            UiIndicator.State.Error -> {
                progress_crop.visibility = View.GONE
                Toast.makeText(this, R.string.IMG_SAVE_ERROR, Toast.LENGTH_SHORT).show()
                enableControls(true)

                setResult(RESULT_CROP_ERROR)
            }
            else -> {
                progress_crop.visibility = View.GONE
                image_crop.visibility = View.GONE
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

        if (autoCrop) {
            button_crop.setImageResource(R.drawable.ic_crop_free_24dp)
        } else {
            button_crop.setImageResource(R.drawable.ic_crop_24dp)
        }
    }

    private fun showImage(bitmap: Bitmap?) {
        StrictModeConfiguration.permitDiskReads { image_crop.setImageToCrop(bitmap?: image_crop.bitmap) }

        if (!autoCrop) {
            image_crop.setFullImgCrop()
        }
    }

    private fun enableControls(enable: Boolean) {
        button_rotate_left.isEnabled = enable
        button_rotate_right.isEnabled = enable
        button_crop.isEnabled = enable
    }

}