package ir.kazemcodes.infinity.api_feature.network

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.PreferenceKeys.TEMP_SAVED_SETTING




val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = TEMP_SAVED_SETTING)


