use "mvn springboot:run" to run the program.
use postman to test post and get methods.
run on "https//localhost/8080/hr-console"
JDBC URL:jdbc:h2:mem:billingdb
USERNAME: sa
Password: leave empty field
add customer from terminal example: Invoke-RestMethod -Uri "http://localhost:8080/api/customers" -Method Post `
>>   -Body '{"name":"mugo","email":"mugo@example.com","phone":"298248-456-7890"}' `
>>   -ContentType "application/json"
add invoice from terminal example: Invoke-RestMethod -Uri "http://localhost:8080/api/invoices" -Method Post `
>>     -Body '{
>>         "customer": {"id": 1},
>>         "amount": 580.0,
>>         "dueDate": "2026-03-15"
>>     }' `
>>     -ContentType "application/json"

add payment from terminal example: 