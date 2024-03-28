package com.example.booklette

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentBookDetailBinding
import com.example.booklette.databinding.FragmentHomeBinding
import com.google.api.Distribution.BucketOptions.Linear

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BookDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var voucherAdapter: bookDetailFragmentVoucherViewPagerAdapter
    private lateinit var shopVoucherAdapter: bookDetailShopVoucherRVAdapter
    private lateinit var otherBookFromShopAdapter: TopBookHomeFragmentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        val view = binding.root

        var data = ArrayList<String>()
        data.add("A")
        data.add("A")
        data.add("A")
        data.add("A")
        data.add("A")

        voucherAdapter = bookDetailFragmentVoucherViewPagerAdapter(requireContext(), data)
        binding.vpVoucherBookDetail.adapter = voucherAdapter
        binding.vpVoucherBookDetail.pageMargin = 20

        shopVoucherAdapter = bookDetailShopVoucherRVAdapter()
        binding.rvBookDetailShopVoucher.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBookDetailShopVoucher.adapter = shopVoucherAdapter

        var books = ArrayList<BookObject>()
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))
        books.add(BookObject("B001", "The Catcher in the Eyes", "Novel", "Chanh Tin", "10/03/2003", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b", 12.50F))

        var stars = ArrayList<Float>()
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)
        stars.add(4.5F)

        otherBookFromShopAdapter = TopBookHomeFragmentAdapter(books, stars)
        binding.rvOtherBookFromShopBookDetail.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvOtherBookFromShopBookDetail.adapter = otherBookFromShopAdapter

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}