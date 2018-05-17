package unidesign.photo360

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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
        ivSettings.setOnClickListener { startActivity(Intent("intent.action.presetedit")) }

        var viewModel = ViewModelProviders.of(this).get(AppViewModel::class.java)
        viewModel.getPreset1().observe(this, object: Observer<String> {
                                                                override fun onChanged(jss: String?) {
                                                                            var settings = Settings(jss!!)
                                                                            preset_name_txt.text = settings.presetName
                                                                }
                                                            })

        return view
    }

    override fun onResume() {
        if (page == MainActivity.currentFragmentId){
            var frames =  MainActivity.sharedPrefs?.framesLeft.toString()
            Log.d("PageFragment onResume()", "frames number set to " + frames)
            frames_txt?.text = frames
        }
        else {
            var frames =  MainActivity.sharedPrefs?.frame.toString()
            Log.d("PageFragment onResume()", "frames number set to " + frames)
            frames_txt?.text = frames
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

//    fun fragmentObserver(jsString: String): Observer<String> {
//        var settings = Settings(jsString)
//        preset_name_txt.text = settings.presetName
//        return
//    }
}