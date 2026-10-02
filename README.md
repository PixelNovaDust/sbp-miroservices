### Folder Structure

```
sbp-microservices/
│
├── sbp-foo-service/
│   ├── pom.xml
│   ├── Dockerfile
│   ├── README.md
│   └── src/
│       ├── main/
│       │   ├── foofoo/com/sbp/foo/
│       │   │   ├── AuthServiceApplication.java
│       │   │   ├── config/
│       │   │   ├── controller/
│       │   │   ├── service/
│       │   │   ├── repository/
│       │   │   ├── domain/
│       │   │   ├── dto/
│       │   │   ├── security/
│       │   │   └── exception/
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       └── db/
│       └── test/
│
├── docker-compose.yml
├── README.md
└── .gitignore
```