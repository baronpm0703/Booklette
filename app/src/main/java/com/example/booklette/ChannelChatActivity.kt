package com.example.booklette

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import com.example.booklette.databinding.ActivityChannelChatBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView

class ChannelChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChannelChatBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var storeUID = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChannelChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storeUID = intent.getStringExtra("storeUID").toString()

        auth = Firebase.auth
        db = Firebase.firestore

        val offlinePluginFactory = StreamOfflinePluginFactory(appContext = this)
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true,
            ),
            appContext = this,
        )

        val client = ChatClient.Builder("egx6qn892ejq", applicationContext)
            .withPlugins(offlinePluginFactory, statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()

        db.collection("accounts").whereEqualTo("UID", auth.currentUser!!.uid).get().addOnSuccessListener { result ->
            val document = result.documents[0]

            val user = User(
                id = auth.currentUser!!.uid,
                name = document.data!!.get("fullname").toString(),
                image = document.data!!.get("avt").toString()
            )

            client.connectUser(
                user = user,
                token = client.devToken(user.id)
            ).enqueue {
                if (it.isSuccess) {
                    val filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.`in`("members", listOf(user.id))
                    )
                    val viewModelFactory =
                        ChannelListViewModelFactory(filter, ChannelListViewModel.DEFAULT_SORT)
                    val viewModel: ChannelListViewModel by viewModels { viewModelFactory }

                    viewModel.bindView(binding.channelListView, this)
                    binding.channelListView.setChannelItemClickListener { channel ->
                        startActivity(ChannelMessageActivity.newIntent(this, channel))
                    }

                    if (storeUID != null && storeUID != "") {
                        client.createChannel(
                            channelType = "messaging",
                            channelId = "",
                            memberIds = listOf(auth.currentUser!!.uid.toString(), storeUID),
                            extraData = emptyMap()
                        ).enqueue { result ->
                            if (result.isSuccess) {
                                Toast.makeText(this@ChannelChatActivity, "SUCCESSFULLY!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@ChannelChatActivity, "FAILED!", Toast.LENGTH_SHORT).show()
                                Log.e("channelError", result.toString())
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "something went wrong!", Toast.LENGTH_SHORT).show()
                    Log.e("chat", it.toString())
                }
            }
        }
    }
}