package com.example.kotlin9.ui.music

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlin9.R
import com.google.firebase.storage.StorageReference

class AudioFilesAdapter : RecyclerView.Adapter<AudioFilesAdapter.AudioFileViewHolder>() {

    private var items = mutableListOf<StorageReference>()
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSongName = ""

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
        val audioFile = items[position]
        holder.bind(audioFile)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class AudioFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val authorTextView: TextView = itemView.findViewById(R.id.authorTextView)
        private val playButton: ImageButton = itemView.findViewById(R.id.playButton)
        private val stopButton: ImageButton = itemView.findViewById(R.id.stopButton)

        init {
            mediaPlayer = MediaPlayer()

            mediaPlayer?.setOnCompletionListener {
                mediaPlayer?.start()
            }
        }

        fun bind(storageReference: StorageReference) {
            val parts = storageReference.name.split("-")
            var authorText = parts[0].replace("_", " ")
            var titleText = parts[1].replace("_", " ")
            titleText = titleText.replace(Regex("\\b(?!\\s)\\w+(?=\\.)"), "")
            titleText = titleText.replace(Regex("\\..*"), "")

            titleTextView.text = titleText
            authorTextView.text = " " + authorText

            playButton.setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    pausePlayback(storageReference)
                    playButton.setImageResource(R.drawable.ic_play)
                } else {
                    startPlayback(storageReference)
                    playButton.setImageResource(R.drawable.ic_pause)
                }
            }

            stopButton.setOnClickListener{
                stopPlayback(storageReference)
                playButton.setImageResource(R.drawable.ic_play)
            }
        }
    }

    private fun startPlayback(storageReference: StorageReference) {
        if (currentSongName.equals(storageReference.name)){
            mediaPlayer?.start()
            isPlaying = true
        }
        else{
            currentSongName = storageReference.name
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource("https://firebasestorage.googleapis.com/v0/b/kotlin9-a1336.appspot.com/o/musics%2F${storageReference.name}?alt=media&token=298cceea-4c6f-45c9-b810-9a420afb4a29")
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    private fun pausePlayback(storageReference: StorageReference) {
        if (currentSongName.equals(storageReference.name)){
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    private fun stopPlayback(storageReference: StorageReference) {
        if (currentSongName.equals(storageReference.name)){
            currentSongName = ""
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            isPlaying = false
        }
    }

    fun stopMusic(){
        if (isPlaying == true){
            currentSongName = ""
            mediaPlayer?.stop()
            mediaPlayer?.prepare()
            isPlaying = false
        }
    }
}