package ru.yandex.buggyweatherapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ru.yandex.buggyweatherapp.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearch(
    onCitySearch: (String) -> Unit,
    onLocationRequest: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search city") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            leadingIcon = {
                IconButton(onClick = { onLocationRequest() }) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Get current location")
                }
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (searchText.isNotBlank()) {
                        onCitySearch(searchText)
                    }
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchText.isNotBlank()) {
                    onCitySearch(searchText)
                }
            })
        )
    }
}

/**
 * UI создавал WeatherRepository/LocationRepository напрямую
 * и игнорировал результат — нарушение слоёв (Compose → data).
 * Метод устранения: только через ViewModel.
 */
@Composable
fun LocationSearchWithDirectApiCall(viewModel: WeatherViewModel) {
    var searchText by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        label = { Text("Search city") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        trailingIcon = {
            IconButton(onClick = {
                if (searchText.isNotBlank()) {
                    viewModel.searchWeatherByCity(searchText)
                }
            }) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}
