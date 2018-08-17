package unidesign.photo360.BackupRestore

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.Window

/**
 * Created by United on 12/28/2017.
 */

class StatusbarColorAnimator(context: Context, internal var statusBarColor: Int,
                             internal var statusBarToColor: Int) : ValueAnimator(), ValueAnimator.AnimatorUpdateListener {

    internal var context: Context? = null
    internal var window: Window

    init {
        this.setFloatValues(0f, 1f)
        this.addUpdateListener(this)
        window = (context as RestoreActivity).window
    }//context.getResources().getColor(R.color.select_mod_status_bar);
    //context.getResources().getColor(R.color.colorPrimaryDark);

    override fun onAnimationUpdate(animation: ValueAnimator) {
        // Use animation position to blend colors.
        val position = animation.animatedFraction

        // Apply blended color to the status bar.
        val blended = blendColors(statusBarColor, statusBarToColor, position)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = blended
        }

    }

    private fun blendColors(from: Int, to: Int, ratio: Float): Int {
        val inverseRatio = 1f - ratio

        val r = Color.red(to) * ratio + Color.red(from) * inverseRatio
        val g = Color.green(to) * ratio + Color.green(from) * inverseRatio
        val b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio

        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }
}
