package io.agora.openlive.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import io.agora.openlive.Constants
import io.agora.openlive.R
import io.agora.rtc.video.VideoEncoderConfiguration.VideoDimensions
import java.util.*

class ResolutionAdapter(private val mContext: Context, var selected: Int) : RecyclerView.Adapter<ViewHolder>() {
    private val mItems = ArrayList<ResolutionItem>()
    private fun initData(context: Context) {
        val size = Constants.VIDEO_DIMENSIONS.size
        val labels = context.resources.getStringArray(R.array.string_array_resolutions)
        for (i in 0 until size) {
            val item = ResolutionItem(labels[i], Constants.VIDEO_DIMENSIONS[i])
            mItems.add(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.dimension_item, parent, false)
        return ResolutionHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = mItems[position]
        val content = (holder as ResolutionHolder).resolution
        content.text = item.label
        content.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                selected = position
                notifyDataSetChanged()
            }
        })
        if (position == selected) content.isSelected = true else content.isSelected = false
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    inner class ResolutionHolder internal constructor(itemView: View) : ViewHolder(itemView) {
        var resolution: TextView

        init {
            resolution = itemView.findViewById(R.id.resolution)
        }
    }

    private class ResolutionItem internal constructor(var label: String, var dimension: VideoDimensions)

    init {
        initData(mContext)
    }
}