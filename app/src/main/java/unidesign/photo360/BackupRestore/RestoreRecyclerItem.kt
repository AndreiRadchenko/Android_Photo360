package unidesign.photo360.BackupRestore

/**
 * Created by United on 12/26/2017.
 */

class RestoreRecyclerItem {

    var name: String? = null
    var comment: String? = null
    var filepath: String? = null

    constructor(name: String, comment: String, mfile_path: String) {
        this.name = name
        this.comment = comment
        this.filepath = mfile_path
    }

    constructor(Item: RestoreRecyclerItem) {
        this.name = Item.name
        this.comment = Item.comment
        this.filepath = Item.filepath
    }

}
