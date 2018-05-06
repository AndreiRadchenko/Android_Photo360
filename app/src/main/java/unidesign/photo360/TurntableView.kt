package unidesign.photo360

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class TurntableView : View {

    private var mCircleYellow: Paint? = null
    private var mCircleGray: Paint? = null

    private var mRadius: Float = 0.toFloat()
    private val mArcBounds = RectF()

    constructor(context: Context) : super(context) {

        // create the Paint and set its color

    }

    @JvmOverloads constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {

        initPaints()
    }

    private fun initPaints() {
        mCircleYellow = Paint(Paint.ANTI_ALIAS_FLAG)
        mCircleYellow!!.style = Paint.Style.FILL
        mCircleYellow!!.color = Color.YELLOW
        mCircleYellow!!.style = Paint.Style.STROKE
        mCircleYellow!!.strokeWidth = 15 * resources.displayMetrics.density
        mCircleYellow!!.strokeCap = Paint.Cap.SQUARE
        // mEyeAndMouthPaint.setColor(getResources().getColor(R.color.colorAccent));
        mCircleYellow!!.color = Color.parseColor("#F9A61A")

        mCircleGray = Paint(Paint.ANTI_ALIAS_FLAG)
        mCircleGray!!.style = Paint.Style.FILL
        mCircleGray!!.color = Color.GRAY
        mCircleGray!!.style = Paint.Style.STROKE
        mCircleGray!!.strokeWidth = 15 * resources.displayMetrics.density
        mCircleGray!!.strokeCap = Paint.Cap.SQUARE
        // mEyeAndMouthPaint.setColor(getResources().getColor(R.color.colorAccent));
        mCircleGray!!.color = Color.parseColor("#76787a")

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mRadius = Math.min(w, h) / 2f

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val w = View.MeasureSpec.getSize(widthMeasureSpec)
        val h = View.MeasureSpec.getSize(heightMeasureSpec)

        val size = Math.min(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val drawUpto = 46f


        val mouthInset = mRadius / 3f
        mArcBounds.set(mouthInset, mouthInset, mRadius * 2 - mouthInset, mRadius * 2 - mouthInset)
        canvas.drawArc(mArcBounds, 0f, 360f, false, mCircleGray!!)

        canvas.drawArc(mArcBounds, 270f, drawUpto, false, mCircleYellow!!)


    }

}
