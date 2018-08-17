package unidesign.photo360.BackupRestore

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import unidesign.photo360.R
import java.net.URL
import java.util.ArrayList


/**
 * Created by United on 12/26/2017.
 */

class RestoreTemplateAdapter(internal var context: Context, mlistItems: MutableList<RestoreRecyclerItem>,
                             var listener: RestoreTemplateAdapter.OnItemClickListener) :
        RecyclerView.Adapter<RestoreTemplateAdapter.ViewHolder>()
{

    var listItems: MutableList<RestoreRecyclerItem> = ArrayList<RestoreRecyclerItem>()
    private var touchHelper: ItemTouchHelper? = null
    //Return all selected ids
    private var mSelectedItemsIds: SparseBooleanArray? = null

    interface OnItemClickListener {
        fun onItemClick(item: RestoreRecyclerItem)
    }

    init {
        //setHasStableIds(true); // this is required for D&D feature.
        this.listItems = mlistItems
        mSelectedItemsIds = SparseBooleanArray()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestoreTemplateAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.restore_recycle_item,
                parent, false)
        return RestoreTemplateAdapter.ViewHolder(v)
    }


    override fun onBindViewHolder(holder: RestoreTemplateAdapter.ViewHolder, position: Int) {

        val image_url: URL? = null
        val itemList = listItems[position]
        holder.bind(itemList, listener)
        //        holder.m_icon_image_view.setImageResource(R.mipmap.ic_kyivstar);
        holder.txtName.setText(itemList.name)
        holder.txtComment.setText(itemList.comment)
        /** Change background color of the selected items in list view   */
        //        holder.itemView
        //                .setBackgroundColor(mSelectedItemsIds.get(position) ? 0x9934B5E4
        //                        : Color.TRANSPARENT);
        holder.restore_item_container.setBackgroundColor(
                if (mSelectedItemsIds!!.get(position))
                    context.resources.getColor(R.color.bg_item_selected_state)
                else
                    context.resources.getColor(R.color.bg_item_normal_state)
        )
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    class ViewHolder
    //        TextView txtDescription;
    //        TextView txtOptionDigit;
    //        ImageView imgIcon;

    (itemView: View) : RecyclerView.ViewHolder(itemView) {

        var restore_item_container: RelativeLayout
        internal var txtName: TextView
        internal var txtComment: TextView

        init {
            restore_item_container = itemView.findViewById<View>(R.id.restore_item_container) as RelativeLayout
            //mDragHandle = itemView.findViewById(R.id.drag_handle);
            txtName = itemView.findViewById<View>(R.id.txtName) as TextView
            txtComment = itemView.findViewById<View>(R.id.txtComment) as TextView
        }

        fun bind(item: RestoreRecyclerItem, listener: RestoreTemplateAdapter.OnItemClickListener) {

            restore_item_container.setOnClickListener { listener.onItemClick(item) }
        }

    }

    fun onViewMoved(oldPosition: Int, newPosition: Int) {
        val targetItem = listItems[oldPosition]
        val Item = RestoreRecyclerItem(targetItem)
        listItems.removeAt(oldPosition)
        listItems.add(newPosition, Item)
        notifyItemMoved(oldPosition, newPosition)
    }

    fun onViewSwiped(position: Int) {
        listItems.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setTouchHelper(touchHelper: ItemTouchHelper) {

        this.touchHelper = touchHelper
    }

    /***
     * Methods required for do selections, remove selections, etc.
     */

    //Toggle selection methods
    fun toggleSelection(position: Int) {
        selectView(position, !mSelectedItemsIds!!.get(position))
    }


    //Remove selected selections
    fun removeSelection() {
        mSelectedItemsIds = SparseBooleanArray()
        notifyDataSetChanged()
    }


    //Put or delete selected position into SparseBooleanArray
    fun selectView(position: Int, value: Boolean) {
        if (value)
            mSelectedItemsIds!!.put(position, value)
        else
            mSelectedItemsIds!!.delete(position)

        notifyDataSetChanged()
    }

    fun selectAllView(listSize: Int, value: Boolean) {
        mSelectedItemsIds = SparseBooleanArray()
        if (value) {
            for (i in 0 until listSize)
                mSelectedItemsIds!!.put(i, value)
        }
        notifyDataSetChanged()
    }

    //Get total selected count
    fun getSelectedCount(): Int {
        return mSelectedItemsIds!!.size()
    }

    //Return all selected ids
    fun getSelectedIds(): SparseBooleanArray? {
        return mSelectedItemsIds
    }

}
