package com.example.booklette

import ShipAddressObject
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import com.example.booklette.databinding.AddShipAddressBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.maxkeppeler.sheets.core.PositiveListener
import com.maxkeppeler.sheets.core.Sheet
import kotlin.math.log

class AddShipAddressDialog: Sheet() {
    override val dialogTag = "Add Ship Address Sheet"
    private lateinit var binding: AddShipAddressBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    fun onPositive(positiveListener: PositiveListener) {
        this.positiveListener = positiveListener
    }

    fun onPositive(@StringRes positiveRes: Int, positiveListener: PositiveListener? = null) {
        this.positiveText = windowContext.getString(positiveRes)
        this.positiveListener = positiveListener
    }

    fun onPositive(positiveText: String, positiveListener: PositiveListener? = null) {
        this.positiveText = positiveText
        this.positiveListener = positiveListener
    }
    override fun onCreateLayoutView(): View {
        return AddShipAddressBinding.inflate(LayoutInflater.from(activity)).also { binding = it }.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.displayButtonsView(false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnAddNewShipAddress.setOnClickListener {
            saveAddressToFirebase()
        }


//        setButtonPositiveListener {  } If you want to override the default positive click listener
//        displayButtonsView() If you want to change the visibility of the buttons view
//        displayButtonPositive() Hiding the positive button will prevent clicks
         //Hide the toolbar of the sheet, the title and the icon
    }

    private fun saveAddressToFirebase() {
        val receiverName = binding.addRecieverName.text.toString()
        val receiverPhone = binding.addRecieverPhone.text.toString()
        val province = binding.addProvince.text.toString()
        val city = binding.addCity.text.toString()
        val ward = binding.addWard.text.toString()
        val addressNumber = binding.addAddressNumber.text.toString()
        var chipText = "Home"


        // Validate input fields
        if (receiverName.isEmpty() || receiverPhone.isEmpty() || province.isEmpty() ||
            city.isEmpty() || ward.isEmpty() || addressNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedChipId = binding.labelGroup.checkedChipId
        val selectedChip = binding.labelGroup.findViewById<Chip>(selectedChipId)
        chipText = selectedChip?.text?.toString() ?: "Default Text"

        val docAddressRef = db.collection("accounts").whereEqualTo("UID", auth.uid)
        docAddressRef.get().addOnSuccessListener { accountList ->
            for (account in accountList) {
                val addressList = account.get("shippingAddress") as ArrayList<Map<String, Any>>

                // Create a new ShipAddressObject for the new address
                val address = mapOf(
                    "receiverName" to receiverName,
                    "receiverPhone" to receiverPhone,
                    "province" to province,
                    "city" to city,
                    "ward" to ward,
                    "addressNumber" to addressNumber,
                    "shipLabel" to chipText,
                    "isDefault" to false
                )

                // Add the new address to the existing list of addresses
                addressList.add(0, address)

                // Update the "shippingAddress" field in the user's document
                account.reference.update("shippingAddress", addressList)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Address added successfully", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                    .addOnFailureListener { exception ->
                        Log.e("error", "Failed to add address", exception)
                        Toast.makeText(requireContext(), "Failed to add address", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }



    fun build(ctx: Context, width: Int? = null, func: AddShipAddressDialog.() -> Unit): AddShipAddressDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        return this
    }

    /** Build and show [AddShipAddressDialog] directly. */
    fun show(ctx: Context, width: Int? = null, func: AddShipAddressDialog.() -> Unit): AddShipAddressDialog {
        this.windowContext = ctx
        this.width = width
        this.func()
        this.show()
        return this
    }
}