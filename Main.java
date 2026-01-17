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

        int[][] matriz1 = {
        {1, 2, 3},
        {4, 5, 6}
    };
    
    int[][] matriz2 = {
        {7, 8},
        {9, 10},
        {11, 12}
    };
    
    System.out.println("Matriz 1 (2x3):");
    PetriNet.imprimirMatriz(matriz1);
    
    System.out.println("\nMatriz 2 (3x2):");
    PetriNet.imprimirMatriz(matriz2);
    
    int[][] producto = PetriNet.multiplyMatrix(matriz1, matriz2);
    
    System.out.println("\nResultado de la multiplicación (2x2):");
    PetriNet.imprimirMatriz(producto);
    }
}