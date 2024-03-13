# Enterprise All Inclusive - Microservice PoC

## Introduction
This is a tenancy-capable microservice to handle user account balance.
Each tenant gets assigned their own schema in the database to completely decouple them from each other.
The intent is to deploy each redundant of applications on a per-tenant basis. This allows for provisional scaling per tenant and even enables per-tenant deployments as all changes to the database schema are separated.

The calling service should know which tenant to call for, either by subdomain (e.g. https://tenant.service.com/api) or via reverse proxy (e.g. https://service.com/tenant/api).
This responsibility falls to the deployment and infrastructure, not this project.

To spin up a new tenant, all that's required is changing the tenant name in the file that's mapped into your container via `/config/application.yml`.

## Authentication
There is no authentication. This application assumes the calling service has handled authentication and data passed here is correct.
Because each tenant has its own set of customers, there is no additional validation whether this customer truly belongs to that tenant.
Such validation is the responsibility of the caller.

## Technology and decisions
Where the money comes from and where the money goes is outside the scope of this projects. We aren't limited to transactions only belonging to the tenant's accounts.

Using Kotlin was a requirement. I have not used Kotlin in production before and am not familiar with all best practices.
I'm aware of the mismatch between Hibernate requiring mutable, proxied entities and the immutable nature of Kotlin, but still found this approach more readable for a simple CRUD application over using something like jOOQ or J2DBC.

This application is realized with Spring Boot and uses one underlying Postgres database. Depending on the scale, this may not be enough to hold all tenants' data.
Tenancy could be migrated on a per-database basis.

Additionally, this may be realized with a queue of transactions modifying some stateful account balance.

Using Hibernate Envers for auditing would also be possible. But for something as simple as a transaction log, I didn't deem this necessary.


## Development
Need a fresh start for your local database? `docker-compose down -v --rmi all && rm -r /tmp/eai-postgres && docker-compose up`