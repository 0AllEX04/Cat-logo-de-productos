package com.example.catlogodeproductos

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.example.catlogodeproductos.model.ProductsResponse
import com.example.catlogodeproductos.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.widget.LinearLayout
import android.widget.TextView
import com.example.catlogodeproductos.model.Product
import android.widget.ImageView
import coil3.load
import android.widget.ScrollView
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View
class MainActivity : AppCompatActivity() {
    private var productos: List<Product> = emptyList()
    private var paginaActual = 0
    private val productosPorPagina = 5
    private val handlerCarga = Handler(Looper.getMainLooper())
    private var peticionActual: Call<ProductsResponse>? = null
    private var cargando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        findViewById<Button>(R.id.btnCargar).setOnClickListener {
            cargarProductos()
        }

        val botonAnterior = findViewById<Button>(R.id.btnAnterior)
        val botonSiguiente = findViewById<Button>(R.id.btnSiguiente)

        botonAnterior.isEnabled = false
        botonSiguiente.isEnabled = false

        botonAnterior.setOnClickListener {
            if (paginaActual > 0) {
                paginaActual--
                actualizarPagina()
            }
        }

        botonSiguiente.setOnClickListener {
            if ((paginaActual + 1) * productosPorPagina < productos.size) {
                paginaActual++
                actualizarPagina()
            }
        }

    }
    private fun cargarProductos() {
        if (cargando) return

        cargando = true
        val inicioCarga = SystemClock.elapsedRealtime()

        val contenedor = findViewById<LinearLayout>(
            R.id.contenedorProductos
        )

        findViewById<View>(R.id.progressBar).visibility = View.VISIBLE
        findViewById<Button>(R.id.btnCargar).isEnabled = false
        findViewById<Button>(R.id.btnAnterior).isEnabled = false
        findViewById<Button>(R.id.btnSiguiente).isEnabled = false

        findViewById<TextView>(R.id.tvCantidad).text =
            "Cargando productos…"

        contenedor.alpha = 0f

        val peticion = RetrofitClient.api.getProducts()
        peticionActual = peticion

        peticion.enqueue(object : Callback<ProductsResponse> {

            override fun onResponse(
                call: Call<ProductsResponse>,
                response: Response<ProductsResponse>
            ) {
                if (isFinishing || isDestroyed) return

                if (!response.isSuccessful) {
                    Log.e(
                        "PRODUCTOS_API",
                        "Error HTTP: ${response.code()}"
                    )
                    mostrarErrorCarga(
                        "No se pudieron cargar los productos. Intenta nuevamente."
                    )
                    return
                }

                val respuesta = response.body()

                if (respuesta == null || respuesta.products.isEmpty()) {
                    mostrarErrorCarga("No se recibieron productos.")
                    return
                }

                Log.d(
                    "PRODUCTOS_API",
                    "Productos recibidos: ${respuesta.products.size}"
                )

                respuesta.products.forEach { producto ->
                    Log.d(
                        "PRODUCTOS_API",
                        "ID: ${producto.id} | Nombre: ${producto.title}"
                    )
                }

                val tiempoTranscurrido =
                    SystemClock.elapsedRealtime() - inicioCarga

                val esperaRestante =
                    (3_000L - tiempoTranscurrido).coerceAtLeast(0L)

                handlerCarga.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        productos = respuesta.products
                        paginaActual = 0
                        actualizarPagina()

                        // actualizarPagina habilita botones:
                        // los bloqueamos hasta terminar la animación.
                        findViewById<Button>(
                            R.id.btnAnterior
                        ).isEnabled = false

                        findViewById<Button>(
                            R.id.btnSiguiente
                        ).isEnabled = false

                        contenedor.animate()
                            .alpha(1f)
                            .setDuration(2_000L)
                            .withEndAction {
                                if (!isFinishing && !isDestroyed) {
                                    finalizarCarga()
                                }
                            }
                            .start()
                    }
                }, esperaRestante)
            }

            override fun onFailure(
                call: Call<ProductsResponse>,
                t: Throwable
            ) {
                if (call.isCanceled || isFinishing || isDestroyed) return

                Log.e("PRODUCTOS_API", "Falló la petición", t)

                mostrarErrorCarga(
                    "No se pudieron cargar los productos. " +
                            "Revisa tu conexión e intenta nuevamente."
                )
            }
        })
    }

    private fun mostrarProductos(lista: List<Product>) {
        val contenedor = findViewById<LinearLayout>(
            R.id.contenedorProductos
        )

        contenedor.removeAllViews()

        lista.forEach { producto ->
            val tarjeta = layoutInflater.inflate(
                R.layout.item_product,
                contenedor,
                false
            )
            val imagen = tarjeta.findViewById<ImageView>(R.id.imgProducto)

            imagen.contentDescription = "Imagen de ${producto.title}"

            imagen.load(producto.thumbnail)

            tarjeta.findViewById<TextView>(R.id.tvNombre).text =
                producto.title

            tarjeta.findViewById<TextView>(R.id.tvDescripcion).text =
                producto.description

            tarjeta.findViewById<TextView>(R.id.tvPrecio).text =
                String.format(
                    java.util.Locale.US,
                    "Precio: $%.2f",
                    producto.price
                )

            tarjeta.findViewById<TextView>(R.id.tvCategoria).text =
                "Categoría: ${producto.category}"

            tarjeta.findViewById<TextView>(R.id.tvRating).text =
                "Rating: ★ ${producto.rating}"

            tarjeta.findViewById<TextView>(R.id.tvStock).text =
                "Stock: ${producto.stock}"

            contenedor.addView(tarjeta)
        }

        findViewById<TextView>(R.id.tvCantidad).text =
            "Mostrando ${lista.size} productos"
    }
    private fun actualizarPagina() {
        val totalPaginas =
            (productos.size + productosPorPagina - 1) / productosPorPagina

        val inicio = paginaActual * productosPorPagina

        val productosDePagina = productos
            .drop(inicio)
            .take(productosPorPagina)

        mostrarProductos(productosDePagina)

        findViewById<TextView>(R.id.tvPagina).text =
            "Página ${paginaActual + 1} de $totalPaginas"

        findViewById<Button>(R.id.btnAnterior).isEnabled =
            paginaActual > 0

        findViewById<Button>(R.id.btnSiguiente).isEnabled =
            paginaActual + 1 < totalPaginas

        val scroll = findViewById<ScrollView>(R.id.scrollProductos)

        scroll.post {
            scroll.scrollTo(0, 0)
        }
    }
    private fun finalizarCarga() {
        cargando = false
        peticionActual = null

        findViewById<View>(R.id.progressBar).visibility = View.GONE

        findViewById<LinearLayout>(
            R.id.contenedorProductos
        ).alpha = 1f

        findViewById<Button>(R.id.btnCargar).isEnabled = true

        findViewById<Button>(R.id.btnAnterior).isEnabled =
            productos.isNotEmpty() && paginaActual > 0

        findViewById<Button>(R.id.btnSiguiente).isEnabled =
            (paginaActual + 1) * productosPorPagina < productos.size
    }

    private fun mostrarErrorCarga(mensaje: String) {
        finalizarCarga()

        val cantidadVisible = productos
            .drop(paginaActual * productosPorPagina)
            .take(productosPorPagina)
            .size

        findViewById<TextView>(R.id.tvCantidad).text =
            if (productos.isEmpty()) {
                "Sin productos cargados"
            } else {
                "Mostrando $cantidadVisible productos"
            }

        Toast.makeText(
            this,
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }
    override fun onDestroy() {
        handlerCarga.removeCallbacksAndMessages(null)
        peticionActual?.cancel()

        findViewById<LinearLayout>(
            R.id.contenedorProductos
        ).animate().cancel()

        super.onDestroy()
    }
}
