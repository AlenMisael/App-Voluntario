package com.example.voluntarios

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.graphics.alpha
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt




class ResumenEncuestaFragment : Fragment(R.layout.fragment_resumen_encuesta) {

    private lateinit var loader: ProgressBar
    private lateinit var content: View


    private val encuestaViewModel: EncuestaViewModel by viewModels {
        EncuestaViewModel.EncuestaViewModelFactory(
            (requireActivity().application as AppVoluntarios).encuestaRepositorio
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val turnoId = arguments?.getString("turnoId") ?: return

        super.onViewCreated(view, savedInstanceState)
        loader = view.findViewById(R.id.loader)
        content = view.findViewById(R.id.contentView)

        // TextViews totales
        val tvSub: TextView = view.findViewById(R.id.tvSubtitulo)
        val tvKcalTotal: TextView = view.findViewById(R.id.tvKcalTotal)
        val tvCarboTotal: TextView = view.findViewById(R.id.tvCarboTotal)
        val tvProteTotal: TextView = view.findViewById(R.id.tvProteTotal)
        val tvColesterolTotal: TextView = view.findViewById(R.id.tvColesterolTotal)
        val tvFibraTotal: TextView = view.findViewById(R.id.tvFibraTotal)
        val tvGrasasTotal: TextView = view.findViewById(R.id.tvGrasasTotal)
        val tvAlcoholTotal: TextView = view.findViewById(R.id.tvAlcoholTotal)
        val tvGramosTotal: TextView = view.findViewById(R.id.tvGramosTotal)

        // TextViews promedios
        val tvKcalProm: TextView = view.findViewById(R.id.tvKcalPromedio)
        val tvCarboProm: TextView = view.findViewById(R.id.tvCarboPromedio)
        val tvProteProm: TextView = view.findViewById(R.id.tvProtePromedio)
        val tvGrasasProm: TextView = view.findViewById(R.id.tvGrasasPromedio)
        val tvColesterolProm: TextView = view.findViewById(R.id.tvColesterolPromedio)
        val tvFibraProm: TextView = view.findViewById(R.id.tvFibraPromedio)

        val btnVolver: ImageButton = view.findViewById(R.id.btnVolver)

        tvSub.text = "Resumen de la encuesta"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        loader.visibility = View.VISIBLE
        content.visibility = View.GONE

        // Cargar datos y calcular totales/promedios en coroutine
        viewLifecycleOwner.lifecycleScope.launch {
            try {

                val alimentos = encuestaViewModel.getAlimentosPorTurno(turnoId)

                var totalKcal = 0.0
                var totalCarbo = 0.0
                var totalProte = 0.0
                var totalColesterol = 0.0
                var totalFibra = 0.0
                var totalGrasas = 0.0
                var totalAlcohol = 0.0
                var totalGramos = 0.0

                alimentos.forEach { a ->
                    totalKcal += a.kcal.toDouble()
                    totalCarbo += a.carbohidratos.toDouble()
                    totalProte += a.proteinas.toDouble()
                    totalColesterol += a.colesterol.toDouble()
                    totalFibra += a.fibra.toDouble()
                    totalGrasas += a.grasas.toDouble()
                    totalAlcohol += a.alcohol.toDouble()
                    totalGramos += a.gramos.toDouble()
                }

                val n = alimentos.size.coerceAtLeast(1)
                fun dbl(v: Double) = ((v * 10.0).roundToInt() / 10.0)

                // Totales
                tvKcalTotal.setStatText("Kcal total", dbl(totalKcal).toString(), "kcal")
                tvCarboTotal.setStatText("Carbohidratos total", dbl(totalCarbo).toString(), "g")
                tvProteTotal.setStatText("Proteínas total", dbl(totalProte).toString(), "g")
                tvColesterolTotal.setStatText("Colesterol total", dbl(totalColesterol).toString(), "mg")
                tvFibraTotal.setStatText("Fibras total", dbl(totalFibra).toString(), "g")
                tvGrasasTotal.setStatText("Grasas totales", dbl(totalGrasas).toString(), "g")
                tvAlcoholTotal.setStatText("Alcohol total", dbl(totalAlcohol).toString(), "g")
                tvGramosTotal.setStatText("Gramos totales", dbl(totalGramos).toString(), "g")

                // Promedios por alimento
                tvKcalProm.setStatText("Kcal promedio", dbl(totalKcal / n).toString(), "kcal")
                tvCarboProm.setStatText("Carbohidratos promedio", dbl(totalCarbo / n).toString(), "g")
                tvProteProm.setStatText("Proteínas promedio", dbl(totalProte / n).toString(), "g")
                tvGrasasProm.setStatText("Grasas totales promedio", dbl(totalGrasas / n).toString(), "g")
                tvColesterolProm.setStatText("Colesterol promedio", dbl(totalColesterol / n).toString(), "mg")
                tvFibraProm.setStatText("Fibra promedio", dbl(totalFibra / n).toString(), "g")
                loader.visibility = View.GONE
                content.alpha = 0f
                content.visibility = View.VISIBLE
                content.animate().alpha(1f).setDuration(300).start()

            } catch (e: Exception) {
                tvKcalTotal.text = "Error cargando datos: ${e.localizedMessage ?: e.message}"
                loader.visibility = View.GONE
                content.visibility = View.VISIBLE
            }
        }

        btnVolver.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

}