package com.example.kotlin9.ui.weather

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kotlin9.api.*
import com.example.kotlin9.databinding.FragmentWeatherBinding
import com.example.kotlin9.novigation.NavigationSecurity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class WeatherFragment : Fragment() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var _binding: FragmentWeatherBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        val tokenString = sharedPreferences.getString("TOKEN_KEY", null)?:""

        val login = NavigationSecurity.decodedToken(tokenString)

        NavigationSecurity.checkNavigation(findNavController(), login)

        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )

            fetchWeeklyForecast(51.51, -0.09)

        } else {
            getLocation { geoLocationUser ->
                if (geoLocationUser != null) {
                    val retrofitGeo = Retrofit.Builder()
                        .baseUrl("http://api.openweathermap.org/geo/1.0/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()

                    val geoService = retrofitGeo.create(WeatherService::class.java)

                    val geoCall = geoService.getCities(geoLocationUser.cityName + "," + geoLocationUser.stateCode + "," + geoLocationUser.countryCode,1,"76920226a5e3ce8c26e38b6419c4ceb2")

                    geoCall.enqueue(object : Callback<List<LocalCity>> {
                        override fun onResponse(call: Call<List<LocalCity>>, response: Response<List<LocalCity>>) {
                            if (response.isSuccessful) {

                                val localCityResponse = response.body()

                                if (localCityResponse != null){
                                    fetchWeeklyForecast(localCityResponse.get(0).lat, localCityResponse.get(0).lon)
                                }
                            }
                        }

                        override fun onFailure(call: Call<List<LocalCity>>, t: Throwable) {
                            showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
                        }
                    })
                }
            }
        }
    }

    private fun getLocation(callback: (GeoLocationUser?) -> Unit){
        var geoLocationUser: GeoLocationUser? = GeoLocationUser("London", "England", "GB")
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    geoLocationUser = getLocationInfo(requireContext(), location)
                    callback(geoLocationUser)
                } else {
                    showAlert("Ошибка", "Не удалось получить местоположение")
                    callback(geoLocationUser)
                }
            }
            .addOnFailureListener { e ->
                showAlert("Ошибка", "Не удалось получить местоположение: ${e.message}")
                callback(geoLocationUser)
            }
    }

    fun getLocationInfo(context: Context, location: Location): GeoLocationUser? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null){
            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val cityName = address.locality
                val stateCode = address.adminArea
                val countryCode = address.countryCode
                return GeoLocationUser(cityName, stateCode, countryCode)
            }
        }
        return GeoLocationUser("London", "England", "GB")
    }

    fun fetchWeeklyForecast(lat : Double, lon : Double){
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherService::class.java)

        val call = apiService.getWeeklyForecast(lat,lon,"76920226a5e3ce8c26e38b6419c4ceb2")

        call.enqueue(object : Callback<WeeklyForecastResponse> {
            override fun onResponse(call: Call<WeeklyForecastResponse>, response: Response<WeeklyForecastResponse>) {
                if (response.isSuccessful) {

                    val weatherResponse = response.body()

                    if (weatherResponse != null){
                        val localPlaceCityWeather: TextView = binding.localPlaceCityWeather
                        localPlaceCityWeather.text = weatherResponse.city.name

//                        println("hello " + weatherResponse.city.timezone)

                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

                        val lstWeatherForecast = ArrayList<WeatherForecast>()
                        val lstTemperaturesAverage = ArrayList<Double>()
                        val lstDays = ArrayList<String>()
                        val lstDaysHours = ArrayList<String>()
                        var temperatureWeatherResponse = 0.0
                        var countDay = 0
                        var countHour = 0

                        for (i in weatherResponse.list.indices) {
                            var formatterDateTime = LocalDateTime.parse(weatherResponse.list.get(i).dt_txt, formatter).plusHours((weatherResponse.city.timezone / 3600).toLong())

                            val dateTime = LocalDate.of(formatterDateTime.year, formatterDateTime.month, formatterDateTime.dayOfMonth)

                            var dateNowZone = ZonedDateTime.now(ZoneId.of("UTC")).plusHours((weatherResponse.city.timezone / 3600).toLong())

                            var dateNow = LocalDate.of(dateNowZone.year, dateNowZone.month, dateNowZone.dayOfMonth)

                            if (dateNow.plusDays(countDay.toLong()).isEqual(dateTime)){
                                if (lstDays.size==0){
                                    lstDays.add(formatterDateTime.format(formatter))
                                }
                                temperatureWeatherResponse += weatherResponse.list.get(i).main.temp
                                countHour++
                            }
                            else{
                                lstDays.add(formatterDateTime.format(formatter))
                                lstTemperaturesAverage.add(temperatureWeatherResponse / countHour)
                                temperatureWeatherResponse = weatherResponse.list.get(i).main.temp
                                countHour = 1
                                countDay++
                            }

                            if (i<8){
                                lstDaysHours.add(formatterDateTime.format(formatter))
                            }

                            if ((i + 1) % 8 == 0){
                                lstWeatherForecast.add(weatherResponse.list.get(i))
                            }
                        }

                        val temperatureDayTodayWeather: TextView = binding.temperatureDayTodayWeather
                        temperatureDayTodayWeather.text = ceil(weatherResponse.list.get(0).main.temp - 273.15).toInt().toString() + "°"

                        val temperatureDayFirstWeather: TextView = binding.temperatureDayFirstWeather
                        temperatureDayFirstWeather.text = ceil(lstTemperaturesAverage.get(0) - 273.15).toInt().toString() + "°"

                        val temperatureDaySecondWeather: TextView = binding.temperatureDaySecondWeather
                        temperatureDaySecondWeather.text = ceil(lstTemperaturesAverage.get(1) - 273.15).toInt().toString() + "°"

                        val temperatureDayThirdWeather: TextView = binding.temperatureDayThirdWeather
                        temperatureDayThirdWeather.text = ceil(lstTemperaturesAverage.get(2) - 273.15).toInt().toString() + "°"

                        val temperatureDayFourthWeather: TextView = binding.temperatureDayFourthWeather
                        temperatureDayFourthWeather.text = ceil(lstTemperaturesAverage.get(3) - 273.15).toInt().toString() + "°"

                        val temperatureDayFifthWeather: TextView = binding.temperatureDayFifthWeather
                        temperatureDayFifthWeather.text = ceil(lstTemperaturesAverage.get(4) - 273.15).toInt().toString() + "°"

                        val nameDaySecondWeather: TextView = binding.nameDaySecondWeather
                        val nameDaySecondWeatherDateTime = LocalDateTime.parse(lstDays.get(1), formatter)
                        nameDaySecondWeather.text = nameDaySecondWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayThirdWeather: TextView = binding.nameDayThirdWeather
                        val nameDayThirdWeatherDateTime = LocalDateTime.parse(lstDays.get(2), formatter)
                        nameDayThirdWeather.text = nameDayThirdWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayFourthWeather: TextView = binding.nameDayFourthWeather
                        val nameDayFourthWeatherDateTime = LocalDateTime.parse(lstDays.get(3), formatter)
                        nameDayFourthWeather.text = nameDayFourthWeatherDateTime.dayOfWeek.getDisplayName(
                            TextStyle.FULL, Locale.getDefault())

                        val nameDayFifthWeather: TextView = binding.nameDayFifthWeather
                        val nameDayFifthWeatherDateTime = LocalDateTime.parse(lstDays.get(4), formatter)
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

                        val nameDayToday15Weather: TextView = binding.nameDayToday15Weather
                        val nameDayToday15WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(0), formatter)
                        nameDayToday15Weather.text = nameDayToday15WeatherDateTime.hour.toString()

                        val nameDayToday18Weather: TextView = binding.nameDayToday18Weather
                        val nameDayToday18WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(1), formatter)
                        nameDayToday18Weather.text = nameDayToday18WeatherDateTime.hour.toString()

                        val nameDayToday21Weather: TextView = binding.nameDayToday21Weather
                        val nameDayToday21WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(2), formatter)
                        nameDayToday21Weather.text = nameDayToday21WeatherDateTime.hour.toString()

                        val nameDayToday00Weather: TextView = binding.nameDayToday00Weather
                        val nameDayToday00WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(3), formatter)
                        nameDayToday00Weather.text = nameDayToday00WeatherDateTime.hour.toString()

                        val nameDayToday03Weather: TextView = binding.nameDayToday03Weather
                        val nameDayToday03WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(4), formatter)
                        nameDayToday03Weather.text = nameDayToday03WeatherDateTime.hour.toString()

                        val nameDayToday06Weather: TextView = binding.nameDayToday06Weather
                        val nameDayToday06WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(5), formatter)
                        nameDayToday06Weather.text = nameDayToday06WeatherDateTime.hour.toString()

                        val nameDayToday09Weather: TextView = binding.nameDayToday09Weather
                        val nameDayToday09WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(6), formatter)
                        nameDayToday09Weather.text = nameDayToday09WeatherDateTime.hour.toString()

                        val nameDayToday12Weather: TextView = binding.nameDayToday12Weather
                        val nameDayToday12WeatherDateTime = LocalDateTime.parse(lstDaysHours.get(7), formatter)
                        nameDayToday12Weather.text = nameDayToday12WeatherDateTime.hour.toString()
                    }
                }
            }

            override fun onFailure(call: Call<WeeklyForecastResponse>, t: Throwable) {
                showAlert("Ошибка сервера", "Пожалуйста попробуйте снова")
            }
        })
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }
}