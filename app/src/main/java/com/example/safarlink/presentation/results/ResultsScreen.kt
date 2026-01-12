package com.example.safarlink.presentation.results

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.safarlink.domain.model.RideOption
import com.example.safarlink.presentation.home.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ResultsScreen(viewModel: HomeViewModel) {
    val rideOptions by viewModel.rideOptions.collectAsState()
    val dropLocation by viewModel.dropLocation.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            "Select Ride",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(rideOptions) { option ->
                RideOptionCard(
                    option = option,
                    onBookClick = {
                        if (option.deepLinkUri.startsWith("PACKAGE:")) {
                            scope.launch(Dispatchers.IO) {
                                var finalAddress = dropLocation?.address ?: ""
                                try {
                                    if (dropLocation != null) {
                                        val geocoder = Geocoder(context, Locale.getDefault())
                                        val addresses = geocoder.getFromLocation(
                                            dropLocation!!.latitude,
                                            dropLocation!!.longitude,
                                            1
                                        )
                                        if (!addresses.isNullOrEmpty()) {
                                            finalAddress = addresses[0].getAddressLine(0) ?: finalAddress
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                                withContext(Dispatchers.Main) {
                                    if (finalAddress.isNotBlank()) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Destination", finalAddress)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied: $finalAddress", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Opening ${option.providerName}...", Toast.LENGTH_SHORT).show()
                                    }
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(option.packageName)
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    } else {
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${option.packageName}")))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            }
                        } else {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(option.deepLinkUri))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${option.packageName}"))
                                try {
                                    context.startActivity(marketIntent)
                                } catch (ex: Exception) {
                                    Toast.makeText(context, "App not installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun RideOptionCard(option: RideOption, onBookClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFFEEEEEE),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = option.providerName.take(1),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.providerName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }

            Button(
                onClick = onBookClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC107),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                modifier = Modifier.height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("BOOK", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}