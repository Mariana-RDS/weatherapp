package com.weatherapp

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.weatherapp.db.fb.FBDatabase
import com.weatherapp.db.fb.toFBUser
import com.weatherapp.model.User
import com.weatherapp.ui.theme.WeatherAppTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RegisterPage(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterPage(modifier: Modifier = Modifier) {

    val activity = LocalContext.current as Activity

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val isFormValid =
        name.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                password == confirmPassword

    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val fieldModifier = Modifier.fillMaxWidth(0.9f)

        Text(
            text = "Registro",
            fontSize = 24.sp
        )

        Spacer(Modifier.size(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome") },
            modifier = fieldModifier
        )

        Spacer(Modifier.size(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            modifier = fieldModifier
        )

        Spacer(Modifier.size(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            modifier = fieldModifier,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.size(10.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Repetir senha") },
            modifier = fieldModifier,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.size(20.dp))

        Row(
            modifier = fieldModifier,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Button(
                onClick = {

                    if (password.length < 6) {
                        Toast.makeText(activity, "Senha deve ter no mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    Firebase.auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(activity) { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(activity, "Registro OK!", Toast.LENGTH_LONG).show()
                                FBDatabase().register(User(name, email).toFBUser())
                            } else {
                                Toast.makeText(
                                    activity,
                                    "Erro: ${task.exception?.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                },
                enabled = isFormValid
            ) {
                Text("Registrar")
            }

            Button(
                onClick = {
                    name = ""
                    email = ""
                    password = ""
                    confirmPassword = ""
                }
            ) {
                Text("Limpar")
            }

            Button(
                onClick = {
                    activity.finish()
                }
            ) {
                Text("Voltar")
            }
        }
    }
}