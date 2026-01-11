package com.example.safarlink.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.safarlink.domain.model.LocationData
import com.example.safarlink.ui.theme.BrandOrange
import com.example.safarlink.ui.theme.BrandBlack
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun MapPickerScreen(
    initialLat: Double = 12.9716, // Bangalore default
    initialLng: Double = 77.5946,
    onLocationSelected: (LocationData) -> Unit
) {
    val context = LocalContext.current

    // 1. Initialize OSM Configuration (Required!)
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // State to hold the center location
    var centerGeoPoint by remember { mutableStateOf(GeoPoint(initialLat, initialLng)) }

    Box(modifier = Modifier.fillMaxSize()) {

        // 2. The Map View (OpenStreetMap)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK) // The standard free OSM style
                    setMultiTouchControls(true)
                    controller.setZoom(18.0) // Zoomed in closer for better picking
                    controller.setCenter(centerGeoPoint)

                    // Listener to track dragging
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean {
                            event?.source?.mapCenter?.let {
                                centerGeoPoint = it as GeoPoint
                            }
                            return true
                        }
                        override fun onZoom(event: ZoomEvent?): Boolean = true
                    })
                }
            }
        )

        // 3. Center Pin (Static Overlay)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Center Pin",
            // FIXED: Replaced 'NeonBlue' with 'BrandOrange'
            tint = BrandOrange,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.Center)
                .offset(y = (-24).dp) // Offset to make the pin tip match the center
        )

        // 4. Confirm Button Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select this location",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandBlack
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onLocationSelected(
                            LocationData(
                                // Create a formatted name string
                                address = "Lat: ${String.format("%.4f", centerGeoPoint.latitude)}, Lng: ${String.format("%.4f", centerGeoPoint.longitude)}",
                                latitude = centerGeoPoint.latitude,
                                longitude = centerGeoPoint.longitude
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("CONFIRM LOCATION", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}