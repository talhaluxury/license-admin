@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.talha.licenseadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class License(
    val uid: String = "",
    val email: String = "",
    val restaurantName: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = 0L
)

class MainActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var loggedIn by remember { mutableStateOf(auth.currentUser != null && auth.currentUser?.email == ADMIN_EMAIL) }
            var loading by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFFFF9D2E))) {
                Surface(Modifier.fillMaxSize()) {
                    if (!loggedIn) {
                        LoginScreen(
                            loading = loading, error = error,
                            onLogin = { email, pass ->
                                loading = true; error = null
                                if (email.trim().lowercase() != ADMIN_EMAIL.lowercase()) {
                                    error = "This app is only for the admin account."
                                    loading = false
                                } else {
                                    auth.signInWithEmailAndPassword(email.trim(), pass)
                                        .addOnSuccessListener { loggedIn = true; loading = false }
                                        .addOnFailureListener { e -> error = e.message ?: "Login failed"; loading = false }
                                }
                            }
                        )
                    } else {
                        LicenseListScreen(firestore = firestore, onLogout = { auth.signOut(); loggedIn = false })
                    }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(loading: Boolean, error: String?, onLogin: (String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFFF9D2E)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.AdminPanelSettings, null, tint = Color.White, modifier = Modifier.size(34.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text("License Admin", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Control which customers can use the Desktop software.", color = Color.Gray, fontSize = 13.sp)
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Admin email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (error != null) { Spacer(Modifier.height(6.dp)); Text(error, color = Color.Red, fontSize = 12.sp) }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = { onLogin(email, password) }, enabled = !loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text(if (loading) "Signing in..." else "Login") }
    }
}

@Composable
fun LicenseListScreen(firestore: FirebaseFirestore, onLogout: () -> Unit) {
    var list by remember { mutableStateOf<List<License>>(emptyList()) }
    var busy by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val reg: ListenerRegistration = firestore.collection("licenses").addSnapshotListener { snap, _ ->
            if (snap != null) {
                list = snap.documents.map { d ->
                    License(
                        uid = d.id, email = d.getString("email") ?: "",
                        restaurantName = d.getString("restaurantName") ?: "",
                        enabled = d.getBoolean("enabled") ?: true,
                        createdAt = (d.get("createdAt") as? Number)?.toLong() ?: 0L
                    )
                }.sortedBy { it.restaurantName.ifBlank { it.email } }
            }
        }
        onDispose { reg.remove() }
    }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Column { Text("Customers (${list.size})"); Text("${list.count { it.enabled }} active", fontSize = 12.sp) } },
            actions = { IconButton(onClick = onLogout) { Icon(Icons.Default.Logout, "Logout") } }
        )
        if (list.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No customers yet.\nThey appear here the first time they open the Desktop app.", textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = Color.Gray) }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list) { lic ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, null, tint = if (lic.enabled) Color(0xFF2E7D32) else Color.Gray)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(lic.restaurantName.ifBlank { "(unnamed restaurant)" }, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(lic.email, color = Color.Gray, fontSize = 13.sp)
                                Text(if (lic.enabled) "Active - can log in" else "Disabled - login blocked", fontSize = 12.sp, color = if (lic.enabled) Color(0xFF2E7D32) else Color.Red)
                            }
                            Switch(
                                checked = lic.enabled,
                                enabled = busy != lic.uid,
                                onCheckedChange = { newVal ->
                                    busy = lic.uid
                                    firestore.collection("licenses").document(lic.uid)
                                        .update("enabled", newVal)
                                        .addOnCompleteListener { busy = null }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
