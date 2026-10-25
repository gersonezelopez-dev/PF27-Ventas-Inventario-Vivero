package com.vivero.pf27.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> respuesta(
            HttpStatus estado, String mensaje) {

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("fecha", LocalDateTime.now());
        error.put("estado", estado.value());
        error.put("mensaje", mensaje);

        return ResponseEntity.status(estado).body(error);
    }

    // Errores de datos o reglas de negocio
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argumentoInvalido(
            IllegalArgumentException ex) {

        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Errores de estado de una operacion
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> conflicto(
            IllegalStateException ex) {

        return respuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Errores de validacion de los DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .findFirst()
                .orElse("Datos invalidos");

        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }
}
