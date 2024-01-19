package com.example.aadhaarqr

data class UserData(
    val name: String,
    val username: String,
    val gender: String,
    val mobileNumber: String,
    val dateOfBirth: String,
    val address: String,
    val base64Image: String
)

data class OldUserData(
    val name: String,
    val uid: String,
    val gender: String,
    val yob: String,
    val co: String,
    val house: String,
    val street: String,
    val lm: String,
    val loc: String,
    val vtc: String,
    val po: String,
    val dist: String,
    val subdist: String,
    val state: String,
    val pc: String,
    val dob: String
)

data class Prescription(
    val medication: String,
    val frequency: String,
    val duration: String,
    val quantity: Long,
    val instructions: String
)
