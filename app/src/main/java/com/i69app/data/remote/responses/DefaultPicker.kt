package com.i69app.data.remote.responses

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.i69app.data.models.IdWithValue
import com.i69app.profile.db.converters.UserConverters

@Entity(tableName = "picker_table")
data class DefaultPicker(
    @PrimaryKey(autoGenerate = false)
    var id: Int = 0,
    @TypeConverters(UserConverters::class)
    val agePicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val ethnicityPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val familyPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val heightsPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val politicsPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val religiousPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val tagsPicker: List<IdWithValue> = emptyList(),
    @TypeConverters(UserConverters::class)
    val zodiacSignPicker: List<IdWithValue> = emptyList()
)