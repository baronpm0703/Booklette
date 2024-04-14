package com.example.booklette

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.example.booklette.model.Photo

class reviewBookDetailPhotoGalleryGridAdapter(private var context: Context, private var items: ArrayList<Photo>): BaseAdapter() {
    private class ViewHolder(row: View?) {
        var image: ImageView? = null

        init {
            image = row?.findViewById(R.id.photoImg)
        }
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
            view = (inflater as LayoutInflater).inflate(R.layout.review_photo_gallery_items_book_detail, null)

            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        var photo = items[position]
        if (photo.nameFile?.endsWith(".png") == true) {
            viewHolder.image?.setBackgroundColor(Color.parseColor("#FFFFFF"))
        }
        viewHolder.image?.setImageBitmap(photo.image)

        return view as View
    }

}