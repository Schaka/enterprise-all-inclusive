# Enterprise All Inclusive - Microservice PoC

## Introduction
This is a tenancy-capable microservice to handle user account balance.
Each tenant gets assigned their own schema in the database to completely decouple them from each other.
The intent is to deploy each redundant set of applications on a per-tenant basis. This allows for provisional scaling per tenant and even enables per-tenant deployments as all changes to the database schema are separated.

The calling service should know which tenant to call for, either by subdomain (e.g. https://tenant.service.com/api) or via reverse proxy (e.g. https://service.com/tenant/api).
This responsibility falls to the deployment and infrastructure, not this project.

To spin up a new tenant, all that's required is changing the tenant name in the file that's mapped into your container via `/config/application.yml`.

## Authentication
There is no authentication. This application assumes the calling service has handled authentication and data passed here is correct.
Because each tenant has its own set of customers, there is no additional validation whether this customer truly belongs to that tenant.
Such validation is the responsibility of the caller.

## Technology and decisions
Where the money comes from and where the money goes is outside the scope of this projects. We aren't limited to transactions only belonging to the tenant's accounts.
The decision was made to not allow any overdraft for accounts, so transactions exceeding the account balance will be rejected.

Using Kotlin was a requirement. I have not used Kotlin in production before and am not familiar with all best practices.
I'm aware of the mismatch between Hibernate requiring mutable, proxied entities and the immutable nature of Kotlin, but still found this approach more readable for a simple CRUD application over using something like jOOQ or J2DBC.

This application is realized with Spring Boot and uses one underlying Postgres database. Depending on the scale, this may not be enough to hold all tenants' data.
Tenancy could be migrated on a per-database basis.

Additionally, this may be realized with a queue of transactions modifying some stateful account balance.

Using Hibernate Envers for auditing would also be possible. But for something as simple as a transaction log, I didn't deem this necessary.

Docs could've been automatically generated with Swagger or OpenDocs, but this would require getting the application to run first.


## Development
Locally, start the docker-compose file, spin up the project in your favorite IDE or call `./gradlew bootRun`.
Need a fresh start for your local database? `docker-compose down -v --rmi all && rm -r /tmp/eai-postgres && docker-compose up`.

Additionally, every commit on the develop branch gets built automatically. You can pull the corresponding Docker image via `docker pull ghcr.io/schaka/enterprise-all-inclusive:develop`.
Commits on the main branch (stable) are built when tagged and can be pulled via `docker pull ghcr.io/schaka/enterprise-all-inclusive:latest`.

The Docker image expects to find [a configuration file](https://github.com/Schaka/enterprise-all-inclusive/blob/develop/src/main/resources/application.yml) at `/config/application.yml`. 

Make sure to map a folder on your host system to `/config` inside the container and adjust the template linked above to access to your own database.
You may change the database provider to MySQL, MariaDB or another supported by Hibernate and Spring-Boot.

Every tenant will receive its own schema, so feel free to spin up as many containers or ReplicaSets as you wish. 

**By default, this application inserts dummy data into the database. You may need to remove the Flyway migration or clean up the database yourself.**


## How to use this project
The follow endpoints are exposed:
- `GET /api/{customer}/{account}/transactions`
- `POST /api/{customer}/{account}/transactions/book`
- `POST /api/{customer}/{account}/transactions/{transaction}/rollback`


#### Transaction history
This is the simplest endpoint to use. Given a valid customer (64 bit integer) and valid account (64 bit integer), it will return the recent transaction history.
It takes the following request parameters:
- page (defaults to 0)
- size (defaults to 50)

Entries are sorted by latest modification date, so most recently processed transactions will be shown first.

The output looks as follows:
```json
{
   "content":[
      {
         "amount":15,
         "type":"IN",
         "status":"ACCEPTED",
         "id":1
      },
      {
         "amount":60,
         "type":"IN",
         "status":"ACCEPTED",
         "id":2
      },
      {
         "amount":25,
         "type":"IN",
         "status":"ACCEPTED",
         "id":3
      }
   ],
   "pageable":{
      "pageNumber":0,
      "pageSize":50,
      "sort":{
         "empty":true,
         "unsorted":true,
         "sorted":false
      },
      "offset":0,
      "paged":true,
      "unpaged":false
   },
   "totalPages":1,
   "totalElements":3,
   "last":true,
   "size":50,
   "number":0,
   "sort":{
      "empty":true,
      "unsorted":true,
      "sorted":false
   },
   "numberOfElements":3,
   "first":true,
   "empty":false
}
```

#### Transaction bookings
Transactions are booked against a customer and account (see above).
They take a JSON body containing the amount and the type of transaction (either `IN` or `OUT`).
No negative amounts are allowed. If you want to remove money from an account, book `OUT`.

The request body looks like this:
```json
{
    "amount": 15,
    "type": "IN"
}
```

The response looks like this:
```json
{
    "amount": 15,
    "type": "IN",
    "status": "ACCEPTED",
    "id": 1
}
```
where status is either `ACCEPTED` or `REJECTED` depending on the available funds to that account.


#### Transaction rollback
Remembering the ID turned by the previous endpoint, we can roll back a transaction at any point. This endpoint takes **no* request body.
If a rollback is successful, the status of the transaction is now `VOID` and cannot be changed anymore.
The account balance will be reverted to its previous state.

The response looks like this:
```json
{
    "amount": 15,
    "type": "IN",
    "status": "VOID",
    "id": 1
}
```