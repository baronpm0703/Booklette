package com.example.booklette

import android.graphics.Bitmap
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract.CommonDataKinds.Im
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booklette.databinding.EditBookInShopDialogBinding
import com.example.booklette.databinding.FragmentManageshopBooksBinding
import com.example.booklette.databinding.FragmentManageshopDiscountprogramsBinding
import com.example.booklette.databinding.FragmentMyshopBinding
import com.example.booklette.databinding.FragmentProfilesettingBinding
import com.example.booklette.model.DiscountProgramObject
import com.example.booklette.model.HRecommendedBookObject
import com.example.booklette.model.ManageShopNewBookObject
import com.example.booklette.model.MyShopBookObject
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.maxkeppeler.sheets.core.SheetStyle
import com.squareup.picasso.Picasso
import io.getstream.chat.android.ui.common.state.messages.Edit
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import kotlin.math.abs
import kotlin.math.round

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileSettingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileSettingFragment : Fragment() {
	private lateinit var storeRef: DocumentReference

	private var _binding: FragmentProfilesettingBinding? = null

	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		_binding = FragmentProfilesettingBinding.inflate(inflater, container, false)
		val view = binding.root

		val auth = Firebase.auth
		val db = Firebase.firestore
		db.collection("accounts").whereEqualTo("UID", auth.uid).get()
			.addOnSuccessListener { documents ->
				if (documents.size() != 1) return@addOnSuccessListener    // Failsafe

				for (document in documents) {
					storeRef = document.getDocumentReference("store")!!

					val fullNameET = view.findViewById<EditText>(R.id.nameET)
					val dobET = view.findViewById<EditText>(R.id.dobET)
					val phoneET = view.findViewById<EditText>(R.id.phoneET)
					val emailET = view.findViewById<EditText>(R.id.emailET)
					val addressET = view.findViewById<EditText>(R.id.addressET)

					fullNameET.setText(document.getString("fullname").toString())
					val format = SimpleDateFormat("yyyy-MM-dd")
					dobET.setText(format.format((document.get("dob") as Timestamp).toDate()))
					phoneET.setText(document.getString("phone").toString())
					emailET.setText(auth.currentUser?.email)
					addressET.setText(document.getString("address").toString())

					editProfileDialog(view)
					changePasswordDialog(view)
				}
			}

		view.findViewById<ImageView>(R.id.backBtn).setOnClickListener {
			requireActivity().onBackPressedDispatcher.onBackPressed()
		}

		return view
	}

	private fun editProfileDialog(view: View) {
		val editProfileBtn = view.findViewById<TextView>(R.id.editProfileBtn)
		val editProfileDialog = ProfileSettingEditProfileDialog()
		editProfileBtn.setOnClickListener {
			activity?.let {
				editProfileDialog.show(it) {
					style(SheetStyle.BOTTOM_SHEET)
					onPositive {
						val db = Firebase.firestore
						val auth = Firebase.auth

						// Upload avt to storage
						val storageRef = Firebase.storage.reference
						val bitmap = editProfileDialog.avt
						val imageRef = storageRef.child("Accounts/${auth.uid}")
						val baos = ByteArrayOutputStream()
						if (bitmap != null) {
							bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
						}
						val data = baos.toByteArray()

						if (editProfileDialog.avt != null) {
							imageRef.putBytes(data).addOnSuccessListener {
								it.storage.downloadUrl.addOnSuccessListener { result ->
									val date = SimpleDateFormat("yyyy-MM-dd").parse(editProfileDialog.dob)
									val dob = date?.let { it1 -> Timestamp(it1) }
									val updatedData = hashMapOf(
										"fullname" to editProfileDialog.name,
										"phone" to editProfileDialog.phone,
										"address" to editProfileDialog.address,
										"dob" to dob,
										"avt" to result
									)

									db.collection("accounts").whereEqualTo("UID", auth.uid).get().addOnSuccessListener {
										val docs = it.documents

										for (doc in docs) {
											db.collection("accounts").document(doc.id).update(updatedData as Map<String, Any>).addOnSuccessListener {
												Handler().postDelayed({
													binding.nameET.setText(editProfileDialog.name)
													binding.dobET.setText(editProfileDialog.dob)
													binding.phoneET.setText(editProfileDialog.phone)
													binding.emailET.setText(editProfileDialog.email)
													binding.addressET.setText(editProfileDialog.address)

													editProfileDialog.dismiss()
												}, 10)
											}
										}
									}
								}
							}
						}
						else {
							val date = SimpleDateFormat("yyyy-MM-dd").parse(editProfileDialog.dob)
							val dob = date?.let { it1 -> Timestamp(it1) }
							val updatedData = hashMapOf(
								"fullname" to editProfileDialog.name,
								"phone" to editProfileDialog.phone,
								"address" to editProfileDialog.address,
								"dob" to dob
							)

							db.collection("accounts").whereEqualTo("UID", auth.uid).get().addOnSuccessListener {
								val docs = it.documents

								for (doc in docs) {
									db.collection("accounts").document(doc.id).update(updatedData as Map<String, Any>).addOnSuccessListener {
										Handler().postDelayed({
											binding.nameET.setText(editProfileDialog.name)
											binding.dobET.setText(editProfileDialog.dob)
											binding.phoneET.setText(editProfileDialog.phone)
											binding.emailET.setText(editProfileDialog.email)
											binding.addressET.setText(editProfileDialog.address)

											editProfileDialog.dismiss()
										}, 10)
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private fun changePasswordDialog(view: View) {
		val changePasswordBtn = view.findViewById<TextView>(R.id.changePasswordBtn)
		val changePasswordDialog = ProfileSettingChangePasswordDialog()
		changePasswordBtn.setOnClickListener {
			activity?.let {
				changePasswordDialog.show(it) {
					style(SheetStyle.BOTTOM_SHEET)
					onPositive {
						val auth = Firebase.auth
						val user = auth.currentUser

						val credential =
							user?.email?.let { it1 -> EmailAuthProvider.getCredential(it1, changePasswordDialog.oldPassword) }
						if (credential != null) {
							user.reauthenticate(credential).addOnSuccessListener {
								user.updatePassword(changePasswordDialog.newPassword).addOnSuccessListener {
									dismiss()
									Toast.makeText(context, resources.getString(R.string.profilesetting_password_change_success), Toast.LENGTH_SHORT).show()
								}
							}.addOnFailureListener {
								Toast.makeText(context, resources.getString(R.string.profilesetting_password_change_wrong), Toast.LENGTH_SHORT).show()
							}
						}
					}
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	fun changeFragmentContainer(fragment: Fragment) {
		parentFragmentManager.beginTransaction().replace(R.id.fcvNavigation, fragment).addToBackStack(null).commit()
	}
}