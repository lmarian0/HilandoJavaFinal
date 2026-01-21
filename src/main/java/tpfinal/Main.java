package tpfinal;

import tpfinal.utils.RELog;
import tpfinal.monitor.Monitor;
import tpfinal.rdp.PetriNet;

public class Main {
    public static void main(String[] args) {
        // // 1. Tomar tiempo de inicio (en milisegundos)
        // long startTime = System.currentTimeMillis();

        // try {
        //     // Aquí inicias tu Monitor, Hilos, etc.
        //     Monitor monitor = new Monitor();
        //     // ... esperar a que terminen los hilos (join) ...
            
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }

        // // 2. Tomar tiempo de fin
        // long endTime = System.currentTimeMillis();

        // // 3. Calcular duración
        // long duration = endTime - startTime;
        
        // System.out.println("Tiempo de ejecución: " + duration + " ms");
        
        // // Verificación para el Requisito 8.a (20-40 seg) [cite: 127]
        // if (duration >= 20000 && duration <= 40000) {
        //     System.out.println("✅ Tiempo válido (entre 20s y 40s)");
        // } else {
        //     System.out.println("❌ Tiempo inválido (fuera de rango)");
        // }
        
        // // 4. Verificar el invariante con RELog
        // System.out.println("\n========== VERIFICACIÓN DE INVARIANTE ==========");
        // RELog reLog = new RELog();
        
        // // Cargar el archivo de log (ajusta el nombre del archivo según corresponda)
        // reLog.loadLog("log.txt");
        
        // // Verificar si el log cumple con el invariante
        // boolean invariantValid = reLog.checkInvariant();
        
        // if (invariantValid) {
        //     System.out.println("✅ El invariante se cumple correctamente");
        // } else {
        //     System.out.println("❌ El invariante NO se cumple");
        // }


    System.out.println("Proxima matriz");
    PetriNet petriNet = new PetriNet();
    for (int i = 0; i < 12; i++) {
        int [][] aux = petriNet.nextIncidentMatrix(i);
        PetriNet.printMatrix(aux);

        if(petriNet.willContinue(aux)){
            System.out.println("Condición para continuar no se cumple.");
            break;    
        }
        else{
            System.out.println("Condición para continuar se cumple.");
        }
    }
    
    
    

    
    
}
}