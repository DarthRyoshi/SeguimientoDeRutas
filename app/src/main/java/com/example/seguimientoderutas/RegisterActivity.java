package com.example.seguimientoderutas;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private Button registerButton, backToLoginButton;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Vincular vistas
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        registerButton = findViewById(R.id.registerButton);
        backToLoginButton = findViewById(R.id.backToLoginButton);

        // Inicializar Firebase Auth y Database
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");

        // Botón para registrarse
        registerButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(email, password);
        });

        // Botón para volver al Login
        backToLoginButton.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish(); // Asegura que no se pueda regresar a esta actividad
        });
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registro exitoso
                        String userId = auth.getCurrentUser().getUid();
                        databaseRef.child(userId).setValue(email) // Guarda el email del usuario
                                .addOnCompleteListener(databaseTask -> {
                                    if (databaseTask.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        // Después de un registro exitoso, redirige al Login
                                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                        finish(); // Finaliza RegisterActivity para evitar que regrese a ella
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Error al guardar los datos", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Error en el registro
                        Toast.makeText(RegisterActivity.this, "Registro fallido: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
