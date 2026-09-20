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

class MainActivity : AppCompatActivity() {
    private var productos: List<Product> = emptyList()
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
    }
    private fun cargarProductos() {
        val botonCargar = findViewById<Button>(R.id.btnCargar)
        botonCargar.isEnabled = false

        RetrofitClient.api.getProducts().enqueue(
            object : Callback<ProductsResponse> {

                override fun onResponse(
                    call: Call<ProductsResponse>,
                    response: Response<ProductsResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    botonCargar.isEnabled = true

                    if (!response.isSuccessful) {
                        Log.e("PRODUCTOS_API", "Error HTTP: ${response.code()}")

                        Toast.makeText(
                            this@MainActivity,
                            "No se pudieron cargar los productos. Intenta nuevamente.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val respuesta = response.body()

                    if (respuesta == null || respuesta.products.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "No se recibieron productos.",
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }
                    productos = respuesta.products
                    mostrarProductos(productos.take(5))
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

                    Toast.makeText(
                        this@MainActivity,
                        "Se recibieron ${respuesta.products.size} productos",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(
                    call: Call<ProductsResponse>,
                    t: Throwable
                ) {
                    if (isFinishing || isDestroyed) return

                    botonCargar.isEnabled = true

                    Log.e("PRODUCTOS_API", "Falló la petición", t)

                    Toast.makeText(
                        this@MainActivity,
                        "No se pudieron cargar los productos. Revisa tu conexión e intenta nuevamente.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
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
}
