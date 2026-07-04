import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import joblib

print("Mulai melatih ulang model Penyakit Jantung (Fix 13 Fitur)...")

df = pd.read_csv("../dataset/jantung.csv")
nama_kolom_target = 'num' 

X = df.drop(columns=[nama_kolom_target, 'id'])
y = df[nama_kolom_target]

# Ganti simbol '?' jadi kosong, lalu paksa jadi angka murni
X = X.replace('?', None)
X = X.apply(pd.to_numeric, errors='coerce')
X.fillna(0, inplace=True)

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

model = RandomForestClassifier(n_estimators=100, random_state=42)
model.fit(X_train, y_train)

akurasi = model.score(X_test, y_test)
print(f"Akurasi Model Jantung: {akurasi * 100:.2f}%")

joblib.dump(model, "../model/model_jantung.pkl")
print("SUKSES: model_jantung.pkl siap digunakan!")