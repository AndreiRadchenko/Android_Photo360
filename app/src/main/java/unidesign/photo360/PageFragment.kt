package unidesign.photo360

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
    var page: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_page, container, false)
        page = getArguments()!!.getInt(PAGE_NUM)

        frames_txt = view.findViewById(R.id.frames_left_txt)
        val ivSettings: ImageView = view.findViewById(R.id.preset_settings)
        ivSettings.setOnClickListener { startActivity(Intent("intent.action.presetedit")) }

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
}