package com.weatherapp.db.fb

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.ktx.snapshots

class FBDatabase {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    val user: Flow<FBUser>
        get() {
            if (auth.currentUser == null) return emptyFlow()

            return db.collection("users")
                .document(auth.currentUser!!.uid)
                .snapshots()
                .map { it.toObject(FBUser::class.java)!! }
        }

    val cities: Flow<List<FBCity>>
        get() {
            if (auth.currentUser == null) return emptyFlow()

            return db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("cities")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FBCity::class.java)
                }
        }

    fun register(user: FBUser) {
        val uid = auth.currentUser?.uid
            ?: throw RuntimeException("User not logged in!")

        db.collection("users")
            .document(uid)
            .set(user)
    }

    fun add(city: FBCity) {
        val uid = auth.currentUser?.uid
            ?: throw RuntimeException("User not logged in!")

        if (city.name.isNullOrEmpty())
            throw RuntimeException("City with null or empty name!")

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .set(city)
    }

    fun remove(city: FBCity) {
        val uid = auth.currentUser?.uid
            ?: throw RuntimeException("User not logged in!")

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .delete()
    }

    fun update(city: FBCity) {
        val uid = auth.currentUser?.uid
            ?: throw RuntimeException("User not logged in!")

        val changes = mapOf(
            "lat" to city.lat,
            "lng" to city.lng,
            "monitored" to city.monitored
        )

        db.collection("users")
            .document(uid)
            .collection("cities")
            .document(city.name!!)
            .update(changes)
    }
}