package com.ick.kalambury.list

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.ick.kalambury.BR
import com.ick.kalambury.BuildConfig
import com.ick.kalambury.R
import com.ick.kalambury.ads.ListNativeAdView
import com.ick.kalambury.list.ListType.ItemMode
import com.ick.kalambury.list.model.ListableData
import com.ick.kalambury.list.views.ListItem
import com.ick.kalambury.util.inflate

@Suppress("UNCHECKED_CAST")
class DataAdapter<T : ListableData>(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val listType: ListType,
    items: List<T>? = null,
    private val callback: ((ListItem<*,*>) -> Unit)? = null,
) : ListAdapter<T, RecyclerView.ViewHolder>(ListableDataDiffCallback()), DefaultLifecycleObserver {

    private val _selectedItemIds: MutableList<String> = mutableListOf()
    val selectedItemIds: List<String>
        get() = _selectedItemIds.toList()

    private var nativeAd: NativeAd? = null
    private var isLoadingAd: Boolean = false
    private val hasAd: Boolean
        get() = nativeAd != null

    init {
        setItems(items)
        lifecycle.addObserver(this)
    }

    fun setItems(list: List<T>?, selected: List<String>? = null, onFinish: (() -> Unit)? = null) {
        _selectedItemIds.clear()
        if (listType.itemMode == ItemMode.SELECTABLE) {
            _selectedItemIds.addAll(selected ?: list?.filter { it.selected }?.map { it.id }.orEmpty())
        }

        if (list == null || list.size < AD_POSITION) {
            destroyAd()
            submitList(list?.toList())
            return
        }

        if (list.size >= AD_POSITION) {
            submitList(list.toList()) {
                onFinish?.invoke()
                loadAd(context)
            }
        }
    }

    fun updateSelection(selected: List<String>) {
        _selectedItemIds.clear()
        _selectedItemIds.addAll(selected)
        notifyItemRangeChanged(0, itemCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_AD) {
            NativeAdViewHolder(parent.inflate(R.layout.list_item_native_ad))
        } else {
            DataViewHolder<T>(parent.inflate(listType.viewId), callback)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_AD) {
            val adViewHolder = holder as NativeAdViewHolder
            (adViewHolder.itemView as ListNativeAdView).setNativeAd(nativeAd!!)
        } else {
            (holder as DataViewHolder<T>).bind(listType.itemMode, getItem(getListPosition(position)))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasAd && position == AD_POSITION) {
            VIEW_TYPE_AD
        } else {
            super.getItemViewType(position)
        }
    }

    override fun getItemCount(): Int {
        var count = super.getItemCount()
        if (hasAd && count >= AD_POSITION) {
            count += 1
        }
        return count
    }

    private fun getListPosition(adapterPosition: Int): Int {
        return if (hasAd && adapterPosition >= AD_POSITION) {
            adapterPosition - 1
        } else {
            adapterPosition
        }
    }

    private fun loadAd(context: Context) {
        if (listType.isWithAds && !hasAd && !isLoadingAd) {
            isLoadingAd = true
            val adLoader = AdLoader.Builder(context, BuildConfig.AD_NATIVE_ID)
                .forNativeAd { ad: NativeAd ->
                    if (this.nativeAd != null) {
                        this.nativeAd!!.destroy()
                        this.nativeAd = ad
                        if (super.getItemCount() >= AD_POSITION) {
                            notifyItemChanged(AD_POSITION)
                        }
                    } else {
                        this.nativeAd = ad
                        if (super.getItemCount() >= AD_POSITION) {
                            notifyItemInserted(AD_POSITION)
                        }
                    }
                    isLoadingAd = false
                }
                .build()
            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    private fun destroyAd() {
        nativeAd?.let {
            notifyItemRemoved(AD_POSITION)
            it.destroy()
        }
        nativeAd = null
    }

    override fun onStop(owner: LifecycleOwner) {
        destroyAd()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        lifecycle.removeObserver(this)
    }

    private inner class DataViewHolder<T : ListableData>(
        itemView: View,
        clickListener: ((ListItem<*,*>) -> Unit)? = null,
    ) : RecyclerView.ViewHolder(itemView) {

        val view: ListItem<*,*>
            get() = itemView as ListItem<*,*>

        init {
            itemView.setOnClickListener {
                if (listType.itemMode == ItemMode.SELECTABLE) {
                    if (_selectedItemIds.contains(view.data.id)) {
                        _selectedItemIds.remove(view.data.id)
                        view.binding.setVariable(BR.isChecked, false)
                    } else {
                        _selectedItemIds.add(view.data.id)
                        view.binding.setVariable(BR.isChecked, true)
                    }
                    view.binding.executePendingBindings()
                }

                clickListener?.invoke(view)
            }
        }

        fun bind(mode: ItemMode, data: T) {
            view.bind(mode, data, _selectedItemIds.contains(data.id))
        }

    }

    private class NativeAdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private class ListableDataDiffCallback<T : ListableData> : DiffUtil.ItemCallback<T>() {

        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

    }

    companion object {
        private const val VIEW_TYPE_AD = 1
        private const val AD_POSITION = 2
    }

}