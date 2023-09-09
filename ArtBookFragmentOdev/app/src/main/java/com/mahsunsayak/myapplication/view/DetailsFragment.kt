package com.mahsunsayak.myapplication.view

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import androidx.room.Room
import com.google.android.material.snackbar.Snackbar
import com.mahsunsayak.myapplication.R
import com.mahsunsayak.myapplication.databinding.FragmentDetailsBinding
import com.mahsunsayak.myapplication.model.Art
import com.mahsunsayak.myapplication.roomdb.ArtDao
import com.mahsunsayak.myapplication.roomdb.ArtDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class DetailsFragment : Fragment() {

    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> //galeriye gitmek için gerekli olan değişken
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    var selectedPicture : Uri? = null
    private var _binding: FragmentDetailsBinding? = null
    // Bu özellik, onCreateView ve onDestroyView arasında geçerlidir.
    private val binding get() = _binding!!
    private lateinit var db : ArtDatabase
    private lateinit var artDao : ArtDao
    private val compositeDisposable = CompositeDisposable()
    var artFromMain : Art? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity ve izin launcher'larını kaydet
        registerLauncher()

        // Room veritabanını oluştur
        db = Room.databaseBuilder(requireContext(), ArtDatabase::class.java, "Arts").build()
        artDao = db.artDao()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        _binding = FragmentDetailsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Görseli seçmek için tıklama olayını tanımla. Kaydetme ve silme işlevlerini tanımla
        binding.imageView.setOnClickListener { selectImgClicked(view) }
        binding.saveButton.setOnClickListener { saveClicked(view) }
        binding.deleteButton.setOnClickListener { deleteClicked(view) }

        arguments?.let {
            val info = DetailsFragmentArgs.fromBundle(it).info
            if (info.equals("new")){
                // Yeni sanat eseri eklemek için gelen durumda görünen öğeleri ayarla
                binding.artNameText.setText("")
                binding.artistNameText.setText("")
                binding.yearText.setText("")
                binding.saveButton.visibility = View.VISIBLE
                binding.deleteButton.visibility = View.GONE

                val selectedImgBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.select_image)
                binding.imageView.setImageBitmap(selectedImgBackground)
            }else{
                // Mevcut sanat eserini düzenlemek için gelen durumda ilgili verileri doldur
                binding.saveButton.visibility = View.GONE
                binding.deleteButton.visibility = View.VISIBLE

                val selectedId = DetailsFragmentArgs.fromBundle(it).id
                compositeDisposable.add(artDao.getArtById(selectedId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponseWithOldArt))
            }
        }
    }

    //Bitmap küçültme fonksiyonu başka projelerde istediğin gibi kopyalayıp kullanabilirsin
    // Resmi küçültmek için kullanılan fonksiyon
    private fun makeSmallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            //landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()

        }else{
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width    = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    // Resmi seçmek için kullanılan fonksiyon
    fun selectImgClicked (view: View){

        activity?.let {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                //Android 33+ -> READ_MEDIA_IMAGES
                //Daha önceden galeriye ulaşmak için izin vermiş mi diye kontrol ediyoruz
                if (ContextCompat.checkSelfPermission(requireContext(),android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    // Kullanıcı izin vermediyse ve izin verme mantığını göstermek gerekiyorsa göster
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_MEDIA_IMAGES)){
                        Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            //request permission
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                    }else{
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }else{
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }else{
                //Android 32- -> READ_EXTERNAL_STORAGE
                //Daha önceden galeriye ulaşmak için izin vermiş mi diye kontrol ediyoruz
                if (ContextCompat.checkSelfPermission(requireActivity().applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //rationale-mantık yani kullanıcı hayırı seçip bir daha select image tıklarsa altta tekrar soru yöneltilecek izin verilme ekraanı açılsın mı
                    if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                        Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                            //request permission
                            permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                    }else{
                        //request permission
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }else{
                    val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    activityResultLauncher.launch(intentToGallery)
                }
            }
        }
    }

    //Bu fonksiyonu diğer uygulamalarda da direkt kullanabilirsin Activity launcherları tanımlamak için yazıyoruz. fonksiyonu onCreate altına yazmayı unutma
    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)  bu kolay olan kısım ama resimi bitmape çevireceğimiz için diğer metodu kullanacağız

                    try {
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(requireActivity().contentResolver,selectedPicture!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectedPicture)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }
                    }catch (e : Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

        //Buraya kadar yapılan galeriye gitmek ve galeriden görseli seçmek
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
            if (result){
                //permission garanted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(requireContext(),"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }
    }

    // Mevcut sanat eserini düzenlemek için kullanılan fonksiyon
    private fun handleResponseWithOldArt(art : Art) {
        artFromMain = art
        binding.artNameText.setText(art.name)
        binding.artistNameText.setText(art.artistName)
        binding.yearText.setText(art.year)
        art.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            binding.imageView.setImageBitmap(bitmap)
        }
    }

    private fun handleResponse() {
        val action = DetailsFragmentDirections.actionDetailsFragmentToArtListFragment()
        Navigation.findNavController(requireView()).navigate(action)
    }

    // Sanat eserini kaydetmek için kullanılan fonksiyon
    fun saveClicked(view: View){

        val artName = binding.artNameText.text.toString()
        val artistName = binding.artistNameText.text.toString()
        val year = binding.yearText.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            val art = Art(artName,artistName,year,byteArray)

            compositeDisposable.add(artDao.insert(art)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))

    }
}

    // Sanat eserini silmek için kullanılan fonksiyon
    fun deleteClicked(view: View){

        artFromMain?.let {
            compositeDisposable.add(artDao.delete(it)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        compositeDisposable.clear()
    }
}