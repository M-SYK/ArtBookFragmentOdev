package com.mahsunsayak.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.mahsunsayak.myapplication.databinding.RecyclerRowBinding
import com.mahsunsayak.myapplication.model.Art
import com.mahsunsayak.myapplication.view.ArtListFragmentDirections

class ArtAdapter(val artList: List<Art>) : RecyclerView.Adapter<ArtAdapter.ArtHolder>() {

    // RecyclerView için her bir öğe tutucusunu temsil eden inner sınıf
    class ArtHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        // Görünümü şişirerek ve görünüm tutucusu oluşturarak yeni bir öğe oluşturulduğunda çağrılır
        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        //Veri kümesindeki öğe sayısını döndürür
        return artList.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        //Öğe verilerini görünüme bağlama işlemi
        holder.recyclerRowBinding.recyclerViewTextView.text = artList[position].name
        //Öğeye tıklanıldığında yapılacak işlemi tanımlar
        holder.itemView.setOnClickListener {
            val action = ArtListFragmentDirections.actionArtListFragmentToDetailsFragment("old", id = artList[position].id)
            Navigation.findNavController(it).navigate(action)
        }
    }

}