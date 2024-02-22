import android.content.Context
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class rememberMeManager(context: Context) {

    // Create the dataStore and give it a name same as shared preferences
    private val dataStore = context.createDataStore(name = "remember_me_manager")

    // Create some keys we will use them to store and retrieve the data
    companion object {
        val rememberMeStatus = preferencesKey<Boolean>("rmStatus")
        val email = preferencesKey<String>("remember_email")
    }

    // Store user data
    // refer to the data store and using edit
    // we can store values using the keys
    suspend fun storeUser(rememberStatus: Boolean, rememberEmail: String) {
        dataStore.edit {
            it[rememberMeStatus] = rememberStatus
            it[email] = rememberEmail
        }
    }

    // Create an age flow to retrieve age from the preferences
    // flow comes from the kotlin coroutine
    val rmFlow: Flow<Boolean> = dataStore.data.map {
        it[rememberMeStatus] ?: false
    }

    // Create a name flow to retrieve name from the preferences
    val emailFlow: Flow<String> = dataStore.data.map {
        it[email] ?: ""
    }
}
