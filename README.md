# DNS

## I decompose the project into three functional modules:

1) Receive the query from the client and get the domain name, search the domain name in the local database.

2) sending and receiving messages between DNS relay and resolver to send back the corresponding IP address to a resolver.

3) Sending and receiving messages between DNS relay and DNS server to find the IP address which domain name is not in a local database.

- - - 

## The implementation process of three functional modules can be described as:

1) Firstly, the program will receive the package from the clients, or the remote DNS severs, and convert it to an array string. There are two cases that the program will analyze the content of the package and judge whether the package is a query or a response.

2) If the package is a query from client resolver, there are three possible cases for the domain name required by a client. I will discuss them respectively:

·The domain name required by a client is included in the local database.

a) Receive the DNS query.

b) Get the domain name from the query and search for it in the local database.

c) Set the IP address is from the database record.

d) Send response to a client.

·The domain name required by a client is included in the local database, but it is a forbidden website. The program will serve as a protector, prevent the client to visit the dangerous website.

a) Receive the DNS query.

b) Get the domain name from the query and search for it in the local database.

c) Set the IP address is 0.0.0.0.

d) Send response to a client.

·The domain name required by a client is included in the local database, and it is not a forbidden website. Then it should change much more parts of the query package, including changing the flag to 0x8180, changing the answer count to 1, and attaching the answer section containing the retrieving IP address.

a) Receive the DNS query.

b) Get the domain name from the query and search for it in the local database.

c) Judge whether the packet is of IPv4 or IPv6 type.

d) If IPv6, change the flag into 8180 and authority count into 1 If IPv4, change the flag into 8180 and answer count into 1. Then it should change much

e) Send response to a client.

·The domain name required by a client isn’t in the local database. The program will serve as a DNS relay to send the request to the remote DNS server.

a)Receive DNS query.

b) Get the domain name from the query and search for it in the local database.

c) Not found, send it to the remote DNS server and keep the address and port of the corresponding client.

3) If it is a response, receive the package from remote DNS server and pass it to the client resolver.
