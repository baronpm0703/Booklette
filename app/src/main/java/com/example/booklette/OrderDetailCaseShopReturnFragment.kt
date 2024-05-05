package com.example.booklette

import android.app.ActionBar.LayoutParams
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booklette.databinding.FragmentShopOrderDetailReturnBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

private const val ORDERID_PARAM = "param1"

class OrderDetailCaseShopReturnFragment : Fragment() {
    private var selectedImageUri: Uri? = null

    // TODO: Rename and change types of parameters
    private var orderID: String? = null
    private var storeUID: String = ""
    private var _binding: FragmentShopOrderDetailReturnBinding? = null
    private lateinit var imagePicker: ImageView
    private lateinit var imagePicker2: ImageView
    private lateinit var exContext: Context
    private lateinit var itemsFragment: OrderDetailItemListFragment
    private var URI1: String = ""
    private var URI2: String = ""

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cloudStorage: FirebaseStorage
    private lateinit var documentID: String
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
        _binding = FragmentShopOrderDetailReturnBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        auth = Firebase.auth
        exContext = requireContext()
        cloudStorage = Firebase.storage
        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout
        val reasonField = binding.returnReasonField
        val shopReasonField = binding.shopReasonField
        val returnRef = db.collection("returnNExchange").whereEqualTo("orderID", orderID)

        imagePicker = binding.imagePicker
        imagePicker2 = binding.imagePicker2
        val returnReasonLabel = binding.returnReasonLabel
        val statusField = binding.statusField
        val shopReasonFrameLayout = binding.orderDetailOrderShopReasonFrameLayout
        val cancelRequestButton = binding.orderDetailRejectRequestButton
        val contactBuyerButton = binding.orderDetailContactBuyerButton
        val acceptRequestButton = binding.orderDetailAcceptRequestButton
        var customerID = ""
        var userSetting = true
        var shopSetting = true
        var hcmusBook = false
        returnRef.get()
            .addOnSuccessListener { QuerySnapshot ->
                for (document in QuerySnapshot) {
                    documentID = document.id
                    val returnData = document.data
                    val imageList = returnData["image"] as? List<String>
                    val itemList = returnData["itemID"] as? List<String>
                    val shopReason = returnData["shopReason"] as? String
                    val status = returnData["status"] as String
                    if (!status.contains("trả đang duyệt", true)) {
                        if (!shopReason.isNullOrEmpty()) {
                            Log.d("shopreason",shopReason)
                            shopReasonField.setText(shopReason)
                        }
                        else{
                            shopReasonField.setText(getString(R.string.no_reason_provided))
                        }
                        shopReasonField.isEnabled = false
                        contactBuyerButton.layoutParams.width = LayoutParams.MATCH_PARENT
                        cancelRequestButton.visibility = View.GONE
                        acceptRequestButton.visibility = View.GONE
                    }
                    statusField.text = changeStatusText(status)

                    val reason = returnData["reason"] as? String
                    if (!reason.isNullOrEmpty()){
                        reasonField.setText(reason)
                    }
                    else{
                        reasonField.setText(getString(R.string.no_reason_provided))
                    }

                    if (imageList != null) {
                        if (imageList[0].isNotEmpty() && imageList[1].isNotEmpty()) {
                            Picasso.get().load(imageList[0]).into(imagePicker)
                            Picasso.get().load(imageList[1]).into(imagePicker2)
                        } else if (imageList[0].isNotEmpty() || imageList[1].isNotEmpty()) {
                            if (imageList[0].isNotEmpty()) {
                                Picasso.get().load(imageList[0]).into(imagePicker)
                            } else {
                                Picasso.get().load(imageList[1]).into(imagePicker)
                            }
                            imagePicker2.visibility = View.GONE
                        } else {
                            returnReasonLabel.visibility = View.GONE
                            imagePicker.visibility = View.GONE
                            imagePicker2.visibility = View.GONE
                        }
                    }
                    orderID?.let {
                        db.collection("orders")
                            .document(it)
                            .get()
                            .addOnSuccessListener { document ->
                                val orderData = document.data
                                customerID = orderData?.get("customerID").toString()
                                storeUID = orderData?.get("customerID").toString()
                                val itemsMap =
                                    orderData?.get("items") as? Map<String, Map<String, Any>>


                                var itemsToDisplay = mutableMapOf<String, Map<String, Any>>()
//                                itemsMap?.forEach { (itemID, itemData) ->
//                                    val bookMap : Map<String,Map<String,Any>> = itemData as Map<String,Map<String,Any>>
//                                    if (itemList != null) {
//                                        if (itemList.contains(itemID)){
//                                            itemsToDisplay[itemID] = itemData
//                                            Log.d("itemsToDisplay",itemsToDisplay.toString())
//                                        }
//                                    }
//                                }
                                itemsMap?.flatMap { (shopID, itemMap) ->
                                    itemMap.map { (itemId, itemData) ->
//                                        Log.d("shopID", shopID)
//                                        Log.d("itemId", itemId)
//                                        Log.d("itemData", itemData.toString())
                                        if (itemList != null) {
                                            if (itemList.contains(itemId)) {
                                                itemsToDisplay[shopID] = itemMap
                                                Log.d("itemsToDisplay", itemsToDisplay.toString())
                                            }
                                        }
                                    }
                                }
                                // setup recycler view for books
                                itemsFragment = OrderDetailItemListFragment.newInstance(
                                    1,
                                    itemsToDisplay,
                                    allowSelection = false,
                                    allowMultipleSelection = false
                                )
                                childFragmentManager.beginTransaction()
                                    .replace(orderItemLayout.id, itemsFragment)
                                    .commit()

                            }
                    }


                }
            }
            .addOnFailureListener {
                Log.e("Loi", it.toString())
            }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        var requestID = ""
        cancelRequestButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val returnDocumentRef =
                                db.collection("returnNExchange").document(documentID)
                            returnDocumentRef.get().addOnSuccessListener { documentSnapshot->
                                val documentSnapshotData = documentSnapshot.data
                                requestID = documentSnapshotData?.get("requestID").toString()
                            }
                            val updates = hashMapOf<String, Any>(
                                "shopReason" to shopReasonField.text.toString(),
                                "status" to "Yêu cầu hoàn trả bị từ chối",
                            )
                            returnDocumentRef
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(
                                        "returnDocument",
                                        "DocumentSnapshot successfully updated!"
                                    )

                                    val orderDocumentRef = orderID?.let { it1 ->
                                        db.collection("orders").document(it1)
                                    }
                                    orderDocumentRef?.update(
                                        "status",
                                        "Yêu cầu hoàn trả bị từ chối"
                                    )
                                        ?.addOnSuccessListener {
                                            Log.d(
                                                "orderDocument",
                                                "DocumentSnapshot successfully updated!"
                                            )
                                            Toast.makeText(
                                                context,
                                                R.string.orderShopDetailReturnReject,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            db.collection("accounts")
                                                .whereEqualTo("UID", auth.uid)
                                                .get().addOnSuccessListener { documents ->
                                                    for (document in documents) {
                                                        val userData = document.data
                                                        val deliveryNotif = userData?.get("deliveryNotif")
                                                        userSetting = if (deliveryNotif is Boolean) {
                                                            deliveryNotif
                                                        } else {
                                                            false // Default value if deliveryNotif is null or not a Boolean
                                                        }

                                                    }
                                                }

                                            //send email
                                            db.collection("orders").document(orderID!!)
                                                .get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    var orderDetail = ""
                                                    val orderData = documentSnapshot.data
                                                    var shopEmail = ""
                                                    var shopFullName = ""
                                                    val itemsMap =
                                                        orderData?.get("items") as? Map<String, Map<String, Any>>
                                                    val fetchBookNamesTasks =
                                                        itemsMap?.flatMap { (shopID, itemMap) ->
                                                            itemMap.map { (itemId, itemData) ->
                                                                Log.d("shopID", shopID)
                                                                Log.d("itemId", itemId)
                                                                Log.d("itemData", itemData.toString())

                                                                // Get shop name
                                                                db.collection("accounts")
                                                                    .whereEqualTo("UID", shopID)
                                                                    .get()
                                                                    .addOnSuccessListener { querySnapshot ->
                                                                        for (document in querySnapshot) {
                                                                            val storeData = document.data
                                                                            val bookStoreRef =
                                                                                storeData["store"] as DocumentReference
                                                                            var bookStoreName =
                                                                                storeData["fullname"].toString()
                                                                            val salesNotif = storeData["salesNotif"]
                                                                            shopSetting = if (salesNotif is Boolean) {
                                                                                salesNotif
                                                                            } else {
                                                                                false // Default value if deliveryNotif is null or not a Boolean
                                                                            }
                                                                            shopFullName = bookStoreName
                                                                            bookStoreRef.get()
                                                                                .addOnSuccessListener {
                                                                                    if (it.exists()) {
                                                                                        val bookStoreData = it.data
                                                                                        bookStoreName =
                                                                                            bookStoreData?.get("storeName")
                                                                                                .toString()
                                                                                        shopEmail =
                                                                                            bookStoreData?.get("email")
                                                                                                .toString()
                                                                                        shopFullName = bookStoreName
                                                                                    }
                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    // Handle any errors
                                                                                    Log.e(
                                                                                        "getStoreNameError",
                                                                                        "Error getting document: $exception"
                                                                                    )
                                                                                }


                                                                            // Fetch books after getting store name
                                                                            db.collection("books")
                                                                                .whereEqualTo("bookID", itemId)
                                                                                .get()
                                                                                .addOnSuccessListener { bookQuerySnapshot ->
                                                                                    for (bookDocument in bookQuerySnapshot) {
                                                                                        val bookData =
                                                                                            bookDocument.data
                                                                                        val id = itemId
                                                                                        val name =
                                                                                            bookData["name"].toString()
                                                                                        val author =
                                                                                            bookData["author"].toString()
                                                                                        val imageUrl =
                                                                                            bookData["image"].toString()

                                                                                        val itemMap2 =
                                                                                            itemData as? Map<*, *>
                                                                                        val genre =
                                                                                            bookData["genre"] as String

                                                                                        val price =
                                                                                            (itemMap2?.get("totalSum") as Number).toLong()
                                                                                        val quantity =
                                                                                            itemMap2.get("quantity") as Long
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.store
                                                                                        ) + ": " + bookStoreName + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_name
                                                                                        ) + ": " + name + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_author
                                                                                        ) + ": " + author + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.quantity
                                                                                        ) + " " + quantity.toString() + "\n\n"
                                                                                    }

                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    Log.e(
                                                                                        "Loi",
                                                                                        exception.toString()
                                                                                    )
                                                                                }
                                                                        }
                                                                    }
                                                                    .addOnFailureListener { exception ->
                                                                        Log.e("Loi", exception.toString())
                                                                    }
                                                            }
                                                        }
                                                    Tasks.whenAllComplete(fetchBookNamesTasks!!)
                                                        .addOnSuccessListener {
                                                            val emailSender = EmailSender(exContext)
                                                            val emailSubject =
                                                                exContext.getString(R.string.orderReturnFailedSubject)
                                                            val shopEmailSubject =
                                                                exContext.getString(R.string.orderReturnFailedSubject)
                                                            val totalMoney =
                                                                (orderData?.get("totalSum") as Number).toLong()
                                                            val status = orderData?.get("status") as String
                                                            val timeStamp =
                                                                orderData?.get("creationDate") as Timestamp

                                                            val date: Date? = timeStamp?.toDate()
                                                            val sdf = SimpleDateFormat("dd-MM-yyyy")
                                                            val paymentMethod =
                                                                orderData?.get("paymentMethod") as? Map<String, Any>
                                                            val paymentMethodType =
                                                                paymentMethod?.get("Type").toString()
                                                            val shippingAddress =
                                                                orderData?.get("shippingAddress") as String
                                                            val beforeDiscount =
                                                                (orderData?.get("beforeDiscount") as Number).toLong()
                                                            var emailBody = ""
                                                            var shopEmailBody = ""
                                                            var userFullName = ""
                                                            var userEmail = ""

                                                            val statusEmail = changeStatusText(status)
                                                            val discount =
                                                                "-" + formatMoney(beforeDiscount - totalMoney)
                                                            var orderMessage = ""
                                                            Log.d("emailHcmus", hcmusBook.toString())
                                                            if (!hcmusBook) {
                                                                orderMessage = exContext.getString(R.string.email_return_failed)
                                                            } else {
                                                                orderMessage =
                                                                    exContext.getString(R.string.email_return_failed)
                                                            }

                                                            val shopOrderMessage = exContext.getString(R.string.email_return_failed_by_shop)
                                                            db.collection("accounts")
                                                                .whereEqualTo("UID", customerID)
                                                                .get()
                                                                .addOnSuccessListener { querySnapshot ->
                                                                    for (snapshot in querySnapshot) {
                                                                        Log.d("emailsnapshot", snapshot.toString())
                                                                        val userData = snapshot.data
                                                                        userFullName =
                                                                            userData["fullname"].toString()
                                                                        val bookStoreRef =
                                                                            userData["store"] as DocumentReference
                                                                        bookStoreRef.get()
                                                                            .addOnSuccessListener {
                                                                                if (it.exists()) {
                                                                                    val bookStoreData = it.data
                                                                                    userEmail =
                                                                                        bookStoreData?.get("email")
                                                                                            .toString()
                                                                                    emailBody = emailSender.setBodyReturnWithResult(
                                                                                        userFullName,
                                                                                        orderMessage,
                                                                                        requestID,
                                                                                        changeStatusText("trả bị từ chối"),
                                                                                        sdf.format(
                                                                                            Date()
                                                                                        ).toString(),
                                                                                        reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                        shopReasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                    )
                                                                                    shopEmailBody =
                                                                                        emailSender.setBodyReturnWithResult(
                                                                                            shopFullName,
                                                                                            shopOrderMessage,
                                                                                            requestID,
                                                                                            changeStatusText("trả bị từ chối"),
                                                                                            sdf.format(
                                                                                                Date()
                                                                                            ).toString(),
                                                                                            reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                            shopReasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                        )
                                                                                    Log.d(
                                                                                        "userEmail",
                                                                                        "user " + userEmail
                                                                                    )
                                                                                    if (userSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            userEmail,
                                                                                            emailSubject,
                                                                                            emailBody
                                                                                        )
                                                                                    }

                                                                                    Log.d(
                                                                                        "shopEmail",
                                                                                        "shop " + shopEmail
                                                                                    )
                                                                                    if (shopSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            shopEmail,
                                                                                            shopEmailSubject,
                                                                                            shopEmailBody
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                            .addOnFailureListener { exception ->
                                                                                // Handle any errors
                                                                                Log.e(
                                                                                    "bookStoreRef",
                                                                                    "Error getting document: $exception"
                                                                                )
                                                                            }

                                                                    }


                                                                }
                                                        }
                                                }
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        ?.addOnFailureListener { e ->
                                            Log.e("orderDocument", "Error updating document", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("returnDocument", "Error updating document", e)
                                }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailReturnRejectLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()

        }

        contactBuyerButton.setOnClickListener {

            val intent = Intent(context, ChannelChatActivity::class.java)
            intent.putExtra("storeUID", storeUID)

            startActivity(intent)

        }

        acceptRequestButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val returnDocumentRef =
                                db.collection("returnNExchange").document(documentID)
                            val updates = hashMapOf<String, Any>(
                                "shopReason" to shopReasonField.text.toString(),
                                "status" to "Yêu cầu hoàn trả thành công",
                            )
                            returnDocumentRef
                                .update(updates)
                                .addOnSuccessListener {
                                    Log.d(
                                        "returnDocument",
                                        "DocumentSnapshot successfully updated!"
                                    )
                                    val orderDocumentRef = orderID?.let { it1 ->
                                        db.collection("orders").document(it1)
                                    }
                                    orderDocumentRef?.update(
                                        "status",
                                        "Yêu cầu hoàn trả thành công"
                                    )
                                        ?.addOnSuccessListener {
                                            Log.d(
                                                "orderDocument",
                                                "DocumentSnapshot successfully updated!"
                                            )
                                            Toast.makeText(
                                                context,
                                                R.string.orderDetailReturnAcceptLabel,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            db.collection("accounts")
                                                .whereEqualTo("UID", auth.uid)
                                                .get().addOnSuccessListener { documents ->
                                                    for (document in documents) {
                                                        val userData = document.data
                                                        val deliveryNotif = userData?.get("deliveryNotif")
                                                        userSetting = if (deliveryNotif is Boolean) {
                                                            deliveryNotif
                                                        } else {
                                                            false // Default value if deliveryNotif is null or not a Boolean
                                                        }

                                                    }
                                                }

                                            //send email
                                            db.collection("orders").document(orderID!!)
                                                .get()
                                                .addOnSuccessListener { documentSnapshot ->
                                                    var orderDetail = ""
                                                    val orderData = documentSnapshot.data
                                                    var shopEmail = ""
                                                    var shopFullName = ""
                                                    val itemsMap =
                                                        orderData?.get("items") as? Map<String, Map<String, Any>>
                                                    val fetchBookNamesTasks =
                                                        itemsMap?.flatMap { (shopID, itemMap) ->
                                                            itemMap.map { (itemId, itemData) ->
                                                                Log.d("shopID", shopID)
                                                                Log.d("itemId", itemId)
                                                                Log.d("itemData", itemData.toString())

                                                                // Get shop name
                                                                db.collection("accounts")
                                                                    .whereEqualTo("UID", shopID)
                                                                    .get()
                                                                    .addOnSuccessListener { querySnapshot ->
                                                                        for (document in querySnapshot) {
                                                                            val storeData = document.data
                                                                            val bookStoreRef =
                                                                                storeData["store"] as DocumentReference
                                                                            var bookStoreName =
                                                                                storeData["fullname"].toString()
                                                                            val salesNotif = storeData["salesNotif"]
                                                                            shopSetting = if (salesNotif is Boolean) {
                                                                                salesNotif
                                                                            } else {
                                                                                false // Default value if deliveryNotif is null or not a Boolean
                                                                            }
                                                                            shopFullName = bookStoreName
                                                                            bookStoreRef.get()
                                                                                .addOnSuccessListener {
                                                                                    if (it.exists()) {
                                                                                        val bookStoreData = it.data
                                                                                        bookStoreName =
                                                                                            bookStoreData?.get("storeName")
                                                                                                .toString()
                                                                                        shopEmail =
                                                                                            bookStoreData?.get("email")
                                                                                                .toString()
                                                                                        shopFullName = bookStoreName
                                                                                    }
                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    // Handle any errors
                                                                                    Log.e(
                                                                                        "getStoreNameError",
                                                                                        "Error getting document: $exception"
                                                                                    )
                                                                                }


                                                                            // Fetch books after getting store name
                                                                            db.collection("books")
                                                                                .whereEqualTo("bookID", itemId)
                                                                                .get()
                                                                                .addOnSuccessListener { bookQuerySnapshot ->
                                                                                    for (bookDocument in bookQuerySnapshot) {
                                                                                        val bookData =
                                                                                            bookDocument.data
                                                                                        val id = itemId
                                                                                        val name =
                                                                                            bookData["name"].toString()
                                                                                        val author =
                                                                                            bookData["author"].toString()
                                                                                        val imageUrl =
                                                                                            bookData["image"].toString()

                                                                                        val itemMap2 =
                                                                                            itemData as? Map<*, *>
                                                                                        val genre =
                                                                                            bookData["genre"] as String

                                                                                        val price =
                                                                                            (itemMap2?.get("totalSum") as Number).toLong()
                                                                                        val quantity =
                                                                                            itemMap2.get("quantity") as Long
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.store
                                                                                        ) + ": " + bookStoreName + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_name
                                                                                        ) + ": " + name + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.manageshop_mybooks_add_author
                                                                                        ) + ": " + author + "\n"
                                                                                        orderDetail += exContext.getString(
                                                                                            R.string.quantity
                                                                                        ) + " " + quantity.toString() + "\n\n"
                                                                                    }

                                                                                }
                                                                                .addOnFailureListener { exception ->
                                                                                    Log.e(
                                                                                        "Loi",
                                                                                        exception.toString()
                                                                                    )
                                                                                }
                                                                        }
                                                                    }
                                                                    .addOnFailureListener { exception ->
                                                                        Log.e("Loi", exception.toString())
                                                                    }
                                                            }
                                                        }
                                                    Tasks.whenAllComplete(fetchBookNamesTasks!!)
                                                        .addOnSuccessListener {
                                                            val emailSender = EmailSender(exContext)
                                                            val emailSubject =
                                                                exContext.getString(R.string.orderReturnSuccessSubject)
                                                            val shopEmailSubject =
                                                                exContext.getString(R.string.orderReturnSuccessSubject)
                                                            val totalMoney =
                                                                (orderData?.get("totalSum") as Number).toLong()
                                                            val status = orderData?.get("status") as String
                                                            val timeStamp =
                                                                orderData?.get("creationDate") as Timestamp

                                                            val date: Date? = timeStamp?.toDate()
                                                            val sdf = SimpleDateFormat("dd-MM-yyyy")
                                                            val paymentMethod =
                                                                orderData?.get("paymentMethod") as? Map<String, Any>
                                                            val paymentMethodType =
                                                                paymentMethod?.get("Type").toString()
                                                            val shippingAddress =
                                                                orderData?.get("shippingAddress") as String
                                                            val beforeDiscount =
                                                                (orderData?.get("beforeDiscount") as Number).toLong()
                                                            var emailBody = ""
                                                            var shopEmailBody = ""
                                                            var userFullName = ""
                                                            var userEmail = ""

                                                            val statusEmail = changeStatusText(status)
                                                            val discount =
                                                                "-" + formatMoney(beforeDiscount - totalMoney)
                                                            var orderMessage = ""
                                                            Log.d("emailHcmus", hcmusBook.toString())
                                                            if (!hcmusBook) {
                                                                orderMessage = exContext.getString(R.string.email_return_successfully)
                                                            } else {
                                                                orderMessage =
                                                                    exContext.getString(R.string.email_return_successfully)
                                                            }

                                                            val shopOrderMessage = exContext.getString(R.string.email_return_aceppted_by_shop)
                                                            db.collection("accounts")
                                                                .whereEqualTo("UID", customerID)
                                                                .get()
                                                                .addOnSuccessListener { querySnapshot ->
                                                                    for (snapshot in querySnapshot) {
                                                                        Log.d("emailsnapshot", snapshot.toString())
                                                                        val userData = snapshot.data
                                                                        userFullName =
                                                                            userData["fullname"].toString()
                                                                        val bookStoreRef =
                                                                            userData["store"] as DocumentReference
                                                                        bookStoreRef.get()
                                                                            .addOnSuccessListener {
                                                                                if (it.exists()) {
                                                                                    val bookStoreData = it.data
                                                                                    userEmail =
                                                                                        bookStoreData?.get("email")
                                                                                            .toString()
                                                                                    emailBody = emailSender.setBodyReturnWithResult(
                                                                                        userFullName,
                                                                                        orderMessage,
                                                                                        requestID,
                                                                                        changeStatusText("trả thành công"),
                                                                                        sdf.format(
                                                                                            Date()
                                                                                        ).toString(),
                                                                                        reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                        shopReasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                    )
                                                                                    shopEmailBody =
                                                                                        emailSender.setBodyReturnWithResult(
                                                                                            shopFullName,
                                                                                            shopOrderMessage,
                                                                                            requestID,
                                                                                            changeStatusText("trả thành công"),
                                                                                            sdf.format(
                                                                                                Date()
                                                                                            ).toString(),
                                                                                            reasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                            shopReasonField.text.toString().takeIf { it.isNotBlank() } ?: exContext.getString(R.string.no_reason_provided),
                                                                                        )
                                                                                    Log.d(
                                                                                        "userEmail",
                                                                                        "user " + userEmail
                                                                                    )
                                                                                    if (userSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            userEmail,
                                                                                            emailSubject,
                                                                                            emailBody
                                                                                        )
                                                                                    }

                                                                                    Log.d(
                                                                                        "shopEmail",
                                                                                        "shop " + shopEmail
                                                                                    )
                                                                                    if (shopSetting) {
                                                                                        emailSender.sendEmail(
                                                                                            shopEmail,
                                                                                            shopEmailSubject,
                                                                                            shopEmailBody
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                            .addOnFailureListener { exception ->
                                                                                // Handle any errors
                                                                                Log.e(
                                                                                    "bookStoreRef",
                                                                                    "Error getting document: $exception"
                                                                                )
                                                                            }

                                                                    }


                                                                }
                                                        }
                                                }
                                            requireActivity().onBackPressedDispatcher.onBackPressed()
                                        }
                                        ?.addOnFailureListener { e ->
                                            Log.e("orderDocument", "Error updating document", e)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    Log.w("returnDocument", "Error updating document", e)
                                }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {
                        }
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailReturnAcceptLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }

        return view
    }

    fun changeStatusText(status: String): String {
        return when {
            status.contains("xử lý", true) -> exContext.getString(R.string.my_order_processing_button)
            status.contains("huỷ", true) -> exContext.getString(R.string.my_order_cancelled_button)
            status.contains(
                "trả đang duyệt",
                true
            ) -> exContext.getString(R.string.my_order_detail_item_return_in_process)

            status.contains(
                "trả thành công",
                true
            ) -> exContext.getString(R.string.my_order_detail_item_return_success)

            status.contains(
                "trả bị từ chối",
                true
            ) -> exContext.getString(R.string.my_order_detail_item_return_failed)

            status.contains("thành công", true) -> exContext.getString(R.string.my_order_completed_button)
            status.contains("đã giao", true) -> exContext.getString(R.string.my_order_delivered_button)
            else -> ""
        }
    }

    fun getFileNameFromUri(uri: Uri): String {
        val file = File(uri.path)
        return file.name
    }

    fun formatMoney(number: Long): String {
        val numberString = number.toString()
        val regex = "(\\d)(?=(\\d{3})+$)".toRegex()
        return numberString.replace(regex, "$1.") + " VND"
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

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(orderID: String) =
            OrderDetailCaseShopReturnFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }

    fun dpToPx(context: Context, dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            context.resources.displayMetrics
        ).toInt()
    }
    private fun setImageToImageView2() {
        selectedImageUri?.let { uri ->
            Picasso.get().load(uri).into(imagePicker2)

        }

    }
    // Lấy ảnh từ user (Hai?)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}