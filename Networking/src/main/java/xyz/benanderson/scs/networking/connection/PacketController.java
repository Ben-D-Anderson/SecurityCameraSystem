package xyz.benanderson.scs.networking.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PacketController implements AutoCloseable {

    private final ObjectOutputStream objectOutputStream;
    private final ObjectInputStream objectInputStream;

    public PacketController(Connection connection) throws IOException {
        this.objectInputStream = new ObjectInputStream(
                connection.getSocket().getInputStream()
        );
        this.objectOutputStream = new ObjectOutputStream(
                connection.getSocket().getOutputStream()
        );
    }

    @Override
    public void close() throws Exception {
        objectInputStream.close();
        objectOutputStream.close();
    }

}
