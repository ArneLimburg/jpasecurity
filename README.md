[![Build Status](https://travis-ci.com/ArneLimburg/jpasecurity.svg?branch=master)](https://travis-ci.com/ArneLimburg/jpasecurity) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=org.jpasecurity%3Ajpasecurity&metric=security_rating)](https://sonarcloud.io/dashboard?id=org.jpasecurity%3Ajpasecurity) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=org.jpasecurity%3Ajpasecurity&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=org.jpasecurity%3Ajpasecurity) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=org.jpasecurity%3Ajpasecurity&metric=bugs)](https://sonarcloud.io/dashboard?id=org.jpasecurity%3Ajpasecurity) [![sonarcloud](https://sonarcloud.io/api/project_badges/measure?project=org.jpasecurity%3Ajpasecurity&metric=coverage)](https://sonarcloud.io/dashboard?id=org.jpasecurity%3Ajpasecurity)

# JPA Security
JPA Security is an Access Control Solution for the Java Persistence API (JPA). Its features include:

* High Performance querying: With JPA Security your access control is performed in the database. You may query the database for all objects of a certain type and will get only the objects you have read access for. This filtering occurs in the database. Unaccessible queried objects will not be loaded into memory.
* Access Control via Configuration: JPA Security enables you to completely remove security-related code from your code-base. All access control may be configured via Annotations or XML.
* Support for role-based access control, access control lists (ACLs) and domain-driven access control: With JPA Security you do not have to change your access control paradigm (but maybe you want to, when you see the great capability of JPA Security). You even can mix access control paradigms easily.
* Integration for Java EE Security and other frameworks: JPA Security is not designed to replace current security solutions, but to extend them. It integrates smoothly into the security mechanisms of the Java EE Platform, but may be used with third-party frameworks like Spring Security or in Java SE, too.
* Easy Extensibility: With the extensibility of JPA Security it is easy to provide your own access control paradigm, access rules storage or login mechanism.

# JPA Version
Currently all JPA 2.1 features are supported.

[What is new in JPA 2.1?](https://en.wikibooks.org/wiki/Java_Persistence/What_is_new_in_JPA_2.1%3F)

# Supported Persistence Provider

* EclipseLink 2.5+
* Hibernate 4.3+
* OpenJPA 2.0+ (To use all JPA 2.1 features, you have to use OpenJPA 3.0)


© 2008-2019 JPA Security
