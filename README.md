Sample AKKA REST service app

With reference to: 
[Building a fully functional REST service using Akka Actors](https://medium.com/@ian.shiundu/building-a-fully-functional-rest-service-using-akka-actors-8b5c12978380)

To test the API endpoints:

```
curl -X POST  "http://localhost:9090/kopi-seller/create/" -H 'Content-Type: application/json' -d '{"name": "mocha"}'

curl -X POST  "http://localhost:9090/kopi-seller/make/" -H 'Content-Type: application/json' -d '{"cups": 100}'

curl -X GET  "http://localhost:9090/kopi-seller/get-count" -H 'Content-Type: application/json' -d '{"name": "mocha"}'

curl -X POST  "http://localhost:9090/kopi-seller/buy" -H 'Content-Type: application/json' -d '{"name": "mocha", "cups": 4}'

curl -X POST  "http://localhost:9090/kopi-seller/clear" -H 'Content-Type: application/json'
```
