import java.util.ArrayList;


class SalaEspera {
    // Recurso compartido
    int max_lugares = 4; // Cantidad maxima de lugares en la sala
    int contador; // Contador de lugares
    Boolean lugares[] = new Boolean[4];

    public SalaEspera(int contador) {
        this.contador = contador;
    }

    public synchronized void incrementarContador() {
        contador = contador + 1;
    }

    public synchronized void decrementarContador() {
        contador = contador - 1;
    }

    public synchronized void esperarLugar(int numero, Boolean sentado) {
        while (contador >= max_lugares && !sentado) {
            System.out.println("Cliente " + numero + " ESPERANDO lugar para sentarse");
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized int getContador() {
        return contador;
    }

    public synchronized void notificar() {
        notifyAll();
    }

}

class SillonPeluquero {
    // Recurso compartido
    Boolean ocupado;
    int id_cliente = 0;

    public SillonPeluquero(Boolean ocupado) {
        this.ocupado = ocupado;
    }

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

    public synchronized void peluqueroEsperaCliente() {
        while (!ocupado) {
            // Bloquear hilo si no hay alguien sentado para oortarse el pelo
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void cortandoPelo() {
        System.out.println("CORTANDO el cabello a cliente: " + id_cliente);
        try {
            // Se tarda un tiempo en cortar el cabello
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void clienteSentarse(int numero) {
        ocupado = true;
        id_cliente = numero;
    }

    public synchronized void clienteLevantarse() {
        ocupado = false;
    }

    public synchronized void notificar() {
        notifyAll();
    }

}


class Peluquero extends Thread {
    // Hilo peluquero
    SillonPeluquero sillonPeluquero;

    public Peluquero(SillonPeluquero sillonPeluquero) {
        this.sillonPeluquero = sillonPeluquero;
    }

    public void run() {
        while (true) {
            sillonPeluquero.peluqueroEsperaCliente();
            sillonPeluquero.cortandoPelo();
            sillonPeluquero.clienteLevantarse();
            sillonPeluquero.notificar();
        }
    }
}


class Cliente extends Thread {
    // Hilo cliente
    int numero;
    Boolean sentado = false;
    SalaEspera salaEspera;
    SillonPeluquero sillonPeluquero;
    int veces = 0;

    public Cliente(int numero, SalaEspera salaEspera, SillonPeluquero sillonPeluquero) {
        this.numero = numero;
        this.salaEspera = salaEspera;
        this.sillonPeluquero = sillonPeluquero;
    }

    public void run() {
        for (; ; ) {

            salaEspera.esperarLugar(numero, sentado);
            salaEspera.incrementarContador();
            sentado = true;
            System.out.println("Cliente " + numero + " se SENTO");
            salaEspera.notificar();

            sillonPeluquero.clienteEsperaDisponibilidad(numero); // esperar a que sillon este desocupado
            sillonPeluquero.clienteSentarse(numero); // marcar sillon como ocupado
            sentado = false;
            sillonPeluquero.notificar();

            salaEspera.decrementarContador();
            //System.out.println("salaespera contador = " + salaEspera.contador);
            salaEspera.notificar();
            veces = veces + 1;
            System.out.println("Cliente " + numero + " se ha CORTADO " + veces + " veces");

            synchronized (salaEspera) {
                // sentado = false;
                // salaEspera.contador = salaEspera.contador - 1;
//                salaEspera.decrementarContador();
//                //System.out.println("salaespera contador = " + salaEspera.contador);
//                salaEspera.notifyAll();
//                veces = veces + 1;
//                System.out.println("Cliente " + numero + " se ha CORTADO " + veces + " veces");
            }

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


public class PeluqueroDormilon {
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