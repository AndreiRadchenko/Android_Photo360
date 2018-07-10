package unidesign.photo360

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.os.Parcel
import android.os.Parcelable

class CustomViewModelFactory(app: Application, page: Int) : ViewModelProvider.NewInstanceFactory() {
    var mpage = page
    var mapp = app

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FragmentViewModel(mapp, mpage) as T
    }
}

