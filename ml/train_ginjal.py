import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import joblib

print("Mulai melatih ulang model Penyakit Ginjal (Fix 24 Fitur)...")

df = pd.read_csv("../dataset/ginjal.csv") 

# Target
nama_kolom_target = 'classification' 

# Buang kolom target dan id
X = df.drop(columns=[nama_kolom_target, 'id'])
y = df[nama_kolom_target]

# 1. BERSINKAN TEKS SECARA MANUAL (Agar kolom tidak bertambah menjadi 220)
X = X.replace({
    'normal': 1, 'abnormal': 0,
    'present': 1, 'notpresent': 0,
    'yes': 1, 'no': 0,
    '\tno': 0, '\tyes': 1, ' yes': 1,
    'good': 1, 'poor': 0,
    '?': None, '\t?': None # Ubah tanda tanya menjadi kosong
})

# 2. Paksa semua kolom menjadi angka murni (Float/Int)
X = X.apply(pd.to_numeric, errors='coerce')

# 3. Isi data kosong dengan 0
X.fillna(0, inplace=True)

# Latih mesinnya
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

akurasi = model.score(X_test, y_test)
print(f"Akurasi Model Ginjal: {akurasi * 100:.2f}%")

joblib.dump(model, "../model/model_ginjal.pkl")
print("SUKSES: model_ginjal.pkl siap dengan TEPAT 24 fitur!")