package fr.alvernhe.surroundead


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fr.alvernhe.surroundead.fragments.MainScreenMenu


class MainActivity : AppCompatActivity() {

    var MenuFragment: MainScreenMenu? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MenuFragment = MainScreenMenu()
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(
                R.id.conteneurMain, MenuFragment!!
            ).commit()

    }








}