package com.i69app.chat.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.i69app.chat.model.ModelQBChatDialogs

@Dao
interface ChatDialogsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatDialog(user: ModelQBChatDialogs?)

    @Query("SELECT * FROM chat_dialogs")
    fun getChatDialogs(): List<ModelQBChatDialogs>
}