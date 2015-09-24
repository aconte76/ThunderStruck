# ThunderStruck
a framework for Rest/JDBC/CP faster self contained microservices

## Motivation
Thunderstruck is a set of small frameworks to faster Your daily annoyance things. 

---
Contraints of Project is to have:
* Faster implied technology maintaining all needed flexibility
* Slimest external framework usage

---
Applicable fields:

* REST/RESTful Microservices
  * REST is Sinatra inspired (no annotations or configurations)
  * Multiple instance standalone service (simple Jetty implementation)
  * Easily usable into a container (Tomcat, Glassfish, JBoss, etc.)

* JDBC
  * Specific RDBMS ConnectionPool implementation (actually only Oracle on HikariCP)
  * Named Parameters support
  * Automatic ResultSet mapping into a pojo class
  * ResultSet mapping with specific handler
  * Streaming ResultSet mapping

