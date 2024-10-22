# Build & Run Steps

1. Ensure java 17 or above is installed, testing was done on mysql:8.0.40
2. Clone from repo link:
3. Go to demo folder
4. Change database user name & password in application.properties
5. Run database script schema.sql under resources
6. Run static main method on GameSalesDataBuilder.java to generate CSV file(game_sales.csv) for upload in the root folder
7. Build project ./gradlew clean build
8. Navigate to jar file path and run command java -jar demo-0.0.1-SNAPSHOT.jar 


# Url Links

http://localhost:8080/demo/game-sales/import


http://localhost:8080/demo/game-sales/getGameSales?page=50

http://localhost:8080/demo/game-sales/getGameSales?page=1&fromDate=2024-04-01&toDate=2024-04-15

http://localhost:8080/demo/game-sales/getGameSales?page=1&fromDate=2024-04-01&toDate=2024-04-15&salePriceLessThan=80

http://localhost:8080/demo/game-sales/getGameSales?page=1&fromDate=2024-04-01&toDate=2024-04-15&salePriceMoreThan=60


http://localhost:8080/demo/game-sales/getTotalSales?category=volume&fromDate=2024-04-01

http://localhost:8080/demo/game-sales/getTotalSales?category=sales&fromDate=2024-04-01&toDate=2024-04-15

http://localhost:8080/demo/game-sales/getTotalSales?category=sales&fromDate=2024-04-01&toDate=2024-04-15&gameNo=3