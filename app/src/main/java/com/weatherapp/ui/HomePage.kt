package com.weatherapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.weatherapp.model.Forecast.ForecastItem
import com.weatherapp.model.MainViewModel
import androidx.compose.ui.res.painterResource
import com.weatherapp.R
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    Column {
        if (viewModel.city == null) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Blue)
                    .wrapContentSize(Alignment.Center)
            ) {
                Text(
                    text = "Selecione uma cidade!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
            }
        } else {

            val cityName = viewModel.city!!
            val city = viewModel.cities.find { it.name == cityName }
            val weather = viewModel.weather(cityName)

            Row(verticalAlignment = Alignment.CenterVertically) {

                AsyncImage(
                    model = weather.imgUrl,
                    modifier = Modifier.size(75.dp),
                    error = painterResource(id = R.drawable.loading),
                    contentDescription = "Imagem"
                )

                Column(modifier = Modifier.weight(1f)) {

                    Spacer(modifier = Modifier.size(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Text(
                            text = cityName,
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        val icon =
                            if (city?.isMonitored == true)
                                Icons.Filled.Notifications
                            else
                                Icons.Outlined.Notifications

                        Icon(
                            imageVector = icon,
                            contentDescription = "Monitorada?",
                            modifier = Modifier
                                .size(32.dp)
                                .clickable {
                                    city?.let {
                                        viewModel.update(
                                            it.copy(
                                                isMonitored = !it.isMonitored
                                            )
                                        )
                                    }
                                }
                        )
                    }

                    Spacer(modifier = Modifier.size(12.dp))

                    Text(
                        text = weather.desc ?: "...",
                        fontSize = 22.sp
                    )

                    Spacer(modifier = Modifier.size(12.dp))

                    Text(
                        text = "Temp: ${weather.temp}℃",
                        fontSize = 22.sp
                    )
                }
            }

            viewModel.forecast(cityName)?.let { forecasts ->
                LazyColumn {
                    items(forecasts) { forecast ->
                        ForecastItem(forecast, onClick = { })
                    }
                }
            }
        }
    }
}