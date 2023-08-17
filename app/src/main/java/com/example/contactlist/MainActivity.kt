package com.example.contactlist

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.contactlist.adapter.RCVAdapter
import com.example.contactlist.databinding.ActivityMainBinding
import com.example.contactlist.model.ContactModel
import com.vmadalin.easypermissions.EasyPermissions
import pub.devrel.easypermissions.AppSettingsDialog
import javax.sql.CommonDataSource

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    lateinit var binding: ActivityMainBinding
    var contactList: ArrayList<ContactModel> = arrayListOf()
    var rcvAdapter: RCVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkContactPermission()) {
            rcvAdapter = RCVAdapter(contactList)
            binding.rcvContactList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = rcvAdapter
            }
            getContactList()
        }
    }

    private fun getContactList() {
        contactList.clear()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        )
        while (cursor!!.moveToNext()) {
            val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contactModel = ContactModel(contactName, contactNumber)
            contactList.add(contactModel)
        }
        rcvAdapter?.notifyDataSetChanged()
        cursor.close()
    }
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            checkContactPermission()
        }
    }
    fun checkContactPermission(): Boolean {
        if(PermissionTracking.hasContactPermission(this)){
           return true
        } else if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            EasyPermissions.requestPermissions(
                this,
            "You will need to accept the permission in order to run the application",
                100,
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS
            )
            return true
        } else {
            return false
        }
    }
}