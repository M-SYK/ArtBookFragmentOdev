package com.mahsunsayak.myapplication.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.mahsunsayak.myapplication.R

class MainActivity : AppCompatActivity() {

    private lateinit var navigationController: NavController //Uygulamadaki farklı fragment'ların ve gezinme grafiğinin yönetimini sağlar.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Bu, activity_main.xml dosyasında belirtilmiş olan fragment adlı view'in içeriğini kontrol etmek için kullanılır.
        navigationController = Navigation.findNavController(this, R.id.fragment)
        NavigationUI.setupActionBarWithNavController(this,navigationController)
    }

    //Menüyü main aktiviteye bağlama işlemi
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        //Inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.art_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    //Bu fonksiyon, Android ActionBar'ındaki geri (up) düğmesinin tıklanmasını yakalar ve bu düğmeye basıldığında ne yapılacağını belirtir.
    override fun onSupportNavigateUp(): Boolean {

        val navController = this.findNavController(R.id.fragment)
        return navController.navigateUp()
    }


    //Menüye tıklanırsa ne olacak
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.add_art) {
            val action = ArtListFragmentDirections.actionArtListFragmentToDetailsFragment("new",0)
            Navigation.findNavController(this, R.id.fragment).navigate(action)

        }

        return super.onOptionsItemSelected(item)
    }

}