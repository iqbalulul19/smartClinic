import sys
import joblib
import os
import warnings

# Tambahkan baris ini untuk menyembunyikan peringatan/warning dari sklearn
warnings.filterwarnings("ignore")

try:
    # 1. Menangkap jenis penyakit dari Java (Argumen ke-1)
    jenis_penyakit = sys.argv[1].lower()
    
    # Menentukan lokasi folder saat ini agar path absolutnya aman
    base_path = os.path.dirname(os.path.abspath(__file__))

    # 2. Logika Pemilihan Model
    if jenis_penyakit == "diabetes":
        model_path = os.path.join(base_path, "../model/model_diabetes.pkl")
        model = joblib.load(model_path)
        
        # Mengambil 8 parameter untuk diabetes
        data = [float(x) for x in sys.argv[2:10]] 
        
        # Melakukan prediksi
        prediksi = model.predict([data])
        if prediksi[0] == 1:
            print("RISIKO DIABETES TINGGI")
        else:
            print("RISIKO DIABETES RENDAH")

    elif jenis_penyakit == "jantung":
        model_path = os.path.join(base_path, "../model/model_jantung.pkl")
        model = joblib.load(model_path)
        
        # CATATAN: Jumlah parameter harus disesuaikan dengan jumlah kolom di jantung.csv
        # Anggap saja mengambil semua argumen setelah jenis_penyakit
        data = [float(x) for x in sys.argv[2:]] 
        
        prediksi = model.predict([data])
        print(f"TINGKAT RISIKO JANTUNG: {prediksi[0]}")

    elif jenis_penyakit == "ginjal":
        model_path = os.path.join(base_path, "../model/model_ginjal.pkl")
        model = joblib.load(model_path)
        
        # CATATAN: Sama seperti jantung, jumlah parameter menyesuaikan CSV
        data = [float(x) for x in sys.argv[2:]] 
        
        prediksi = model.predict([data])
        # Asumsi 'ckd' atau 1 = sakit
        print(f"HASIL PREDIKSI GINJAL: {prediksi[0]}")

    else:
        print("Error: Jenis penyakit tidak dikenali oleh sistem Python.")

except Exception as e:
    print(f"Error pada Python: {e}")