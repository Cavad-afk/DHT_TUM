This is going to a be a Kademlia like network. I’m planning to use coroutines. There’ll be a loop accepting connections and parsing incoming data. To avoid data corruption, all messages have SHA-256 checksum at the end that covers everything after size. For bootstrapping, each node will have a list of hardcoded nodes. Due to the nature of the project, there’ll be very little replication and no resaving. Values will be stored for less than 10 seconds.


In addition to standard:


650. DHT_PUT
651. DHT_GET
652. DHT_SUCCESS
653. DHT_FAILURE


there’ll be:


654. DHT_PING

655. DHT_PONG

656. DHT_FIND_NODE

657. DHT_FIND_VALUE

658. DHT_ERROR

659. DHT_NODE

660. DHT_VALUE


DHT_PING, DHT_PONG, DHT_FIND_NODE, DHT_FIND_VALUE, DHT_ERROR.


DHT_PING is used to check if the dedicating port is running a compatible server, and to check if the node is still alive. The response should be DHT_PONG.



Size (16 bit)

DHT_PING (16 bit)

Random value (32 bit)


SHA-256 of all bits after size (256 bit)


1. DHT_PING



Size (16 bit)

DHT_PONG (16 bit)

Random value from DHT_PING (32 bit)


SHA-256 of all bits after size (256 bit)


2. DHT_PONG


DHT_FIND_NODE is used to look for n nodes closest to the provided key. The response would be DHT_NODES. DHT_ERROR in case of an error.


Size (16 bit)

DHT_FIND_NODE (16 bit)

Key (256 bit)


SHA-256 of all bits after size (256 bit)


3. DHT_FIND_NODE


Size (16 bit)

DHT_FIND_NODE (16 bit)

*start*

IP_TYPE - 4 or 6 (8 bit)

IP (32 or 128 bit)

PORT (16 bit)

NODE_ID (256 bit)

*goto start*

SHA-256 of all bits after size (256 bit)

4. DHT_NODES


DHT_FIND_VALUE is same as DHT_FIND_NODE, but if the recipient has the requested key, it will return the corresponding value. The response would be DHT_NODES or DHT_VALUE. DHT_ERROR in case of an error.



Size (16 bit)

DHT_FIND_VALUE (16 bit)

Key (256 bit)


SHA-256 of all bits after size (256 bit)


5. DHT_FIND_VALUE



Size (16 bit)

DHT_VALUE (16 bit)

Value (0 - size* bit)


SHA-256 of all bits after size (256 bit)


6. DHT_VALUE


Size (16 bit)

DHT_ERROR (16 bit)

Error code (32 bit)

Error message (0 - size* bit)

SHA-256 of all bits after size (256 bit)


7. DHT_ERROR

