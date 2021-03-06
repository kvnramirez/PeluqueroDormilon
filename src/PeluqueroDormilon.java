import java.util.ArrayList;

/**
 * Recurso compartido, SalaEspera
 */
class SalaEspera {
    int max_lugares = 4; // Cantidad maxima de lugares en la sala
    int contador; // Contador de lugares

    /**
     * Constructor clase SalaEspera
     *
     * @param contador numero entero paa iniciar el contador
     */
    public SalaEspera(int contador) {
        this.contador = contador;
    }

    /**
     * Metodo de clase para incrementar contador
     */
    public synchronized void incrementarContador() {
        contador = contador + 1;
    }

    /**
     * Metodo de clase para decrementar contador
     */
    public synchronized void decrementarContador() {
        contador = contador - 1;
        //System.out.println("Contador decrementado = " + contador);
    }

    /**
     * Metodo de clase para bloquear hilo de cliente en lo que se desocupa un lugar
     *
     * @param numero  numero de cliente
     * @param sentado booleano que indica si el cliente esta sentado
     */
    public synchronized void esperarLugar(int numero, Boolean sentado) {
        while (contador >= max_lugares && !sentado) {
            System.out.println("Cliente " + numero + " ESPERANDO lugar para sentarse, contador= " + contador);
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo para despertar a los otros hilos
     */
    public synchronized void notificar() {
        notifyAll();
    }

}

class SillonPeluquero {
    // Recurso compartido
    Boolean ocupado;
    int id_cliente = 0;

    /**
     * Constructor de clase SillonPeluquero
     *
     * @param ocupado estado del sillon del peluquero
     */
    public SillonPeluquero(Boolean ocupado) {
        this.ocupado = ocupado;
    }

    /**
     * Metodo de clase para bloquear hilo peluquero en lo que llega un cliente
     *
     * @param numero numero de cliente
     */
    public synchronized void clienteEsperaDisponibilidad(int numero) {
        while (ocupado) {
            try {
                //System.out.println("bloqueado... cliente: " + numero);
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Bloquear hilo si no hay alguien sentado para oortarse el pelo
     */
    public synchronized void peluqueroEsperaCliente() {
        while (!ocupado) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metodo que coloca un mensaje de que se esta cortando el pelo a un cliente
     */
    public synchronized void cortandoPelo() {
        System.out.println("CORTANDO el cabello a cliente: " + id_cliente);
        try {
            // Se tarda un tiempo en cortar el cabello
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo que almacena que el cliente de determinado numero se sento
     *
     * @param numero
     */
    public synchronized void clienteSentarse(int numero) {
        ocupado = true;
        id_cliente = numero;
        System.out.println("Cliente " + id_cliente + " PASA con el peluquero");
    }

    /**
     * Metodo que almacena que el cliente termino de cortarse el pelo al desocupar el lugar
     */
    public synchronized void clienteLevantarse() {
        ocupado = false;
    }

    /**
     * Metodo para enviar notificación a todos los hilos para despertarlos
     */
    public synchronized void notificar() {
        notifyAll();
    }

}


/**
 * Clase para crear hilo Peluquero
 */
class Peluquero extends Thread {
    SillonPeluquero sillonPeluquero;

    /**
     * Constructor de clase Peluquero
     *
     * @param sillonPeluquero objeto de tipo SillonPeluquero
     */
    public Peluquero(SillonPeluquero sillonPeluquero) {
        this.sillonPeluquero = sillonPeluquero;
    }

    /**
     * Metodo con codigo a ejecutar por hilo Peluquero
     */
    public void run() {
        while (true) {
            sillonPeluquero.peluqueroEsperaCliente();
            sillonPeluquero.cortandoPelo();
            sillonPeluquero.clienteLevantarse();
            sillonPeluquero.notificar();
        }
    }
}


/**
 * Clase para crear hilo cliente
 */
class Cliente extends Thread {
    int numero;
    Boolean sentado = false;
    SalaEspera salaEspera;
    SillonPeluquero sillonPeluquero;
    int veces = 0;

    /**
     * Constructor de clase Cliente
     *
     * @param numero          numero entero indentificador del cliente
     * @param salaEspera      objeto SalaEspera
     * @param sillonPeluquero objeto sillonPeluquero
     */
    public Cliente(int numero, SalaEspera salaEspera, SillonPeluquero sillonPeluquero) {
        this.numero = numero;
        this.salaEspera = salaEspera;
        this.sillonPeluquero = sillonPeluquero;
    }

    /**
     * Metodo que ejecuta el codigo del hilo Cliente
     */
    public void run() {
        for (; ; ) {

            salaEspera.esperarLugar(numero, sentado);
            salaEspera.incrementarContador();
            sentado = true;
            System.out.println("Cliente " + numero + " se SENTO");
            salaEspera.notificar();

            sillonPeluquero.clienteEsperaDisponibilidad(numero);
            sillonPeluquero.clienteSentarse(numero);
            sentado = false;
            sillonPeluquero.notificar();

            salaEspera.decrementarContador();
            salaEspera.notificar();
            veces = veces + 1;
            System.out.println("Cliente " + numero + " se ha CORTADO " + veces + " veces");

            yield();

//            try {
//                sleep(1000);
//                System.out.println("Esperando que me crezca el pelo...");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            // salaEspera.notificar();

        }

    }
}


/**
 * Clase del programa principal
 */
public class PeluqueroDormilon {
    /**
     * Metodo main de ejecucion del programa
     *
     * @param arg
     */
    public static void main(String arg[]) {
        int cantidad_clientes = 6;
        ArrayList<Cliente> clientes = new ArrayList<>();

        SillonPeluquero sillonPeluquero = new SillonPeluquero(false);
        SalaEspera salaEspera = new SalaEspera(0);

        for (int i = 1; i <= cantidad_clientes; i++) {
            Cliente cliente = new Cliente(i, salaEspera, sillonPeluquero);
            clientes.add(cliente);
            cliente.start();
        }

        Peluquero peluquero = new Peluquero(sillonPeluquero);
        peluquero.start();

    }
}