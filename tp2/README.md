# Sistema Bancario - Proyecto DAAS

![Java](https://img.shields.io/badge/Java-25-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.4-blue.svg)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED.svg)

Proyecto desarrollado para la asignatura **Desarrollo y Arquitecturas Avanzadas de Software** (Ingeniería en Informática - Universidad Nacional de Jujuy). La aplicación implementa el modelo de dominio, la capa de persistencia JPA y los servicios (operaciones de negocio) para un sistema bancario.

**Autor:** Facundo Ezequiel Tolaba Catarí
**Autor:** Maximiliano Luis Esteban Diaz
---

## Tabla de Contenidos

1. [Descripción del Sistema](#-descripción-del-sistema)
2. [Arquitectura y Tecnologías](#-arquitectura-y-tecnologías)
3. [Modelo de Datos e Integridad](#-modelo-de-datos-e-integridad)
4. [Estructura del Proyecto](#-estructura-del-proyecto)
5. [Requisitos Previos e Instalación](#-requisitos-previos-e-instalación)

---

## 🏦 Descripción del Sistema

El sistema administra las operaciones transaccionales y la estructura de dominios de una entidad bancaria. A diferencia de otras arquitecturas, esta iteración no expone una API REST , sino que encapsula estrictamente la lógica en una arquitectura interna basada en `Service → Repository → JPA/Hibernate → MySQL`.

### Funcionalidades Clave:
* **Gestión de Clientes y Co-titularidad:** Un cliente puede tener varias cuentas y una cuenta puede pertenecer a múltiples titulares.
* **Control de Cuentas Financieras:** Implementación de restricciones para garantizar que el CBU y el Alias sean únicos en la base de datos.
* **Procesamiento de Transacciones:** Conservación del historial de transacciones vinculadas a cada cuenta sin eliminaciones accidentales en cascada.
* **Auditoría Transversal y Herencia:** Uso de anotaciones de auditoría en la persistencia y estrategia de herencia `JOINED` para modelar jerarquías en la base de datos.

---

## 🛠️ Arquitectura y Tecnologías

* **Lenguaje:** Java 25
* **Framework Principal:** Spring Boot
    * **Spring Data JPA:** Abstracción de persistencia con interfaces de Repositories y al menos dos Query Methods por entidad.
* **Base de Datos:** MySQL 8.4.11
* **Contenedores:** Docker Desktop 
* **Testing:** Pruebas unitarias e integración de comportamientos con JUnit y Mockito para la capa de servicios.

---

## 🗄️ Modelo de Datos e Integridad

El esquema relacional garantiza la integridad referencial y resuelve la multiplicidad de la siguiente manera:

```mermaid
classDiagram
    class EntidadAuditable {
        <<MappedSuperclass>>
        +LocalDateTime fechaCreacion
        +LocalDateTime fechaModificacion
    }

    class Cliente {
        +UUID id
        +String nombre;
        +String cuil;
        +String email;
        +String telefono;
        +String direccion
    }

    class CuentaFinanciera {
        +UUID id
        +String CBU
        +String Alias
        +double SaldoOperativo
        +String Estado
    }

    class CajaDeAhorro {
        +BigDecimal tasaInteresAnual
        +int limiteExtraccionesMensualesSinCosto
    }

    class CuentaCorriente {
        +BigDecimal descubiertoAutorizado
        +BigDecimal costoComisionMantenimientoMensual
    }

    class Transaccion {
        +UUID id
        +LocalDateTime fechaHora
        +BigDecimal monto
        +TipoTransaccion tipo
        +EstadoTransaccion estadoTransaccion



    }

    EntidadAuditable <|-- Cliente
    EntidadAuditable <|-- CuentaFinanciera
    EntidadAuditable <|-- Transaccion
      
    Cliente "1" --> "0..*" Cliente : agrupa
    Cliente "1" --> "1..*" CuentaFinanciera : posee
    CuentaFinanciera <|-- CajaDeAhorro
    CuentaFinanciera <|-- CuentaCorriente
    CuentaFinanciera "1" --> "1..*" Transaccion : registra
 ```   
