package com.example.giroracing.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.giroracing.viewmodel.CarModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CAR_COLOR = intPreferencesKey("car_color")
        val CAR_MODEL = stringPreferencesKey("car_model")
    }

    val carColor: Flow<Color> = context.dataStore.data.map { preferences ->
        val colorInt = preferences[PreferencesKeys.CAR_COLOR] ?: Color(0xFFE10600).toArgb()
        Color(colorInt)
    }

    val carModel: Flow<CarModel> = context.dataStore.data.map { preferences ->
        val modelName = preferences[PreferencesKeys.CAR_MODEL] ?: CarModel.F1.name
        try {
            CarModel.valueOf(modelName)
        } catch (e: IllegalArgumentException) {
            CarModel.F1
        }
    }

    suspend fun saveCarSkin(color: Color, model: CarModel) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAR_COLOR] = color.toArgb()
            preferences[PreferencesKeys.CAR_MODEL] = model.name
        }
    }
}
