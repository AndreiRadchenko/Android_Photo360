package unidesign.photo360

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import unidesign.photo360.R.id.turntable_view
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.animation.PropertyValuesHolder
import android.util.Log
import unidesign.photo360.MainActivity.Companion.runningFragmentId


class PageFragment : Fragment() {

    lateinit var frames_txt: TextView
    lateinit var preset_name_txt: TextView
    lateinit var param1_txt: TextView
    lateinit var param2_txt: TextView
    lateinit var param3_txt: TextView
    lateinit var param4_txt: TextView
    lateinit var turntabe: TurntableView
    lateinit var valueAnimator: ValueAnimator
    var page: Int = 0
    lateinit var viewModel: FragmentViewModel
    lateinit var modeArray: Array<String>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        page = arguments!!.getInt(PAGE_NUM)
        modeArray = resources.getStringArray(R.array.shootingmode_array)

        frames_txt = view.findViewById(R.id.frames_left_txt)
        preset_name_txt = view.findViewById(R.id.preset_name)
        param1_txt = view.findViewById(R.id.param1_value)
        param2_txt = view.findViewById(R.id.param2_value)
        param3_txt = view.findViewById(R.id.param3_value)
        param4_txt = view.findViewById(R.id.param4_value)
        val ivSettings: ImageView = view.findViewById(R.id.preset_settings)
        turntabe = view.findViewById(R.id.turntable_view)
        turntabe.animation

        ivSettings.setOnClickListener {
            var editIntent = Intent()
            editIntent.action = "intent.action.presetedit"
            editIntent.putExtra("page", page)
            startActivity(editIntent)
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 360f)
        //valueAnimator.duration = 5000
        valueAnimator.addUpdateListener {
            val value = it.animatedValue as Float
            turntabe.drawUpto = value
            //Log.d("addUpdateListener", "turntabe.drawUpto = " + turntabe.drawUpto)
            turntabe.invalidate()
        }

        viewModel = ViewModelProviders.of(this, CustomViewModelFactory(this.activity!!.application, page)).
                get(FragmentViewModel::class.java)

        viewModel.getPreset().
                observe(this, Observer<Settings> { set ->
                    displaySettings(set!!)
                })
        return view
    }

    override fun onStart(){
        super.onStart()
        EventBus.getDefault().register(this)
        //valueAnimator.start()
    }

    override fun onStop(){
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        viewModel.initPreferencesRequest()
        if (page == runningFragmentId)
            viewModel.setChanges2View(MainActivity.postSettings.framesLeft)

        super.onResume()
    }

    override fun onPause() {
        //MainActivity.currentFragmentId = mViewPager.currentItem
        //runningFragmentId = mViewPager.currentItem

        super.onPause()
    }

    companion object {
        val PAGE_NUM = "PAGE_NUM"
        val PAGE_FAMES = "PAGE_FRAMES"

        fun newInstance(page: Int
        ): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putInt(PAGE_NUM, page)
            //fragment.setArguments(args)
            fragment.arguments = args
            return fragment
        }
    }

    fun displaySettings (mSettings: Settings){
        preset_name_txt.text = mSettings.presetName
        param1_txt.text = mSettings.frame.toString()
        param2_txt.text = mSettings.delay.toString()
        param3_txt.text = mSettings.speed.toString()
        when (mSettings.shootingMode) {
            "inter" -> param4_txt.text = modeArray[0]
            "nonST" -> param4_txt.text = modeArray[1]
        }
        frames_txt.text = mSettings.framesLeft.toString()

        var prevAnimValue: Float
        var animValue: Float

        var prevframeLeft = mSettings.framesLeft + 1
        if (mSettings.framesLeft == mSettings.frame)
            prevframeLeft = mSettings.frame

        prevAnimValue = (1 - (prevframeLeft.toFloat() / mSettings.frame.toFloat()))*360f
        animValue = (1 - (mSettings.framesLeft.toFloat() / mSettings.frame.toFloat()))*360f

        //Log.d("displaySettings", "MainActivity.postSettings.direction = " + MainActivity.postSettings.direction)
        if (MainActivity.postSettings.direction == 1){
            prevAnimValue = - prevAnimValue
            animValue = - animValue
            //valueAnimator.reverse()
            //valueAnimator.setFloatValues(animValue, prevAnimValue)
        }

        valueAnimator.setFloatValues(prevAnimValue, animValue)

        valueAnimator.duration = mSettings.delay.toLong()
        valueAnimator.start()

//        Log.d("displaySettings", "animValue = " + animValue)
/*        Log.d("displaySettings", "mSettings.framesLeft = " + mSettings.framesLeft)
        Log.d("displaySettings", "mSettings.frame = " + mSettings.frame)*/
    }

    @Subscribe(threadMode = ThreadMode.MAIN, priority = 1)
    public fun onMessage(event: wsMessage){
        Log.d("PageFragment.onMessage", Settings.FRAMES_LEFT + ": " + event.framesLeft)
        Log.d("PageFragment.onMessage", "page = $page, MainActivity.runningFragmentId = " + runningFragmentId)
        if (page == runningFragmentId)
            viewModel.setChanges2View(event.framesLeft)
    }

/*    private fun createAnimator(): ValueAnimator {
        val propertyX = PropertyValuesHolder.ofInt(PROPERTY_X, 100, 300)
        val propertyY = PropertyValuesHolder.ofInt(PROPERTY_Y, 100, 300)
        val propertyAlpha = PropertyValuesHolder.ofInt(PROPERTY_ALPHA, 0, 255)

        val animator = ValueAnimator()
        animator.setValues(propertyX, propertyY, propertyAlpha)
        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()

        animator.addUpdateListener {
            //TODO invalidate view with new values
        }

        return animator
    }*/

}