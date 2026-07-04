package com.weatherapp.model.Forecast

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun ForecastItem(
    forecast: Forecast,
    modifier: Modifier = Modifier,
    onClick: (Forecast) -> Unit
) {
    val format = DecimalFormat("#.0")
    val tempMin = format.format(forecast.tempMin)
    val tempMax = format.format(forecast.tempMax)
    Row(
        modifier = modifier.fillMaxWidth().padding(12.dp)
            .clickable( onClick = { onClick(forecast) }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon( imageVector = Icons.Filled.LocationOn,
            contentDescription = "Localized description",
            modifier = modifier.size(48.dp) )
        Spacer(modifier = modifier.size(16.dp))
        Column {
            Text(modifier = modifier, text = forecast.weather, fontSize = 24.sp)
            Row {
                Text(modifier = modifier, text = forecast.date, fontSize = 20.sp)
                Spacer(modifier = modifier.size(12.dp))
                Text(modifier = modifier, text = "Min: $tempMin℃", fontSize = 16.sp)
                Spacer(modifier = modifier.size(12.dp))
                Text(modifier = modifier, text = "Max: $tempMax℃", fontSize = 16.sp)
            }
        }
    }
}