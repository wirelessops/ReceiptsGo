package co.smartreceipts.android.images

import android.content.res.Resources
import android.graphics.*
import com.squareup.picasso.Transformation

class RoundedTransformation(
    radiusInDp: Int = 10, // dp
    marginInDp: Int = 0// dp
) : Transformation {
    private val radius = (radiusInDp * Resources.getSystem().displayMetrics.density).toInt()
    private val margin = (marginInDp * Resources.getSystem().displayMetrics.density).toInt()
    private val key: String = "rounded_$radius$margin"
    private var topCorners = true
    private var bottomCorners = true

    /**
     * Creates rounded transformation for top or bottom corners.
     *
     * @param radius radius is corner radii in dp
     * @param margin margin is the board in dp
     * @param topCornersOnly Rounded corner for top corners only.
     * @param bottomCornersOnly Rounded corner for bottom corners only.
     */
    constructor(
        radius: Int, margin: Int, topCornersOnly: Boolean,
        bottomCornersOnly: Boolean
    ) : this(radius, margin) {
        topCorners = topCornersOnly
        bottomCorners = bottomCornersOnly
    }

    override fun transform(source: Bitmap): Bitmap {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        if (topCorners && bottomCorners) {
            // Uses native method to draw symmetric rounded corners
            canvas.drawRoundRect(
                RectF(
                    margin.toFloat(), margin.toFloat(), (source.width - margin).toFloat(),
                    (source.height - margin).toFloat()
                ), radius.toFloat(), radius.toFloat(), paint
            )
        } else {
            // Uses custom path to generate rounded corner individually
            canvas.drawPath(
                RoundedRect(
                    margin.toFloat(),
                    margin.toFloat(),
                    (source.width - margin).toFloat(),
                    (
                            source.height - margin).toFloat(),
                    radius.toFloat(),
                    radius.toFloat(),
                    topCorners,
                    topCorners,
                    bottomCorners,
                    bottomCorners
                ), paint
            )
        }
        if (source != output) {
            source.recycle()
        }
        return output
    }

    override fun key(): String {
        return key
    }

    companion object {
        /**
         * Prepares a path for rounded corner selectively.
         *
         *
         * Source taken from http://stackoverflow.com/a/35668889/6635889 <br></br>
         * Usage:
         * <pre>
         * Path path = RoundedRect(0, 0, fwidth , fheight , 5,5, false, true, true, false);
         * canvas.drawPath(path, myPaint);
        </pre> *
         *
         * @param leftX The X coordinate of the left side of the rectangle
         * @param topY The Y coordinate of the top of the rectangle
         * @param rightX The X coordinate of the right side of the rectangle
         * @param bottomY The Y coordinate of the bottom of the rectangle
         * @param rx The x-radius of the oval used to round the corners
         * @param ry The y-radius of the oval used to round the corners
         * @param topLeft
         * @param topRight
         * @param bottomRight
         * @param bottomLeft
         * @return
         */
        fun RoundedRect(
            leftX: Float, topY: Float, rightX: Float, bottomY: Float, rx: Float,
            ry: Float, topLeft: Boolean, topRight: Boolean, bottomRight: Boolean, bottomLeft: Boolean
        ): Path {
            var rx = rx
            var ry = ry
            val path = Path()
            if (rx < 0) rx = 0f
            if (ry < 0) ry = 0f
            val width = rightX - leftX
            val height = bottomY - topY
            if (rx > width / 2) rx = width / 2
            if (ry > height / 2) ry = height / 2
            val widthMinusCorners = width - 2 * rx
            val heightMinusCorners = height - 2 * ry
            path.moveTo(rightX, topY + ry)
            if (topRight) path.rQuadTo(0f, -ry, -rx, -ry) //top-right corner
            else {
                path.rLineTo(0f, -ry)
                path.rLineTo(-rx, 0f)
            }
            path.rLineTo(-widthMinusCorners, 0f)
            if (topLeft) path.rQuadTo(-rx, 0f, -rx, ry) //top-left corner
            else {
                path.rLineTo(-rx, 0f)
                path.rLineTo(0f, ry)
            }
            path.rLineTo(0f, heightMinusCorners)
            if (bottomLeft) path.rQuadTo(0f, ry, rx, ry) //bottom-left corner
            else {
                path.rLineTo(0f, ry)
                path.rLineTo(rx, 0f)
            }
            path.rLineTo(widthMinusCorners, 0f)
            if (bottomRight) path.rQuadTo(rx, 0f, rx, -ry) //bottom-right corner
            else {
                path.rLineTo(rx, 0f)
                path.rLineTo(0f, -ry)
            }
            path.rLineTo(0f, -heightMinusCorners)
            path.close() //Given close, last lineto can be removed.
            return path
        }
    }

}