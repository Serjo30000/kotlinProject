package com.example.kotlin9.ui.music

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin9.R
import com.google.firebase.storage.StorageReference

class AudioFilesAdapter : RecyclerView.Adapter<AudioFilesAdapter.AudioFileViewHolder>() {
    private var items = mutableListOf<StorageReference>()

    fun setItems(newItems: List<StorageReference>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioFileViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_file, parent, false)
        return AudioFileViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioFileViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class AudioFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.textView)

        fun bind(storageReference: StorageReference) {
            textView.text = storageReference.name
        }
    }
}