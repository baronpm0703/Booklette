package com.example.booklette

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(private val context: Context) {

    val username = "kiznlh@gmail.com"
    // will remove this later for secure purpose
    val password = "hilo zpmx rhhk kulv"
    fun sendEmail(recipientEmail: String, subject: String, body: String) {
        GlobalScope.launch(Dispatchers.IO){
            val props = Properties()
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(username, password)
                }
            })
            try {
                val message = MimeMessage(session)
                message.setFrom(InternetAddress(username))
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                message.subject = subject
                message.setText(body)

                Transport.send(message)

                Log.d("email","Email sent successfully.")

            } catch (e: MessagingException) {
                Log.e("email", e.toString())
            }
        }

    }
    fun getSubject(): String {

        var subject: String = ""

        return subject
    }
    fun setBody(
        userName: String,
        orderMessage: String,
        orderID: String,
        status: String,
        date: String,
        bitmap: Bitmap,
        address: String,
        paymentMethod: String,
        totalAmount: String,
        discount: String,
        finalPrice: String
    ): String {
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append("$orderMessage\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $orderID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />") // Inline image
            append("\n")
            append(context.getString(R.string.shipping_address) +" $address\n")
            append(context.getString(R.string.payment_method) + " $paymentMethod\n")
            append(context.getString(R.string.total_amount) + " $totalAmount\n")
            append(context.getString(R.string.discount) + " $discount\n")
            append(context.getString(R.string.total_amount) + " $finalPrice\n\n")
            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }
    fun setBodyOrderCreated(
        userName: String,
        orderID: String,
        status: String,
        date: String,
        bitmap: Bitmap,
        address: String,
        paymentMethod: String,
        totalAmount: String,
        discount: String,
        finalPrice: String
    ): String {
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append(context.getString(R.string.email_created) + "\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $orderID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            append(context.getString(R.string.shipping_address) +" $address\n")
            append(context.getString(R.string.payment_method) + " $paymentMethod\n")
            append(context.getString(R.string.total_amount) + " $totalAmount\n")
            append(context.getString(R.string.discount) + " $discount\n")
            append(context.getString(R.string.total_amount) + " $finalPrice\n\n")
            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }
    fun setBodyOrderDeliveried(
        userName: String,
        orderMessage: String,
        orderID: String,
        status: String,
        date: String,
        bitmap: Bitmap,
        address: String,
        paymentMethod: String,
        totalAmount: String,
        discount: String,
        finalPrice: String
    ): String {
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append("$orderMessage\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $orderID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            append(context.getString(R.string.shipping_address) +" $address\n")
            append(context.getString(R.string.payment_method) + " $paymentMethod\n")
            append(context.getString(R.string.total_amount) + " $totalAmount\n")
            append(context.getString(R.string.discount) + " $discount\n")
            append(context.getString(R.string.total_amount) + " $finalPrice\n\n")
            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }
    fun setBodyOrderCompleted(
        userName: String,
        orderMessage: String,
        orderID: String,
        status: String,
        date: String,
        bitmap: Bitmap,
        address: String,
        paymentMethod: String,
        totalAmount: String,
        discount: String,
        finalPrice: String
    ): String {
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append("$orderMessage\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $orderID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            append(context.getString(R.string.shipping_address) +" $address\n")
            append(context.getString(R.string.payment_method) + " $paymentMethod\n")
            append(context.getString(R.string.total_amount) + " $totalAmount\n")
            append(context.getString(R.string.discount) + " $discount\n")
            append(context.getString(R.string.total_amount) + " $finalPrice\n\n")
            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }

    fun setBodyReturn(
        userName: String,
        orderMessage: String,
        requestID: String,
        status: String,
        date: String,
        userReason: String,
        bitmap: Bitmap,
    ): String{
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append("$orderMessage\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $requestID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            append(context.getString(R.string.buyerReasonLabel) +" $userReason\n")
            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }
    fun setBodyReturnWithResult(
        userName: String,
        orderMessage: String,
        requestID: String,
        status: String,
        date: String,
        userReason: String,
        sellerReason: String,
        bitmap: Bitmap,
    ): String{
        val stringBuilder = StringBuilder()

        stringBuilder.apply {
            append(context.getString(R.string.email_welcome) + " $userName" +", \n")
            append("$orderMessage\n\n")
            append(context.getString(R.string.order_item_tracking_number) + " $requestID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            if (userReason.isNotEmpty()){
                append(context.getString(R.string.buyerReasonLabel) +" $userReason\n")
            }
            else append(context.getString(R.string.buyerReasonLabel) + context.getString(R.string.no_reason_provided) + "\n")
            if (sellerReason.isNotEmpty()){
                append(context.getString(R.string.shopReasonLabel) +" $sellerReason\n")
            }
            else append(context.getString(R.string.shopReasonLabel) + context.getString(R.string.no_reason_provided) + "\n")

            append(context.getString(R.string.email_thank_you))
        }

        return stringBuilder.toString()
    }
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.getEncoder().encodeToString(byteArray)
    }
}