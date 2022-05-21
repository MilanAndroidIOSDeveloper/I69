package com.i69app.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.i69app.chat.model.ModelQBChatDialogs
import com.i69app.chat.dao.ChatDialogsDao
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker
import com.i69app.profile.db.converters.PickerConverters
import com.i69app.profile.db.converters.UserConverters
import com.i69app.profile.db.dao.UserDao

@Database(entities = [User::class, DefaultPicker::class, ModelQBChatDialogs::class], version = 8, exportSchema = false)
@TypeConverters(UserConverters::class, PickerConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    abstract fun chatDialogDao(): ChatDialogsDao

}