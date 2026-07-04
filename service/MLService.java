package service; // Sesuaikan jika kamu menaruhnya di package 'model'

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MLService {

    // Simpan path absolut sebagai variabel agar rapi dan mudah diubah
    private final String scriptPath = "D:/Kulyeah/Semester 4/Pemrograman Berbasis Objek/smartclinic-main/ml/predict.py";
    private final String workingDirectory = "D:/Kulyeah/Semester 4/Pemrograman Berbasis Objek/smartclinic-main/";

    /**
     * Fungsi utama untuk mengirim data ke Python dan mengembalikan hasil prediksinya
     */
    public String predict(String jenisPenyakit, String... parameterMedis) {
        try {
            List<String> command = new ArrayList<>();
            command.add("python");
            command.add(scriptPath);
            command.add(jenisPenyakit);
            command.addAll(Arrays.asList(parameterMedis));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new java.io.File(workingDirectory));

            Process process = pb.start();

            // 1. Cek apakah ada Error dari Python
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder fullError = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                fullError.append(line).append("\n");
            }

            if (fullError.length() > 0) {
                System.err.println("MLService Error (Python): " + fullError.toString());
                return "Error: " + fullError.toString();
            } 
            
            // 2. Jika aman, baca hasilnya
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String hasil = reader.readLine();
            
            if (hasil != null) {
                return hasil;
            } else {
                return "Error: Tidak ada respon dari mesin " + jenisPenyakit;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error (Java): " + e.getMessage();
        }
    }
}