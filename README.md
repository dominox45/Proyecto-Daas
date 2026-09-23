<h1 align="center">🏦 Sistema Bancario</h1>

<p align="center"> <strong>Desarrollo y Arquitecturas Avanzadas de Software</strong><br> Ingeniería en Informática · Universidad Nacional de Jujuy<br><br> <img src="https://img.shields.io/badge/Java-25-orange?logo=openjdk&logoColor=white" alt="Java"> <img src="https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen?logo=springboot&logoColor=white" alt="Spring Boot"> <img src="https://img.shields.io/badge/Spring%20Data-JPA-6DB33F?logo=spring&logoColor=white" alt="Spring Data JPA"> <img src="https://img.shields.io/badge/Hibernate-ORM-59666C?logo=hibernate&logoColor=white" alt="Hibernate"> <img src="https://img.shields.io/badge/MySQL-8.4.11-4479A1?logo=mysql&logoColor=white" alt="MySQL"> <img src="https://img.shields.io/badge/Maven-Wrapper-C71A36?logo=apachemaven&logoColor=white" alt="Maven"> <img src="https://img.shields.io/badge/JUnit-Testing-25A162?logo=junit5&logoColor=white" alt="JUnit"> </p>

<hr>

<h2>📌 Descripción</h2>

<p> Sistema bancario desarrollado para la asignatura <strong>Desarrollo y Arquitecturas Avanzadas de Software</strong> de la carrera Ingeniería en Informática de la Universidad Nacional de Jujuy. </p>

<p> El proyecto modela el núcleo de un sistema bancario, contemplando la gestión de clientes, cuentas financieras y transacciones, junto con su persistencia mediante JPA/Hibernate sobre MySQL. </p>

<p>La implementación aplica conceptos de:</p>

<ul> <li>Programación Orientada a Objetos.</li> <li>Herencia de entidades.</li> <li>Relaciones entre entidades.</li> <li>Tipos enumerados.</li> <li>Persistencia mediante Jakarta Persistence (JPA).</li> <li>Mapeo objeto-relacional mediante Hibernate ORM.</li> <li>Auditoría automática de entidades.</li> <li>Integridad referencial.</li> <li>Repositorios mediante Spring Data JPA.</li> <li>Query Methods mediante convención de nombres.</li> <li>Pruebas de integración sobre una instancia real de MySQL.</li> </ul>

<hr>

<h2>🏦 Dominio del sistema</h2>

<p> El sistema representa las principales entidades involucradas en la gestión de clientes, cuentas bancarias y operaciones financieras. </p>

<h3>👤 Clientes</h3>

<p> Un <code>Cliente</code> representa una persona o entidad registrada en el sistema bancario. Cada cliente posee: </p>

<ul> <li>Identificador UUID.</li> <li>Nombre o Razón Social.</li> <li>CUIL.</li> <li>Email.</li> <li>Teléfono.</li> <li>Dirección.</li> <li>Fecha de creación.</li> <li>Fecha de última modificación.</li> </ul>

<p> El modelo contempla además una relación reflexiva entre clientes mediante la cual un cliente puede estar asociado a otro cliente como titular. </p>

<h3>💳 Cuentas financieras</h3>

<p> <code>CuentaFinanciera</code> constituye la abstracción base para los diferentes productos de cuentas bancarias. </p>

<p> La entidad es abstracta y utiliza herencia JPA mediante: </p>

<pre><code class="language-java">@Inheritance(strategy = InheritanceType.JOINED)</code></pre>

<p>Cada cuenta financiera posee:</p>

<ul> <li>Identificador UUID.</li> <li>CBU.</li> <li>Alias.</li> <li>Saldo operativo.</li> <li>Estado.</li> <li>Fecha de creación.</li> <li>Fecha de última modificación.</li> </ul>

<p>El estado de una cuenta se representa mediante <code>EstadoCuenta</code>:</p>

<ul> <li><code>ACTIVA</code></li> <li><code>SUSPENDIDA</code></li> <li><code>BLOQUEADA</code></li> </ul>

<p>El sistema contempla dos especializaciones:</p>

<ol> <li><code>CajaDeAhorro</code></li> <li><code>CuentaCorriente</code></li> </ol>

<h3>💰 Caja de Ahorro</h3>

<p> <code>CajaDeAhorro</code> hereda de <code>CuentaFinanciera</code> y agrega: </p>

<ul> <li><code>tasaInteresAnual</code></li> <li><code>limiteExtraccionesMensualesSinCosto</code></li> </ul>

<h3>🏧 Cuenta Corriente</h3>

<p> <code>CuentaCorriente</code> hereda de <code>CuentaFinanciera</code> y agrega: </p>

<ul> <li><code>descubiertoAutorizado</code></li> <li><code>costoComisionMantenimientoMensual</code></li> </ul>

<p> Los importes monetarios se representan mediante <code>BigDecimal</code>. </p>

<h3>🔄 Transacciones</h3>

<p> <code>Transaccion</code> representa una operación realizada sobre una cuenta financiera. Cada transacción posee: </p>

<ul> <li>Identificador UUID.</li> <li>Fecha y hora.</li> <li>Monto.</li> <li>Tipo de transacción.</li> <li>Estado de procesamiento.</li> <li>Cuenta financiera asociada.</li> <li>Fecha de creación.</li> <li>Fecha de última modificación.</li> </ul>

<p>Los tipos de transacción disponibles son:</p>

<ul> <li><code>DEPOSITO</code></li> <li><code>EXTRACCION</code></li> <li><code>TRANSFERENCIA_ENVIADA</code></li> <li><code>TRANSFERENCIA_RECIBIDA</code></li> </ul>

<p>El estado de procesamiento se representa mediante:</p>

<ul> <li><code>PENDIENTE</code></li> <li><code>COMPLETADA</code></li> <li><code>RECHAZADA</code></li> <li><code>REVERTIDA</code></li> </ul>

<p> Cada transacción pertenece a una cuenta financiera, manteniendo una relación de persistencia entre <code>Transaccion</code> y <code>CuentaFinanciera</code>. </p>

<hr>

<h2>🧱 Arquitectura</h2>

<p> La implementación se concentra actualmente en el modelo de dominio y la persistencia. El acceso a los datos se organiza mediante Spring Data JPA sobre Hibernate ORM. </p>

<pre><code>┌─────────────────────────────┐ │ Modelo de Dominio │ │ Entidades + Enumeraciones │ └──────────────┬──────────────┘ │ ┌──────────────▼──────────────┐ │ Repository │ │ Acceso a datos │ └──────────────┬──────────────┘ │ ┌──────────────▼──────────────┐ │ JPA / Hibernate │ │ Persistencia ORM │ └──────────────┬──────────────┘ │ ┌──────────────▼──────────────┐ │ MySQL │ │ Base de datos │ └─────────────────────────────┘</code></pre>

<hr>

<h2>🛠️ Tecnologías</h2>

<table> <thead> <tr> <th>Tecnología</th> <th>Versión / Uso</th> </tr> </thead> <tbody> <tr> <td><strong>Java</strong></td> <td>25</td> </tr> <tr> <td><strong>Spring Boot</strong></td> <td>4.1.1</td> </tr> <tr> <td><strong>Spring Data JPA</strong></td> <td>Persistencia y repositorios</td> </tr> <tr> <td><strong>Hibernate ORM</strong></td> <td>7.4.5.Final</td> </tr> <tr> <td><strong>MySQL</strong></td> <td>8.4.11</td> </tr> <tr> <td><strong>Maven Wrapper</strong></td> <td>Gestión y ejecución del proyecto</td> </tr> <tr> <td><strong>Lombok</strong></td> <td>Reducción de código repetitivo</td> </tr> <tr> <td><strong>JUnit</strong></td> <td>Pruebas automatizadas</td> </tr> </tbody> </table>

<hr>

<h2>🗄️ Persistencia e integridad</h2>

<p> La persistencia se implementa utilizando Jakarta Persistence (JPA) con Hibernate ORM sobre MySQL. Hibernate realiza el mapeo entre las entidades Java y las tablas relacionales correspondientes. </p>

<h3>Identificadores</h3>

<p> Las entidades utilizan identificadores de tipo <code>UUID</code> generados mediante: </p>

<pre><code class="language-java">@GeneratedValue(strategy = GenerationType.UUID)</code></pre>

<p> En MySQL estos identificadores son almacenados como valores binarios. </p>

<h3>Restricciones de cuentas</h3>

<p> Las cuentas financieras poseen restricciones de persistencia sobre sus identificadores bancarios: </p>

<pre><code>CBU ├── obligatorio ├── único └── longitud máxima: 22 caracteres Alias ├── obligatorio └── único</code></pre>

<p> El esquema generado por Hibernate refleja estas restricciones mediante columnas <code>NOT NULL</code>, índices únicos y claves primarias. </p>

<h3>Valores monetarios</h3>

<p> Los importes financieros utilizan <code>BigDecimal</code> y se almacenan mediante columnas decimales. </p>

<pre><code class="language-sql">decimal(19,2)</code></pre>

<p> Esto permite representar importes monetarios sin recurrir a tipos de punto flotante. </p>

<hr>

<h2>🧬 Herencia JPA</h2>

<p> La jerarquía de cuentas utiliza la estrategia: </p>

<pre><code class="language-java">@Inheritance(strategy = InheritanceType.JOINED)</code></pre>

<p>El modelo queda estructurado de la siguiente manera:</p>

<pre><code> CuentaFinanciera │ ┌────────┴────────┐ │ │ CajaDeAhorro CuentaCorriente</code></pre>

<p> Con <code>JOINED</code>, los atributos comunes de <code>CuentaFinanciera</code> se almacenan en la tabla <code>cuentas_financieras</code>, mientras que los atributos específicos de cada subtipo se almacenan en sus respectivas tablas: </p>

<ul> <li><code>cajas_de_ahorro</code></li> <li><code>cuentas_corrientes</code></li> </ul>

<p> Las tablas de las especializaciones utilizan el mismo identificador de la cuenta financiera como clave primaria y referencia a la tabla padre. </p>

<hr>

<h2>🔗 Relaciones entre entidades</h2>

<p> El modelo implementa las relaciones necesarias para representar el dominio bancario. </p>

<h3>Cliente — Cuenta Financiera</h3>

<p> Un cliente puede poseer una o más cuentas y una cuenta puede pertenecer a múltiples clientes mediante una relación de co-titularidad. </p>

<p> La relación se materializa mediante una tabla intermedia: </p>

<pre><code>clientes_cuentas ├── cliente_id └── cuenta_id</code></pre>

<p> La clave primaria está compuesta por <code>(cliente_id, cuenta_id)</code>, representando una relación muchos-a-muchos entre clientes y cuentas. </p>

<h3>Cliente — Cliente</h3>

<p> El modelo contempla una relación reflexiva entre clientes mediante: </p>

<pre><code>clientes └── titular_id</code></pre>

<p> Esta relación permite representar la estructura de titularidad definida en el modelo. </p>

<h3>Cuenta Financiera — Transacción</h3>

<p> Una cuenta financiera puede registrar múltiples transacciones. La relación se materializa mediante: </p>

<pre><code>transacciones └── cuenta_id</code></pre>

<p> La columna <code>cuenta_id</code> constituye una clave foránea hacia <code>cuentas_financieras</code>. </p>

<p> La relación se encuentra respaldada por una restricción de no nulidad, por lo que una <code>Transaccion</code> requiere una cuenta financiera asociada para poder persistirse. </p>

<hr>

<h2>🕒 Auditoría</h2>

<p> Las entidades auditables utilizan la clase base <code>EntidadAuditable</code>, definida como una <code>@MappedSuperclass</code>. </p>

<p>La auditoría utiliza:</p>

<pre><code class="language-java">@CreatedDate @LastModifiedDate</code></pre>

<p> junto con: </p>

<pre><code class="language-java">@EntityListeners(AuditingEntityListener.class)</code></pre>

<p> La aplicación habilita la funcionalidad mediante: </p>

<pre><code class="language-java">@EnableJpaAuditing</code></pre>

<p> De esta manera se registran automáticamente las fechas de: </p>

<ul> <li><code>fechaCreacion</code></li> <li><code>fechaModificacion</code></li> </ul>

<p> La funcionalidad fue verificada mediante pruebas de integración con Spring Boot y JPA. </p>

<hr>

<h2>📦 Capa Repository</h2>

<p> El acceso a los datos se implementa mediante interfaces de <code>JpaRepository</code>. Esto permite utilizar las operaciones CRUD proporcionadas por Spring Data JPA sin implementar manualmente las operaciones básicas de persistencia. </p>

<p>Los repositorios implementados son:</p>

<pre><code>repository/ ├── CajaDeAhorroRepository ├── ClienteRepository ├── CuentaCorrienteRepository ├── CuentaFinancieraRepository └── TransaccionRepository</code></pre>

<p> Todos los repositorios utilizan identificadores de tipo <code>UUID</code>. </p>

<hr>

<h2>🔎 Query Methods</h2>

<p> Además de las operaciones heredadas de <code>JpaRepository</code>, se implementaron Query Methods personalizados utilizando la convención de nombres de Spring Data JPA. </p>

<h3>ClienteRepository</h3>

<p>Permite localizar clientes mediante:</p>

<ul> <li><code>findByCuil(...)</code></li> <li><code>findByEmail(...)</code></li> </ul>

<h3>CuentaFinancieraRepository</h3>

<p>Permite localizar cuentas mediante:</p>

<ul> <li><code>findByCbu(...)</code></li> <li><code>findByAlias(...)</code></li> </ul>

<h3>CajaDeAhorroRepository</h3>

<p> Permite realizar consultas derivadas sobre: </p>

<ul> <li><code>limiteExtraccionesMensualesSinCosto</code></li> <li><code>tasaInteresAnual</code></li> </ul>

<p> utilizando condiciones <code>GreaterThan</code>. </p>

<h3>CuentaCorrienteRepository</h3>

<p> Permite realizar consultas derivadas sobre: </p>

<ul> <li><code>descubiertoAutorizado</code></li> <li><code>costoComisionMantenimientoMensual</code></li> </ul>

<p> utilizando condiciones <code>GreaterThan</code>. </p>

<h3>TransaccionRepository</h3>

<p> Permite consultar transacciones mediante sus atributos de estado y tipo: </p>

<ul> <li><code>estadoTransaccion</code></li> <li><code>tipo</code></li> </ul>

<p> utilizando Query Methods derivados por Spring Data JPA. </p>

<hr>

<h2>📐 Modelo de dominio</h2>

classDiagram

    class Cliente {
        UUID id
        String nombre
        String cuil
        String email
        String telefono
        String direccion
        UUID titular_id
    }

    class CuentaFinanciera {
        <<abstract>>
        UUID id
        String cbu
        String alias
        BigDecimal saldoOperativo
        EstadoCuenta estado
    }

    class CajaDeAhorro {
        BigDecimal tasaInteresAnual
        int limiteExtraccionesMensualesSinCosto
    }

    class CuentaCorriente {
        BigDecimal descubiertoAutorizado
        BigDecimal costoComisionMantenimientoMensual
    }

    class Transaccion {
        UUID id
        LocalDateTime fechaHora
        BigDecimal monto
        TipoTransaccion tipo
        EstadoTransaccion estadoTransaccion
    }

    class EstadoCuenta {
        <<enumeration>>
        ACTIVA
        SUSPENDIDA
        BLOQUEADA
    }

    class TipoTransaccion {
        <<enumeration>>
        DEPOSITO
        EXTRACCION
        TRANSFERENCIA_ENVIADA
        TRANSFERENCIA_RECIBIDA
    }

    class EstadoTransaccion {
        <<enumeration>>
        PENDIENTE
        COMPLETADA
        RECHAZADA
        REVERTIDA
    }

    CuentaFinanciera <|-- CajaDeAhorro
    CuentaFinanciera <|-- CuentaCorriente

    Cliente "0..*" -- "0..*" CuentaFinanciera : co-titularidad

    Cliente "0..1" --> "0..*" Cliente : titular

    CuentaFinanciera "1" --> "0..*" Transaccion : registra

    CuentaFinanciera --> EstadoCuenta
    Transaccion --> TipoTransaccion
    Transaccion --> EstadoTransaccion

<p> El diagrama representa las entidades y relaciones principales del dominio. La infraestructura de auditoría no forma parte del modelo conceptual, por lo que <code>EntidadAuditable</code> se documenta independientemente en la sección de auditoría. </p>

<hr>

<h2>📂 Estructura del proyecto</h2>

<pre><code>tp2/ ├── src/ │ ├── main/ │ │ ├── java/ │ │ │ └── ar/ │ │ │ └── edu/ │ │ │ └── unju/ │ │ │ └── fi/ │ │ │ └── arquitecturas/ │ │ │ └── tp2/ │ │ │ ├── model/ │ │ │ │ ├── CajaDeAhorro.java │ │ │ │ ├── Cliente.java │ │ │ │ ├── CuentaCorriente.java │ │ │ │ ├── CuentaFinanciera.java │ │ │ │ ├── EntidadAuditable.java │ │ │ │ ├── Transaccion.java │ │ │ │ └── enums/ │ │ │ ├── EstadoCuenta.java │ │ │ ├── EstadoTransaccion.java │ │ │ └── TipoTransaccion.java │ │ │ │ │ │ ├── repository/ │ │ │ │ ├── CajaDeAhorroRepository.java │ │ │ │ ├── ClienteRepository.java │ │ │ │ ├── CuentaCorrienteRepository.java │ │ │ │ ├── CuentaFinancieraRepository.java │ │ │ │ └── TransaccionRepository.java │ │ │ │ │ │ │ └── Tp2Application.java │ │ │ │ │ └── resources/ │ │ └── application.yml │ │ │ └── test/ │ └── java/ │ └── ar/ │ └── edu/ │ └── unju/ │ └── fi/ │ └── arquitecturas/ │ └── tp2/ │ ├── AuditoriaTest.java │ ├── RelacionesJpaTest.java │ ├── RepositoryIntegrationTest.java │ └── Tp2ApplicationTests.java │ ├── .gitignore ├── .gitattributes ├── .mvn/ ├── mvnw ├── mvnw.cmd ├── pom.xml └── README.md</code></pre>

<hr>

<h2>⚙️ Requisitos previos</h2>

<p>Antes de ejecutar el proyecto se necesita tener instalado:</p>

<ul> <li>JDK 25</li> <li>MySQL 8.4.x</li> <li>Git</li> </ul>

<p> No es necesario instalar Maven de forma global, ya que el proyecto utiliza Maven Wrapper. </p>

<hr>

<h2>🗃️ Configuración de MySQL</h2>

<p>Crear la base de datos:</p>

<pre><code class="language-sql">CREATE DATABASE tp2;</code></pre>

<p> La aplicación utiliza variables de entorno para evitar almacenar las credenciales de la base de datos directamente en el repositorio. </p>

<h3>Windows PowerShell</h3>

<pre><code class="language-powershell">$env:DB_HOST="localhost" $env:DB_PORT="3306" $env:DB_NAME="tp2" $env:DB_USERNAME="root" $env:DB_PASSWORD="TU_CONTRASEÑA"</code></pre>

<p> ⚠️ La contraseña real de MySQL no debe almacenarse en <code>application.yml</code>, README, commits ni en el repositorio remoto. </p>

<p> La configuración de Spring utiliza estas variables para construir la conexión: </p>

<pre><code class="language-yaml">spring: datasource: url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?serverTimezone=UTC username: ${DB_USERNAME} password: ${DB_PASSWORD} jpa: show-sql: true hibernate: ddl-auto: update</code></pre>

<hr>

<h2>▶️ Ejecución</h2>

<p>Desde la carpeta raíz del proyecto:</p>

<h3>Windows</h3>

<pre><code class="language-powershell">.\mvnw.cmd spring-boot:run</code></pre>

<p> La aplicación se inicia utilizando el puerto <code>8080</code>. </p>

<hr>

<h2>🧪 Testing</h2>

<p> El proyecto incorpora pruebas automatizadas orientadas principalmente a verificar el comportamiento del modelo, la persistencia, las relaciones, la auditoría y los repositorios. </p>

<p>Se verifican, entre otros aspectos:</p>

<ul> <li>Carga correcta del contexto de Spring Boot.</li> <li>Persistencia de entidades mediante JPA.</li> <li>Herencia entre entidades.</li> <li>Relaciones entre entidades.</li> <li>Auditoría automática.</li> <li>Consultas mediante Spring Data JPA.</li> <li>Persistencia y recuperación mediante los repositorios.</li> <li>Integración entre Spring Boot, JPA, Hibernate y MySQL.</li> </ul>

<h3>Ejecutar todas las pruebas</h3>

<pre><code class="language-powershell">.\mvnw.cmd test</code></pre>

<h3>Ejecutar las pruebas de auditoría</h3>

<pre><code class="language-powershell">.\mvnw.cmd -Dtest=AuditoriaTest test</code></pre>

<h3>Pruebas de Repository</h3>

<p> <code>RepositoryIntegrationTest</code> verifica la persistencia y recuperación mediante los cinco repositorios implementados y sus Query Methods personalizados. </p>

<pre><code>Cliente ├── guardar ├── buscar por CUIL └── buscar por Email CajaDeAhorro ├── guardar ├── consultar límite de extracciones └── consultar tasa de interés CuentaCorriente ├── guardar ├── consultar descubierto autorizado └── consultar comisión de mantenimiento CuentaFinanciera ├── guardar ├── buscar por CBU └── buscar por Alias Transaccion ├── guardar ├── consultar por estado └── consultar por tipo</code></pre>

<p> Las pruebas de integración fueron ejecutadas utilizando una instancia real de MySQL, verificando que los repositorios sean detectados por Spring Data JPA y que puedan persistir y recuperar información correctamente. </p>

<p><strong>Resultado de la ejecución de las pruebas de Repository:</strong></p>

<pre><code>Tests run: 5 Failures: 0 Errors: 0 Skipped: 0 BUILD SUCCESS</code></pre>

<hr>

<h2>👨‍💻 Autores</h2>

<ul> <li><strong>Facundo Ezequiel Tolaba Catarí</strong></li> <li><strong>Maximiliano Luis Esteban Diaz</strong></li> </ul>

<p> <strong>Ingeniería en Informática — UNJu</strong><br> Desarrollo y Arquitecturas Avanzadas de Software · 2026 </p>

<p><em>Proyecto académico</em></p>