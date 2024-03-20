package com.example.booklette

import android.graphics.Color
import android.graphics.Paint
import CartFragmentRecyclerViewAdapter
import CategoryFragmentGridViewAdapter
import android.graphics.Canvas
import android.graphics.RectF
import com.example.booklette.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.FragmentCartBinding


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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        val view = binding.root

        val bookList = ArrayList<String>()
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("1")
        bookList.add("2")
        bookList.add("1")
        bookList.add("2")

        // Add more items as needed

        val adapter = CartFragmentRecyclerViewAdapter(requireContext(), bookList)
        binding.rvCart.adapter = adapter
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvCart)


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
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            // Xóa item khi người dùng vuốt sang trái hoặc sang phải
            val position = viewHolder.adapterPosition
            (binding.rvCart.adapter as? CartFragmentRecyclerViewAdapter)?.removeItem(position)
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {

            val clampedDX = if (Math.abs(dX) > 200) {
                if (dX > 0) 200.toFloat() else -200.toFloat()
            } else {
                dX
            }

            // Vẽ background và icon cho Swipe-To-Remove
            val itemView = viewHolder.itemView
            val itemHeight = itemView.bottom.toFloat() - itemView.top.toFloat()
            val itemWidth = itemHeight / 3

            val p = Paint()
            if (clampedDX  < 0) {
                p.color = Color.rgb(200, 0, 0) // Màu đỏ
                val background = RectF(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                val newHeight = itemHeight*0.85f
                val bottomMargin = (itemHeight - newHeight)
                val adjustedBackground = RectF(background.left, background.top + bottomMargin -3f, background.right, background.bottom-10f)
                c.drawRoundRect(adjustedBackground, 50f, 50f, p)
                val iconSize = 70 // Kích thước của biểu tượng xóa
                val iconMargin = 70// Chuyển đổi itemHeight sang kiểu Int
                val iconLeft = (itemView.right - iconMargin - iconSize ).toInt()
                val iconTop = (itemView.top + (itemHeight - iconSize) / 2 + 26f).toInt()
                val iconRight = (itemView.right - iconMargin).toInt()
                val iconBottom = (iconTop + iconSize).toInt()
                val deleteIcon = resources.getDrawable(R.drawable.ic_delete) // Thay R.drawable.ic_delete bằng ID của biểu tượng xóa thực tế
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, clampedDX, dY, actionState, isCurrentlyActive)
            }
        }
    }
}