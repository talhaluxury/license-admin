package com.talha.licenseadmin

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * !!! CHANGE THIS before you build and install this app !!!
 * The only email allowed to sign in here and control everyone's access.
 * Must exactly match a Firebase Auth account's email on this project, AND must
 * exactly match the email in firestore.rules' isSuperAdmin() function.
 */
const val ADMIN_EMAIL = "ubaidquershi221@gmail.com"

// Same Firebase project as the website, RestaurantPOS, Rider app and Desktop app.
class LicenseAdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            val options = FirebaseOptions.Builder()
                .setApiKey("AIzaSyAfB61zHgxWxnzbWOvFPVhvn0Yz9ejZ9-A")
                .setApplicationId("1:1072408505617:web:939ae2d2be43d456e730b0")
                .setProjectId("bill-apps-3d675")
                .setDatabaseUrl("https://bill-apps-3d675-default-rtdb.firebaseio.com")
                .setStorageBucket("bill-apps-3d675.firebasestorage.app")
                .setGcmSenderId("1072408505617")
                .build()
            FirebaseApp.initializeApp(this, options)
        }
    }
}
