package Client.Logic;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/***
 * Clase de conexión para comunicarse con el servidor del juego.
 */
public class Connection {

    private final String host;
    private final Integer port;

    /***
     * Constructor del objeto de conexión predeterminado.
     * @param host Dirección del servidor al cual conectarse.
     * @param port Puerto por el cuál conectarse al servidor.
     */
    public Connection(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    /***
     * Función para realizar la conexión al servidor utilizando una instancia del objeto.
     * @param data Datos a ser enviados al servidor.
     * @return Respuesta del servidor.
     */
    public String connect(String data) {
        return connect(host, port, data);
    }

    /***
     * Función para realizar la conexión al servidor.
     * @param host Dirección del servidor al cual conectarse.
     * @param port Puerto por el cuál conectarse al servidor.
     * @param data Datos a ser enviados al servidor.
     * @return Respuesta del servidor.
     */
    public static String connect(String host, Integer port, String data) {
        Socket socket;

        try {
            socket = new Socket(host, port);
        } catch (UnknownHostException e) {
            System.err.println("[Error] No se pudo crear el socket.");
            return null;
        } catch (IOException e) {
            System.err.println("[Error] IOException error.");
            return null;
        }

        //System.out.println("[Info] Conectado al servidor.");

        DataOutputStream outputStream;

        try {
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.err.println("[Error] Error al obtener el output stream de la conexión.");
            return null;
        }

        var outputBytes = data.getBytes();

        try {
            outputStream.write(outputBytes, 0, outputBytes.length);
        } catch (IOException e) {
            System.err.println("[Error] Error al enviar datos.");
            return null;
        }

        DataInputStream inputStream;

        try {
            inputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("[Error] Error al conectarse al input stream de la conexión.");
            return null;
        }

        var inputData = new byte[4086];

        try {
            var bytesRead = inputStream.read(inputData);
        } catch (IOException e) {
            System.err.println("[Error] Error al leer los datos del servidor.");
            return null;
        }

        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("[Error] Error al intentar cerrar los streams.");
            return null;
        }

        return new String(inputData);
    }
}
