package com.weatherapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.weatherapp.model.MainViewModel
import com.google.maps.android.compose.*

@Composable
fun MapPage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    val recife = remember { MarkerState(position = LatLng(-8.05, -34.9)) }
    val caruaru = remember { MarkerState(position = LatLng(-8.27, -35.98)) }
    val joaopessoa = remember { MarkerState(position = LatLng(-7.12, -34.84)) }

    val camPosState = rememberCameraPositionState()

    GoogleMap(
        modifier = modifier.fillMaxSize(),

        onMapClick = {
            viewModel.add(
                name = "Cidade@${it.latitude}:${it.longitude}",
                location = it
            )
        },

        cameraPositionState = camPosState
    ) {

        Marker(
            state = recife,
            title = "Recife",
            snippet = "Marcador em Recife",
            icon = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_BLUE
            )
        )

        Marker(
            state = caruaru,
            title = "Caruaru",
            snippet = "Marcador em Caruaru",
            icon = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN
            )
        )

        Marker(
            state = joaopessoa,
            title = "João Pessoa",
            snippet = "Marcador em João Pessoa",
            icon = BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_RED
            )
        )

        viewModel.cities.forEach { city ->
            if (city.location != null) {
                Marker(
                    state = MarkerState(position = city.location),
                    title = city.name,
                    snippet = "${city.location}"
                )
            }
        }
    }
}