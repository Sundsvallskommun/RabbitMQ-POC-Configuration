RabbitMQ is ran in a docker container, use the following docker compose.

When the container is running, you can access a graphical interface at http://localhost:15672/

Default username is: guest

Default password is: guest

```version: '3.3'
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: some-rabbit
    ports:
      - 5672:5672
      - 15672:15672
    restart: always
