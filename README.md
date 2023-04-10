# Bankontop microservices

## Installation

### Postman

is included a collection with the endpoints of the microservices

### Keycloak

Install Keycloak for authorization, to install you can use a docker image using instructions describe it [here](https://www.keycloak.org/getting-started/getting-started-docker).

or you can download the openjdk version [here](https://www.keycloak.org/getting-started/getting-started-zip)

after installation you can run it with the command
```bash
kc.sh start-dev
```

the first time the user is admin and the password one defined for you

then you have to create the following:

1. Create a realm with the name ontopbanking
2. Create client with the name ontopclient

2.1. In client type choose open id Connect

2.2. In valid redirect uri write http://localhost:8082/*

2.3. In advanced settings, Authentication flow overrides:

browser Flow choose browser

direct grant flow choose direct grant

2.4. Create user with the name ontopuser

In credentials assign password, for this is admin

2.5. Assign ontoprole to the use onto-user

## Mysql

Is necessary install mysql with user: root , pass: password

## Executables

you can run directly using the jars in the folder onto-jar from google drive or run each project using your favorite ide

for run use the following order
```java

# for eureka server
java -jar internet-banking-service-registry-0.0.1-SNAPSHOT.jar

# for config server
java -jar internet-banking-config-server-0.0.1-SNAPSHOT.jar

# for API Gateway
java -jar internet-banking-api-gateway-0.0.1-SNAPSHOT.jar

# for core bank service
java -jar core-banking-service-0.0.1-SNAPSHOT.jar

#for transfer service
java -jar internet-banking-fund-transfer-service-0.0.1-SNAPSHOT.jar

```
The url for eureka is this http://localhost:8081/
The url for Keycloak is this http://localhost:8080/

## License

[MIT](https://choosealicense.com/licenses/mit/)
