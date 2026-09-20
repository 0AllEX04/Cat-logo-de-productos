package com.example.catlogodeproductos

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashActivity : AppCompatActivity() {

    private var animacionEntrada: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.bienvenidaRoot)
        ) { vista, insets ->
            val barras = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            vista.setPadding(
                barras.left,
                barras.top,
                barras.right,
                barras.bottom
            )
            insets
        }

        val boton = findViewById<Button>(R.id.btnEntrar)
        val progreso = findViewById<ProgressBar>(R.id.progresoEntrada)
        val estado = findViewById<View>(R.id.tvEstadoEntrada)

        boton.setOnClickListener {
            boton.isEnabled = false
            boton.text = "Entrando…"
            progreso.visibility = View.VISIBLE
            estado.visibility = View.VISIBLE

            animacionEntrada = ObjectAnimator.ofInt(
                progreso,
                "progress",
                0,
                100
            ).apply {
                duration = 5_000L
                interpolator = LinearInterpolator()

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (isFinishing || isDestroyed) return

                        startActivity(
                            Intent(this@SplashActivity, MainActivity::class.java)
                        )
                        finish()
                    }
                })

                start()
            }
        }
    }

    override fun onDestroy() {
        animacionEntrada?.removeAllListeners()
        animacionEntrada?.cancel()
        super.onDestroy()
    }
}