this url for request : 


GET : 

http://localhost:8080/persons

POST : 

curl --location 'http://localhost:8080/persons' \
--header 'Content-Type: application/x-www-form-urlencoded' \
--data-urlencode 'abdulrhman='

PUT : 
http://localhost:8080/persons
body : 
{"id": 5, "name": "abdulrhman"}

DELETE : 
http://localhost:8080/persons/4
