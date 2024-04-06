package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import CartFragmentRecyclerViewAdapter
import CategoryFragmentGridViewAdapter
import CheckoutFragment
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.graphics.Canvas
import android.graphics.RectF
import com.example.booklette.R
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentCartBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CategoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

//    private lateinit var auth: FirebaseAuth
//    private lateinit var db: FirebaseFirestore

    private var isFailedQuery: Boolean = false
    private var cartList: ArrayList<CartObject> = ArrayList()


    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root
        // Add more items as needed

        val adapter = CartFragmentRecyclerViewAdapter(requireContext(), cartList)
        binding.rvCart.adapter = adapter
        binding.rvCart.visibility = View.GONE
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())

//        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
//        itemTouchHelper.attachToRecyclerView(binding.rvCart)



        adapter.quantityChangeListener = object : CartFragmentRecyclerViewAdapter.OnQuantityChangeListener {
            override fun onQuantityDecreased(position: Int) {
                // Decrease item quantity
                // Implement your logic here...
            }

            override fun onQuantityIncreased(position: Int) {
                // Increase item quantity
                // Implement your logic here...
            }
        }
        val auth = Firebase.auth
        val db = Firebase.firestore

        // Shop info
        db.collection("accounts").whereEqualTo("UID", auth.uid).get()
            .addOnSuccessListener { documents ->
            if (documents.size() != 1) return@addOnSuccessListener
            for (document in documents) {
                // Get avatar and seller's name
                if (document.data.get("cart") != null) {
                    val cartArray = document.data.get("cart") as ArrayList<Map<String, Any>>
                    cartArray.let {
                        for (cartMap in it) {
                            val itemId = cartMap["itemId"] as? String
                            val storeId = cartMap["storeId"] as? String
                            if(itemId != null && storeId != null){
                                db.collection("accounts").whereEqualTo("UID", storeId).get()
                                    .addOnSuccessListener { storeDocument ->
                                        for(eachStore in storeDocument){
                                            val storeName = eachStore.data.get("fullname")
                                            eachStore.getDocumentReference("store")!!.get().addOnSuccessListener { personalStoreDocument ->
                                                val itemList = personalStoreDocument["items"] as? ArrayList<Map<String, Any>>
                                                itemList?.let {
                                                    for (item in it) {
                                                        val bookID = item["itemID"] as? String
                                                        if (itemId == bookID) {
//                                                        val id = itemIds.
                                                            // Retrieve book details using itemId
                                                            db.collection("books")
                                                                .whereEqualTo("bookID", itemId)
                                                                .get()
                                                                .addOnSuccessListener { bookDocument ->
                                                                    for (eachBook in bookDocument) {
                                                                        cartList.add(
                                                                            CartObject(
                                                                                eachBook.data["bokID"].toString(),
                                                                                storeId.toString(),
                                                                                storeName.toString(),
                                                                                eachBook.data["image"].toString(),
                                                                                eachBook.data["name"].toString(),
                                                                                eachBook.data["author"].toString(),
                                                                                item["price"].toString()
                                                                                    .toFloat(),
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                        }

                                                    }

                                                }
                                            }
                                        }

                                    }

                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()

            Handler().postDelayed({
                // Code to be executed after the delay
                // For example, you can start a new activity or update UI elements
                binding.rvCart.visibility = View.VISIBLE
            }, 2000)
        }
            .addOnFailureListener {
                Log.i("UID", auth.uid.toString())
            }
            .addOnCanceledListener {
                Log.i("UID", auth.uid.toString())
            }

        binding.btnCheckOut.setOnClickListener {
            val checkOutFragment = CheckoutFragment()

            val ft = activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.fcvNavigation, checkOutFragment)
                ?.commit()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
//
//    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
//        return false
//    }
//
////    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
////        // Xóa item khi người dùng vuốt sang trái hoặc sang phải
////        val position = viewHolder.adapterPosition
////        val adapter = binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter
////        adapter?.let {
////            // Lưu trữ thông tin về mục bị xóa để sử dụng khi xác nhận
////            var deletedItemInfo = it.getItemInfo(position)
////            // Hiển thị AlertDialog để xác nhận
////            showDeleteConfirmationDialog(deletedItemInfo, position)
////        }
////    }
//
//    override fun onChildDraw(
//        c: Canvas,
//        recyclerView: RecyclerView,
//        viewHolder: RecyclerView.ViewHolder,
//        dX: Float,
//        dY: Float,
//        actionState: Int,
//        isCurrentlyActive: Boolean
//    ) {
//
//        val clampedDX = if (Math.abs(dX) > 200) {
//            if (dX > 0) 200.toFloat() else -200.toFloat()
//        } else {
//            dX
//        }
//
//        // Vẽ background và icon cho Swipe-To-Remove
//        val itemView = viewHolder.itemView
//        val itemHeight = itemView.bottom.toFloat() - itemView.top.toFloat()
//        val itemWidth = itemHeight
//
//        val p = Paint()
//        if (clampedDX  < 0) {
//            p.color = Color.rgb(200, 0, 0) // Màu đỏ
//            val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
//            val newHeight = itemHeight*0.85f
//            val bottomMargin = (itemHeight - newHeight)
//            val adjustedBackground = RectF(background.left, background.top + bottomMargin -3f, background.right, background.bottom-10f)
//            c.drawRoundRect(adjustedBackground, 50f, 50f, p)
//            val iconSize = 70 // Kích thước của biểu tượng xóa
//            val iconMargin = 70// Chuyển đổi itemHeight sang kiểu Int
//            val iconLeft = (itemView.right - iconMargin - iconSize ).toInt()
//            val iconTop = (itemView.top + (itemHeight - iconSize) / 2 + 26f).toInt()
//            val iconRight = (itemView.right - iconMargin).toInt()
//            val iconBottom = (iconTop + iconSize).toInt()
//            val deleteIcon = resources.getDrawable(R.drawable.ic_delete) // Thay R.drawable.ic_delete bằng ID của biểu tượng xóa thực tế
//            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
//            deleteIcon.draw(c)
//            super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
//        }
//    }
//}
//
//    private fun showDeleteConfirmationDialog(deletedItemInfo: String, position: Int) {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setMessage("Are you sure you want to delete this item?")
//
//        // Xác nhận xóa item
//        builder.setPositiveButton("Delete") { dialog, which ->
//            val adapter = binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter
//            adapter?.removeItem(position)
//            adapter?.notifyItemRemoved(position) // Cập nhật giao diện
//            dialog.dismiss()
//        }
//
//        // Hủy thao tác xóa item
//        builder.setNegativeButton("Cancel") { dialog, which ->
//            dialog.dismiss()
//        }
//
//        val alertDialog = builder.create()
//        alertDialog.show()
//    }
}