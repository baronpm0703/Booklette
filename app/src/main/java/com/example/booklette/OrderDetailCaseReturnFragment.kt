package com.example.booklette

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentOrderDetailReturnBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso


/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseReturnFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
private const val ORDERID_PARAM = "param1"
class OrderDetailCaseReturnFragment : Fragment() {
    private var selectedImageUri: Uri? = null
    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var _binding: FragmentOrderDetailReturnBinding? = null
    private lateinit var imagePicker: ImageView
    private lateinit var imagePicker2: ImageView

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderID = it.getString(ORDERID_PARAM)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOrderDetailReturnBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore


        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout

        var tempTotalOrgMoney: Float = 0.0F
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener {document->
                        val orderData = document.data
                        val itemsMap = orderData?.get("items") as? Map<String, Any>
                        var totalQuantity: Long = 0
                        itemsMap?.forEach { (itemID, itemData) ->

                            val itemMap = itemData as? Map<String, Any>

                            //Log.d("number",itemMap.toString())
                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toFloat()
                            totalQuantity += itemMap?.get("quantity") as Long
                        }

                        // setup recycler view for books
                        val itemsFragment = OrderDetailItemListFragment.newInstance(1, itemsMap!!)
                        childFragmentManager.beginTransaction()
                            .replace(orderItemLayout.id,itemsFragment)
                            .commit()



                }
        }

        // back
        val backButton = binding.backButton
        backButton.setOnClickListener{
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        imagePicker = binding.imagePicker
        imagePicker2 = binding.imagePicker2
        imagePicker.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST)
        }
        imagePicker2.setOnClickListener{
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, PICK_IMAGE_REQUEST)
        }

        // return button
        val returnButton = binding.returnButton
        returnButton.setOnClickListener {

            //implement later when document finished.
//            val dialogClickListener =
//                DialogInterface.OnClickListener { dialog, which ->
//                    when (which) {
//                        DialogInterface.BUTTON_POSITIVE -> {
//                            val docRef = orderID?.let { it1 -> db.collection("orders").document(it1) }
//                            docRef?.update("status","Bị huỷ")?.addOnSuccessListener {
//                                // will change to motion toast later
//                                Toast.makeText(
//                                    context,
//                                    R.string.orderDetailCancelArgument,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                requireActivity().onBackPressedDispatcher.onBackPressed()
//                            }?.addOnFailureListener{
//                                Toast.makeText(
//                                    context,
//                                    R.string.orderDetailFailedArgument,
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                        DialogInterface.BUTTON_NEGATIVE -> {}
//                    }
//                }
//
//            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
//
//            builder.setMessage(R.string.orderDetailProcessingCancelLabel).setPositiveButton(R.string.yes, dialogClickListener)
//                .setNegativeButton(R.string.no, dialogClickListener).show()

        }
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param orderID Parameter 1.
         * @param orderFullName Parameter 2.
         * @return A new instance of fragment OrderDetailFragment.
         */
        private const val PICK_IMAGE_REQUEST = 1335
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(orderID: String) =
            OrderDetailCaseReturnFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }
    private fun setImageToImageView() {
        selectedImageUri?.let { uri ->
            Picasso.get().load(uri).into(imagePicker)
        }
        imagePicker2.visibility = View.VISIBLE
    }
    // Lấy ảnh từ user (Hai?)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1335 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            setImageToImageView()
        }
        Toast.makeText(context,selectedImageUri.toString(),Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}