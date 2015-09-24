# ThunderStruck
A little fast framework for Rest/JDBC/CP faster self contained micro services

## Motivation
Thunderstruck is a set of small frameworks to faster Your daily annoyance things. 


Constraints of Project is to have:

* Faster implied technology maintaining all needed flexibility
* Slim external framework usage


Applicable fields:

* REST/RESTful micro services
  * REST is Sinatra inspired (no annotations or configurations)
  * Multiple instance standalone service (simple Jetty implementation)
  * Easily usable into a container (Tomcat, Glassfish, JBoss, etc.)

* JDBC
  * Specific RDBMS ConnectionPool implementation (actually only Oracle on HikariCP)
  * Named Parameters support
  * Automatic ResultSet mapping into a pojo (using reflection)
  * ResultSet mapping with specific handler (using custom handler)
  * Streaming ResultSet mapping (using listener)

