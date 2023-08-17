package com.example.contactlist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contactlist.databinding.ItemContactBinding
import com.example.contactlist.model.ContactModel

class RCVAdapter(val contactList: ArrayList<ContactModel>):RecyclerView.Adapter<RCVAdapter.MyViewHolder>(){
    inner class MyViewHolder(val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    }

    override fun getItemCount(): Int = contactList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = contactList[position]
        holder.binding.contactPersonsName.text = item.displayName
        holder.binding.contactNumber.text = item.number
        holder.binding.personsCompanyName.text = item.companyName
        holder.binding.personsDesignation.text = item.contactDesignation


    }


}