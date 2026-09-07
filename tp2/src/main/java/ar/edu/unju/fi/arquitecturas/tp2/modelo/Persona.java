package ar.edu.unju.fi.arquitecturas.tp2.modelo;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
//aca usamos los atajos que nos proporciona la libreria lombok para no tener que escribir
//codigo manual
@SuperBuilder @AllArgsConstructor @NoArgsConstructor @Getter @Setter
//aca indicamos que es una clase abstracta y que no debera ser mapeada ademas tambien
//Extrae todos los atributos y reglas que se programan dentro de la clase y
//Inyecta esos atributos directamente como columnas nuevas dentro de las
// tablas de las clases hijas que sí llevan la etiqueta @Entity
@MappedSuperclass

public abstract class Persona {
    @Id //indicamos la clave primaria que sera la clave que utilizaran las
    //hijas que si seran tablas de bs
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nya;
    private Long cuil;
}
