package com.example.contactlist

import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactlist.adapter.RCVAdapter
import com.example.contactlist.databinding.ActivityMainBinding
import com.example.contactlist.databinding.ItemContactBinding
import com.example.contactlist.model.ContactModel
import com.vmadalin.easypermissions.EasyPermissions
import pub.devrel.easypermissions.AppSettingsDialog


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    lateinit var binding: ActivityMainBinding
    lateinit var bindingItem: ItemContactBinding
    var contactList: ArrayList<ContactModel> = arrayListOf()
    var rcvAdapter: RCVAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        bindingItem = ItemContactBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkContactPermission()) {
            rcvAdapter = RCVAdapter(contactList)
            binding.rcvContactList.apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = rcvAdapter
            }
            getContactList()
        }

        // After initializing the rcvAdapter
        binding.rcvContactList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = rcvAdapter
        }

        // Set OnClickListener for the deleteIcon FloatingActionButton
        bindingItem.deleteIcon.setOnClickListener {
            val position = binding.rcvContactList.getChildAdapterPosition(it)
            if (position != RecyclerView.NO_POSITION) {
                deleteContact(position)
            }
        }

    }


    private fun getContactList() {
        contactList.clear()

        val projection = arrayOf(
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Organization.COMPANY,
            ContactsContract.CommonDataKinds.Organization.TITLE,
            ContactsContract.Contacts._ID
        )

        val selection = "${ContactsContract.Data.MIMETYPE} = ?"
        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        )

        val sortOrder = "${ContactsContract.Data.DISPLAY_NAME} ASC"

        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )

        while (cursor?.moveToNext() == true) {
            val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
            val contactNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contactDesignation = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))

            val mContactId: String = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
            val mRawContactId = getRawContactID(mContactId)
            val mCompanyName = getCompanyName(mRawContactId!!) ?: ""

            val contactModel = ContactModel(contactName, contactNumber, mCompanyName, contactDesignation, mContactId) // Include mContactId
            contactList.add(contactModel)
        }


        rcvAdapter?.notifyDataSetChanged()
        cursor?.close()
    }


    private fun getRawContactID(contactId: String): String? {
        val projection = arrayOf(ContactsContract.RawContacts._ID)
        val selection = ContactsContract.RawContacts.CONTACT_ID + "=?"
        val selectionArgs = arrayOf(contactId)
        val c: Cursor = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
            ?: return null
        var rawContactId = -1
        if (c.moveToFirst()) {
            rawContactId = c.getInt(c.getColumnIndex(ContactsContract.RawContacts._ID))
        }
        c.close()
        return rawContactId.toString()
    }

    private fun getCompanyName(rawContactId: String): String? {
        return try {
            val orgWhere =
                ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?"
            val orgWhereParams = arrayOf(
                rawContactId,
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE
            )
            val cursor: Cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                null, orgWhere, orgWhereParams, null
            ) ?: return null
            var name: String? = null
            if (cursor.moveToFirst()) {
                name =
                    cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
            }
            cursor.close()
            name
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    private fun deleteContact(position: Int) {
        val contactToRemove = contactList[position]

        val contentUri = ContactsContract.RawContacts.CONTENT_URI
        val whereClause = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
        val whereArgs = arrayOf(contactToRemove.contactId)

        contentResolver.delete(contentUri, whereClause, whereArgs)

        contactList.removeAt(position)
        rcvAdapter?.notifyItemRemoved(position)
    }



    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            checkContactPermission()
        }
    }

    fun checkContactPermission(): Boolean {
        if (PermissionTracking.hasContactPermission(this)) {
            return true
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
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