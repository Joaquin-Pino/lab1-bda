package cl.grupo3.lab1.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/** Solo ADMINISTRADOR. Usar en la clase o en el método del controlador. */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('ADMINISTRADOR')")
public @interface SoloAdmin {
}
