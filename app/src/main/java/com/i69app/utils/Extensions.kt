package com.i69app.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Fragment.toast(message:String){
    Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
}

fun AppCompatActivity.toast(message:String){
    Toast.makeText(applicationContext,message,Toast.LENGTH_LONG).show()
}

fun hasPermissions(context: Context, permissions: Array<String>): Boolean = permissions.all {
    ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
}