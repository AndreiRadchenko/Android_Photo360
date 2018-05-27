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

class PageFragment : Fragment() {

    lateinit var frames_txt: TextView
    lateinit var preset_name_txt: TextView
    lateinit var param1_txt: TextView
    lateinit var param2_txt: TextView
    lateinit var param3_txt: TextView
    lateinit var param4_txt: TextView
    var page: Int = 0
    lateinit var viewModel: FragmentViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        page = getArguments()!!.getInt(PAGE_NUM)

        frames_txt = view.findViewById(R.id.frames_left_txt)
        preset_name_txt = view.findViewById(R.id.preset_name)
        param1_txt = view.findViewById(R.id.param1_value)
        param2_txt = view.findViewById(R.id.param2_value)
        param3_txt = view.findViewById(R.id.param3_value)
        param4_txt = view.findViewById(R.id.param4_value)
        val ivSettings: ImageView = view.findViewById(R.id.preset_settings)

        ivSettings.setOnClickListener {
            var editIntent = Intent()
            editIntent.action = "intent.action.presetedit"
            editIntent.putExtra("page", page)
            startActivity(editIntent)
        }

        viewModel = ViewModelProviders.of(this).get(FragmentViewModel::class.java)

        viewModel.getPreset(page).
                observe(this, object: Observer<String> {
                    override fun onChanged(jss: String?) {
                        var settings = Settings(jss!!)
                        displaySettings(settings)
                    }
                })

/*        when (page) {
            0 -> viewModel.getPreset1().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            1 -> viewModel.getPreset2().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            2 -> viewModel.getPreset3().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
            3 -> viewModel.getPreset4().
                    observe(this, object: Observer<String> {
                        override fun onChanged(jss: String?) {
                            var settings = Settings(jss!!)
                            displaySettings(settings)
                        }
                    })
        }*/
        //viewModel.initPreferencesRequest()
        return view
    }

    override fun onResume() {
        viewModel.initPreferencesRequest(page)
        if (page == MainActivity.currentFragmentId){
            //var frames =  MainActivity.sharedPrefs?.framesLeft.toString()
            //Log.d("PageFragment onResume()", "frames number set to " + frames)
            //frames_txt?.text = frames
        }
        else {
            //var frames =  MainActivity.sharedPrefs?.frame.toString()
           // Log.d("PageFragment onResume()", "frames number set to " + frames)
            //frames_txt?.text = frames
        }
        super.onResume()
    }

    companion object {
        val PAGE_NUM = "PAGE_NUM"
        val PAGE_FAMES = "PAGE_FRAMES"

        fun newInstance(page: Int
        ): PageFragment {
            val fragment = PageFragment()
            val args = Bundle()
            args.putInt(PAGE_NUM, page)
            fragment.setArguments(args)
            return fragment
        }
    }

    fun displaySettings (mSettings: Settings){
        preset_name_txt.text = mSettings.presetName
        param1_txt.text = mSettings.frame.toString()
        param2_txt.text = mSettings.delay.toString()
        param3_txt.text = mSettings.speed.toString()
        param4_txt.text = mSettings.acceleration.toString()
        frames_txt.text = mSettings.framesLeft.toString()
    }
//    fun fragmentObserver(jsString: String): Observer<String> {
//        var settings = Settings(jsString)
//        preset_name_txt.text = settings.presetName
//        return
//    }
}