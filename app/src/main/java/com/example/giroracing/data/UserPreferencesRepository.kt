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

/**
 * Handles saving and loading user preferences like the selected car color and model.
 * Uses Jetpack DataStore for persistent storage.
 */
class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CAR_COLOR = intPreferencesKey("car_color")
        val CAR_MODEL = stringPreferencesKey("car_model")
    }

    /**
     * Flow that emits the saved car color, or a default red if nothing is saved.
     */
    val carColor: Flow<Color> = context.dataStore.data.map { preferences ->
        val colorInt = preferences[PreferencesKeys.CAR_COLOR] ?: Color(0xFFE10600).toArgb()
        Color(colorInt)
    }

    /**
     * Flow that emits the saved car model, or F1 by default.
     */
    val carModel: Flow<CarModel> = context.dataStore.data.map { preferences ->
        val modelName = preferences[PreferencesKeys.CAR_MODEL] ?: CarModel.F1.name
        try {
            CarModel.valueOf(modelName)
        } catch (e: IllegalArgumentException) {
            CarModel.F1
        }
    }

    /**
     * Saves the car color and model to the data store.
     */
    suspend fun saveCarSkin(color: Color, model: CarModel) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CAR_COLOR] = color.toArgb()
            preferences[PreferencesKeys.CAR_MODEL] = model.name
        }
    }
}
