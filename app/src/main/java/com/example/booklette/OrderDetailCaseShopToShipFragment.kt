package com.example.booklette

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

import com.example.booklette.databinding.FragmentShopOrderDetailToShipBinding
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Date

/**
 * A simple [Fragment] subclass.
 * Use the [OrderDetailCaseShopToShipFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
private const val ORDERID_PARAM = "param1"

class OrderDetailCaseShopToShipFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var storeUID: String = ""
    private var orderID: String? = null
    private var _binding: FragmentShopOrderDetailToShipBinding? = null
    private var itemsMap: Map<String, Map<String, Any>>? = null

    private lateinit var exContext: Context

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private lateinit var db: FirebaseFirestore
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
        _binding = FragmentShopOrderDetailToShipBinding.inflate(inflater, container, false)
        val view = binding.root

        db = Firebase.firestore
        exContext = requireContext()
        val numberField: TextView = binding.orderDetailNumberField
        val dateField = binding.orderDetailDateField
        val trackingNumberField = binding.orderDetailTrackingNumberField
        val statusField = binding.orderDetailStatusField
        val buyerNameField = binding.orderDetailBuyerNameField
        val shippingAddressField = binding.orderDetailShippingAddressField
        val paymentMethodField = binding.orderDetailPaymentMethodField
        val beforeDiscountField = binding.orderDetailBeforeDiscountField
        val discountField = binding.orderDetailDiscountField
        val totalField = binding.orderDetailTotalField


        val orderItemLayout = binding.orderDetailProductsFragmentFrameLayout


        var tempTotalOrgMoney: Long = 0
        var customerID = ""
        var userSetting = true
        var shopSetting = true
        var hcmusBook = false
        orderID?.let {
            db.collection("orders")
                .document(it)
                .get()
                .addOnSuccessListener { document ->
                    val orderData = document.data
                    val timeStamp = orderData?.get("creationDate") as Timestamp
                    customerID = orderData?.get("customerID").toString()
                    storeUID = customerID.toString()
                    val customerRef = db.collection("accounts").whereEqualTo("UID", customerID)
                    var customerName = "Minh Bảo"
                    customerRef.get()
                        .addOnSuccessListener { QuerySnapshot ->
                            for (userDocument in QuerySnapshot) {
                                val userData = userDocument.data
                                customerName = userData?.get("fullname").toString()
                            }
                        }
                    val date: Date? = timeStamp?.toDate()
                    itemsMap = orderData?.get("items") as? Map<String, Map<String, Any>>
                    var totalQuantity: Long = 0
//                        itemsMap?.forEach { (itemID, itemData) ->
//
//                            val itemMap = itemData as? Map<String, Any>
//
//                            //Log.d("number",itemMap.toString())
//                            tempTotalOrgMoney += (itemMap?.get("totalSum") as Number).toFloat()
//                            totalQuantity += itemMap?.get("quantity") as Long
//                        }
                    val totalMoney = (orderData?.get("totalSum") as Number).toLong()
                    val status = orderData?.get("status") as String

                    val paymentMethod = orderData?.get("paymentMethod") as? Map<String, Any>
                    val paymentMethodType = paymentMethod?.get("Type")
                    val shippingAddress = orderData?.get("shippingAddress") as String
                    val beforeDiscount = (orderData?.get("beforeDiscount") as Number).toLong()
                    // assign to field in view
                    var orderName = ""
                    val fetchBookNamesTasks = itemsMap?.flatMap { (shopID, itemMap) ->
                        itemMap.map { (itemId, itemData) ->
                            Log.d("shopID", shopID)
                            Log.d("itemId", itemId)
                            Log.d("itemData", itemData.toString())

                            db.collection("books")
                                .whereEqualTo("bookID", itemId)
                                .get()
                                .addOnSuccessListener { bookSnapshot ->
                                    for (book in bookSnapshot.documents) {
                                        val bookData = book.data
                                        val bookName = bookData?.get("name") as? String
                                        if (!bookName.isNullOrEmpty()) {
                                            orderName += "$bookName, "
                                        }
                                        val genre = bookData?.get("genre") as String
                                        if (genre == "Hcmus-book") {
                                            hcmusBook = true
                                        }
                                    }
                                }
                        }
                    }
                    // setup recycler view for books
                    val itemsFragment = OrderDetailItemListFragment.newInstance(
                        1,
                        itemsMap!!,
                        allowSelection = false,
                        allowMultipleSelection = false
                    )
                    childFragmentManager.beginTransaction()
                        .replace(orderItemLayout.id, itemsFragment)
                        .commit()
                    Tasks.whenAllComplete(fetchBookNamesTasks!!)
                        .addOnSuccessListener {
                            if (orderName.length >= 2) {
                                orderName = orderName.dropLast(2)
                            }
                            if (orderName.length > 20) {
                                orderName = orderName.substring(0, 20) + "..."
                            }
                            numberField.text = orderName
                            val sdf = SimpleDateFormat("dd-MM-yyyy")
                            dateField.text = sdf.format(date)

                            buyerNameField.text = customerName
                            trackingNumberField.text = orderID
                            statusField.text = changeStatusText(status)
                            shippingAddressField.text = shippingAddress
                            paymentMethodField.text = paymentMethodType.toString()
                            beforeDiscountField.text = formatMoney(beforeDiscount)
                            discountField.text = "-" + formatMoney(beforeDiscount - totalMoney)
                            val formattedMoney = formatMoney(totalMoney)
                            totalField.text = formattedMoney
                        }

                }
        }


        // back
        val backButton = binding.backButton
        backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        // cancel order
        val cancelButton = binding.orderDetailCancelOrderButton
        cancelButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val docRef =
                                orderID?.let { it1 -> db.collection("orders").document(it1) }
                            db.collection("accounts")
                                .whereEqualTo("UID", customerID)
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
                                                exContext.getString(R.string.orderCancelSubject)
                                            val shopEmailSubject =
                                                exContext.getString(R.string.orderCancelSubject)
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
                                                orderMessage = exContext.getString(R.string.email_canceled)
                                            } else {
                                                orderMessage =
                                                    exContext.getString(R.string.email_canceled)
                                            }

                                            val shopOrderMessage = exContext.getString(R.string.email_canceled)
                                            db.collection("accounts")
                                                .whereEqualTo("UID", customerID)
                                                .get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    for (snapshot in querySnapshot) {
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
                                                                    emailBody = emailSender.setBody(
                                                                        userFullName,
                                                                        orderMessage,
                                                                        documentSnapshot.id,
                                                                        statusEmail,
                                                                        sdf.format(date),
                                                                        orderDetail,
                                                                        shippingAddress,
                                                                        paymentMethodType,
                                                                        formatMoney(beforeDiscount),
                                                                        discount,
                                                                        formatMoney(totalMoney),
                                                                    )
                                                                    shopEmailBody =
                                                                        emailSender.setBody(
                                                                            shopFullName,
                                                                            shopOrderMessage,
                                                                            documentSnapshot.id,
                                                                            statusEmail,
                                                                            sdf.format(date),
                                                                            orderDetail,
                                                                            shippingAddress,
                                                                            paymentMethodType,
                                                                            formatMoney(
                                                                                beforeDiscount
                                                                            ),
                                                                            discount,
                                                                            formatMoney(totalMoney),
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
                            docRef?.update("status", "Bị huỷ")?.addOnSuccessListener {
                                // will change to motion toast later
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailCancelArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }?.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailFailedArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailProcessingCancelLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }
        // accept order
        val acceptButton = binding.orderDetailAcceptOrderButton
        acceptButton.setOnClickListener {
            val dialogClickListener =
                DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            val docRef =
                                orderID?.let { it1 -> db.collection("orders").document(it1) }



                            db.collection("accounts")
                                .whereEqualTo("UID", customerID)
                                .get().addOnSuccessListener { documents ->
                                    for (document in documents) {
                                        val userData = document.data
                                        userSetting = userData?.get("deliveryNotif") as Boolean

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
                                                            shopSetting =
                                                                storeData["salesNotif"] as Boolean
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
                                            val emailSender = EmailSender(requireContext())
                                            val emailSubject =
                                                exContext.getString(R.string.orderReiceivedSubject)
                                            val shopEmailSubject =
                                                exContext.getString(R.string.orderReiceivedSubject)
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
                                                orderMessage = getString(R.string.email_delivered)
                                            } else {
                                                orderMessage =
                                                    getString(R.string.email_delivered_hcmus)
                                            }

//                                            val shopOrderMessage = getString(R.string.email_has_order)
                                            db.collection("accounts")
                                                .whereEqualTo("UID", customerID)
                                                .get()
                                                .addOnSuccessListener { querySnapshot ->
                                                    for (snapshot in querySnapshot) {
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
                                                                    emailBody = emailSender.setBody(
                                                                        userFullName,
                                                                        orderMessage,
                                                                        documentSnapshot.id,
                                                                        statusEmail,
                                                                        sdf.format(date),
                                                                        orderDetail,
                                                                        shippingAddress,
                                                                        paymentMethodType,
                                                                        formatMoney(beforeDiscount),
                                                                        discount,
                                                                        formatMoney(totalMoney),
                                                                    )
                                                                    //                                                    shopEmailBody = emailSender.setBody(
                                                                    //                                                        shopFullName,
                                                                    //                                                        shopOrderMessage,
                                                                    //                                                        documentSnapshot.id,
                                                                    //                                                        statusEmail,
                                                                    //                                                        sdf.format(date),
                                                                    //                                                        orderDetail,
                                                                    //                                                        shippingAddress,
                                                                    //                                                        paymentMethodType,
                                                                    //                                                        formatMoney(beforeDiscount),
                                                                    //                                                        discount,
                                                                    //                                                        formatMoney(totalMoney),
                                                                    //                                                    )
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

                                                                    //                                                    Log.d("shopEmail","shop " + shopEmail)
                                                                    //                                                    if (shopSetting){
                                                                    //                                                        emailSender.sendEmail(
                                                                    //                                                            shopEmail,
                                                                    //                                                            shopEmailSubject,
                                                                    //                                                            shopEmailBody
                                                                    //                                                        )
                                                                    //                                                    }
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
                            docRef?.update("status", "Đã giao")?.addOnSuccessListener {
                                // will change to motion toast later
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailAcceptSuccessfulArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().onBackPressedDispatcher.onBackPressed()
                            }?.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    R.string.orderDetailAcceptFailedArgument,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        DialogInterface.BUTTON_NEGATIVE -> {}
                    }
                }

            val builder: AlertDialog.Builder = AlertDialog.Builder(context)

            builder.setMessage(R.string.orderDetailToShipAcceptLabel)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener).show()
        }

        val contactButton = binding.orderDetailContactBuyerButton
        contactButton.setOnClickListener {
            val intent = Intent(context, ChannelChatActivity::class.java)
            intent.putExtra("storeUID", storeUID)

            startActivity(intent)

        }
        return view
    }

    fun changeStatusText(status: String): String {
        return when {
            status.contains(
                "xử lý",
                true
            ) -> exContext.getString(R.string.my_order_processing_button)

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

            status.contains(
                "thành công",
                true
            ) -> exContext.getString(R.string.my_order_completed_button)

            status.contains(
                "đã giao",
                true
            ) -> exContext.getString(R.string.my_order_delivered_button)

            else -> ""
        }
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
            OrderDetailCaseShopToShipFragment().apply {
                arguments = Bundle().apply {
                    putString(ORDERID_PARAM, orderID)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}