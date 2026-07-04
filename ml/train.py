import pandas as pd
from sklearn.ensemble import RandomForestClassifier
import joblib
 
df = pd.read_csv("../dataset/diabetes.csv")

X = df.drop("Outcome", axis=1)
y = df["Outcome"]

model = RandomForestClassifier(
    n_estimators=100,
    random_state=42
    )

model.fit(X, y)
joblib.dump(
    model,
    "../model/model_diabetes.pkl"
)

print("Model berhasil disimpan")