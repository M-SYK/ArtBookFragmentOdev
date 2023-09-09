package com.mahsunsayak.myapplication.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.mahsunsayak.myapplication.R
import com.mahsunsayak.myapplication.adapter.ArtAdapter
import com.mahsunsayak.myapplication.databinding.FragmentArtListBinding
import com.mahsunsayak.myapplication.databinding.FragmentDetailsBinding
import com.mahsunsayak.myapplication.model.Art
import com.mahsunsayak.myapplication.roomdb.ArtDao
import com.mahsunsayak.myapplication.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class ArtListFragment : Fragment() {

    private var _binding: FragmentArtListBinding? = null
    // Bu özellik, onCreateView ve onDestroyView arasında geçerlidir.
    private val binding get() = _binding!!
    private val compositeDisposable = CompositeDisposable()
    private lateinit var artAdapter : ArtAdapter
    private lateinit var artDao: ArtDao
    private lateinit var artDatabase: ArtDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Room veritabanını oluştur
        artDatabase = Room.databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts").build()
        artDao = artDatabase.artDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArtListBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SQL veritabanından verileri al
        getFromSQL()
    }

    // SQL veritabanından verileri almak için kullanılan fonksiyon
    fun getFromSQL(){
        compositeDisposable.add(
            artDao.getArtWithNameAndId()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    // Verileri işlemek için kullanılan fonksiyon
    private fun handleResponse(artList: List<Art>) {
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        artAdapter = ArtAdapter(artList)
        binding.recyclerView.adapter = artAdapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Görünümü temizle ve Disposable'ları temizle
        _binding = null
        compositeDisposable.clear()
    }

}