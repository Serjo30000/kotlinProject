package com.example.kotlin9.ui.weather

import android.app.AlertDialog
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.kotlin9.R
import com.example.kotlin9.api.WeatherForecast
import com.example.kotlin9.api.WeatherService
import com.example.kotlin9.api.WeeklyForecastResponse
import com.example.kotlin9.databinding.FragmentWeatherBinding
import com.example.kotlin9.ui.personalarea.PersonalAreaViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import com.squareup.picasso.Picasso

class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherService::class.java)

        val city = "Saratov"

        val call = apiService.getWeeklyForecast(51.53,46.03,"76920226a5e3ce8c26e38b6419c4ceb2")

        call.enqueue(object : Callback<WeeklyForecastResponse> {
            override fun onResponse(call: Call<WeeklyForecastResponse>, response: Response<WeeklyForecastResponse>) {
                if (response.isSuccessful) {

                    val weatherResponse = response.body()

                    if (weatherResponse != null){
                        val localPlaceCityWeather: TextView = binding.localPlaceCityWeather
                        localPlaceCityWeather.text = weatherResponse.city.name

                        val lstWeatherForecast = ArrayList<WeatherForecast>()

                        for (i in weatherResponse.list.indices) {
                            if (i % 8 == 0){
                                lstWeatherForecast.add(weatherResponse.list.get(i))
                            }
                        }

                        val temperatureDayTodayWeather: TextView = binding.temperatureDayTodayWeather
                        temperatureDayTodayWeather.text = ceil(lstWeatherForecast.get(0).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayFirstWeather: TextView = binding.temperatureDayFirstWeather
                        temperatureDayFirstWeather.text = ceil(lstWeatherForecast.get(0).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDaySecondWeather: TextView = binding.temperatureDaySecondWeather
                        temperatureDaySecondWeather.text = ceil(lstWeatherForecast.get(1).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayThirdWeather: TextView = binding.temperatureDayThirdWeather
                        temperatureDayThirdWeather.text = ceil(lstWeatherForecast.get(2).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayFourthWeather: TextView = binding.temperatureDayFourthWeather
                        temperatureDayFourthWeather.text = ceil(lstWeatherForecast.get(3).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayFifthWeather: TextView = binding.temperatureDayFifthWeather
                        temperatureDayFifthWeather.text = ceil(lstWeatherForecast.get(4).main.temp - 273.15).toInt().toString() + "°"

                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                        val nameDaySecondWeather: TextView = binding.nameDaySecondWeather
                        val nameDaySecondWeatherDateTime = LocalDateTime.parse(lstWeatherForecast.get(1).dt_txt, formatter)
                        nameDaySecondWeather.text = nameDaySecondWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayThirdWeather: TextView = binding.nameDayThirdWeather
                        val nameDayThirdWeatherDateTime = LocalDateTime.parse(lstWeatherForecast.get(2).dt_txt, formatter)
                        nameDayThirdWeather.text = nameDayThirdWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayFourthWeather: TextView = binding.nameDayFourthWeather
                        val nameDayFourthWeatherDateTime = LocalDateTime.parse(lstWeatherForecast.get(3).dt_txt, formatter)
                        nameDayFourthWeather.text = nameDayFourthWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayFifthWeather: TextView = binding.nameDayFifthWeather
                        val nameDayFifthWeatherDateTime = LocalDateTime.parse(lstWeatherForecast.get(4).dt_txt, formatter)
                        nameDayFifthWeather.text = nameDayFifthWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val imageDayFirstWeather: ImageView = binding.imageDayFirstWeather
                        val imageDayFirstWeatherURL = "https://openweathermap.org/img/w/" + lstWeatherForecast.get(0).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayFirstWeatherURL).into(imageDayFirstWeather)

                        val imageDaySecondWeather: ImageView = binding.imageDaySecondWeather
                        val imageDaySecondWeatherURL = "https://openweathermap.org/img/w/" + lstWeatherForecast.get(1).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDaySecondWeatherURL).into(imageDaySecondWeather)

                        val imageDayThirdWeather: ImageView = binding.imageDayThirdWeather
                        val imageDayThirdWeatherURL = "https://openweathermap.org/img/w/" + lstWeatherForecast.get(2).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayThirdWeatherURL).into(imageDayThirdWeather)

                        val imageDayFourthWeather: ImageView = binding.imageDayFourthWeather
                        val imageDayFourthWeatherURL = "https://openweathermap.org/img/w/" + lstWeatherForecast.get(3).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayFourthWeatherURL).into(imageDayFourthWeather)

                        val imageDayFifthWeather: ImageView = binding.imageDayFifthWeather
                        val imageDayFifthWeatherURL = "https://openweathermap.org/img/w/" + lstWeatherForecast.get(4).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayFifthWeatherURL).into(imageDayFifthWeather)

                        val temperatureDayToday15Weather: TextView = binding.temperatureDayToday15Weather
                        temperatureDayToday15Weather.text = ceil(weatherResponse.list.get(0).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday18Weather: TextView = binding.temperatureDayToday18Weather
                        temperatureDayToday18Weather.text = ceil(weatherResponse.list.get(1).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday21Weather: TextView = binding.temperatureDayToday21Weather
                        temperatureDayToday21Weather.text = ceil(weatherResponse.list.get(2).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday00Weather: TextView = binding.temperatureDayToday00Weather
                        temperatureDayToday00Weather.text = ceil(weatherResponse.list.get(3).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday03Weather: TextView = binding.temperatureDayToday03Weather
                        temperatureDayToday03Weather.text = ceil(weatherResponse.list.get(4).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday06Weather: TextView = binding.temperatureDayToday06Weather
                        temperatureDayToday06Weather.text = ceil(weatherResponse.list.get(5).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday09Weather: TextView = binding.temperatureDayToday09Weather
                        temperatureDayToday09Weather.text = ceil(weatherResponse.list.get(6).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayToday12Weather: TextView = binding.temperatureDayToday12Weather
                        temperatureDayToday12Weather.text = ceil(weatherResponse.list.get(7).main.temp - 273.15).toInt().toString() + "°"

                        val imageDayToday15Weather: ImageView = binding.imageDayToday15Weather
                        val imageDayToday15WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(0).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday15WeatherURL).into(imageDayToday15Weather)

                        val imageDayToday18Weather: ImageView = binding.imageDayToday18Weather
                        val imageDayToday18WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(1).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday18WeatherURL).into(imageDayToday18Weather)

                        val imageDayToday21Weather: ImageView = binding.imageDayToday21Weather
                        val imageDayToday21WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(2).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday21WeatherURL).into(imageDayToday21Weather)

                        val imageDayToday00Weather: ImageView = binding.imageDayToday00Weather
                        val imageDayToday00WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(3).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday00WeatherURL).into(imageDayToday00Weather)

                        val imageDayToday03Weather: ImageView = binding.imageDayToday03Weather
                        val imageDayToday03WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(4).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday03WeatherURL).into(imageDayToday03Weather)

                        val imageDayToday06Weather: ImageView = binding.imageDayToday06Weather
                        val imageDayToday06WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(5).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday06WeatherURL).into(imageDayToday06Weather)

                        val imageDayToday09Weather: ImageView = binding.imageDayToday09Weather
                        val imageDayToday09WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(6).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday09WeatherURL).into(imageDayToday09Weather)

                        val imageDayToday12Weather: ImageView = binding.imageDayToday12Weather
                        val imageDayToday12WeatherURL = "https://openweathermap.org/img/w/" + weatherResponse.list.get(7).weather.get(0).icon + ".png"
                        Picasso.get().load(imageDayToday12WeatherURL).into(imageDayToday12Weather)
                    }
                }
            }

            override fun onFailure(call: Call<WeeklyForecastResponse>, t: Throwable) {
                showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
            }
        })
    }

    fun getLocationInfo(context: Context, location: Location) {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null){
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val cityName = address.locality
                val stateCode = address.adminArea // Код региона (штата или провинции)
                val countryCode = address.countryCode

            }
        }
    }

    private fun showAlert(title: String, message: String) {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(title)
        alertDialogBuilder.setMessage(message)
        alertDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialogBuilder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}