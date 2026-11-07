package com.acompanAR.backend.controller;

import com.acompanAR.backend.entity.Localidad;
import com.acompanAR.backend.entity.TipoInstitucion;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Listas fijas (enums) que el front necesita para armar los desplegables.
// Son públicas porque el registro las usa antes de iniciar sesión.
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @GetMapping("/localidades")
    public List<Map<String, String>> localidades() {
        return Arrays.stream(Localidad.values())
                .map(l -> opcion(l.name(), l.getNombre()))
                .toList();
    }

    @GetMapping("/tipos-institucion")
    public List<Map<String, String>> tiposInstitucion() {
        return Arrays.stream(TipoInstitucion.values())
                .map(t -> opcion(t.name(), t.getNombre()))
                .toList();
    }

    private Map<String, String> opcion(String codigo, String nombre) {
        Map<String, String> opcion = new LinkedHashMap<>();
        opcion.put("codigo", codigo);
        opcion.put("nombre", nombre);
        return opcion;
    }
}
