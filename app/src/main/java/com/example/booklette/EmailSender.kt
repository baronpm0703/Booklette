package com.example.booklette

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class EmailSender(private val context: Context) {

    val username = "kiznlh@gmail.com"
    val password = "mikr vppy trmr prnc"
    fun sendEmail(recipientEmail: String, subject: String, body: String) {
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
    fun getSubject(): String {

        var subject: String = ""

        return subject
    }
    fun setBody(
        orderName: String,
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
            append(context.getString(R.string.order_item_order) + " $orderName\n")
            append(context.getString(R.string.order_item_tracking_number) + " $orderID\n")
            append(context.getString(R.string.orderDetailStatus) + " $status\n")
            append(context.getString(R.string.orderDayCreated) + " $date\n")
            append("<img src=\"data:image/png;base64,${bitmapToBase64(bitmap)}\" />\n") // Inline image
            append(context.getString(R.string.shipping_address) +" $address\n")
            append(context.getString(R.string.payment_method) + " $paymentMethod\n")
            append(context.getString(R.string.total_amount) + " $totalAmount\n")
            append(context.getString(R.string.discount) + " $discount\n")
            append(context.getString(R.string.total_amount) + " $finalPrice\n")
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