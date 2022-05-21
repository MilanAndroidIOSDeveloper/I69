package com.i69app.profile.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.i69app.data.models.IdWithValue

class PickerConverters {
    var gson = Gson()

    @TypeConverter
    fun toStringPickers(list: List<IdWithValue>?): String?{
        val type = object: TypeToken<List<IdWithValue>>(){}.type
        return gson.toJson(list, type)
    }

    @TypeConverter
    fun stringToPickers(info: String?): List<IdWithValue>?{
        val type = object: TypeToken<List<IdWithValue>>(){}.type
        return gson.fromJson(info, type)
    }
}