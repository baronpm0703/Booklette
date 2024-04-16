package com.example.booklette

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.model.Photo

class ChosenReviewPhotoBookDetailRVAdapter(
    private val context: Context,
    private var dataPhotos: ArrayList<Photo>
): RecyclerView.Adapter<ChosenReviewPhotoBookDetailRVAdapter.ViewHolder>() {
    var onItemClick: ((Photo) -> Unit)? = null
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val imgView = listItemView.findViewById<ImageView>(R.id.photoImg)
        init{
            listItemView.setOnClickListener {onItemClick?.invoke(dataPhotos[adapterPosition]) }
        }
    }

    fun updateDataPhoto(dataPhotos: ArrayList<Photo>){
        this.dataPhotos = dataPhotos
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        val photoView = inflater.inflate(R.layout.review_chosen_photo_gallery_items_book_detail, parent, false)

        return ViewHolder(photoView)
    }

    override fun getItemCount(): Int {
        return dataPhotos.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val detail: Photo = dataPhotos.get(position)

        var photoView = holder.imgView
        if (detail.nameFile?.endsWith(".png") == true) {
            photoView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
        photoView.setImageBitmap(detail.image)

    }
}