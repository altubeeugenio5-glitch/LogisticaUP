LogisticaUP
Plataforma de gestión logística y optimización inteligente de rutas de entrega.
LogisticaUP es una aplicación diseñada para administrar operaciones logísticas, gestionar pedidos y coordinar repartidores, incorporando un servicio de optimización de rutas desarrollado en Python.
El sistema integra una arquitectura basada en servicios independientes que se comunican mediante APIs REST y mensajería asíncrona.
________________________________________
Características principales
Gestión logística
•	Administración de usuarios, clientes y repartidores.
•	Gestión de productos e inventario.
•	Registro y seguimiento de pedidos.
•	Gestión de movimientos de stock.
•	Creación y administración de rutas de entrega.
•	Asignación y desasignación de repartidores.
•	Asociación de pedidos a rutas.
•	Administración de múltiples paradas por ruta.
Optimización de rutas
•	Servicio de optimización desarrollado en Python.
•	Utilización de Google OR-Tools para resolver problemas de optimización.
•	Procesamiento asíncrono mediante RabbitMQ.
•	Comunicación entre el backend y el optimizador.
•	Actualización de los resultados de optimización en el sistema.
Seguridad
•	Autenticación mediante JWT.
•	Control de acceso a los endpoints protegidos.
•	Gestión de usuarios y roles.
________________________________________
Arquitectura del sistema
El proyecto está dividido en tres componentes principales:
Componente	  Tecnología	              Responsabilidad
Frontend	    React, TypeScript y Vite	Interfaz de usuario
Backend	      Java y Spring Boot	      Lógica de negocio y API REST
Optimizador	  Python y OR-Tools	        Optimización de rutas
Flujo de optimización
1.	El usuario solicita optimizar una ruta desde el frontend.
2.	El backend procesa la solicitud.
3.	La solicitud de optimización se publica en RabbitMQ.
4.	El servicio Python recibe los datos y calcula el orden optimizado de las paradas.
5.	El optimizador publica el resultado mediante RabbitMQ.
6.	El backend recibe el resultado y actualiza la ruta.
7.	El frontend puede consultar la información actualizada.
