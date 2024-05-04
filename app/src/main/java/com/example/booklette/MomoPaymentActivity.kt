package com.example.booklette

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.booklette.databinding.ActivityMomoPaymentBinding
import org.json.JSONException
import org.json.JSONObject
import vn.momo.momo_partner.AppMoMoLib
import vn.momo.momo_partner.MoMoParameterNamePayment
import java.util.UUID


class MomoPaymentActivity : AppCompatActivity() {
    private var amount = "10000"
    private val fee = "0"
    private var environment = 0 //developer default

    private val merchantName = "Booklette"
    private val merchantCode = "Booklette2024"
    private val merchantNameLabel = "Nhà cung cấp"
    private val description = "Book Market"
    private lateinit var binding: ActivityMomoPaymentBinding

    lateinit var tvEnvironment: TextView
    lateinit var tvMerchantCode: TextView
    lateinit var tvMerchantName: TextView
    lateinit var edAmount: EditText
    lateinit var tvMessage: TextView
    lateinit var btnPayMoMo: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMomoPaymentBinding.inflate(layoutInflater)
        val view = binding.root

        tvEnvironment = binding.tvEnvironment
        tvMerchantCode = binding.tvMerchantCode
        tvMerchantName = binding.tvMerchantName
        edAmount = binding.edAmount
        tvMessage = binding.tvMessage
        btnPayMoMo = binding.btnPayMoMo

        val data = intent.extras
        if (data != null) {
            environment = data.getInt(MoMoConstants.KEY_ENVIRONMENT)
        }
        if (environment == 0) {
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEBUG)
            tvEnvironment.text = "Debug Environment"
        } else if (environment == 1) {
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.DEVELOPMENT)
            tvEnvironment.text = "Development Environment"
        } else if (environment == 2) {
            AppMoMoLib.getInstance().setEnvironment(AppMoMoLib.ENVIRONMENT.PRODUCTION)
            tvEnvironment.text = "PRODUCTION Environment"
        }

        tvMerchantCode.text = "Merchant Code: $merchantCode"
        tvMerchantName.text = "Merchant Name: $merchantName"

        btnPayMoMo.setOnClickListener {
            requestPayment()
        }

        setContentView(view)

    }

    //example payment
    private fun requestPayment() {
        AppMoMoLib.getInstance().setAction(AppMoMoLib.ACTION.PAYMENT)
        AppMoMoLib.getInstance().setActionType(AppMoMoLib.ACTION_TYPE.GET_TOKEN)
        if (edAmount.getText().toString() != null && edAmount.getText().toString().trim()
                .length !== 0
        ) amount = edAmount.getText().toString().trim()
        val eventValue: MutableMap<String, Any> = HashMap()
        //client Required
        eventValue[MoMoParameterNamePayment.MERCHANT_NAME] = merchantName
        eventValue[MoMoParameterNamePayment.MERCHANT_CODE] = merchantCode
        eventValue[MoMoParameterNamePayment.AMOUNT] = amount
        eventValue[MoMoParameterNamePayment.DESCRIPTION] = description
        //client Optional
        eventValue[MoMoParameterNamePayment.FEE] = fee
        eventValue[MoMoParameterNamePayment.MERCHANT_NAME_LABEL] = merchantNameLabel
        eventValue[MoMoParameterNamePayment.REQUEST_ID] =
            merchantCode + "-" + UUID.randomUUID().toString()
        eventValue[MoMoParameterNamePayment.PARTNER_CODE] = "CGV19072017"
        val objExtraData = JSONObject()
        try {
            objExtraData.put("site_code", "008")
            objExtraData.put("site_name", "CGV Cresent Mall")
            objExtraData.put("screen_code", 0)
            objExtraData.put("screen_name", "Special")
            objExtraData.put("movie_name", "Kẻ Trộm Mặt Trăng 3")
            objExtraData.put("movie_format", "2D")
            objExtraData.put(
                "ticket",
                "{\"ticket\":{\"01\":{\"type\":\"std\",\"price\":110000,\"qty\":3}}}"
            )
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        eventValue[MoMoParameterNamePayment.EXTRA_DATA] = objExtraData.toString()
        eventValue[MoMoParameterNamePayment.REQUEST_TYPE] = "payment"
        eventValue[MoMoParameterNamePayment.LANGUAGE] = "vi"
        eventValue[MoMoParameterNamePayment.EXTRA] = "true"

        //Request momo app
        AppMoMoLib.getInstance().requestMoMoCallBack(this, eventValue)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppMoMoLib.getInstance().REQUEST_CODE_MOMO && resultCode == -1) {
            if (data != null) {
                if (data.getIntExtra("status", -1) == 0) {
                    tvMessage.setText("message: " + "Get token " + data.getStringExtra("message"))
                    if (data.getStringExtra("data") != null && data.getStringExtra("data") != "") {
                        // TODO:
                    } else {
                        tvMessage.setText("message: " + "ko co thong tin")
                    }
                } else if (data.getIntExtra("status", -1) == 1) {
                    val message =
                        if (data.getStringExtra("message") != null) data.getStringExtra("message") else "Thất bại"
                    tvMessage.setText("message: $message")
                } else if (data.getIntExtra("status", -1) == 2) {
                    tvMessage.setText("message: " + "ko co thong tin")
                } else {
                    tvMessage.setText("message: " + "ko co thong tin")
                }
            } else {
                tvMessage.setText("message: " + "ko co thong tin")
            }
        } else {
            tvMessage.setText("message: " + "ko co thong tin")
        }
    }


}