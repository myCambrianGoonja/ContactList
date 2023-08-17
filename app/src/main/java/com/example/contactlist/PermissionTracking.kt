package com.example.contactlist

import android.content.Context
import pub.devrel.easypermissions.EasyPermissions

//Checking if user has given us permission to access their contact
object PermissionTracking {
    fun hasContactPermission(context: Context): Boolean =
    EasyPermissions.hasPermissions(
        context,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.WRITE_CONTACTS
    )
}