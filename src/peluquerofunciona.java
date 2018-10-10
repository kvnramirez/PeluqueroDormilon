//import java.util.ArrayList;
//
//class SalaEspera {
//    // Recurso compartido
//    int max_lugares = 4; // Cantidad maxima de lugares en la sala
//    int contador; // Contador de lugares
//    Boolean lugares[] = new Boolean[4];
//
//    public SalaEspera(int contador) {
//        this.contador = contador;
//    }
//
//    public synchronized void notificar() {
//        notifyAll();
//    }
//
//}
//
//class SillonPeluquero {
//    // Recurso compartido
//    Boolean ocupado;
//    int id_cliente = 0;
//
//    public SillonPeluquero(Boolean ocupado) {
//        this.ocupado = ocupado;
//    }
//
//}
//
//
//class Peluquero extends Thread {
//    // Hilo peluquero
//    SillonPeluquero sillonPeluquero;
//
//    public Peluquero(SillonPeluquero sillonPeluquero) {
//        this.sillonPeluquero = sillonPeluquero;
//    }
//
//    public void run() {
//        while (true) {
//
//            synchronized (sillonPeluquero) {
//                while (!sillonPeluquero.ocupado) {
//                    // Bloquear hilo si no hay alguien sentado para oortarse el pelo
//                    try {
//                        sillonPeluquero.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                System.out.println("CORTANDO el cabello a cliente: " + sillonPeluquero.id_cliente);
//                try {
//                    // Se tarda un tiempo en cortar el cabello
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                // Sale el cliente, entonces el sillon del peluquero esta disponible
//                sillonPeluquero.ocupado = false;
//                sillonPeluquero.notifyAll();
//            }
//
//        }
//    }
//}
//
//
//class Cliente extends Thread {
//    // Hilo cliente
//    int numero;
//    Boolean sentado = false;
//    SalaEspera salaEspera;
//    SillonPeluquero sillonPeluquero;
//    int veces = 0;
//
//    public Cliente(int numero, SalaEspera salaEspera, SillonPeluquero sillonPeluquero) {
//        this.numero = numero;
//        this.salaEspera = salaEspera;
//        this.sillonPeluquero = sillonPeluquero;
//    }
//
//    public void run() {
//        for (; ; ) {
//
//            synchronized (salaEspera) {
//
//                while (salaEspera.contador >= salaEspera.max_lugares && !sentado) {
//                    System.out.println("Cliente " + numero + " ESPERANDO lugar para sentarse");
//                    try {
//                        salaEspera.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                salaEspera.contador = salaEspera.contador + 1;
//                sentado = true;
//
//                System.out.println("Cliente " + numero + " se SENTO" + " | contador = " + salaEspera.contador);
//
//                salaEspera.notifyAll();
//            }
//
//            synchronized (sillonPeluquero) {
//
//                while (sillonPeluquero.ocupado) {
//                    try {
//                        System.out.println("bloqueado... cliente: " + numero);
//                        sillonPeluquero.wait();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                sillonPeluquero.ocupado = true;
//                sillonPeluquero.id_cliente = numero;
//                sillonPeluquero.notifyAll();
//            }
//
//            synchronized (salaEspera) {
//                sentado = false;
//                sillonPeluquero.ocupado = false;
//                //System.out.println("sillonPeluquero.ocupado = " + sillonPeluquero.ocupado);
//                salaEspera.contador = salaEspera.contador - 1;
//                //System.out.println("salaespera contador = " + salaEspera.contador);
//                salaEspera.notifyAll();
//                veces = veces + 1;
//                System.out.println("Cliente " + numero + " se ha CORTADO " + veces + " veces");
//            }
//
//            yield();
//
////            try {
////                sleep(1000);
////                System.out.println("Esperando que me crezca el pelo...");
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
//
//            salaEspera.notificar();
//
//        }
//
//    }
//}
//
//
//public class PeluqueroDormilon {
//    public static void main(String arg[]) {
//        int cantidad_clientes = 6;
//        ArrayList<Cliente> clientes = new ArrayList<>();
//
//        SillonPeluquero sillonPeluquero = new SillonPeluquero(false);
//        SalaEspera salaEspera = new SalaEspera(0);
//
//        for (int i = 1; i <= cantidad_clientes; i++) {
//            Cliente cliente = new Cliente(i, salaEspera, sillonPeluquero);
//            clientes.add(cliente);
//            cliente.start();
//        }
//
//        Peluquero peluquero = new Peluquero(sillonPeluquero);
//        peluquero.start();
//
//    }
//}