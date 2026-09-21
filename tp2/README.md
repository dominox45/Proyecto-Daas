Sistema Bancario - Proyecto DAAS

Proyecto desarrollado para la asignatura Desarrollo y Arquitecturas Avanzadas de Software de la carrera Ingeniería en Informática de la Universidad Nacional de Jujuy.

El proyecto implementa el modelo de dominio y su persistencia mediante JPA/Hibernate sobre MySQL, utilizando Spring Boot.

Tecnologías
Java 25
Spring Boot 4.1.1
Spring Data JPA
Hibernate ORM
MySQL 8.4
Maven
Lombok
JUnit
Estado actual

La implementación actual comprende el modelo de dominio y su configuración de persistencia.

Se encuentra configurada y verificada la conexión entre Spring Boot, Hibernate y MySQL 8.4.

También se encuentra implementada la auditoría básica mediante Spring Data JPA.

Las capas Repository y Service corresponden a etapas posteriores del desarrollo del proyecto.

Modelo de dominio

El dominio contempla:

Clientes.
Cuentas financieras.
Cajas de ahorro.
Cuentas corrientes.
Transacciones.
Auditoría de creación y modificación.

Las cuentas bancarias utilizan una jerarquía de entidades basada en CuentaFinanciera, con CajaDeAhorro y CuentaCorriente como especializaciones.

La estrategia de herencia JPA utilizada es:

InheritanceType.JOINED

Esto se refleja en el esquema generado por Hibernate mediante una tabla para cuentas_financieras y tablas específicas para las especializaciones.

Persistencia JPA

La persistencia se encuentra configurada mediante Spring Data JPA e Hibernate.

Hibernate genera y actualiza el esquema de la base de datos a partir de las entidades mediante:

spring:
jpa:
hibernate:
ddl-auto: update

La base de datos utilizada durante el desarrollo es:

MySQL 8.4
Configuración de la base de datos

La aplicación no almacena las credenciales de MySQL en el código fuente.

La configuración utiliza variables de entorno:

DB_HOST
DB_PORT
DB_NAME
DB_USERNAME
DB_PASSWORD

Ejemplo para una instalación local:

DB_HOST=localhost
DB_PORT=3306
DB_NAME=tp2
DB_USERNAME=root
DB_PASSWORD=<contraseña-local>
Configuración en PowerShell

Ejemplo:

$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="tp2"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="TU_CONTRASEÑA"

La contraseña debe mantenerse únicamente en la configuración local del desarrollador y no debe incorporarse al repositorio.

Creación de la base de datos

Antes de ejecutar la aplicación debe existir el esquema tp2.

Desde MySQL:

CREATE DATABASE tp2;

Luego:

USE tp2;

Si se desea regenerar completamente el esquema durante el desarrollo, puede eliminarse y recrearse la base:

DROP DATABASE tp2;
CREATE DATABASE tp2;

Posteriormente Hibernate volverá a generar las tablas al iniciar la aplicación.

Ejecución

Desde el directorio del proyecto:

.\mvnw.cmd spring-boot:run

La aplicación utilizará los valores definidos en las variables de entorno para establecer la conexión con MySQL.

Esquema generado

Actualmente Hibernate genera las siguientes tablas:

cajas_de_ahorro
clientes
cuentas_corrientes
cuentas_financieras
transacciones

La estrategia JOINED genera las relaciones entre la tabla base y las tablas de las especializaciones.

Por ejemplo:

cuentas_financieras
│
├── cuentas_corrientes
│
└── cajas_de_ahorro

En cuentas_corrientes, la columna id funciona también como clave foránea hacia cuentas_financieras.

Restricciones de cuentas financieras

Las cuentas financieras definen:

CBU
Alias

como identificadores únicos.

La entidad utiliza:

@Column(unique = true, nullable = false, length = 22)
private String cbu;

@Column(unique = true, nullable = false)
private String alias;

Por lo tanto, Hibernate genera restricciones de integridad para garantizar:

CBU obligatorio.
CBU único.
CBU con longitud máxima de 22 caracteres.
Alias obligatorio.
Alias único.

Estas restricciones fueron verificadas directamente sobre el esquema generado en MySQL.

Auditoría

Las entidades auditables heredan de:

EntidadAuditable

La clase utiliza:

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)

y define:

@CreatedDate
private LocalDateTime fechaCreacion;

@LastModifiedDate
private LocalDateTime fechaModificacion;

La aplicación habilita la auditoría mediante:

@EnableJpaAuditing

La funcionalidad fue verificada mediante una prueba de integración que:

Persiste una cuenta.
Comprueba que fechaCreacion sea registrada.
Comprueba que fechaModificacion sea registrada.
Modifica la entidad.
Ejecuta nuevamente la persistencia.
Comprueba que la fecha de creación permanezca sin modificaciones.

La prueba se encuentra en:

src/test/java/ar/edu/unju/fi/arquitecturas/tp2/AuditoriaTest.java
Pruebas

El proyecto cuenta actualmente con una prueba de carga del contexto de Spring:

Tp2ApplicationTests

y una prueba específica de auditoría:

AuditoriaTest

Para ejecutar la prueba de auditoría:

.\mvnw.cmd -Dtest=AuditoriaTest test

Para ejecutar todas las pruebas:

.\mvnw.cmd clean test

Un resultado exitoso debe finalizar con:

BUILD SUCCESS
Estructura principal
src/
├── main/
│   ├── java/
│   │   └── ar/
│   │       └── edu/
│   │           └── unju/
│   │               └── fi/
│   │                   └── arquitecturas/
│   │                       └── tp2/
│   │                           ├── model/
│   │                           └── Tp2Application.java
│   │
│   └── resources/
│       └── application.yml
│
└── test/
└── java/
└── ar/
└── edu/
└── unju/
└── fi/
└── arquitecturas/
└── tp2/
├── Tp2ApplicationTests.java
└── AuditoriaTest.java
Verificación rápida de MySQL

Después de iniciar la aplicación se puede verificar el esquema mediante:

USE tp2;

SHOW TABLES;

Para verificar la estructura de las cuentas financieras:

SHOW CREATE TABLE cuentas_financieras\G

Para verificar sus índices:

SHOW INDEX FROM cuentas_financieras;

Deben existir índices únicos para:

alias
cbu
Próximas etapas

El proyecto continuará incorporando las funcionalidades definidas para los siguientes trabajos prácticos.

Entre ellas se encuentran:

Capa Repository.
Query Methods de Spring Data JPA.
Capa de Servicios.
Pruebas unitarias.
Ampliación y verificación de las relaciones del modelo.
Co-titularidad entre clientes y cuentas.