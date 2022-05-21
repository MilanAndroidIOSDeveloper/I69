package com.i69app.ui.interfaces

import android.content.DialogInterface

interface AlertDialogCallback {
    fun onNegativeButtonClick(dialog: DialogInterface)
    fun onPositiveButtonClick(dialog: DialogInterface)
}