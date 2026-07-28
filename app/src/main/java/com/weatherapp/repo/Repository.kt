package com.weatherapp.repo

import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.db.fb.toFBCity
import com.weatherapp.db.local.LocalDatabase
import com.weatherapp.db.local.toCity
import com.weatherapp.db.local.toLocalCity
import com.weatherapp.model.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class Repository(
    private val fbDB: FBDatabase,
    private val localDB: LocalDatabase
) {

    private var ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    private var cityMap = emptyMap<String, City>()

    val cities = localDB.getCities().map { list ->
        list.map { it.toCity() }
    }

    val user = fbDB.user.map { it.toUser() }

    init {
        ioScope.launch {

            fbDB.cities.collect { fbCityList ->

                val cityList = fbCityList.map { it.toCity() }
                val nameList = cityList.map { it.name }

                val deletedCities = cityMap.filter { it.key !in nameList }
                val updatedCities = cityList.filter { it.name in cityMap.keys }
                val newCities = cityList.filter { it.name !in cityMap.keys }

                newCities.forEach {
                    localDB.insert(it.toLocalCity())
                }

                updatedCities.forEach {
                    localDB.update(it.toLocalCity())
                }

                deletedCities.forEach {
                    localDB.delete(it.value.toLocalCity())
                }

                cityMap = cityList.associateBy { it.name }
            }
        }
    }

    fun add(city: City) {
        ioScope.launch {
            fbDB.add(city.toFBCity())
        }
    }

    fun remove(city: City) {
        ioScope.launch {
            fbDB.remove(city.toFBCity())
        }
    }

    fun update(city: City) {
        ioScope.launch {
            fbDB.update(city.toFBCity())
        }
    }
}