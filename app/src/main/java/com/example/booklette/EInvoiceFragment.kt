package com.example.booklette

import DetailInvoiceItem
import EInvoiceAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.booklette.databinding.FragmentEInvoiceBinding
import com.example.booklette.model.CartObject
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ORDERID_PARAM = "param1"
private const val ORDERNAME_PARAM = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseProcessingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EInvoiceFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var _binding: FragmentEInvoiceBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var itemList: ArrayList<DetailInvoiceItem> = ArrayList()
    private lateinit var adapter: EInvoiceAdapter

    var orderID = arguments?.getString(ORDERID_PARAM)

    // Trong phương thức companion object newInstance, nhận thêm một tham số để truyền dữ liệu
    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentEInvoiceBinding.inflate(inflater, container, false)
        val view = binding.root
        adapter = EInvoiceAdapter(requireContext(), itemList)

        db = Firebase.firestore
        auth = Firebase.auth

        orderID = arguments?.getString(ORDERID_PARAM)

        Log.d("orderid", orderID.toString())

        binding.rvEinvoiceItem.adapter = adapter
        binding.rvEinvoiceItem.layoutManager = LinearLayoutManager(requireContext())
        if(itemList.size == 0){
            loadItemListData()
        }
        else {
            binding.rvEinvoiceItem.visibility = View.VISIBLE
        }
        binding.ivBackToPrev.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnExportPdf.setOnClickListener {
            exportToPdf()
        }
        // Other code for handling UI interactions

        return view
    }


    private fun exportToPdf() {
        // Create a new PDF document
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(binding.einvoiceLayout.width, binding.einvoiceLayout.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val background = ContextCompat.getDrawable(requireContext(), R.drawable.einvoicebg) // Your background drawable if needed
        background?.setBounds(0, 0, canvas.width, canvas.height)
        background?.draw(canvas)

        // Draw your layout on the PDF canvas
        binding.einvoiceLayout.draw(canvas)

        // Finish and save the PDF
        pdfDocument.finishPage(page)

        try {
            val random = Random().nextInt(1000) // Generate a random number
            val fileName = "einvoice_$random.pdf" // PDF file name with random number
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            pdfDocument.writeTo(FileOutputStream(file))
            MotionToast.createColorToast(
                context as Activity,
                getString(R.string.successfully),
                "PDF saved to your Downloads folder",
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(
                    context as Activity,
                    www.sanju.motiontoast.R.font.helvetica_regular
                )
            )
        } catch (e: Exception) {
            Log.e("EInvoiceFragment", "Error exporting to PDF: $e")
            MotionToast.createColorToast(
                context as Activity,
                getString(R.string.failed),
                "Error exporting to PDF",
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(
                    context as Activity,
                    www.sanju.motiontoast.R.font.helvetica_regular
                )
            )
        }

        // Close the document
        pdfDocument.close()
    }


    fun loadItemListData() {
        itemList.clear()
        binding.rvEinvoiceItem.visibility = View.GONE
        binding.orderId.text = orderID.toString()
        orderID?.let { orderId ->
            db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener { document ->
                    val orderData = document.data
                    binding.orderId.text = orderID.toString()

                    val afterFomartedSubTotalMoney = String.format("%,.0f", orderData?.get("beforeDiscount"))
                    binding.subtotal.text = "$afterFomartedSubTotalMoney VND"

                    val discountMoney = (orderData?.get("totalSum").toString().toFloat()) - (orderData?.get("beforeDiscount").toString().toFloat())
                    val afterFomartedDiscountMoney = String.format("%,.0f", discountMoney)
                    binding.discount.text = "$afterFomartedDiscountMoney VND"

                    val afterFomartedTotalAmountMoney = String.format("%,.0f", orderData?.get("totalSum"))
                    binding.totalAmount.text = "$afterFomartedTotalAmountMoney VND"

                    val afterFomartedMoneyCollected = String.format("%,.0f", orderData?.get("totalSum"))
                    binding.moneyCollected.text = "$afterFomartedMoneyCollected VND"

                    binding.customerAddress.text = orderData?.get("shippingAddress").toString()

                    val timeStamp = orderData?.get("creationDate") as? Timestamp
                    val date: Date? = timeStamp?.toDate()
                    val sdf = SimpleDateFormat("dd-MM-yyyy")
                    binding.invoiceDate.text = sdf.format(date).toString()

                    val items = orderData?.get("items") as? Map<String, Map<String, Any>> // Assuming "items" is a Map of Maps
                    val fetchBookNamesTasks = items?.flatMap { (shopID, itemMap) ->
                        itemMap.map { (itemId, itemData) ->
                            Log.d("shopID", shopID)
                            Log.d("itemId", itemId)
                            Log.d("itemData", itemData.toString())

                            val quantity = ((itemData as Map<*, *>)["quantity"] as? Number) ?: 0
                            val totalSum = ((itemData as Map<*, *>)["totalSum"] as? Number) ?: 0
                            db.collection("books")
                                .whereEqualTo("bookID", itemId)
                                .get()
                                .addOnSuccessListener { bookSnapshot ->
                                    for (book in bookSnapshot.documents) {
                                        val bookData = book.data
                                        val bookName = bookData?.get("name") as? String
                                        itemList.add(
                                            DetailInvoiceItem(
                                                bookName.toString(),
                                                quantity?.toInt() ?: 1,
                                                (totalSum?.toFloat() ?: 0.0F) / (quantity?.toFloat() ?: 1.0F),
                                                totalSum?.toFloat() ?: 0.0F
                                            )
                                        )
                                        adapter.notifyDataSetChanged()

                                        Handler().postDelayed({
                                            binding.rvEinvoiceItem.visibility = View.VISIBLE
                                        }, 200)
                                    }
                                }
                        }
                    }

                }
                .addOnFailureListener { e ->
                    Log.e("loadItemListData", "Error loading item list: $e")
                }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
//        _binding = null
    }
}