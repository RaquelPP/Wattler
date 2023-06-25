package com.example.mysmartplugdemo;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Date;

public class RegistroEncendido extends AppCompatActivity {

    private Date fecha;
    private boolean encendido;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void EncendidoRegistro(Date fecha, boolean encendido) {
        this.fecha = fecha;
        this.encendido = encendido;
    }

    public Date getFecha() {

        return fecha;
    }

    public boolean isEncendido() {

        return encendido;
    }
}
