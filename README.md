# Country Currency & Exchange API

A RESTful API built with **Java Spring Boot** that fetches global country data and currency exchange rates, stores them in MySQL, and provides CRUD operations plus analytics (GDP estimates and summary image).



## 🚀 Features
- Fetch country data from [REST Countries API](https://restcountries.com/v2/all?fields=name,capital,region,population,flag,currencies)
- Fetch exchange rates from [Open Exchange API](https://open.er-api.com/v6/latest/USD)
- Compute estimated GDP = population × random(1000–2000) ÷ exchange_rate
- Cache and update data in MySQL
- Filter, sort, and retrieve country data
- Generate and serve a summary image of top 5 GDPs
- Detailed error handling and JSON-only responses



## 🏗️ Tech Stack
| Component | Technology |
|------------|-------------|
| Language | Java 17 |
| Framework | Spring Boot 3 |
| Database | MySQL |
| ORM | Spring Data JPA |
| Build Tool | Maven |
| HTTP Client | RestTemplate |
| Image Creation | Java AWT & ImageIO |



## ⚙️ Setup Instructions

### 1️⃣ Clone the Repository
```bash
Clone the repository: git clone cd
cd CountryCurrency


##Configure Database
spring.datasource.url=jdbc:mysql://localhost:3306/country_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
server.port=8083

Run Application


