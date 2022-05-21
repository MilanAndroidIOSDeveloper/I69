package com.i69app.profile.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.i69app.data.models.User
import com.i69app.data.remote.responses.DefaultPicker

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: User?)

    @Query("SELECT * FROM user_table WHERE id = :userId")
    fun getUser(userId: String?): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPicker(defaultPicker: DefaultPicker?)

    @Query("SELECT * FROM picker_table ORDER BY id DESC LIMIT 1")
    fun getPicker(): DefaultPicker

}